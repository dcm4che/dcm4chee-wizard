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

import java.io.IOException;
import java.io.InputStream;

import javax.security.auth.Subject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.wicket.Page;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.settings.IExceptionSettings.AjaxErrorStrategy;
import org.dcm4chee.wizard.common.component.secure.SecureWebApplication;
import org.dcm4chee.wizard.common.login.context.LoginContextSecurityHelper;
import org.dcm4chee.wizard.common.login.context.SSOLoginContext;
import org.dcm4chee.wizard.common.login.secure.SecureSession;
import org.dcm4chee.wizard.page.MainPage;
import org.dcm4chee.wizard.tcxml.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.security.authentication.LoginException;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class WizardApplication extends SecureWebApplication {

    protected static Logger log = LoggerFactory.getLogger(WizardApplication.class);

    private DicomConfigurationManager dicomConfigurationManager;
    private Root root;

    @Override
    protected void init() {
        super.init();
        getExceptionSettings().setAjaxErrorHandlingStrategy(AjaxErrorStrategy.INVOKE_FAILURE_HANDLER);
        getRequestCycleListeners().add(new AbstractRequestCycleListener() {
            public IRequestHandler onException(RequestCycle cycle, Exception e) {
//                while (e.getCause() != null)
//                    e = (Exception) e.getCause();
//                if (!(e instanceof PageExpiredException))
//                    cycle.setResponsePage(new InternalErrorPage(e, null));
                log.error("{}: Unexpected exception: {}", this, e);
                return cycle.getRequestHandlerScheduledAfterCurrent();
            }
        });
        getDicomConfigurationManager();
        getTransferCapabilityProfiles();
    }

    public synchronized DicomConfigurationManager getDicomConfigurationManager() {
        if (dicomConfigurationManager == null)
            dicomConfigurationManager = new DicomConfigurationManager(getInitParameter("ldapPropertiesURL"));
        return dicomConfigurationManager;
    }

    public synchronized Root getTransferCapabilityProfiles() {
        if (root == null) {
            InputStream stream = getServletContext().getResourceAsStream("/WEB-INF/transferCapabilities.xml");
            try {
                root = (Root) JAXBContext.newInstance(Root.class).createUnmarshaller().unmarshal(stream);
            } catch (JAXBException je) {
                log.error("Error processing transfer capability profiles from xml file", je);
            } finally {
                try {
                    stream.close();
                } catch (IOException ignore) {}
            }
        }
        return root;
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return MainPage.class;
    }

    @Override
    public SecureSession newSession(Request request, Response response) {
        Subject jaasSubject = LoginContextSecurityHelper.getJaasSubject();
        SecureSession session = new SecureSession(this, request);
        if (jaasSubject != null) {
            try {
                session.login(new SSOLoginContext(session, jaasSubject));
                log.debug("Container authenticated session login done! Unbind session '{}' from SessionStore!",
                        session.getId());
            } catch (LoginException x) {
                log.error(getClass().getName() + ": Failed login", x);
            }
        }
        return session;
    }
}
