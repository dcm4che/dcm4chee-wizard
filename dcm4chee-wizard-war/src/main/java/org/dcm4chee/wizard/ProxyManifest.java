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

package org.dcm4chee.wizard;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class ProxyManifest {

    protected static Logger log = LoggerFactory.getLogger(ProxyManifest.class);

    private static Map<String, String> properties = null;

    public static Map<String, String> get(String proxyJarNameProperty) {
        if (properties != null)
            return properties;

        properties = new HashMap<String, String>();
        String wizardWarName = null;
        String proxyJarName = null;
        String manifestPath = null;
        try {
            wizardWarName = ((WebApplication) Application.get()).getInitParameter("wizardWarName");
            proxyJarName = ((WebApplication) Application.get()).getInitParameter(proxyJarNameProperty);

            if (wizardWarName == null)
                throw new RuntimeException("Can't get InitParameter 'wizardWarName' from Wicket Application!");
            if (proxyJarName == null)
                throw new RuntimeException("Can't get InitParameter 'proxyJarName' from Wicket Application!");

            String osName = System.getProperty("os.name");
            manifestPath = osName != null && osName.toLowerCase().contains("windows") ? "vfs:/"
                    + System.getProperty("jboss.home.dir").replace("\\", "/") + "/bin/content/" + wizardWarName
                    + "/WEB-INF/lib/" + proxyJarName + "/META-INF/MANIFEST.MF" : "vfs:/content/" + wizardWarName
                    + "/WEB-INF/lib/" + proxyJarName + "/META-INF/MANIFEST.MF";

            final Attributes attributes = new Manifest(new URL(manifestPath).openStream()).getMainAttributes();
            for (Object key : attributes.keySet())
                properties.put(key.toString(), attributes.getValue((Name) key));
        } catch (Exception e) {
            log.error("Failed to retrieve " + manifestPath, e);
        }
        return properties;
    }
}
