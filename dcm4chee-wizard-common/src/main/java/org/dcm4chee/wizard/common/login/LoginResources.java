package org.dcm4chee.wizard.common.login;

import java.io.InputStream;
import java.util.PropertyResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginResources {

    protected static Logger log = LoggerFactory.getLogger(LoginResources.class);
    
    PropertyResourceBundle prb;
    
    public LoginResources() {
        setLocale("en");
    }

    public void setLocale(String locale) {
        try {
            InputStream resource = this.getClass().getResourceAsStream("locale/login_" + locale + ".properties");
            if (resource == null) {
                log.warn("Could not get locale " + locale + " for login page");
                return;
            }
            prb = new PropertyResourceBundle(resource);
        } catch (Exception e) {
            log.error("Error processing locale " + locale + " for login page: ", e);
        }
    }
    
    public String getBrowser_title() {
        return prb.getString("login.browser_title");
    }
    
    public String getLoginLabel() {
        return prb.getString("login.loginLabel");
    }

    public String getUsername() {
        return prb.getString("login.username");
    }

    public String getPassword() {
        return prb.getString("login.password");
    }

    public String getSubmit() {
        return prb.getString("login.submit");
    }
    
    public String getReset() {
        return prb.getString("login.reset");
    }

    public String getLoginFailed() {
        return prb.getString("login.loginFailed");
    }
}
