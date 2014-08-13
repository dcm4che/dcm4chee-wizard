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

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.time.Duration;
import org.dcm4chee.wizard.DicomConfigurationManager;
import org.dcm4chee.wizard.WizardApplication;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ModalWindowRuntimeException;
import org.dcm4chee.wizard.common.component.secure.MessageWindow;
import org.dcm4chee.wizard.common.component.secure.SecureSessionCheckPage;
import org.dcm4chee.wizard.tcxml.Group;
import org.dcm4chee.wizard.tcxml.Profile;
import org.dcm4chee.wizard.tree.ConfigTreeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class AutoDetectTransferCapabilities extends SecureSessionCheckPage {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(AutoDetectTransferCapabilities.class);
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    public AutoDetectTransferCapabilities(final ModalWindow window, final String connectedDeviceUrl, final String aeTitle, final String deviceName) {
        super();
        
        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");

        add(form);

        final Model<String> feedbackErrorModel = new Model<String>();
        final Label feedbackErrorLabel = new Label("feedback.error", feedbackErrorModel);
        
        final Model<String> feedbackSuccessModel = new Model<String>();
        final Label feedbackSuccessLabel = new Label("feedback.success", feedbackSuccessModel);

        final Model<String> feedbackProgressModel = new Model<String>();
        final Label feedbackProgressLabel = new Label("feedback.progress", feedbackProgressModel);
        
        isStarted.set(false);
        
        form.add(new Label("dicom.autoDetectTC.device", new Model<String>(deviceName)));
        form.add(new Label("dicom.autoDetectTC.ae", new Model<String>(aeTitle)));
        
        feedbackSuccessLabel.setOutputMarkupId(true);
        feedbackErrorLabel.setOutputMarkupId(true);
        feedbackProgressLabel.setOutputMarkupId(true);
        
        final Button noBtn = new AjaxFallbackButton("no", new Model<String>("Close"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                window.close(target);
            }

            @Override
            protected void onError(AjaxRequestTarget arg0, Form<?> arg1) {
            }
        }.setDefaultFormProcessing(false);
        form.add(noBtn.setOutputMarkupPlaceholderTag(true));
        
        AbstractAjaxTimerBehavior atb = new AbstractAjaxTimerBehavior(Duration.seconds(2)) {

            @Override
            protected void onTimer(AjaxRequestTarget target) {
                // if process was launched, do update the progress
                if (isStarted.get()) {

                    String req = "getAutoConfigProgress";
                    HttpURLConnection connection;
                    try {
                        connection = (HttpURLConnection) new URL(connectedDeviceUrl + (connectedDeviceUrl.endsWith("/") ? req : "/" + req))
                                .openConnection();

                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);

                        StringWriter writer = new StringWriter();
                        IOUtils.copy(connection.getInputStream(), writer);
                        String returnedMessage = writer.toString();
                        int responseCode = connection.getResponseCode();

                        if (responseCode == 200) {

                            feedbackProgressModel.setObject(new DecimalFormat("#.##").format(Float.parseFloat(returnedMessage)) + " % completed");
                            target.add(feedbackProgressLabel);
                            
                            // if 100 reached
                            if (Math.abs(Float.parseFloat(returnedMessage)-100.0)< 0.0001) {

                                log.info("Autodetection finished");

                                // show 'close' button
                                noBtn.setVisible(true);
                                target.add(noBtn);
                                
                                // stop updating
                                isStarted.set(false);
                                
                                // reload the config
                                ((WizardApplication) getApplication()).getDicomConfigurationManager().getDicomConfiguration().sync();
                                ((WizardApplication) getApplication()).getDicomConfigurationManager().resetDeviceMap();
                                ConfigTreeProvider.get().loadDeviceList();
                            }

                        } else
                            throw new Exception("Unexpected response from the server (" + responseCode + ")");

                    } catch (Exception e) {
                        feedbackErrorModel.setObject("Error while trying to request current status: " + e.getMessage());
                        target.add(feedbackErrorLabel);
                    }
                }
            }
            
        };
        
        form.add(atb);
        
       
        
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
                            this.setVisible(false);
                            isStarted.set(true);
                            
                            feedbackProgressModel.setObject("0.0 % completed");
                            target.add(feedbackProgressLabel);

                            
                            noBtn.setVisible(false);
                            target.add(feedbackSuccessLabel);
                            target.add(noBtn);
                            target.add(this);
                          
                        } else if (responseCode == 404) {
                                String msg = "The server has not found anything matching the Request-URI "
                                        + connection.getURL().toString() + ", HTTP Status "
                                        + connection.getResponseCode() + ": " + connection.getResponseMessage();
                                throw new Exception(msg);
                           
                        } else throw new Exception(returnedMessage);

                    } catch (Exception e) {
                        log.error("Error launching auto detection of TCs",e);

                        // show error message to user
                        feedbackErrorModel.setObject(e.getClass().getName()+" -- "+e.getMessage());
                        target.add(feedbackErrorLabel);
                    }                    
                    
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


        
        
        form.add(feedbackErrorLabel);
        form.add(feedbackSuccessLabel);
        form.add(feedbackProgressLabel);
    }
}
