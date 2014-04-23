/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2012
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.wizard.common.login.context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import net.sf.json.JSONObject;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.wizard.common.component.secure.SecureWebApplication;
import org.dcm4chee.wizard.common.login.secure.SecureSession;
import org.jboss.security.SecurityContextAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.security.hive.authentication.DefaultSubject;
import org.wicketstuff.security.hive.authorization.SimplePrincipal;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @author Robert David <robert.david@agfa.com>
 */
public class LoginContextSecurityHelper {

    private static String rolesGroupName;

    protected static Logger log = LoggerFactory.getLogger(LoginContextSecurityHelper.class);

    static Map<String, String> readSwarmPrincipals() throws MalformedURLException, IOException {
        InputStream in = ((WebApplication) Application.get()).getServletContext().getResource("/WEB-INF/dcm4chee.hive")
                .openStream();
        BufferedReader dis = new BufferedReader(new InputStreamReader(in));
        HashMap<String, String> principals = new LinkedHashMap<String, String>();
        String line;
        String principal = null;
        while ((line = dis.readLine()) != null)
            if (line.startsWith("grant principal ")) {
                principal = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                principals.put(principal, null);
            } else if ((principal != null) && (line.trim().startsWith("// KEY:"))) {
                principals.put(principal, line.substring(line.indexOf("// KEY:") + 7).trim());
                principal = null;
            }
        in.close();
        return principals;
    }

    static DefaultSubject mapSwarmSubject(Subject jaasSubject, SecureSession session) throws IOException {
        getJaasRolesGroupName();
        DefaultSubject subject = new DefaultSubject();
        Map<String, Set<String>> mappings = null;
        Set<String> swarmPrincipals = new HashSet<String>();
        for (Principal principal : jaasSubject.getPrincipals()) {
            if (!(principal instanceof Group) && (session != null))
                session.setUsername(principal.getName());
            if ((principal instanceof Group) && (rolesGroupName.equalsIgnoreCase(principal.getName()))) {
                Enumeration<? extends Principal> members = ((Group) principal).members();
                if (mappings == null) {
                    mappings = readRolesFile();
                }
                Set<String> set;
                while (members.hasMoreElements()) {
                    Principal member = members.nextElement();
                    if ((set = mappings.get(member.getName())) != null) {
                        for (Iterator<String> i = set.iterator(); i.hasNext();) {
                            String appRole = i.next();
                            if (swarmPrincipals.add(appRole))
                                subject.addPrincipal(new SimplePrincipal(appRole));
                        }
                    }
                }
            }
        }
        return subject;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Set<String>> readRolesFile() throws IOException {
        String fn = System.getProperty("dcm4chee-wizard.cfg.path");
        if (fn == null) {
            log.warn("Wizard config path not found! Not specified with System property 'dcm4chee-wizard.cfg.path'");
            fn = JBossAS7SystemProperties.JBOSS_SERVER_CONFIG_DIR + "/dcm4chee-wizard/";
            log.warn("Using default config path of: " + fn);
        }
        File mappingFile = new File(StringUtils.replaceSystemProperties(fn) + "/roles.json");
        if (!mappingFile.isAbsolute())
            mappingFile = new File(JBossAS7SystemProperties.JBOSS_SERVER_BASE_DIR, mappingFile.getPath());
        Map<String, Set<String>> mappings = new HashMap<String, Set<String>>();
        String line;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(mappingFile));
            while ((line = reader.readLine()) != null) {
                JSONObject jsonObject = JSONObject.fromObject(line);
                Set<String> set = new HashSet<String>();
                Iterator<String> i = jsonObject.getJSONArray("swarmPrincipals").iterator();
                while (i.hasNext())
                    set.add(i.next());
                mappings.put(jsonObject.getString("rolename"), set);
            }
            return mappings;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    static boolean checkLoginAllowed(DefaultSubject subject) {
        String loginAllowedRolename = SecureWebApplication.get().getInitParameter("LoginAllowedRolename");
        return loginAllowedRolename == null ? false : subject.getPrincipals().contains(
                new SimplePrincipal(loginAllowedRolename));
    }

    public static Subject getJaasSubject() {
        try {
            return SecurityContextAssociation.getSubject();
        } catch (Exception x) {
            log.error("Failed to get subject using org.jboss.security.SecurityContextAssociation.getSubject", x);
            return null;
        }
    }

    public static String getJaasRolesGroupName() {
        if (rolesGroupName == null) {
            try {
                rolesGroupName = ((WebApplication) Application.get()).getInitParameter("rolesGroupName");
                if (rolesGroupName == null)
                    rolesGroupName = "Roles";
            } catch (Exception x) {
                log.error("Can't get InitParameter 'rolesGroupName' from Wicket Application!", x);
            }
        }
        return rolesGroupName;
    }

    public static final List<String> getJaasRoles() {
        getJaasRolesGroupName();
        List<String> roles = new ArrayList<String>();
        String rolesGroupName = ((WebApplication) Application.get()).getInitParameter("rolesGroupName");
        if (rolesGroupName == null)
            rolesGroupName = "Roles";
        try {
            for (Principal principal : ((Subject) PolicyContext.getContext("javax.security.auth.Subject.container"))
                    .getPrincipals()) {
                if ((principal instanceof Group) && rolesGroupName.equalsIgnoreCase(principal.getName())) {
                    Enumeration<? extends Principal> members = ((Group) principal).members();
                    while (members.hasMoreElements())
                        roles.add(members.nextElement().getName());
                }
            }
        } catch (Exception e) {
            log.error("Failed to get jaas subject from javax.security.auth.Subject.container", e);
        }
        return roles;
    }
}
