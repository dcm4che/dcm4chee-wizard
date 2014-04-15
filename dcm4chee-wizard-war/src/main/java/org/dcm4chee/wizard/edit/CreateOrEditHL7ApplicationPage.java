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

package org.dcm4chee.wizard.edit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4chee.wizard.common.behavior.FocusOnLoadBehavior;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ModalWindowRuntimeException;
import org.dcm4chee.wizard.common.component.secure.SecureSessionCheckPage;
import org.dcm4chee.wizard.model.ConnectionModel;
import org.dcm4chee.wizard.model.DeviceModel;
import org.dcm4chee.wizard.model.StringArrayModel;
import org.dcm4chee.wizard.model.hl7.HL7ApplicationModel;
import org.dcm4chee.wizard.tree.ConfigTreeNode;
import org.dcm4chee.wizard.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.validator.ConnectionProtocolValidator;
import org.dcm4chee.wizard.validator.ConnectionReferenceValidator;
import org.dcm4chee.wizard.validator.HL7NameValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditHL7ApplicationPage extends SecureSessionCheckPage {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditHL7ApplicationPage.class);

    // mandatory
    private Model<String> applicationNameModel;
    private Model<ArrayList<ConnectionModel>> connectionReferencesModel;

    // optional
    private StringArrayModel acceptedMessageTypesModel;
    private StringArrayModel acceptedSendingApplicationsModel;
    private Model<String> defaultCharacterSetModel;
    private Model<Boolean> installedModel;

    private List<String> installedRendererChoices;

    public CreateOrEditHL7ApplicationPage(final ModalWindow window, final HL7ApplicationModel hl7ApplicationModel,
            final ConfigTreeNode deviceNode) {
        super();

        add(new WebMarkupContainer("create-hl7-application-title").setVisible(hl7ApplicationModel == null));
        add(new WebMarkupContainer("edit-hl7-application-title").setVisible(hl7ApplicationModel != null));

        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");
        form.setResourceIdPrefix("dicom.edit.hl7-application.");
        add(form);

        installedRendererChoices = new ArrayList<String>();
        installedRendererChoices.add(new ResourceModel("dicom.installed.true.text").wrapOnAssignment(this).getObject());
        installedRendererChoices
                .add(new ResourceModel("dicom.installed.false.text").wrapOnAssignment(this).getObject());

        ArrayList<ConnectionModel> connectionReferences = new ArrayList<ConnectionModel>();

        try {
            connectionReferencesModel = new Model<ArrayList<ConnectionModel>>();
            connectionReferencesModel.setObject(new ArrayList<ConnectionModel>());
            for (Connection connection : ((DeviceModel) deviceNode.getModel()).getDevice().listConnections()) {
                ConnectionModel connectionReference = new ConnectionModel(connection, 0);
                connectionReferences.add(connectionReference);
                if (hl7ApplicationModel != null
                        && hl7ApplicationModel.getHL7Application().getConnections().contains(connection))
                    connectionReferencesModel.getObject().add(connectionReference);
            }

            if (hl7ApplicationModel == null) {
                applicationNameModel = Model.of();
                acceptedMessageTypesModel = new StringArrayModel(null);
                acceptedSendingApplicationsModel = new StringArrayModel(null);
                defaultCharacterSetModel = Model.of();
                installedModel = Model.of();
            } else {
                HL7Application hl7Application = hl7ApplicationModel.getHL7Application();

                applicationNameModel = Model.of(hl7Application.getApplicationName());
                acceptedMessageTypesModel = new StringArrayModel(hl7Application.getAcceptedMessageTypes());
                acceptedSendingApplicationsModel = new StringArrayModel(hl7Application.getAcceptedSendingApplications());
                defaultCharacterSetModel = Model.of(hl7Application.getHL7DefaultCharacterSet());
                installedModel = Model.of(hl7Application.getInstalled());
            }
        } catch (ConfigurationException ce) {
            log.error(this.getClass().toString() + ": " + "Error retrieving hl7 application data: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new ModalWindowRuntimeException(ce.getLocalizedMessage());
        }

        form.add(
                new Label("applicationName.label",
                        new ResourceModel("dicom.edit.hl7-application.applicationName.label"))).add(
                new TextField<String>("applicationName", applicationNameModel)
                        .add(new HL7NameValidator(applicationNameModel.getObject())).setRequired(true)
                        .add(FocusOnLoadBehavior.newFocusAndSelectBehaviour()));

        form.add(new CheckBoxMultipleChoice<ConnectionModel>("connections", connectionReferencesModel,
                new Model<ArrayList<ConnectionModel>>(connectionReferences), new IChoiceRenderer<ConnectionModel>() {

                    private static final long serialVersionUID = 1L;

                    public Object getDisplayValue(ConnectionModel connectionReference) {
                        Connection connection = null;
                        try {
                            connection = connectionReference.getConnection();
                        } catch (Exception e) {
                            log.error(this.getClass().toString() + ": " + "Error obtaining connection: "
                                    + e.getMessage());
                            log.debug("Exception", e);
                            throw new ModalWindowRuntimeException(e.getLocalizedMessage());
                        }
                        String location = connection.getHostname()
                                + (connection.getPort() == -1 ? "" : ":" + connection.getPort());
                        return connection.getCommonName() != null ? connection.getCommonName() + " (" + location + ")"
                                : location;
                    }

                    public String getIdValue(ConnectionModel model, int index) {
                        return String.valueOf(index);
                    }
                }).add(new ConnectionReferenceValidator())
                .add(new ConnectionProtocolValidator(Connection.Protocol.HL7)));

        final WebMarkupContainer optionalContainer = new WebMarkupContainer("optional");
        form.add(optionalContainer.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(false));

        form.add(new Label("toggleOptional.label", new ResourceModel("dicom.edit.toggleOptional.label"))).add(
                new AjaxCheckBox("toggleOptional", new Model<Boolean>()) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        target.add(optionalContainer.setVisible(this.getModelObject()));
                    }
                });

        optionalContainer.add(
                new Label("acceptedMessageTypes.label", new ResourceModel(
                        "dicom.edit.hl7-application.optional.acceptedMessageTypes.label"))).add(
                new TextArea<String>("acceptedMessageTypes", acceptedMessageTypesModel));

        optionalContainer.add(
                new Label("acceptedSendingApplications.label", new ResourceModel(
                        "dicom.edit.hl7-application.optional.acceptedSendingApplications.label"))).add(
                new TextArea<String>("acceptedSendingApplications", acceptedSendingApplicationsModel));

        optionalContainer.add(
                new Label("defaultCharacterSet.label", new ResourceModel(
                        "dicom.edit.hl7-application.optional.defaultCharacterSet.label"))).add(
                new TextField<String>("defaultCharacterSet", defaultCharacterSetModel));

        optionalContainer.add(
                new Label("installed.label", new ResourceModel("dicom.edit.hl7-application.optional.installed.label")))
                .add(new DropDownChoice<Boolean>("installed", installedModel, Arrays.asList(new Boolean[] {
                        new Boolean(true), new Boolean(false) }), new IChoiceRenderer<Boolean>() {

                    private static final long serialVersionUID = 1L;

                    public String getDisplayValue(Boolean object) {
                        return object.booleanValue() ? installedRendererChoices.get(0) : installedRendererChoices
                                .get(1);
                    }

                    public String getIdValue(Boolean object, int index) {
                        return String.valueOf(index);
                    }
                }).setNullValid(true));

        form.add(new IndicatingAjaxButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    String oldName = hl7ApplicationModel == null ? null : hl7ApplicationModel.getHL7Application()
                            .getApplicationName();

                    HL7Application hl7Application = hl7ApplicationModel == null ? new HL7Application(
                            applicationNameModel.getObject()) : (HL7Application) hl7ApplicationModel
                            .getHL7Application();

                    hl7Application.setApplicationName(applicationNameModel.getObject());
                    hl7Application.getConnections().clear();
                    for (ConnectionModel connectionReference : connectionReferencesModel.getObject())
                        for (Connection connection : ((DeviceModel) deviceNode.getModel()).getDevice()
                                .listConnections())
                            if (connectionReference.getConnection().getHostname().equals(connection.getHostname())
                                    && connectionReference.getConnection().getPort() == connection.getPort())
                                hl7Application.addConnection(connection);

                    hl7Application.setAcceptedMessageTypes(acceptedMessageTypesModel.getArray());
                    hl7Application.setAcceptedSendingApplications(acceptedSendingApplicationsModel.getArray());
                    hl7Application.setHL7DefaultCharacterSet(defaultCharacterSetModel.getObject());
                    hl7Application.setInstalled(installedModel.getObject());

                    if (hl7ApplicationModel == null) {
                        HL7DeviceExtension hl7DeviceExtension = ((DeviceModel) deviceNode.getModel()).getDevice()
                                .getDeviceExtension(HL7DeviceExtension.class);
                        if (hl7DeviceExtension == null) {
                            hl7DeviceExtension = new HL7DeviceExtension();
                            ((DeviceModel) deviceNode.getModel()).getDevice().addDeviceExtension(hl7DeviceExtension);
                        }
                        hl7DeviceExtension.addHL7Application(hl7Application);
                    } else
                        ConfigTreeProvider.get().unregisterHL7Application(oldName);
                    ConfigTreeProvider.get().mergeDevice(hl7Application.getDevice());
                    ConfigTreeProvider.get().registerHL7Application(applicationNameModel.getObject());
                    window.close(target);
                } catch (Exception e) {
                    log.error(this.getClass().toString() + ": " + "Error modifying HL7 application: " + e.getMessage());
                    log.debug("Exception", e);
                    throw new ModalWindowRuntimeException(e.getLocalizedMessage());
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                if (target != null)
                    target.add(form);
            }
        });
        form.add(new AjaxFallbackButton("cancel", new ResourceModel("cancelBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                window.close(target);
            }

            @Override
            protected void onError(AjaxRequestTarget arg0, Form<?> arg1) {
            }
        }.setDefaultFormProcessing(false));
    }
}
