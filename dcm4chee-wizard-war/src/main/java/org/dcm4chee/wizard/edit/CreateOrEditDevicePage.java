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
import java.util.Collection;
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

    // ProxyDevice only
    private Model<Integer> schedulerIntervalModel;
    private Model<Integer> forwardThreadsModel;
    private Model<Integer> staleTimeoutModel;

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
        final Form<?> optionalContainer = new Form<Object>("optional");
        form.add(optionalContainer.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(false));

        optionalContainer.add(
                new Label("description.label", new ResourceModel("dicom.edit.device.optional.description.label"))).add(
                new TextField<String>("description", descriptionModel));

        form.add(new Label("toggleOptional.label", new ResourceModel("dicom.edit.toggleOptional.label"))).add(
                new AjaxCheckBox("toggleOptional", new Model<Boolean>()) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        target.add(optionalContainer.setVisible(this.getModelObject()));
                    }
                });

        optionalContainer.add(
                new Label("deviceSerialNumber.label", new ResourceModel(
                        "dicom.edit.device.optional.deviceSerialNumber.label"))).add(
                new TextField<String>("deviceSerialNumber", deviceSerialNumberModel));

        optionalContainer.add(
                new Label("institutionAddress.label", new ResourceModel(
                        "dicom.edit.device.optional.institutionAddress.label"))).add(
                new TextArea<String>("institutionAddress", institutionAddressModel));

        TextField<String> codeValueTextField = new TextField<String>("institutionCodeValue",
                institutionCodeModel.getCodeFieldModel(0));
        optionalContainer.add(
                new Label("institutionCodeValue.label", new ResourceModel(
                        "dicom.edit.device.optional.institutionCodeValue.label"))).add(codeValueTextField);

        TextField<String> codingSchemeDesignatorTextField = new TextField<String>("institutionCodingSchemeDesignator",
                institutionCodeModel.getCodeFieldModel(1));
        optionalContainer.add(
                new Label("institutionCodingSchemeDesignator.label", new ResourceModel(
                        "dicom.edit.device.optional.institutionCodingSchemeDesignator.label"))).add(
                codingSchemeDesignatorTextField);

        TextField<String> codingSchemeVersionTextField = new TextField<String>("institutionCodingSchemeVersion",
                institutionCodeModel.getCodeFieldModel(2));
        optionalContainer.add(
                new Label("institutionCodingSchemeVersion.label", new ResourceModel(
                        "dicom.edit.device.optional.institutionCodingSchemeVersion.label"))).add(
                codingSchemeVersionTextField);

        TextField<String> codeMeaningTextField = new TextField<String>("institutionCodeMeaning",
                institutionCodeModel.getCodeFieldModel(3));
        optionalContainer.add(
                new Label("institutionCodeMeaning.label", new ResourceModel(
                        "dicom.edit.device.optional.institutionCodeMeaning.label"))).add(codeMeaningTextField);

        optionalContainer.add(new CodeValidator(codeValueTextField, codingSchemeDesignatorTextField,
                codingSchemeVersionTextField, codeMeaningTextField));

        optionalContainer.add(
                new Label("institutionalDepartmentName.label", new ResourceModel(
                        "dicom.edit.device.optional.institutionalDepartmentName.label"))).add(
                new TextArea<String>("institutionalDepartmentName", institutionalDepartmentNameModel));

        optionalContainer.add(
                new Label("institutionName.label",
                        new ResourceModel("dicom.edit.device.optional.institutionName.label"))).add(
                new TextArea<String>("institutionName", institutionNameModel));

        optionalContainer.add(
                new Label("issuerOfAccessionNumber.label", new ResourceModel(
                        "dicom.edit.device.optional.issuerOfAccessionNumber.label"))).add(
                new TextField<String>("issuerOfAccessionNumber", issuerOfAccessionNumberModel));

        optionalContainer.add(
                new Label("issuerOfAdmissionID.label", new ResourceModel(
                        "dicom.edit.device.optional.issuerOfAdmissionID.label"))).add(
                new TextField<String>("issuerOfAdmissionID", issuerOfAdmissionIDModel));

        optionalContainer.add(
                new Label("issuerOfContainerIdentifier.label", new ResourceModel(
                        "dicom.edit.device.optional.issuerOfContainerIdentifier.label"))).add(
                new TextField<String>("issuerOfContainerIdentifier", issuerOfContainerIdentifierModel));

        optionalContainer.add(
                new Label("issuerOfPatientID.label", new ResourceModel(
                        "dicom.edit.device.optional.issuerOfPatientID.label"))).add(
                new TextField<String>("issuerOfPatientID", issuerOfPatientIDModel));

        optionalContainer.add(
                new Label("issuerOfServiceEpisodeID.label", new ResourceModel(
                        "dicom.edit.device.optional.issuerOfServiceEpisodeID.label"))).add(
                new TextField<String>("issuerOfServiceEpisodeID", issuerOfServiceEpisodeIDModel));

        optionalContainer.add(
                new Label("issuerOfSpecimenIdentifier.label", new ResourceModel(
                        "dicom.edit.device.optional.issuerOfSpecimenIdentifier.label"))).add(
                new TextField<String>("issuerOfSpecimenIdentifier", issuerOfSpecimenIdentifierModel));

        optionalContainer.add(
                new Label("manufacturer.label", new ResourceModel("dicom.edit.device.optional.manufacturer.label")))
                .add(new TextField<String>("manufacturer", manufacturerModel));

        optionalContainer.add(
                new Label("manufacturerModelName.label", new ResourceModel(
                        "dicom.edit.device.optional.manufacturerModelName.label"))).add(
                new TextField<String>("manufacturerModelName", manufacturerModelNameModel));

        optionalContainer.add(
                new Label("orderFillerIdentifier.label", new ResourceModel(
                        "dicom.edit.device.optional.orderFillerIdentifier.label"))).add(
                new TextField<String>("orderFillerIdentifier", orderFillerIdentifierModel));

        optionalContainer.add(
                new Label("orderPlacerIdentifier.label", new ResourceModel(
                        "dicom.edit.device.optional.orderPlacerIdentifier.label"))).add(
                new TextField<String>("orderPlacerIdentifier", orderPlacerIdentifierModel));

        optionalContainer.add(
                new Label("primaryDeviceTypes.label", new ResourceModel(
                        "dicom.edit.device.optional.primaryDeviceTypes.label"))).add(
                new TextArea<String>("primaryDeviceTypes", primaryDeviceTypesModel));

        optionalContainer.add(
                new Label("softwareVersions.label", new ResourceModel(
                        "dicom.edit.device.optional.softwareVersions.label"))).add(
                new TextArea<String>("softwareVersions", softwareVersionsModel));

        optionalContainer.add(
                new Label("stationName.label", new ResourceModel("dicom.edit.device.optional.stationName.label"))).add(
                new TextField<String>("stationName", stationNameModel));

        optionalContainer.add(
                new Label("trustStoreURL.label", new ResourceModel("dicom.edit.device.optional.trustStoreURL.label")))
                .add(new TextField<String>("trustStoreURL", trustStoreURLModel).add(new UrlValidator()));

        optionalContainer
                .add(new Label("trustStoreType.label", new ResourceModel(
                        "dicom.edit.device.optional.trustStoreType.label"))).add(
                        new DropDownChoice<String>("trustStoreType", trustStoreTypeModel, keyStoreTypes)
                                .setNullValid(true));

        optionalContainer.add(
                new Label("trustStorePin.label", new ResourceModel("dicom.edit.device.optional.trustStorePin.label")))
                .add(new TextField<String>("trustStorePin", trustStorePinModel));

        optionalContainer.add(new CheckBox("useTrustStorePinProperty", useTrustStorePinProperty));

        optionalContainer.add(
                new Label("keyStoreURL.label", new ResourceModel("dicom.edit.device.optional.keyStoreURL.label"))).add(
                new TextField<String>("keyStoreURL", keyStoreURLModel).add(new UrlValidator()));

        optionalContainer.add(
                new Label("keyStoreType.label", new ResourceModel("dicom.edit.device.optional.keyStoreType.label")))
                .add(new DropDownChoice<String>("keyStoreType", keyStoreTypeModel, keyStoreTypes).setNullValid(true));

        optionalContainer.add(
                new Label("keyStorePin.label", new ResourceModel("dicom.edit.device.optional.keyStorePin.label"))).add(
                new TextField<String>("keyStorePin", keyStorePinModel));

        optionalContainer.add(new CheckBox("useKeyStorePinProperty", useKeyStorePinProperty));

        optionalContainer
                .add(new Label("keyStoreKeyPin.label", new ResourceModel(
                        "dicom.edit.device.optional.keyStoreKeyPin.label"))).add(
                        new TextField<String>("keyStoreKeyPin", keyStoreKeyPinModel));

        optionalContainer.add(new CheckBox("useKeyStoreKeyPinProperty", useKeyStoreKeyPinProperty));

        WebMarkupContainer optionalProxyContainer = new WebMarkupContainer("optionalProxyContainer") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return optionalContainer.isVisible() && typeModel.getObject().equals(ConfigurationType.Proxy);
            }
        };
        optionalContainer.add(optionalProxyContainer.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));

        optionalProxyContainer.add(
                new Label("forwardThreads.label", new ResourceModel("dicom.edit.device.optional.forwardThreads.label"))
                        .setOutputMarkupPlaceholderTag(true)).add(
                new TextField<Integer>("forwardThreads", forwardThreadsModel).setType(Integer.class).add(
                        new RangeValidator<Integer>(1, 256)));

        optionalProxyContainer.add(
                new Label("staleTimeout.label", new ResourceModel("dicom.edit.device.optional.staleTimeout.label"))
                        .setOutputMarkupPlaceholderTag(true)).add(
                new TextField<Integer>("staleTimeout", staleTimeoutModel).setType(Integer.class).add(
                        new RangeValidator<Integer>(0, Integer.MAX_VALUE)));

        WebMarkupContainer relatedDeviceRefsContainer = new WebMarkupContainer("relatedDeviceRefsContainer");
        optionalContainer.add(relatedDeviceRefsContainer);
        relatedDeviceRefsContainer.add(
                new Label("relatedDeviceRefs.label", new ResourceModel("dicom.edit.device.relatedDeviceRefs.label")))
                .add(new TextArea<String>("relatedDeviceRefs", relatedDeviceRefsModel).setEnabled(false));
        relatedDeviceRefsContainer.setVisible(relatedDeviceRefsModel.getArray().length > 0);

        WebMarkupContainer vendorDataContainer = new WebMarkupContainer("vendorDataContainer");
        optionalContainer.add(vendorDataContainer);
        vendorDataContainer.add(
                new Label("vendorData.label", new ResourceModel("dicom.edit.applicationEntity.vendorData.label"))).add(
                new Label("vendorData", vendorDataModel));
        vendorDataContainer.setVisible(!vendorDataModel.getObject().equals("size 0"));

        addSaveButton(window, deviceModel, form, optionalContainer);
        addCancelButton(window, form);
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
            Collection<DeviceExtension> devExt = deviceModel.getDevice().listDeviceExtensions();
            if (devExt.contains(ProxyDeviceExtension.class))
                typeModel = Model.of(ConfigTreeProvider.ConfigurationType.Proxy);
            else if (devExt.contains(XCAiInitiatingGWCfg.class) 
                    || devExt.contains(XCAInitiatingGWCfg.class)
                    || devExt.contains(XCAiRespondingGWCfg.class) 
                    || devExt.contains(XCARespondingGWCfg.class)
                    || devExt.contains(XdsRegistry.class) 
                    || devExt.contains(XdsRepository.class))
                typeModel = Model.of(ConfigTreeProvider.ConfigurationType.XDS);
            else
                typeModel = Model.of(ConfigTreeProvider.ConfigurationType.Basic);

            deviceNameModel = Model.of(deviceModel.getDevice().getDeviceName());
            installedModel = Model.of(deviceModel.getDevice().isInstalled());

            descriptionModel = Model.of(deviceModel.getDevice().getDescription());
            deviceSerialNumberModel = Model.of(deviceModel.getDevice().getDeviceSerialNumber());
            institutionAddressModel = new StringArrayModel(deviceModel.getDevice().getInstitutionAddresses());

            if (deviceModel.getDevice().getInstitutionCodes().length > 0) {
                Code code = deviceModel.getDevice().getInstitutionCodes()[0];
                institutionCodeModel = new InstitutionCodeModel(code);
            }

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

    private void setXdsDevExtConfiguration(DeviceModel deviceModel) {
        if (typeModel == Model.of(ConfigTreeProvider.ConfigurationType.XDS)) {
        }
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
        typeModel = Model.of(ConfigTreeProvider.ConfigurationType.Basic);
        deviceNameModel = Model.of();
        installedModel = Model.of(true);
        schedulerIntervalModel = Model.of(ProxyDeviceExtension.DEFAULT_SCHEDULER_INTERVAL);
        descriptionModel = Model.of();
        deviceSerialNumberModel = Model.of();
        institutionAddressModel = new StringArrayModel(null);
        institutionCodeModel = new InstitutionCodeModel();
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
                    Collection<DeviceExtension> devExt = new ArrayList<>();
                    if (deviceModel != null) {
                        device = deviceModel.getDevice();
                        devExt.addAll(device.listDeviceExtensions());
                    } else {
                        device = initDeviceExtensions(devExt);
                    }

                    device.setInstalled(installedModel.getObject());
                    if (optionalContainer.isVisible()) {
                        setDeviceAttributes(device);
                        setProxyDeviceAttributes(devExt, device);
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

            private Device initDeviceExtensions(Collection<DeviceExtension> devExt) {
                Device device = new Device(deviceNameModel.getObject());
                if (typeModel.getObject().equals(ConfigurationType.Proxy))
                    initProxyDeviceExtension(devExt, device);
                if (typeModel.getObject().equals(ConfigurationType.AuditRecordRepository))
                    initARRExtension(devExt, device);
                if (typeModel.getObject().equals(ConfigurationType.XDS))
                    initXdsExtensions(devExt, device);
                return device;
            }

            private void initXdsExtensions(Collection<DeviceExtension> devExt, Device device) {
                XCAiInitiatingGWCfg xcaiInit = new XCAiInitiatingGWCfg();
                device.addDeviceExtension(xcaiInit);
                devExt.add(xcaiInit);
                XCAInitiatingGWCfg xcaInit = new XCAInitiatingGWCfg();
                device.addDeviceExtension(xcaInit);
                devExt.add(xcaInit);
                XCAiRespondingGWCfg xcaiResp = new XCAiRespondingGWCfg();
                device.addDeviceExtension(xcaiResp);
                devExt.add(xcaiResp);
                XCARespondingGWCfg xcaResp = new XCARespondingGWCfg();
                device.addDeviceExtension(xcaResp);
                devExt.add(xcaResp);
                XdsRegistry xdsReg = new XdsRegistry();
                device.addDeviceExtension(xdsReg);
                devExt.add(xdsReg);
                XdsRepository xdsRep = new XdsRepository();
                device.addDeviceExtension(xdsRep);
                devExt.add(xdsRep);
            }

            private void initARRExtension(Collection<DeviceExtension> devExt, Device device) {
                AuditRecordRepository auditRecordRepository = new AuditRecordRepository();
                device.addDeviceExtension(auditRecordRepository);
                devExt.add(auditRecordRepository);
            }

            private void initProxyDeviceExtension(Collection<DeviceExtension> devExt, Device device) {
                ProxyDeviceExtension proxyDeviceExtension = new ProxyDeviceExtension();
                proxyDeviceExtension.setSchedulerInterval(schedulerIntervalModel.getObject());
                device.addDeviceExtension(proxyDeviceExtension);
                device.addDeviceExtension(new HL7DeviceExtension());
                devExt.add(proxyDeviceExtension);
            }

            private void setProxyDeviceAttributes(Collection<DeviceExtension> devExt, Device device) {
                if (devExt.contains(ProxyDeviceExtension.class)) {
                    ProxyDeviceExtension proxyDevExt = device.getDeviceExtension(ProxyDeviceExtension.class);
                    proxyDevExt.setSchedulerInterval(schedulerIntervalModel.getObject());
                    proxyDevExt.setConfigurationStaleTimeout(
                            staleTimeoutModel.getObject() != null 
                                ? staleTimeoutModel.getObject() 
                                : 60);
                    if (forwardThreadsModel.getObject() != null)
                        proxyDevExt.setForwardThreads(forwardThreadsModel.getObject());
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
