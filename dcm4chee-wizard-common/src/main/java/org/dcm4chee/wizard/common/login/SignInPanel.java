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

package org.dcm4chee.wizard.common.login;

import java.lang.reflect.Field;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.http.WebRequest;
import org.dcm4chee.wizard.common.login.context.WebLoginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.security.WaspSession;
import org.wicketstuff.security.authentication.LoginException;

public class SignInPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private static final String SIGN_IN_FORM = "signInForm";

    /** True if the panel should display a remember-me checkbox */
    private boolean includeRememberMe = true;

    /** True if the user should be remembered via form persistence (cookies) */
    private boolean rememberMe = true;

    /** password. */
    private String password;

    /** user name. */
    private String username;

    private static Logger log = LoggerFactory.getLogger(SignInPanel.class);
    
    /**
     * @see org.apache.wicket.Component#Component(String)
     */
    public SignInPanel(final String id) {
        this(id, true);
    }

    /**
     * @param id
     *            See Component constructor
     * @param includeRememberMe
     *            True if form should include a remember-me checkbox
     * @see org.apache.wicket.Component#Component(String)
     */
    public SignInPanel(final String id, final boolean includeRememberMe) {
        super(id);

        this.includeRememberMe = includeRememberMe;

        // Create feedback panel and add to page
        add(new FeedbackPanel("feedback"));

        // Add sign-in form to page, passing feedback panel as
        // validation error handler
        add(new SignInForm(SIGN_IN_FORM));
    }

    /**
     * 
     * @return signin form
     */
    protected SignInForm getForm() {
        return (SignInForm) get(SIGN_IN_FORM);
    }

    /**
     * @see org.apache.wicket.Component#onBeforeRender()
     */
    @Override
    protected void onBeforeRender() {
        // logged in already?
        if (isSignedIn() == false) {
            IAuthenticationStrategy authenticationStrategy = getApplication().getSecuritySettings()
                    .getAuthenticationStrategy();
            // get username and password from persistence store
            String[] data = jaasLoggedIn(this.getWebRequest());
            if (data == null) {
                data = authenticationStrategy.load();
            }
            if ((data != null) && (data.length > 1)) {
                // try to sign in the user
                if (signIn(data[0], data[1])) {
                    username = data[0];
                    password = data[1];

                    // logon successful. Continue to the original destination
                    continueToOriginalDestination();
                    // Ups, no original destination. Go to the home page
                    throw new RestartResponseException(getSession().getPageFactory().newPage(
                            getApplication().getHomePage()));
                } else {
                    // the loaded credentials are wrong. erase them.
                    authenticationStrategy.remove();
                }
            }
        }

        // don't forget
        super.onBeforeRender();
    }

    @SuppressWarnings("rawtypes")
    private String[] jaasLoggedIn(WebRequest webRequest) {
        Object cr = getWebRequest().getContainerRequest();
        Class crClass = cr.getClass();
        try {
            Field f = crClass.getDeclaredField("request");
            f.setAccessible(true);
            org.apache.catalina.connector.Request req = (org.apache.catalina.connector.Request) f.get(cr);
            log.info("request:"+req.getClass().getName());
            Object pr = req.getPrincipal();
            if (pr == null) {
                log.info("No JAAS login principal!");
                return null;
            }
            Field credField = pr.getClass().getDeclaredField("credentials");
            credField.setAccessible(true);
            Object cred = credField.get(pr);        
            return new String[]{req.getRemoteUser(), cred.toString()};
        } catch (Exception x) {
            log.error("Failed to get login info of JAAS login!", x);
        }
        return null;
    }

    /**
     * Convenience method to access the password.
     * 
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password
     * 
     * @param password
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Convenience method to access the username.
     * 
     * @return The user name
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the username
     * 
     * @param username
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * Get model object of the rememberMe checkbox
     * 
     * @return True if user should be remembered in the future
     */
    public boolean getRememberMe() {
        return rememberMe;
    }

    /**
     * @param rememberMe
     *            If true, rememberMe will be enabled (username and password
     *            will be persisted somewhere)
     */
    public void setRememberMe(final boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    /**
     * Sign in user if possible.
     * 
     * @param username
     *            The username
     * @param password
     *            The password
     * @return True if signin was successful
     */
    private boolean signIn(String username, String password) {
        try {
            ((WaspSession) getSession()).login(new WebLoginContext(username, password));
        } catch (LoginException e) {
            return false;
        }
        return true;
    }

    /**
     * @return true, if signed in
     */
    private boolean isSignedIn() {
        return ((WaspSession) getSession()).isUserAuthenticated();
    }

    /**
     * Called when sign in failed
     */
    protected void onSignInFailed() {
        // Try the component based localizer first. If not found try the
        // application localizer. Else use the default
        error(getLocalizer().getString("signInFailed", this, "Sign in failed"));
    }

    /**
     * Called when sign in was successful
     */
    protected void onSignInSucceeded() {
        // If login has been called because the user was not yet logged in, than
        // continue to the
        // original destination, otherwise to the Home page
        continueToOriginalDestination();
        setResponsePage(getApplication().getHomePage());
    }

    /**
     * Sign in form.
     */
    public final class SignInForm extends StatelessForm<SignInPanel> {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         * 
         * @param id
         *            id of the form component
         */
        public SignInForm(final String id) {
            super(id);

            setModel(new CompoundPropertyModel<SignInPanel>(SignInPanel.this));

            // Attach textfields for username and password
            add(new TextField<String>("username"));
            add(new PasswordTextField("password"));

            // MarkupContainer row for remember me checkbox
            WebMarkupContainer rememberMeRow = new WebMarkupContainer("rememberMeRow");
            add(rememberMeRow);

            // Add rememberMe checkbox
            rememberMeRow.add(new CheckBox("rememberMe"));

            // Show remember me checkbox?
            rememberMeRow.setVisible(includeRememberMe);
        }

        /**
         * @see org.apache.wicket.markup.html.form.Form#onSubmit()
         */
        @Override
        public final void onSubmit() {
            IAuthenticationStrategy strategy = getApplication().getSecuritySettings().getAuthenticationStrategy();

            if (signIn(getUsername(), getPassword())) {
                if (rememberMe == true) {
                    strategy.save(username, password);
                } else {
                    strategy.remove();
                }

                onSignInSucceeded();
            } else {
                onSignInFailed();
                strategy.remove();
            }
        }
    }
}
