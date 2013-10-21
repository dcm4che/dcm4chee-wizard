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
import java.util.Collections;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.audit.AuditLogger;
import org.dcm4che.net.audit.AuditLogger.Facility;
import org.dcm4che.net.audit.AuditLogger.Severity;
import org.dcm4chee.wizard.WizardApplication;
import org.dcm4chee.wizard.common.behavior.FocusOnLoadBehavior;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ModalWindowRuntimeException;
import org.dcm4chee.wizard.common.component.secure.SecureSessionCheckPage;
import org.dcm4chee.wizard.model.AuditLoggerModel;
import org.dcm4chee.wizard.model.ConnectionModel;
import org.dcm4chee.wizard.model.DeviceModel;
import org.dcm4chee.wizard.model.StringArrayModel;
import org.dcm4chee.wizard.tree.ConfigTreeNode;
import org.dcm4chee.wizard.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.validator.ConnectionProtocolValidator;
import org.dcm4chee.wizard.validator.ConnectionReferenceValidator;
import org.dcm4chee.wizard.validator.DeviceListValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditAuditLoggerPage extends SecureSessionCheckPage {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditAuditLoggerPage.class);

    // mandatory
    private Model<String> applicationNameModel;
    private Model<ArrayList<ConnectionModel>> connectionReferencesModel;
    private Model<String> arrDeviceNameModel;

    // optional
    private Model<String> auditEnterpriseSiteIDModel;
    private Model<String> auditSourceIDModel;
    private StringArrayModel auditSourceTypeCodesModel;
    private Model<String> encodingModel;
    private Model<Facility> facilityModel;
    private Model<Boolean> formatXMLModel;
    private Model<Boolean> includeBOMModel;
    private Model<Boolean> installedModel;
    private Model<String> messageIDModel;
    private Model<Severity> majorFailureSeverityModel;
    private Model<String> schemaURIModel;
    private Model<Severity> minorFailureSeverityModel;
    private Model<Severity> seriousFailureSeverityModel;
    private Model<Severity> successSeverityModel;
    private Model<Boolean> timestampInUTCModel;

    private List<String> installedRendererChoices;

    public CreateOrEditAuditLoggerPage(final ModalWindow window, final AuditLoggerModel auditLoggerModel,
            final ConfigTreeNode deviceNode) {
        super();

        add(new WebMarkupContainer("create-audit-logger-title").setVisible(auditLoggerModel == null));
        add(new WebMarkupContainer("edit-audit-logger-title").setVisible(auditLoggerModel != null));

        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");
        form.setResourceIdPrefix("dicom.edit.audit-logger.");
        add(form);

        installedRendererChoices = new ArrayList<String>();
        installedRendererChoices.add(new ResourceModel("dicom.installed.true.text").wrapOnAssignment(this).getObject());
        installedRendererChoices
                .add(new ResourceModel("dicom.installed.false.text").wrapOnAssignment(this).getObject());

        ArrayList<ConnectionModel> connectionReferences = new ArrayList<ConnectionModel>();
        List<String> deviceList = null;

        try {
            connectionReferencesModel = new Model<ArrayList<ConnectionModel>>();
            connectionReferencesModel.setObject(new ArrayList<ConnectionModel>());
            for (Connection connection : ((DeviceModel) deviceNode.getModel()).getDevice().listConnections()) {
                ConnectionModel connectionReference = new ConnectionModel(connection, 0);
                connectionReferences.add(connectionReference);
                if (auditLoggerModel != null && auditLoggerModel.getAuditLogger().getConnections().contains(connection))
                    connectionReferencesModel.getObject().add(connectionReference);
            }

            deviceList = ((WizardApplication) getApplication()).getDicomConfigurationManager().listDevices();
            Collections.sort(deviceList);

            arrDeviceNameModel = Model.of();
            if (auditLoggerModel == null) {
                applicationNameModel = Model.of();
                auditEnterpriseSiteIDModel = Model.of();
                auditSourceIDModel = Model.of();
                auditSourceTypeCodesModel = new StringArrayModel(null);
                encodingModel = Model.of("UTF-8");
                facilityModel = Model.of(AuditLogger.Facility.authpriv);
                formatXMLModel = Model.of();
                includeBOMModel = Model.of();
                installedModel = Model.of();
                majorFailureSeverityModel = Model.of(AuditLogger.Severity.crit);
                messageIDModel = Model.of(AuditLogger.MESSAGE_ID);
                minorFailureSeverityModel = Model.of(AuditLogger.Severity.warning);
                schemaURIModel = Model.of();
                seriousFailureSeverityModel = Model.of(AuditLogger.Severity.err);
                successSeverityModel = Model.of(AuditLogger.Severity.notice);
                timestampInUTCModel = Model.of();
            } else {
                AuditLogger auditLogger = auditLoggerModel.getAuditLogger();
                applicationNameModel = Model.of(auditLogger.getApplicationName());
                auditEnterpriseSiteIDModel = Model.of(auditLogger.getAuditEnterpriseSiteID());
                if (auditLogger.getAuditRecordRepositoryDevice() != null)
                    arrDeviceNameModel = Model.of(auditLogger.getAuditRecordRepositoryDevice().getDeviceName());
                Collections.sort(deviceList);
                auditSourceIDModel = Model.of(auditLogger.getAuditSourceID());
                auditSourceTypeCodesModel = new StringArrayModel(auditLogger.getAuditSourceTypeCodes());
                encodingModel = Model.of(auditLogger.getEncoding());
                facilityModel = Model.of(auditLogger.getFacility());
                formatXMLModel = Model.of(auditLogger.isFormatXML());
                includeBOMModel = Model.of(auditLogger.isIncludeBOM());
                installedModel = Model.of(auditLogger.getInstalled());
                majorFailureSeverityModel = Model.of(auditLogger.getMajorFailureSeverity());
                messageIDModel = Model.of(auditLogger.getMessageID());
                minorFailureSeverityModel = Model.of(auditLogger.getMinorFailureSeverity());
                schemaURIModel = Model.of(auditLogger.getSchemaURI());
                seriousFailureSeverityModel = Model.of(auditLogger.getSeriousFailureSeverity());
                successSeverityModel = Model.of(auditLogger.getSuccessSeverity());
                timestampInUTCModel = Model.of(auditLogger.isTimestampInUTC());
            }
        } catch (ConfigurationException ce) {
            log.error(this.getClass().toString() + ": " + "Error retrieving audit logger data: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new ModalWindowRuntimeException(ce.getLocalizedMessage());
        }

        form.add(new Label("applicationName.label", new ResourceModel("dicom.edit.audit-logger.applicationName.label")))
                .add(new TextField<String>("applicationName", applicationNameModel).setRequired(true).add(
                        FocusOnLoadBehavior.newFocusAndSelectBehaviour()));

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
                }).add(new ConnectionReferenceValidator()).add(
                new ConnectionProtocolValidator(Connection.Protocol.SYSLOG_TLS, Connection.Protocol.SYSLOG_UDP)));

        DeviceListValidator deviceListValidator = null;
        if (arrDeviceNameModel.getObject() != null && !deviceList.contains(arrDeviceNameModel.getObject())) {
            deviceList.add(0, arrDeviceNameModel.getObject());
            deviceListValidator = new DeviceListValidator();
        }

        DropDownChoice<String> arrDeviceNameDropDownChoice;
        form.add(new Label("arrDeviceName.label", new ResourceModel("dicom.edit.audit-logger.arrDeviceName.label")))
                .add((arrDeviceNameDropDownChoice = new DropDownChoice<String>("arrDeviceName", arrDeviceNameModel,
                        deviceList)).setNullValid(false).setRequired(true));
        if (arrDeviceNameModel.getObject() == null)
            arrDeviceNameModel.setObject(deviceList.get(0));
        if (deviceListValidator != null)
            arrDeviceNameDropDownChoice.add(deviceListValidator);

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
                new Label("auditEnterpriseSiteID.label", new ResourceModel(
                        "dicom.edit.audit-logger.optional.auditEnterpriseSiteID.label"))).add(
                new TextField<String>("auditEnterpriseSiteID", auditEnterpriseSiteIDModel));

        optionalContainer.add(
                new Label("auditSourceID.label", new ResourceModel(
                        "dicom.edit.audit-logger.optional.auditSourceID.label"))).add(
                new TextField<String>("auditSourceID", auditSourceIDModel));

        optionalContainer.add(
                new Label("auditSourceTypeCodes.label", new ResourceModel(
                        "dicom.edit.audit-logger.optional.auditSourceTypeCodes.label"))).add(
                new TextArea<String>("auditSourceTypeCodes", auditSourceTypeCodesModel));

        optionalContainer.add(
                new Label("encoding.label", new ResourceModel("dicom.edit.audit-logger.optional.encoding.label"))).add(
                new TextField<String>("encoding", encodingModel).setRequired(true));

        optionalContainer.add(
                new Label("facility.label", new ResourceModel("dicom.edit.audit-logger.optional.facility.label"))).add(
                new DropDownChoice<Facility>("facility", facilityModel, Arrays.asList(AuditLogger.Facility.values())));

        optionalContainer.add(
                new Label("formatXML.label", new ResourceModel("dicom.edit.audit-logger.optional.formatXML.label")))
                .add(new CheckBox("formatXML", formatXMLModel));

        optionalContainer.add(
                new Label("includeBOM.label", new ResourceModel("dicom.edit.audit-logger.optional.includeBOM.label")))
                .add(new CheckBox("includeBOM", includeBOMModel));

        optionalContainer.add(
                new Label("installed.label", new ResourceModel("dicom.edit.audit-logger.optional.installed.label")))
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

        optionalContainer.add(
                new Label("messageID.label", new ResourceModel("dicom.edit.audit-logger.optional.messageID.label")))
                .add(new TextField<String>("messageID", messageIDModel).setRequired(true));

        optionalContainer.add(
                new Label("schemaURI.label", new ResourceModel("dicom.edit.audit-logger.optional.schemaURI.label")))
                .add(new TextField<String>("schemaURI", schemaURIModel));

        optionalContainer.add(
                new Label("timestampInUTC.label", new ResourceModel(
                        "dicom.edit.audit-logger.optional.timestampInUTC.label"))).add(
                new CheckBox("timestampInUTC", timestampInUTCModel));

        List<Severity> severities = Arrays.asList(AuditLogger.Severity.values());

        optionalContainer.add(
                new Label("majorFailureSeverity.label", new ResourceModel(
                        "dicom.edit.audit-logger.optional.majorFailureSeverity.label"))).add(
                new DropDownChoice<Severity>("majorFailureSeverity", majorFailureSeverityModel, severities));

        optionalContainer.add(
                new Label("minorFailureSeverity.label", new ResourceModel(
                        "dicom.edit.audit-logger.optional.minorFailureSeverity.label"))).add(
                new DropDownChoice<Severity>("minorFailureSeverity", minorFailureSeverityModel, severities));

        optionalContainer.add(
                new Label("seriousFailureSeverity.label", new ResourceModel(
                        "dicom.edit.audit-logger.optional.seriousFailureSeverity.label"))).add(
                new DropDownChoice<Severity>("seriousFailureSeverity", seriousFailureSeverityModel, severities));

        optionalContainer.add(
                new Label("successSeverity.label", new ResourceModel(
                        "dicom.edit.audit-logger.optional.successSeverity.label"))).add(
                new DropDownChoice<Severity>("successSeverity", successSeverityModel, severities));

        form.add(new AjaxFallbackButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    AuditLogger auditLogger = auditLoggerModel == null ? new AuditLogger() : auditLoggerModel
                            .getAuditLogger();

                    auditLogger.setApplicationName(applicationNameModel.getObject());
                    auditLogger.getConnections().clear();
                    for (ConnectionModel connectionReference : connectionReferencesModel.getObject())
                        for (Connection connection : ((DeviceModel) deviceNode.getModel()).getDevice()
                                .listConnections())
                            if (connectionReference.getConnection().getHostname().equals(connection.getHostname())
                                    && connectionReference.getConnection().getPort() == connection.getPort())
                                auditLogger.addConnection(connection);

                    auditLogger.setAuditRecordRepositoryDevice(((WizardApplication) getApplication())
                            .getDicomConfigurationManager().getDicomConfiguration()
                            .findDevice(arrDeviceNameModel.getObject()));

                    if (optionalContainer.isVisible()) {
                        auditLogger.setAuditEnterpriseSiteID(auditEnterpriseSiteIDModel.getObject());
                        auditLogger.setAuditSourceID(auditSourceIDModel.getObject());
                        auditLogger.setAuditSourceTypeCodes(auditSourceTypeCodesModel.getArray());
                        if (encodingModel.getObject() != null)
                            auditLogger.setEncoding(encodingModel.getObject());
                        auditLogger.setFacility(facilityModel.getObject());
                        auditLogger.setFormatXML(formatXMLModel.getObject());
                        auditLogger.setIncludeBOM(includeBOMModel.getObject());
                        auditLogger.setInstalled(installedModel.getObject());
                        auditLogger.setMessageID(messageIDModel.getObject());
                        auditLogger.setSchemaURI(schemaURIModel.getObject());
                        auditLogger.setTimestampInUTC(timestampInUTCModel.getObject());
                        auditLogger.setSeriousFailureSeverity(seriousFailureSeverityModel.getObject());
                        auditLogger.setMajorFailureSeverity(majorFailureSeverityModel.getObject());
                        auditLogger.setMinorFailureSeverity(minorFailureSeverityModel.getObject());
                        auditLogger.setSuccessSeverity(successSeverityModel.getObject());
                    }

                    Device device = ((DeviceModel) deviceNode.getModel()).getDevice();
                    if (auditLoggerModel == null)
                        device.addDeviceExtension(auditLogger);
                    ConfigTreeProvider.get().mergeDevice(device);
                    window.close(target);
                } catch (Exception e) {
                    log.error(this.getClass().toString() + ": " + "Error modifying HL7 application: " + e.getMessage());
                    if (log.isDebugEnabled())
                        e.printStackTrace();
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
