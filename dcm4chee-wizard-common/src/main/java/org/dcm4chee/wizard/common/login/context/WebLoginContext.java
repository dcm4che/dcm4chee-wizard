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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.dcm4chee.wizard.common.login.secure.SecureSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.security.authentication.LoginException;
import org.wicketstuff.security.hive.authentication.DefaultSubject;
import org.wicketstuff.security.hive.authentication.UsernamePasswordContext;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class WebLoginContext extends UsernamePasswordContext {

    protected static Logger log = LoggerFactory.getLogger(WebLoginContext.class);

    public WebLoginContext() {
    }

    public WebLoginContext(String username, String password) {
        super(username, password);
    }

    @Override
    protected org.wicketstuff.security.hive.authentication.Subject getSubject(String username, String password)
            throws LoginException {
        WebApplication app = (WebApplication) Application.get();
        String webApplicationPolicy = app.getInitParameter("webApplicationPolicy");
        if (webApplicationPolicy == null)
            webApplicationPolicy = "dcm4chee";
        String rolesGroupName = app.getInitParameter("rolesGroupName");
        if (rolesGroupName == null)
            rolesGroupName = "Roles";

        LoginCallbackHandler handler = new LoginCallbackHandler(username, password);
        LoginContext context;
        SecureSession secureSession;
        try {
            secureSession = (SecureSession) Session.get();
            // secureSession.setManageUsers(BaseCfgDelegate.getInstance().getManageUsers());
            context = new LoginContext(webApplicationPolicy, handler);
            context.login();
            secureSession.setUsername(username);
        } catch (Exception e) {
            log.warn("Login failed. Reason: " + e.getMessage());
            throw new LoginException();
        }

        if (!readHiveFile())
            return null;

        DefaultSubject subject;
        try {
            subject = LoginContextSecurityHelper.mapSwarmSubject(context.getSubject(), null);
            if (!LoginContextSecurityHelper.checkLoginAllowed(subject)) {
                ((SecureSession) Session.get()).invalidate();
                log.warn("Failed to authorize subject for login, denied. See 'LoginAllowedRolename' parameter in web.xml.");
            }
            secureSession.extendedLogin(username, password, subject);
        } catch (Exception e) {
            log.error("Login failed for user " + username, e);
            ((SecureSession) Session.get()).invalidate();
            subject = new DefaultSubject();
        }
        return subject;
    }

    private boolean readHiveFile() {
        try {
            ((SecureSession) Session.get()).setAllSwarmPrincipals(LoginContextSecurityHelper.readSwarmPrincipals());
            return true;
        } catch (Exception e) {
            log.error("Error processing hive file", e);
            ((SecureSession) Session.get()).invalidate();
            return false;
        }
    }

    private class LoginCallbackHandler implements CallbackHandler {

        private String user;
        private String passwd;

        public LoginCallbackHandler(String user, String passwd) {
            this.user = user;
            this.passwd = passwd;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback cb : callbacks) {
                if (cb instanceof NameCallback) {
                    ((NameCallback) cb).setName(user);
                } else if (cb instanceof PasswordCallback) {
                    ((PasswordCallback) cb).setPassword(passwd.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(cb,
                            "Callback not supported! (only Name and Password Callback are supported)");
                }
            }
        }
    }
}
