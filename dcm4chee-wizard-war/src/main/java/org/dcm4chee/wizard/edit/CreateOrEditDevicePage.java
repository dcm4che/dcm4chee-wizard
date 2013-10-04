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
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.RangeValidator;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.data.Code;
import org.dcm4che.data.Issuer;
import org.dcm4che.net.Device;
import org.dcm4che.net.DeviceExtension;
import org.dcm4che.net.audit.AuditRecordRepository;
import org.dcm4che.net.hl7.HL7DeviceExtension;
import org.dcm4chee.proxy.conf.ProxyDeviceExtension;
import org.dcm4chee.wizard.common.behavior.FocusOnLoadBehavior;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ModalWindowRuntimeException;
import org.dcm4chee.wizard.model.DeviceModel;
import org.dcm4chee.wizard.model.InstitutionCodeModel;
import org.dcm4chee.wizard.model.StringArrayModel;
import org.dcm4chee.wizard.page.DicomConfigurationWebPage;
import org.dcm4chee.wizard.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.tree.ConfigTreeProvider.ConfigurationType;
import org.dcm4chee.wizard.validator.CodeValidator;
import org.dcm4chee.wizard.validator.DeviceNameValidator;
import org.dcm4chee.wizard.validator.UrlValidator;
import org.dcm4chee.xds2.conf.XCAInitiatingGWCfg;
import org.dcm4chee.xds2.conf.XCARespondingGWCfg;
import org.dcm4chee.xds2.conf.XCAiInitiatingGWCfg;
import org.dcm4chee.xds2.conf.XCAiRespondingGWCfg;
import org.dcm4chee.xds2.conf.XdsRegistry;
import org.dcm4chee.xds2.conf.XdsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class CreateOrEditDevicePage extends DicomConfigurationWebPage {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditDevicePage.class);

    private List<String> keyStoreTypes = Arrays.asList(new String[] { "JKS", "PKCS12" });

    // configuration type selection
    private Model<ConfigurationType> typeModel;

    // mandatory
    private Model<String> deviceNameModel;
    private IModel<Boolean> installedModel;

    // ProxyDevice
    private Model<Integer> schedulerIntervalModel;
    private Model<Integer> forwardThreadsModel;
    private Model<Integer> staleTimeoutModel;

    // XDS Device
    //XCAiInitiatingGW
    private Model<String> xdsApplicationNameModel;
    private Model<String> xdsHomeCommunityIdModel;
    private StringArrayModel xdsRespondingGatewayUrlModel;

    // optional
    private Model<String> descriptionModel;
    private Model<String> deviceSerialNumberModel;
    private StringArrayModel institutionAddressModel;
    private InstitutionCodeModel institutionCodeModel;
    private StringArrayModel institutionalDepartmentNameModel;
    private StringArrayModel institutionNameModel;
    private Model<String> issuerOfAccessionNumberModel;
    private Model<String> issuerOfAdmissionIDModel;
    private Model<String> issuerOfContainerIdentifierModel;
    private Model<String> issuerOfPatientIDModel;
    private Model<String> issuerOfServiceEpisodeIDModel;
    private Model<String> issuerOfSpecimenIdentifierModel;
    private Model<String> manufacturerModel;
    private Model<String> manufacturerModelNameModel;
    private Model<String> orderFillerIdentifierModel;
    private Model<String> orderPlacerIdentifierModel;
    private StringArrayModel primaryDeviceTypesModel;
    private StringArrayModel relatedDeviceRefsModel;
    private StringArrayModel softwareVersionsModel;
    private Model<String> stationNameModel;
    private Model<String> vendorDataModel;
    private Model<String> trustStoreURLModel;
    private Model<String> trustStoreTypeModel;
    private Model<String> trustStorePinModel;
    private Model<Boolean> useTrustStorePinProperty;
    private Model<String> keyStoreURLModel;
    private Model<String> keyStoreTypeModel;
    private Model<String> keyStorePinModel;
    private Model<Boolean> useKeyStorePinProperty;
    private Model<String> keyStoreKeyPinModel;
    private Model<Boolean> useKeyStoreKeyPinProperty;

    public CreateOrEditDevicePage(final ModalWindow window, final DeviceModel deviceModel) {
        super();
        add(new WebMarkupContainer("create-device-title").setVisible(deviceModel == null));
        add(new WebMarkupContainer("edit-device-title").setVisible(deviceModel != null));
        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");
        form.setResourceIdPrefix("dicom.edit.device.");
        add(form);
        if (deviceModel == null)
            initBasicConfigurationTypeModel();
        else
            initDeviceModel(deviceModel);
        addTypeLabel(form);
        DropDownChoice<ConfigTreeProvider.ConfigurationType> typeDropDown = setConfigurationTypeList();
        addOnChangeUpdate(deviceModel, form, typeDropDown);
        addDeviceTitle(deviceModel, form);
        addInstalledLabel(form);
        form.add(proxyWebMarkupContainer(deviceModel));
        form.add(xdsWebMarkupContainer(deviceModel));
        final Form<?> optionalContainer = addOptionalContainer(form);
        addSaveButton(window, deviceModel, form, optionalContainer);
        addCancelButton(window, form);
    }

    private Form<?> addOptionalContainer(final ExtendedForm form) {
        final Form<?> optionalContainer = new Form<Object>("optional");
        optionalContainer.setOutputMarkupId(true);
        optionalContainer.setOutputMarkupPlaceholderTag(true);
        optionalContainer.setVisible(false);
        addTextField(optionalContainer, 
                "dicom.edit.device.optional.description.label", 
                "description.label", 
                "description", 
                descriptionModel);
        addToggleOptionalCheckBox(form, optionalContainer);
        addOptionalParameters(optionalContainer);
        addOptionalProxyContainer(optionalContainer);
        addRelatedDeviceRefsContainer(optionalContainer);
        addWebMarkupContainer(optionalContainer);
        form.add(optionalContainer);
        return optionalContainer;
    }

    private void addToggleOptionalCheckBox(final ExtendedForm form, final Form<?> optionalContainer) {
        form.add(new Label("toggleOptional.label", new ResourceModel("dicom.edit.toggleOptional.label")));
        form.add(new AjaxCheckBox("toggleOptional", new Model<Boolean>()) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(optionalContainer.setVisible(this.getModelObject()));
            }
        });
    }

    private void addWebMarkupContainer(final Form<?> optionalContainer) {
        WebMarkupContainer vendorDataContainer = new WebMarkupContainer("vendorDataContainer");
        optionalContainer.add(vendorDataContainer);
        vendorDataContainer.add(new Label("vendorData.label", new ResourceModel(
                "dicom.edit.applicationEntity.vendorData.label")));
        vendorDataContainer.add(new Label("vendorData", vendorDataModel));
        vendorDataContainer.setVisible(!vendorDataModel.getObject().equals("size 0"));
    }

    private void addRelatedDeviceRefsContainer(final Form<?> optionalContainer) {
        WebMarkupContainer relatedDeviceRefsContainer = new WebMarkupContainer("relatedDeviceRefsContainer");
        optionalContainer.add(relatedDeviceRefsContainer);
        relatedDeviceRefsContainer.add(new Label("relatedDeviceRefs.label", new ResourceModel(
                "dicom.edit.device.relatedDeviceRefs.label")));
        relatedDeviceRefsContainer.add(new TextArea<String>("relatedDeviceRefs", relatedDeviceRefsModel)
                .setEnabled(false));
        relatedDeviceRefsContainer.setVisible(relatedDeviceRefsModel.getArray().length > 0);
    }

    private void addOptionalProxyContainer(final Form<?> optionalContainer) {
        WebMarkupContainer optionalProxyContainer = new WebMarkupContainer("optionalProxyContainer") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return optionalContainer.isVisible() && typeModel.getObject().equals(ConfigurationType.Proxy);
            }
        };
        optionalContainer.add(optionalProxyContainer.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));

        optionalProxyContainer.add(new Label("forwardThreads.label", new ResourceModel(
                "dicom.edit.device.optional.forwardThreads.label")).setOutputMarkupPlaceholderTag(true));
        optionalProxyContainer.add(new TextField<Integer>("forwardThreads", forwardThreadsModel).setType(Integer.class)
                .add(new RangeValidator<Integer>(1, 256)));

        optionalProxyContainer.add(new Label("staleTimeout.label", new ResourceModel(
                "dicom.edit.device.optional.staleTimeout.label")).setOutputMarkupPlaceholderTag(true));
        optionalProxyContainer.add(new TextField<Integer>("staleTimeout", staleTimeoutModel).setType(Integer.class)
                .add(new RangeValidator<Integer>(0, Integer.MAX_VALUE)));
    }

    private void addOptionalParameters(final Form<?> optionalContainer) {
        addTextField(optionalContainer,
                "dicom.edit.device.optional.deviceSerialNumber.label",
                "deviceSerialNumber.label",
                "deviceSerialNumber", 
                deviceSerialNumberModel);

        addTextArea(optionalContainer,
                "dicom.edit.device.optional.institutionAddress.label",
                "institutionAddress.label",
                "institutionAddress",
                institutionAddressModel);

        TextField<String> codeValueTextField = addTextField(optionalContainer, 
                "dicom.edit.device.optional.institutionCodeValue.label", 
                "institutionCodeValue.label", 
                "institutionCodeValue", 
                institutionCodeModel.getCodeFieldModel(0));

        TextField<String> codingSchemeDesignatorTextField = addTextField(optionalContainer, 
                "dicom.edit.device.optional.institutionCodingSchemeDesignator.label", 
                "institutionCodingSchemeDesignator.label", 
                "institutionCodingSchemeDesignator", 
                institutionCodeModel.getCodeFieldModel(1));

        TextField<String> codingSchemeVersionTextField = addTextField(optionalContainer, 
                "dicom.edit.device.optional.institutionCodingSchemeVersion.label", 
                "institutionCodingSchemeVersion.label", 
                "institutionCodingSchemeVersion", 
                institutionCodeModel.getCodeFieldModel(2));

        TextField<String> codeMeaningTextField = addTextField(optionalContainer, 
                "dicom.edit.device.optional.institutionCodeMeaning.label", 
                "institutionCodeMeaning.label", 
                "institutionCodeMeaning", 
                institutionCodeModel.getCodeFieldModel(3));

        optionalContainer.add(new CodeValidator(codeValueTextField, codingSchemeDesignatorTextField,
                codingSchemeVersionTextField, codeMeaningTextField));

        addTextArea(optionalContainer, 
                "dicom.edit.device.optional.institutionalDepartmentName.label", 
                "institutionalDepartmentName.label", 
                "institutionalDepartmentName", 
                institutionalDepartmentNameModel);

        addTextArea(optionalContainer, 
                "dicom.edit.device.optional.institutionName.label", 
                "institutionName.label", 
                "institutionName", 
                institutionNameModel);

        addTextField(optionalContainer, 
                "dicom.edit.device.optional.issuerOfAccessionNumber.label", 
                "issuerOfAccessionNumber.label", 
                "issuerOfAccessionNumber", 
                issuerOfAccessionNumberModel);

        optionalContainer.add(new Label("issuerOfAdmissionID.label", new ResourceModel(
                "dicom.edit.device.optional.issuerOfAdmissionID.label")));
        optionalContainer.add(new TextField<String>("issuerOfAdmissionID", issuerOfAdmissionIDModel));

        optionalContainer.add(new Label("issuerOfContainerIdentifier.label", new ResourceModel(
                "dicom.edit.device.optional.issuerOfContainerIdentifier.label")));
        optionalContainer.add(new TextField<String>("issuerOfContainerIdentifier", issuerOfContainerIdentifierModel));

        optionalContainer.add(new Label("issuerOfPatientID.label", new ResourceModel(
                "dicom.edit.device.optional.issuerOfPatientID.label")));
        optionalContainer.add(new TextField<String>("issuerOfPatientID", issuerOfPatientIDModel));

        optionalContainer.add(new Label("issuerOfServiceEpisodeID.label", new ResourceModel(
                "dicom.edit.device.optional.issuerOfServiceEpisodeID.label")));
        optionalContainer.add(new TextField<String>("issuerOfServiceEpisodeID", issuerOfServiceEpisodeIDModel));

        optionalContainer.add(new Label("issuerOfSpecimenIdentifier.label", new ResourceModel(
                "dicom.edit.device.optional.issuerOfSpecimenIdentifier.label")));
        optionalContainer.add(new TextField<String>("issuerOfSpecimenIdentifier", issuerOfSpecimenIdentifierModel));

        optionalContainer.add(new Label("manufacturer.label", new ResourceModel(
                "dicom.edit.device.optional.manufacturer.label")));
        optionalContainer.add(new TextField<String>("manufacturer", manufacturerModel));

        optionalContainer.add(new Label("manufacturerModelName.label", new ResourceModel(
                "dicom.edit.device.optional.manufacturerModelName.label")));
        optionalContainer.add(new TextField<String>("manufacturerModelName", manufacturerModelNameModel));

        optionalContainer.add(new Label("orderFillerIdentifier.label", new ResourceModel(
                "dicom.edit.device.optional.orderFillerIdentifier.label")));
        optionalContainer.add(new TextField<String>("orderFillerIdentifier", orderFillerIdentifierModel));

        optionalContainer.add(new Label("orderPlacerIdentifier.label", new ResourceModel(
                "dicom.edit.device.optional.orderPlacerIdentifier.label")));
        optionalContainer.add(new TextField<String>("orderPlacerIdentifier", orderPlacerIdentifierModel));

        optionalContainer.add(new Label("primaryDeviceTypes.label", new ResourceModel(
                "dicom.edit.device.optional.primaryDeviceTypes.label")));
        optionalContainer.add(new TextArea<String>("primaryDeviceTypes", primaryDeviceTypesModel));

        optionalContainer.add(new Label("softwareVersions.label", new ResourceModel(
                "dicom.edit.device.optional.softwareVersions.label")));
        optionalContainer.add(new TextArea<String>("softwareVersions", softwareVersionsModel));

        optionalContainer.add(new Label("stationName.label", new ResourceModel(
                "dicom.edit.device.optional.stationName.label")));
        optionalContainer.add(new TextField<String>("stationName", stationNameModel));

        optionalContainer.add(new Label("trustStoreURL.label", new ResourceModel(
                "dicom.edit.device.optional.trustStoreURL.label")));
        optionalContainer.add(new TextField<String>("trustStoreURL", trustStoreURLModel).add(new UrlValidator()));

        optionalContainer.add(new Label("trustStoreType.label", new ResourceModel(
                "dicom.edit.device.optional.trustStoreType.label")));
        optionalContainer.add(new DropDownChoice<String>("trustStoreType", trustStoreTypeModel, keyStoreTypes)
                .setNullValid(true));

        optionalContainer.add(new Label("trustStorePin.label", new ResourceModel(
                "dicom.edit.device.optional.trustStorePin.label")));
        optionalContainer.add(new TextField<String>("trustStorePin", trustStorePinModel));

        optionalContainer.add(new CheckBox("useTrustStorePinProperty", useTrustStorePinProperty));

        optionalContainer.add(new Label("keyStoreURL.label", new ResourceModel(
                "dicom.edit.device.optional.keyStoreURL.label")));
        optionalContainer.add(new TextField<String>("keyStoreURL", keyStoreURLModel).add(new UrlValidator()));

        optionalContainer.add(new Label("keyStoreType.label", new ResourceModel(
                "dicom.edit.device.optional.keyStoreType.label")));
        optionalContainer.add(new DropDownChoice<String>("keyStoreType", keyStoreTypeModel, keyStoreTypes)
                .setNullValid(true));

        optionalContainer.add(new Label("keyStorePin.label", new ResourceModel(
                "dicom.edit.device.optional.keyStorePin.label")));
        optionalContainer.add(new TextField<String>("keyStorePin", keyStorePinModel));

        optionalContainer.add(new CheckBox("useKeyStorePinProperty", useKeyStorePinProperty));

        optionalContainer.add(new Label("keyStoreKeyPin.label", new ResourceModel(
                "dicom.edit.device.optional.keyStoreKeyPin.label")));
        optionalContainer.add(new TextField<String>("keyStoreKeyPin", keyStoreKeyPinModel));

        optionalContainer.add(new CheckBox("useKeyStoreKeyPinProperty", useKeyStoreKeyPinProperty));
    }

    private void addTextArea(Form<?> form, String resourceModelString, String labelString,
            String textAreaString, StringArrayModel textAreaModel) {
        form.add(new Label(labelString, new ResourceModel(resourceModelString)));
        form.add(new TextArea<String>(textAreaString, textAreaModel));
    }

    private TextField<String> addTextField(Form<?> form, String resourceModelString, String labelString,
            String textFieldId, IModel<String> textFieldModel) {
        form.add(new Label(labelString, new ResourceModel(resourceModelString)));
        TextField<String> textField = new TextField<String>(textFieldId, textFieldModel);
        form.add(textField);
        return textField;
    }

    private WebMarkupContainer proxyWebMarkupContainer(final DeviceModel deviceModel) {
        WebMarkupContainer proxyWMC = new WebMarkupContainer("proxy") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return typeModel.getObject().equals(ConfigurationType.Proxy);
            }
        };
        Label schedulerIntervalLabel = new Label("schedulerInterval.label", new ResourceModel(
                "dicom.edit.device.proxy.schedulerInterval.label"));
        proxyWMC.add(schedulerIntervalLabel);
        FormComponent<Integer> schedulerIntervalTextField = new TextField<Integer>("schedulerInterval",
                schedulerIntervalModel);
        schedulerIntervalTextField.setType(Integer.class);
        schedulerIntervalTextField.setRequired(true);
        proxyWMC.add(schedulerIntervalTextField);
        proxyWMC.setOutputMarkupPlaceholderTag(true);
        return proxyWMC;
    }

    private WebMarkupContainer xdsWebMarkupContainer(final DeviceModel deviceModel) {
        WebMarkupContainer xdsWMC = new WebMarkupContainer("xds") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return typeModel.getObject().equals(ConfigurationType.XDS);
            }
        };
        xdsWMC.setOutputMarkupPlaceholderTag(true);
        addXCAiInitiatingGWAttributes(xdsWMC);
        return xdsWMC;
    }

    private void addXCAiInitiatingGWAttributes(WebMarkupContainer xdsWMC) {
        Label applicationNameLabel = new Label("applicationName.label", new ResourceModel(
                "dicom.edit.device.xds.applicationName.label"));
        xdsWMC.add(applicationNameLabel);
        FormComponent<String> applicationNameTextField = new TextField<String>("applicationName",
                xdsApplicationNameModel);
        applicationNameTextField.setType(String.class);
        applicationNameTextField.setRequired(true);
        xdsWMC.add(applicationNameTextField);

        Label homeCommunityIdLabel = new Label("homeCommunityId.label", new ResourceModel(
                "dicom.edit.device.xds.homeCommunityId.label"));
        xdsWMC.add(homeCommunityIdLabel);
        FormComponent<String> homeCommunityIdTextField = new TextField<String>("homeCommunityId",
                xdsHomeCommunityIdModel);
        homeCommunityIdTextField.setType(String.class);
        homeCommunityIdTextField.setRequired(true);
        xdsWMC.add(homeCommunityIdTextField);

        Label respondingGatewayUrlLabel = new Label("respondingGatewayUrl.label", new ResourceModel(
                "dicom.edit.device.xds.respondingGatewayUrl.label"));
        xdsWMC.add(respondingGatewayUrlLabel);
        FormComponent<String> respondingGatewayUrlTextArea = new TextArea<String>("respondingGatewayUrl",
                xdsRespondingGatewayUrlModel);
        respondingGatewayUrlTextArea.setType(String.class);
        respondingGatewayUrlTextArea.setRequired(true);
        xdsWMC.add(respondingGatewayUrlTextArea);
    }

    private void addInstalledLabel(final ExtendedForm form) {
        form.add(new Label("installed.label", new ResourceModel("dicom.edit.device.installed.label"))).add(
                new CheckBox("installed", installedModel));
    }

    private void addTypeLabel(final ExtendedForm form) {
        form.add(new Label("type.label", new ResourceModel("dicom.edit.device.type.label")));
    }

    private void addOnChangeUpdate(final DeviceModel deviceModel, final ExtendedForm form,
            DropDownChoice<ConfigTreeProvider.ConfigurationType> typeDropDown) {
        form.add(typeDropDown.setNullValid(false).setEnabled(deviceModel == null)
                .add(new AjaxFormComponentUpdatingBehavior("onchange") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        target.add(form.get("proxy"));
                        target.add(form.get("xds"));
                    }
                }));
    }

    private void addDeviceTitle(final DeviceModel deviceModel, final ExtendedForm form) {
        try {
            form.add(new Label("title.label", new ResourceModel("dicom.edit.device.title.label"))).add(
                    new TextField<String>("title", deviceNameModel)
                            .add(new DeviceNameValidator(getDicomConfigurationManager().listDevices(), deviceNameModel
                                    .getObject())).setRequired(true)
                            .add(FocusOnLoadBehavior.newFocusAndSelectBehaviour()).setEnabled(deviceModel == null));
        } catch (ConfigurationException ce) {
            log.error(this.getClass().toString() + ": " + "Error listing devices: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new ModalWindowRuntimeException(ce.getLocalizedMessage());
        }
    }

    private void initDeviceModel(final DeviceModel deviceModel) {
        try {
            Iterator<DeviceExtension> iter = deviceModel.getDevice().listDeviceExtensions().iterator();
            if (!iter.hasNext())
                typeModel = Model.of(ConfigTreeProvider.ConfigurationType.Basic);
            else {
                DeviceExtension ext = iter.next();
                if (ext instanceof ProxyDeviceExtension)
                    typeModel = Model.of(ConfigTreeProvider.ConfigurationType.Proxy);
                else if (ext instanceof XCAiInitiatingGWCfg 
                        || ext instanceof XCAInitiatingGWCfg
                        || ext instanceof XCAiRespondingGWCfg 
                        || ext instanceof XCARespondingGWCfg
                        || ext instanceof XdsRegistry 
                        || ext instanceof XdsRepository)
                    typeModel = Model.of(ConfigTreeProvider.ConfigurationType.XDS);
                else
                    typeModel = Model.of(ConfigTreeProvider.ConfigurationType.Basic);
            }

            deviceNameModel = Model.of(deviceModel.getDevice().getDeviceName());
            installedModel = Model.of(deviceModel.getDevice().isInstalled());

            descriptionModel = Model.of(deviceModel.getDevice().getDescription());
            deviceSerialNumberModel = Model.of(deviceModel.getDevice().getDeviceSerialNumber());
            institutionAddressModel = new StringArrayModel(deviceModel.getDevice().getInstitutionAddresses());

            Code code = null;
            if (deviceModel.getDevice().getInstitutionCodes().length > 0)
                code = deviceModel.getDevice().getInstitutionCodes()[0];
            institutionCodeModel = new InstitutionCodeModel(code);

            institutionalDepartmentNameModel = new StringArrayModel(deviceModel.getDevice()
                    .getInstitutionalDepartmentNames());
            institutionNameModel = new StringArrayModel(deviceModel.getDevice().getInstitutionNames());
            if (deviceModel.getDevice().getIssuerOfAccessionNumber() == null)
                issuerOfAccessionNumberModel = Model.of();
            else
                issuerOfAccessionNumberModel = Model
                        .of(deviceModel.getDevice().getIssuerOfAccessionNumber().toString());
            if (deviceModel.getDevice().getIssuerOfAdmissionID() == null)
                issuerOfAdmissionIDModel = Model.of();
            else
                issuerOfAdmissionIDModel = Model.of(deviceModel.getDevice().getIssuerOfAdmissionID().toString());
            if (deviceModel.getDevice().getIssuerOfContainerIdentifier() == null)
                issuerOfContainerIdentifierModel = Model.of();
            else
                issuerOfContainerIdentifierModel = Model.of(deviceModel.getDevice().getIssuerOfContainerIdentifier()
                        .toString());
            if (deviceModel.getDevice().getIssuerOfPatientID() == null)
                issuerOfPatientIDModel = Model.of();
            else
                issuerOfPatientIDModel = Model.of(deviceModel.getDevice().getIssuerOfPatientID().toString());
            if (deviceModel.getDevice().getIssuerOfServiceEpisodeID() == null)
                issuerOfServiceEpisodeIDModel = Model.of();
            else
                issuerOfServiceEpisodeIDModel = Model.of(deviceModel.getDevice().getIssuerOfServiceEpisodeID()
                        .toString());
            if (deviceModel.getDevice().getIssuerOfSpecimenIdentifier() == null)
                issuerOfSpecimenIdentifierModel = Model.of();
            else
                issuerOfSpecimenIdentifierModel = Model.of(deviceModel.getDevice().getIssuerOfSpecimenIdentifier()
                        .toString());
            manufacturerModel = Model.of(deviceModel.getDevice().getManufacturer());
            manufacturerModelNameModel = Model.of(deviceModel.getDevice().getManufacturerModelName());
            if (deviceModel.getDevice().getOrderFillerIdentifier() == null)
                orderFillerIdentifierModel = Model.of();
            else
                orderFillerIdentifierModel = Model.of(deviceModel.getDevice().getOrderFillerIdentifier().toString());
            if (deviceModel.getDevice().getOrderPlacerIdentifier() == null)
                orderPlacerIdentifierModel = Model.of();
            else
                orderPlacerIdentifierModel = Model.of(deviceModel.getDevice().getOrderPlacerIdentifier().toString());
            setDeviceConfiguration(deviceModel);
            setProxyDevExtConfiguration(deviceModel);
            setXdsDevExtConfiguration(deviceModel);
        } catch (ConfigurationException ce) {
            log.error(this.getClass().toString() + ": " + "Error retrieving device data: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new ModalWindowRuntimeException(ce.getLocalizedMessage());
        }
    }

    private void setXdsDevExtConfiguration(DeviceModel deviceModel) throws ConfigurationException {
        if (typeModel == Model.of(ConfigTreeProvider.ConfigurationType.XDS)) {
            //TODO: load xds extensions and set values
            setXCAiInitGWAttributes(deviceModel);
        }
    }

    private void setXCAiInitGWAttributes(DeviceModel deviceModel) throws ConfigurationException {
        // TODO Auto-generated method stub
        XCAiInitiatingGWCfg ext = deviceModel.getDevice().getDeviceExtension(XCAiInitiatingGWCfg.class);
        xdsApplicationNameModel = Model.of(ext.getApplicationName());
        xdsHomeCommunityIdModel = Model.of(ext.getHomeCommunityID());
        xdsRespondingGatewayUrlModel = new StringArrayModel(ext.getRespondingGWURLs());
    }

    private void setDeviceConfiguration(final DeviceModel deviceModel) throws ConfigurationException {
        primaryDeviceTypesModel = new StringArrayModel(deviceModel.getDevice().getPrimaryDeviceTypes());
        relatedDeviceRefsModel = new StringArrayModel(deviceModel.getDevice().getRelatedDeviceRefs());
        softwareVersionsModel = new StringArrayModel(deviceModel.getDevice().getSoftwareVersions());
        stationNameModel = Model.of(deviceModel.getDevice().getStationName());
        vendorDataModel = Model.of("size " + deviceModel.getDevice().getVendorData().length);
        trustStoreURLModel = Model.of(deviceModel.getDevice().getTrustStoreURL());
        trustStoreTypeModel = Model.of(deviceModel.getDevice().getTrustStoreType());
        trustStorePinModel = Model.of(deviceModel.getDevice().getTrustStorePinProperty() != null ? deviceModel
                .getDevice().getTrustStorePinProperty() : deviceModel.getDevice().getTrustStorePin());
        useTrustStorePinProperty = Model.of(deviceModel.getDevice().getTrustStorePinProperty() != null);
        keyStoreURLModel = Model.of(deviceModel.getDevice().getKeyStoreURL());
        keyStoreTypeModel = Model.of(deviceModel.getDevice().getKeyStoreType());
        keyStorePinModel = Model.of(deviceModel.getDevice().getKeyStorePinProperty() != null ? deviceModel
                .getDevice().getKeyStorePinProperty() : deviceModel.getDevice().getKeyStorePin());
        useKeyStorePinProperty = Model.of(deviceModel.getDevice().getKeyStorePinProperty() != null);
        keyStoreKeyPinModel = Model
                .of(deviceModel.getDevice().getKeyStoreKeyPinProperty() != null ? deviceModel.getDevice()
                        .getKeyStoreKeyPinProperty() : deviceModel.getDevice().getKeyStoreKeyPin());
        useKeyStoreKeyPinProperty = Model.of(deviceModel.getDevice().getKeyStoreKeyPinProperty() != null);
    }

    private void setProxyDevExtConfiguration(final DeviceModel deviceModel) throws ConfigurationException {
        if (typeModel == Model.of(ConfigTreeProvider.ConfigurationType.Proxy)) {
            ProxyDeviceExtension proxyDevExt = deviceModel.getDevice().getDeviceExtension(
                    ProxyDeviceExtension.class);
            schedulerIntervalModel = Model.of(proxyDevExt.getSchedulerInterval());
            forwardThreadsModel = Model.of(proxyDevExt.getForwardThreads());
            staleTimeoutModel = Model.of(proxyDevExt.getConfigurationStaleTimeout());
        }
    }

    private void initBasicConfigurationTypeModel() {
        initBasicDeviceConfigurationTypeModel();
        initProxyConfigurationTypeModel();
        initXdsConfigurationTypeModel();
    }

    private void initXdsConfigurationTypeModel() {
        // TODO Auto-generated method stub
        initXCAiInitGWConfigurationTypeModel();
    }

    private void initXCAiInitGWConfigurationTypeModel() {
        // TODO Auto-generated method stub
        xdsApplicationNameModel = Model.of();
        xdsHomeCommunityIdModel = Model.of();
        xdsRespondingGatewayUrlModel = new StringArrayModel(null);
    }

    private void initBasicDeviceConfigurationTypeModel() {
        typeModel = Model.of(ConfigTreeProvider.ConfigurationType.Basic);
        deviceNameModel = Model.of();
        installedModel = Model.of(true);
        descriptionModel = Model.of();
        deviceSerialNumberModel = Model.of();
        institutionAddressModel = new StringArrayModel(null);
        institutionCodeModel = new InstitutionCodeModel(null);
        institutionalDepartmentNameModel = new StringArrayModel(null);
        institutionNameModel = new StringArrayModel(null);
        issuerOfAccessionNumberModel = Model.of();
        issuerOfAdmissionIDModel = Model.of();
        issuerOfContainerIdentifierModel = Model.of();
        issuerOfPatientIDModel = Model.of();
        issuerOfServiceEpisodeIDModel = Model.of();
        issuerOfSpecimenIdentifierModel = Model.of();
        manufacturerModel = Model.of();
        manufacturerModelNameModel = Model.of();
        orderFillerIdentifierModel = Model.of();
        orderPlacerIdentifierModel = Model.of();
        primaryDeviceTypesModel = new StringArrayModel(null);
        relatedDeviceRefsModel = new StringArrayModel(null);
        softwareVersionsModel = new StringArrayModel(null);
        stationNameModel = Model.of();
        vendorDataModel = Model.of("size 0");
        trustStoreURLModel = Model.of();
        trustStoreTypeModel = Model.of();
        trustStorePinModel = Model.of();
        useTrustStorePinProperty = Model.of(false);
        keyStoreURLModel = Model.of();
        keyStoreTypeModel = Model.of();
        keyStorePinModel = Model.of();
        useKeyStorePinProperty = Model.of(false);
        keyStoreKeyPinModel = Model.of();
        useKeyStoreKeyPinProperty = Model.of(false);
    }

    private void initProxyConfigurationTypeModel() {
        schedulerIntervalModel = Model.of(ProxyDeviceExtension.DEFAULT_SCHEDULER_INTERVAL);
        forwardThreadsModel = Model.of(ProxyDeviceExtension.DEFAULT_FORWARD_THREADS);
        staleTimeoutModel = Model.of(60);
    }

    private void addCancelButton(final ModalWindow window, final ExtendedForm form) {
        form.add(new AjaxButton("cancel", new ResourceModel("cancelBtn"), form) {

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

    private void addSaveButton(final ModalWindow window, final DeviceModel deviceModel, final ExtendedForm form,
            final Form<?> optionalContainer) {
        form.add(new IndicatingAjaxButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    Device device = null;
                    if (deviceModel != null) {
                        device = deviceModel.getDevice();
                    } else {
                        device = initDeviceExtensions();
                    }
                    device.setInstalled(installedModel.getObject());
                    if (optionalContainer.isVisible()) {
                        setDeviceAttributes(device);
                        setProxyDeviceAttributes(device);
                        setXdsDeviceAttributes(device);
                    }
                    if (deviceModel == null)
                        ConfigTreeProvider.get().persistDevice(device);
                    else
                        ConfigTreeProvider.get().mergeDevice(device);
                    window.close(target);
                } catch (Exception e) {
                    log.error(this.getClass().toString() + ": " + "Error modifying device: " + e.getMessage());
                    log.debug("Exception", e);
                    throw new ModalWindowRuntimeException(e.getLocalizedMessage());
                }
            }

            private Device initDeviceExtensions() {
                Device device = new Device(deviceNameModel.getObject());
                if (typeModel.getObject().equals(ConfigurationType.Proxy))
                    initProxyDeviceExtension(device);
                if (typeModel.getObject().equals(ConfigurationType.AuditRecordRepository))
                    initARRExtension(device);
                if (typeModel.getObject().equals(ConfigurationType.XDS))
                    initXdsExtensions(device);
                return device;
            }

            private void initXdsExtensions(Device device) {
                //TODO: init attributes per extension
                initXCAiInitGWAttributes(device);
                device.addDeviceExtension(new XCAInitiatingGWCfg());
                device.addDeviceExtension(new XCAiRespondingGWCfg());
                device.addDeviceExtension(new XCARespondingGWCfg());
                device.addDeviceExtension(new XdsRegistry());
                device.addDeviceExtension(new XdsRepository());
                device.addDeviceExtension(new HL7DeviceExtension());
            }

            private void initXCAiInitGWAttributes(Device device) {
                // TODO Auto-generated method stub
                XCAiInitiatingGWCfg ext = new XCAiInitiatingGWCfg();
                ext.setApplicationName(xdsApplicationNameModel.getObject());
                ext.setHomeCommunityID(xdsHomeCommunityIdModel.getObject());
                ext.setRespondingGWURLs(xdsRespondingGatewayUrlModel.getArray());
                device.addDeviceExtension(ext);
            }

            private void initARRExtension(Device device) {
                AuditRecordRepository auditRecordRepository = new AuditRecordRepository();
                device.addDeviceExtension(auditRecordRepository);
            }

            private void initProxyDeviceExtension(Device device) {
                ProxyDeviceExtension proxyDeviceExtension = new ProxyDeviceExtension();
                proxyDeviceExtension.setSchedulerInterval(schedulerIntervalModel.getObject());
                device.addDeviceExtension(proxyDeviceExtension);
                device.addDeviceExtension(new HL7DeviceExtension());
            }

            private void setProxyDeviceAttributes(Device device) {
                ProxyDeviceExtension proxyDevExt = device.getDeviceExtension(ProxyDeviceExtension.class);
                if (proxyDevExt != null) {
                    proxyDevExt.setSchedulerInterval(schedulerIntervalModel.getObject());
                    proxyDevExt.setConfigurationStaleTimeout(
                            staleTimeoutModel.getObject() != null 
                                ? staleTimeoutModel.getObject() 
                                : 60);
                    if (forwardThreadsModel.getObject() != null)
                        proxyDevExt.setForwardThreads(forwardThreadsModel.getObject());
                }
            }

            private void setXdsDeviceAttributes(Device device) {
                //TODO: set attributes per extension
                setXCAiInitGWAttributes(device);
            }

            private void setXCAiInitGWAttributes(Device device) {
                XCAiInitiatingGWCfg ext = device.getDeviceExtension(XCAiInitiatingGWCfg.class);
                if (ext != null) {
                    //TODO: set attributes
                    ext.setApplicationName(xdsApplicationNameModel.getObject());
                    ext.setHomeCommunityID(xdsHomeCommunityIdModel.getObject());
                    ext.setRespondingGWURLs(xdsRespondingGatewayUrlModel.getArray());
                }
            }

            private void setDeviceAttributes(Device device) {
                device.setDescription(descriptionModel.getObject());
                device.setDeviceSerialNumber(deviceSerialNumberModel.getObject());
                device.setInstitutionAddresses(institutionAddressModel.getArray());
                device.setInstitutionCodes(institutionCodeModel.getCode() == null ? new Code[] {}
                        : new Code[] { institutionCodeModel.getCode() });
                device.setInstitutionalDepartmentNames(institutionalDepartmentNameModel.getArray());
                device.setInstitutionNames(institutionNameModel.getArray());
                device.setIssuerOfAccessionNumber(issuerOfAccessionNumberModel.getObject() == null ? null
                        : new Issuer(issuerOfAccessionNumberModel.getObject()));
                device.setIssuerOfAdmissionID(issuerOfAdmissionIDModel.getObject() == null ? null : new Issuer(
                        issuerOfAdmissionIDModel.getObject()));
                device.setIssuerOfContainerIdentifier(issuerOfContainerIdentifierModel.getObject() == null ? null
                        : new Issuer(issuerOfContainerIdentifierModel.getObject()));
                device.setIssuerOfPatientID(issuerOfPatientIDModel.getObject() == null ? null : new Issuer(
                        issuerOfPatientIDModel.getObject()));
                device.setIssuerOfServiceEpisodeID(issuerOfServiceEpisodeIDModel.getObject() == null ? null
                        : new Issuer(issuerOfServiceEpisodeIDModel.getObject()));
                device.setIssuerOfSpecimenIdentifier(issuerOfSpecimenIdentifierModel.getObject() == null ? null
                        : new Issuer(issuerOfSpecimenIdentifierModel.getObject()));
                device.setManufacturer(manufacturerModel.getObject());
                device.setManufacturerModelName(manufacturerModelNameModel.getObject());
                device.setOrderFillerIdentifier(orderFillerIdentifierModel.getObject() == null ? null
                        : new Issuer(orderFillerIdentifierModel.getObject()));
                device.setOrderPlacerIdentifier(orderPlacerIdentifierModel.getObject() == null ? null
                        : new Issuer(orderPlacerIdentifierModel.getObject()));
                device.setPrimaryDeviceTypes(primaryDeviceTypesModel.getArray());
                device.setRelatedDeviceRefs(relatedDeviceRefsModel.getArray());
                device.setSoftwareVersions(softwareVersionsModel.getArray());
                device.setStationName(stationNameModel.getObject());
                device.setTrustStoreURL(trustStoreURLModel.getObject());
                device.setTrustStoreType(trustStoreTypeModel.getObject());
                device.setTrustStorePinProperty(useTrustStorePinProperty.getObject() ? trustStorePinModel
                        .getObject() : null);
                device.setTrustStorePin(!useTrustStorePinProperty.getObject() ? trustStorePinModel.getObject()
                        : null);
                device.setKeyStoreURL(keyStoreURLModel.getObject());
                device.setKeyStoreType(keyStoreTypeModel.getObject());
                device.setKeyStorePinProperty(useKeyStorePinProperty.getObject() ? keyStorePinModel.getObject()
                        : null);
                device.setKeyStorePin(!useKeyStorePinProperty.getObject() ? keyStorePinModel.getObject() : null);
                device.setKeyStoreKeyPinProperty(useKeyStoreKeyPinProperty.getObject() ? keyStoreKeyPinModel
                        .getObject() : null);
                device.setKeyStoreKeyPin(!useKeyStoreKeyPinProperty.getObject() ? keyStoreKeyPinModel
                        .getObject() : null);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                if (target != null)
                    target.add(form);
            }
        });
    }

    private DropDownChoice<ConfigTreeProvider.ConfigurationType> setConfigurationTypeList() {
        ArrayList<ConfigTreeProvider.ConfigurationType> configurationTypeList = new ArrayList<ConfigTreeProvider.ConfigurationType>();
        configurationTypeList.add(ConfigTreeProvider.ConfigurationType.Basic);
        configurationTypeList.add(ConfigTreeProvider.ConfigurationType.Proxy);
        configurationTypeList.add(ConfigTreeProvider.ConfigurationType.XDS);
        configurationTypeList.add(ConfigTreeProvider.ConfigurationType.AuditRecordRepository);
        // configurationTypeList.add(ConfigTreeProvider.ConfigurationType.Archive);
        DropDownChoice<ConfigTreeProvider.ConfigurationType> typeDropDown = new DropDownChoice<ConfigTreeProvider.ConfigurationType>(
                "type", typeModel, configurationTypeList);
        return typeDropDown;
    }
}
