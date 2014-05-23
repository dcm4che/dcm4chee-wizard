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

package org.dcm4chee.wizard.common.component.secure;

import java.net.MalformedURLException;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.pages.AccessDeniedPage;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.settings.IExceptionSettings;
import org.dcm4chee.wizard.common.component.InternalErrorPage;
import org.dcm4chee.wizard.common.login.LoginPage;
import org.dcm4chee.wizard.common.login.secure.ExtendedSwarmStrategy;
import org.dcm4chee.wizard.common.login.secure.SecureSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.security.components.SecureWebPage;
import org.wicketstuff.security.hive.HiveMind;
import org.wicketstuff.security.hive.config.PolicyFileHiveFactory;
import org.wicketstuff.security.hive.config.SwarmPolicyFileHiveFactory;
import org.wicketstuff.security.strategies.WaspAuthorizationStrategy;
import org.wicketstuff.security.swarm.SwarmWebApplication;
import org.wicketstuff.security.swarm.strategies.SwarmStrategyFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class SecureWebApplication extends SwarmWebApplication {

    private Class<? extends Page> homePage;
    private Class<? extends Page> signinPage;

    private final static Logger log = LoggerFactory.getLogger(SecureWebApplication.class);

    
    public SecureWebApplication() {
    }

    @Override
    protected void init() {
        super.init();
        
        signinPage = (Class<? extends Page>) getPageClass(getInitParameter("signinPageClass"), LoginPage.class);
        homePage = getPageClass(getInitParameter("homePageClass"), null);
        Class<? extends Page> internalErrorPage = getPageClass(getInitParameter("internalErrorPageClass"),
                InternalErrorPage.class);
        getApplicationSettings().setAccessDeniedPage(
                getPageClass(getInitParameter("accessDeniedPageClass"), AccessDeniedPage.class));
        getApplicationSettings().setPageExpiredErrorPage(
                getPageClass(getInitParameter("pageExpiredPageClass"), getHomePage()));
        if (internalErrorPage != null) {
            getApplicationSettings().setInternalErrorPage(internalErrorPage);
            this.getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Page> getPageClass(String className, Class<? extends Page> def) {
        Class<?> clazz = null;
        if (className != null) {
            try {
                clazz = (Class<? extends Page>) Class.forName(className);
            } catch (Throwable t) {
                log.error("Could not get Class " + className + "! use default:" + def, t);
            }
        }
        return (Class<? extends SecureWebPage>) (clazz == null ? def : clazz);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        if (homePage == null) {
            throw new RuntimeException("No HomePage is set!"
                    + " You have to set init-param 'homePageClass' in web.xml "
                    + "or subclass BaseWicketApplication and override getHomePage()!");
        }
        return homePage;
    }

    @Override
    protected Object getHiveKey() {
        return "hive_" + getName();
    }

    @Override
    protected void setUpHive() {
        PolicyFileHiveFactory factory = new SwarmPolicyFileHiveFactory(getActionFactory());
        try {
            factory.addPolicyFile(getServletContext().getResource("/WEB-INF/dcm4chee.hive"));
        } catch (MalformedURLException e) {
            throw new WicketRuntimeException(e);
        }
        HiveMind.registerHive(getHiveKey(), factory);
    }

    @Override
    protected void setupStrategyFactory() {
        setStrategyFactory(new SwarmStrategyFactory(getHiveKey()) {

            @Override
            public WaspAuthorizationStrategy newStrategy() {
                return new ExtendedSwarmStrategy(getHiveKey());
            }
        });
    }

    public Class<? extends Page> getLoginPage() {
        return signinPage;
    }

    @Override
    public SecureSession newSession(Request request, Response response) {
        return new SecureSession(this, request);
    }
}
