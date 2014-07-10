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

package org.dcm4chee.wizard.page;

import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.io.IOUtils;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ModalWindowRuntimeException;
import org.dcm4chee.wizard.common.component.secure.SecureSessionCheckPage;
import org.dcm4chee.wizard.tcxml.Group;
import org.dcm4chee.wizard.tcxml.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class AutoDetectTransferCapabilities extends SecureSessionCheckPage {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(AutoDetectTransferCapabilities.class);


    public AutoDetectTransferCapabilities(final ModalWindow window, final String connectedDeviceUrl, final String aeTitle, final String deviceName) {
        super();
        
        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");

        add(form);

        final Model feedbackErrorModel = new Model<String>();
        final Label feedbackErrorLabel = new Label("feedback.error", feedbackErrorModel);
        
        final Model feedbackSuccessModel = new Model<String>();
        final Label feedbackSuccessLabel = new Label("feedback.success", feedbackSuccessModel);
        

        form.add(new Label("dicom.autoDetectTC.device", new Model<String>(deviceName)));
        form.add(new Label("dicom.autoDetectTC.ae", new Model<String>(aeTitle)));
        
        feedbackSuccessLabel.setOutputMarkupId(true);
        feedbackErrorLabel.setOutputMarkupId(true);
        
        form.add(new IndicatingAjaxButton("yes", new ResourceModel("yesBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                
                try {

                    try {
                        String req = "setTransferCapabilities/"+aeTitle;
                        
                        log.info("Request: {}",req);
                        
                        HttpURLConnection connection = (HttpURLConnection) new URL(connectedDeviceUrl
                                + (connectedDeviceUrl.endsWith("/") ? req : "/" + req)).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);
                        
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(connection.getInputStream(), writer);
                        String returnedMessage = writer.toString();
                        int responseCode = connection.getResponseCode();
                        
                        connection.disconnect();

                        if (responseCode == 200) {
                            // show error message to user
                            feedbackSuccessModel.setObject(returnedMessage);
                            target.add(feedbackSuccessLabel);
                          
                        } else if (responseCode == 404) {
                                String msg = "The server has not found anything matching the Request-URI "
                                        + connection.getURL().toString() + ", HTTP Status "
                                        + connection.getResponseCode() + ": " + connection.getResponseMessage();
                                throw new Exception(msg);
                           
                        } else throw new Exception(returnedMessage);

                        /*
                        ((WizardApplication) getApplication()).getDicomConfigurationManager().clearReload(
                                rowModel.getObject().getName());*/
                    } catch (Exception e) {
                        log.error("Error launching auto detection of TCs",e);

                        // show error message to user
                        feedbackErrorModel.setObject(e.getClass().getName()+" -- "+e.getMessage());
                        target.add(feedbackErrorLabel);
                    }                    
                    
                    //window.close(target);
                } catch (Exception e) {
                    throw new ModalWindowRuntimeException(e.getLocalizedMessage());
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                if (target != null)
                    target.add(form);
            }
        });
        form.add(new AjaxFallbackButton("no", new ResourceModel("noBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                window.close(target);
            }

            @Override
            protected void onError(AjaxRequestTarget arg0, Form<?> arg1) {
            }
        }.setDefaultFormProcessing(false));
        
        form.add(feedbackErrorLabel);
        form.add(feedbackSuccessLabel);
    }

    private List<Profile> orderedProfiles(Group group) {
        List<Profile> profiles = group.getTransferCapabilityProfiles();
        Collections.sort(profiles, new Comparator<Profile>() {
            public int compare(Profile profile1, Profile profile2) {
                return profile1.name.compareToIgnoreCase(profile2.name);
            }
        });
        return profiles;
    }
}
