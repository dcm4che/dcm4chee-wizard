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
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
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
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.RangeValidator;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4chee.proxy.conf.ProxyDeviceExtension;
import org.dcm4chee.storage.conf.StorageConfiguration;
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
import org.dcm4chee.xds2.conf.XDSiSourceCfg;
import org.dcm4chee.xds2.conf.XdsRegistry;
import org.dcm4chee.xds2.conf.XdsRepository;
import org.dcm4chee.xds2.conf.XdsSource;
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

    // Mandatory Device Attributes
    private Model<String> deviceNameModel;
    private IModel<Boolean> installedModel;
    
    // Optional Device Attributes
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

    // Proxy Device Attributes
    private Model<Integer> schedulerIntervalModel;
    private Model<Integer> forwardThreadsModel;
    private Model<Integer> staleTimeoutModel;

    // XDS Device Extensions
    private IModel<Boolean> xcaiInitiatingGatewayModel;
    private IModel<Boolean> xcaiRespondingGatewayModel;
    private IModel<Boolean> xcaInitiatingGatewayModel;
    private IModel<Boolean> xcaRespondingGatewayModel;
    private IModel<Boolean> xdsRegistryModel;
    private IModel<Boolean> xdsRepositoryModel;
    private IModel<Boolean> xdsSourceModel;
    private IModel<Boolean> xdsStorageModel;
    private IModel<Boolean> xdsiSourceModel;

    public CreateOrEditDevicePage(final ModalWindow window, final DeviceModel deviceModel) {
        super();
        add(new WebMarkupContainer("create-device-title").setVisible(deviceModel == null));
        add(new WebMarkupContainer("edit-device-title").setVisible(deviceModel != null));
        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");
        form.setResourceIdPrefix("dicom.edit.device.");
        add(form);
        if (deviceModel == null)
            initNewConfigurationTypeModel();
        else
            initDeviceModel(deviceModel);
        addTypeLabel(form);
        DropDownChoice<ConfigTreeProvider.ConfigurationType> typeDropDown = setConfigurationTypeList();
        addDeviceTitle(deviceModel, form);
        addInstalledLabel(form);
        form.add(proxyWebMarkupContainer());
        form.add(xdsWebMarkupContainer(form));
        addOptionalContainer(form);
        addSaveButton(window, deviceModel, form);
        addCancelButton(window, form);
        addOnChangeUpdate(deviceModel, form, typeDropDown);
    }

    private void addOptionalContainer(final ExtendedForm form) {
        final Form<?> optionalContainer = new Form<Object>("optional");
        optionalContainer.setOutputMarkupId(true);
        optionalContainer.setOutputMarkupPlaceholderTag(true);
        optionalContainer.setVisible(false);
        form.add(optionalContainer);
        addTextField(optionalContainer, 
                "dicom.edit.device.optional.description.label", 
                "description.label", 
                "description", 
                descriptionModel);
        addToggleOptionalCheckBox(form, optionalContainer);
        addOptionalParameters(optionalContainer);
        optionalContainer.add(getOptionalProxyContainer());
        optionalContainer.add(getRelatedDeviceRefsContainer());
        addWebMarkupContainer(optionalContainer);
    }

    private void addToggleOptionalCheckBox(final ExtendedForm form, final Form<?> optionalContainer) {
        form.add(new Label("toggleOptional.label", new ResourceModel("dicom.edit.toggleOptional.label")));

        AjaxCheckBox ajaxCheckBox = new AjaxCheckBox("toggleOptional", new Model<Boolean>()) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(optionalContainer.setVisible(this.getModelObject()));
            }
        };

        AjaxFormSubmitBehavior onClick = new AjaxFormSubmitBehavior(optionalContainer, "change") {

            private static final long serialVersionUID = 1L;

            protected void onEvent(final AjaxRequestTarget target) {
                super.onEvent(target);
            }
        };

        ajaxCheckBox.add(onClick);
        form.add(ajaxCheckBox);
    }

    private void addWebMarkupContainer(final Form<?> optionalContainer) {
        WebMarkupContainer vendorDataContainer = new WebMarkupContainer("vendorDataContainer");
        optionalContainer.add(vendorDataContainer);
        vendorDataContainer.add(new Label("vendorData.label", new ResourceModel(
                "dicom.edit.applicationEntity.vendorData.label")));
        vendorDataContainer.add(new Label("vendorData", vendorDataModel));
        vendorDataContainer.setVisible(!vendorDataModel.getObject().equals("size 0"));
    }

    private WebMarkupContainer getRelatedDeviceRefsContainer() {
        WebMarkupContainer relatedDeviceRefsContainer = new WebMarkupContainer("relatedDeviceRefsContainer");
        relatedDeviceRefsContainer.add(new Label("relatedDeviceRefs.label", new ResourceModel(
                "dicom.edit.device.relatedDeviceRefs.label")));
        relatedDeviceRefsContainer.add(new TextArea<String>("relatedDeviceRefs", relatedDeviceRefsModel)
                .setEnabled(false));
        relatedDeviceRefsContainer.setVisible(relatedDeviceRefsModel.getArray().length > 0);
        return relatedDeviceRefsContainer;
    }

    private WebMarkupContainer getOptionalProxyContainer() {
        WebMarkupContainer optionalProxyContainer = new WebMarkupContainer("optionalProxyContainer") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return typeModel.getObject().equals(ConfigurationType.Proxy);
            }
        };

        optionalProxyContainer.add(new Label("forwardThreads.label", new ResourceModel(
                "dicom.edit.device.optional.forwardThreads.label")).setOutputMarkupPlaceholderTag(true));
        optionalProxyContainer.add(new TextField<Integer>("forwardThreads", forwardThreadsModel).setType(Integer.class)
                .add(new RangeValidator<Integer>(1, 256)));

        optionalProxyContainer.add(new Label("staleTimeout.label", new ResourceModel(
                "dicom.edit.device.optional.staleTimeout.label")).setOutputMarkupPlaceholderTag(true));
        optionalProxyContainer.add(new TextField<Integer>("staleTimeout", staleTimeoutModel).setType(Integer.class)
                .add(new RangeValidator<Integer>(0, Integer.MAX_VALUE)));

        optionalProxyContainer.setOutputMarkupId(true);
        optionalProxyContainer.setOutputMarkupPlaceholderTag(true);
        return optionalProxyContainer;
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

    private WebMarkupContainer proxyWebMarkupContainer() {
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

    private WebMarkupContainer xdsWebMarkupContainer(ExtendedForm form) {
        WebMarkupContainer xdsWMC = new WebMarkupContainer("xds") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return typeModel.getObject().equals(ConfigurationType.XDS);
            }
        };
        addXdsConfigurationTypeCheckBoxes(xdsWMC, form);
        xdsWMC.setOutputMarkupPlaceholderTag(true);
        return xdsWMC;
    }

    private void addXdsConfigurationTypeCheckBoxes(final WebMarkupContainer xdsWMC, ExtendedForm form) {
        addXcaiInitiatingGatewayCheckBox(xdsWMC, form);
        addXcaInitiatingGatewayCheckBox(xdsWMC, form);
        addXcaiRespondingGatewayCheckBox(xdsWMC, form);
        addXcaRespondingGatewayCheckBox(xdsWMC, form);
        addXdsRegistryCheckBox(xdsWMC, form);
        addXdsRepositoryCheckBox(xdsWMC, form);
        addXdsSourceCheckBox(xdsWMC, form);
        addXdsStorageCheckBox(xdsWMC, form);
    }

    private void addXdsStorageCheckBox(final WebMarkupContainer xdsWMC, ExtendedForm form) {
        xdsWMC.add(new Label("xdsStorage.label", new ResourceModel("dicom.edit.xds.xdsStorage.label")));
        AjaxCheckBox xdsStorageCheckBox = new AjaxCheckBox("xdsStorage", xdsStorageModel) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (xdsStorageModel.getObject().booleanValue())
                    return;

                if (!isXdsExtensionSelected()) {
                    log.error("{}: Need to select at least one XDS Extension", this);
                    xdsStorageModel.setObject(true);
                    this.error(new ValidationError("Need to select at least one XDS Extension"));
                    target.add(this);
                }
            }
        };
        AjaxFormSubmitBehavior onClick = new AjaxFormSubmitBehavior(form, "change") {

            private static final long serialVersionUID = 1L;

            protected void onEvent(final AjaxRequestTarget target) {
                super.onEvent(target);
            }
        };
        xdsStorageCheckBox.add(onClick);
        xdsWMC.add(xdsStorageCheckBox.setOutputMarkupId(true));
    }

    private void addXdsSourceCheckBox(final WebMarkupContainer xdsWMC, ExtendedForm form) {
        xdsWMC.add(new Label("xdsSource.label", new ResourceModel("dicom.edit.xds.xdsSource.label")));
        AjaxCheckBox xdsSourceCheckBox = new AjaxCheckBox("xdsSource", xdsSourceModel) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (xdsSourceModel.getObject().booleanValue())
                    return;

                if (!isXdsExtensionSelected()) {
                    log.error("{}: Need to select at least one XDS Extension", this);
                    xdsSourceModel.setObject(true);
                    this.error(new ValidationError("Need to select at least one XDS Extension"));
                    target.add(this);
                }
            }
        };
        AjaxFormSubmitBehavior onClick = new AjaxFormSubmitBehavior(form, "change") {

            private static final long serialVersionUID = 1L;

            protected void onEvent(final AjaxRequestTarget target) {
                super.onEvent(target);
            }
        };
        xdsSourceCheckBox.add(onClick);
        xdsWMC.add(xdsSourceCheckBox.setOutputMarkupId(true));
    }

    private void addXdsiSourceCheckBox(final WebMarkupContainer xdsWMC, ExtendedForm form) {
        xdsWMC.add(new Label("xdsSource.label", new ResourceModel("dicom.edit.xds.xdsSource.label")));
        AjaxCheckBox xdsiSourceCheckBox = new AjaxCheckBox("xdsiSource", xdsiSourceModel) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (xdsiSourceModel.getObject().booleanValue())
                    return;

                if (!isXdsExtensionSelected()) {
                    log.error("{}: Need to select at least one XDS Extension", this);
                    xdsiSourceModel.setObject(true);
                    this.error(new ValidationError("Need to select at least one XDS Extension"));
                    target.add(this);
                }
            }
        };
        AjaxFormSubmitBehavior onClick = new AjaxFormSubmitBehavior(form, "change") {

            private static final long serialVersionUID = 1L;

            protected void onEvent(final AjaxRequestTarget target) {
                super.onEvent(target);
            }
        };
        xdsiSourceCheckBox.add(onClick);
        xdsWMC.add(xdsiSourceCheckBox.setOutputMarkupId(true));
    }

    private void addXdsRegistryCheckBox(final WebMarkupContainer xdsWMC, ExtendedForm form) {
        xdsWMC.add(new Label("xdsRegistry.label", new ResourceModel("dicom.edit.xds.xdsRegistry.label")));
        AjaxCheckBox xdsRegistryCheckBox = new AjaxCheckBox("xdsRegistry", xdsRegistryModel) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (xdsRegistryModel.getObject().booleanValue())
                    return;

                if (!isXdsExtensionSelected()) {
                    log.error("{}: Need to select at least one XDS Extension", this);
                    xdsRegistryModel.setObject(true);
                    this.error(new ValidationError("Need to select at least one XDS Extension"));
                    target.add(this);
                }
            }
        };
        AjaxFormSubmitBehavior onClick = new AjaxFormSubmitBehavior(form, "change") {
            
            private static final long serialVersionUID = 1L;
            
            protected void onEvent(final AjaxRequestTarget target) {
                super.onEvent(target);
            }
        };
        xdsRegistryCheckBox.add(onClick);
        xdsWMC.add(xdsRegistryCheckBox.setOutputMarkupId(true));
    }

    private void addXcaRespondingGatewayCheckBox(final WebMarkupContainer xdsWMC, ExtendedForm form) {
        xdsWMC.add(new Label("xcaRespondingGateway.label", new ResourceModel("dicom.edit.xds.xcaRespondingGateway.label")));
        AjaxCheckBox xcaRespondingGatewayCheckBox = new AjaxCheckBox("xcaRespondingGateway", xcaRespondingGatewayModel) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (xcaRespondingGatewayModel.getObject().booleanValue())
                    return;

                if (!isXdsExtensionSelected()) {
                    log.error("{}: Need to select at least one XDS Extension", this);
                    xcaRespondingGatewayModel.setObject(true);
                    this.error(new ValidationError("Need to select at least one XDS Extension"));
                    target.add(this);
                }
            }
        };
        AjaxFormSubmitBehavior onClick = new AjaxFormSubmitBehavior(form, "change") {
            
            private static final long serialVersionUID = 1L;
            
            protected void onEvent(final AjaxRequestTarget target) {
                super.onEvent(target);
            }
        };
        xcaRespondingGatewayCheckBox.add(onClick);
        xdsWMC.add(xcaRespondingGatewayCheckBox.setOutputMarkupId(true));
    }

    private void addXcaiRespondingGatewayCheckBox(final WebMarkupContainer xdsWMC, ExtendedForm form) {
        xdsWMC.add(new Label("xcaiRespondingGateway.label", new ResourceModel("dicom.edit.xds.xcaiRespondingGateway.label")));
        AjaxCheckBox xcaiRespondingGatewayCheckBox = new AjaxCheckBox("xcaiRespondingGateway", xcaiRespondingGatewayModel) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (xcaiRespondingGatewayModel.getObject().booleanValue())
                    return;

                if (!isXdsExtensionSelected()) {
                    log.error("{}: Need to select at least one XDS Extension", this);
                    xcaiRespondingGatewayModel.setObject(true);
                    this.error(new ValidationError("Need to select at least one XDS Extension"));
                    target.add(this);
                }
            }
        };
        AjaxFormSubmitBehavior onClick = new AjaxFormSubmitBehavior(form, "change") {
            
            private static final long serialVersionUID = 1L;
            
            protected void onEvent(final AjaxRequestTarget target) {
                super.onEvent(target);
            }
        };
        xcaiRespondingGatewayCheckBox.add(onClick);
        xdsWMC.add(xcaiRespondingGatewayCheckBox.setOutputMarkupId(true));
    }

    private void addXcaInitiatingGatewayCheckBox(final WebMarkupContainer xdsWMC, ExtendedForm form) {
        xdsWMC.add(new Label("xcaInitiatingGateway.label", new ResourceModel("dicom.edit.xds.xcaInitiatingGateway.label")));
        AjaxCheckBox xcaInitiatingGatewayCheckBox = new AjaxCheckBox("xcaInitiatingGateway", xcaInitiatingGatewayModel) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (xcaInitiatingGatewayModel.getObject().booleanValue())
                    return;

                if (!isXdsExtensionSelected()) {
                    log.error("{}: Need to select at least one XDS Extension", this);
                    xcaInitiatingGatewayModel.setObject(true);
                    this.error(new ValidationError("Need to select at least one XDS Extension"));
                    target.add(this);
                }
            }
        };
        AjaxFormSubmitBehavior onClick = new AjaxFormSubmitBehavior(form, "change") {
            
            private static final long serialVersionUID = 1L;
            
            protected void onEvent(final AjaxRequestTarget target) {
                super.onEvent(target);
            }
        };
        xcaInitiatingGatewayCheckBox.add(onClick);
        xdsWMC.add(xcaInitiatingGatewayCheckBox.setOutputMarkupId(true));
    }

    private void addXdsRepositoryCheckBox(final WebMarkupContainer xdsWMC, ExtendedForm form) {
        xdsWMC.add(new Label("xdsRepository.label", new ResourceModel("dicom.edit.xds.xdsRepository.label")));
        AjaxCheckBox xdsRepositoryCheckBox = new AjaxCheckBox("xdsRepository", xdsRepositoryModel) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (xdsRepositoryModel.getObject().booleanValue())
                    return;

                if (!isXdsExtensionSelected()) {
                    log.error("{}: Need to select at least one XDS Extension", this);
                    xdsRepositoryModel.setObject(true);
                    this.error(new ValidationError("Need to select at least one XDS Extension"));
                    target.add(this);
                }
            }
        };
        AjaxFormSubmitBehavior onClick = new AjaxFormSubmitBehavior(form, "change") {
            
            private static final long serialVersionUID = 1L;
            
            protected void onEvent(final AjaxRequestTarget target) {
                super.onEvent(target);
            }
        };
        xdsRepositoryCheckBox.add(onClick);
        xdsWMC.add(xdsRepositoryCheckBox.setOutputMarkupId(true));
    }

    private void addXcaiInitiatingGatewayCheckBox(final WebMarkupContainer xdsWMC, ExtendedForm form) {
        AjaxFormSubmitBehavior onClick = new AjaxFormSubmitBehavior(form, "change") {
            
            private static final long serialVersionUID = 1L;
            
            protected void onEvent(final AjaxRequestTarget target) {
                super.onEvent(target);
            }
        };

        xdsWMC.add(new Label("xcaiInitiatingGateway.label", new ResourceModel("dicom.edit.xds.xcaiInitiatingGateway.label")));
        AjaxCheckBox xcaiInitiatingGatewayCheckBox = new AjaxCheckBox("xcaiInitiatingGateway", xcaiInitiatingGatewayModel) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (xcaiInitiatingGatewayModel.getObject().booleanValue())
                    return;

                if (!isXdsExtensionSelected()) {
                    log.error("{}: Need to select at least one XDS Extension", this);
                    xcaiInitiatingGatewayModel.setObject(true);
                    this.error(new ValidationError("Need to select at least one XDS Extension"));
                    target.add(this);
                }
            }
        };
        xcaiInitiatingGatewayCheckBox.add(onClick);
        xdsWMC.add(xcaiInitiatingGatewayCheckBox);
    }

    private boolean isXdsExtensionSelected() {
        return xcaiInitiatingGatewayModel.getObject().booleanValue()
                || xcaiRespondingGatewayModel.getObject().booleanValue()
                || xcaInitiatingGatewayModel.getObject().booleanValue()
                || xcaRespondingGatewayModel.getObject().booleanValue()
                || xdsRegistryModel.getObject().booleanValue()
                || xdsSourceModel.getObject().booleanValue()
                || xdsiSourceModel.getObject().booleanValue()
                || xdsStorageModel.getObject().booleanValue()
                || xdsRepositoryModel.getObject().booleanValue();
    }

    private void addInstalledLabel(final ExtendedForm form) {
        form.add(new Label("installed.label", new ResourceModel("dicom.edit.device.installed.label")));
        form.add(new CheckBox("installed", installedModel));
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
                        target.add(form.get("optional"));
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
            setTypeModel(deviceModel);
            setDeviceConfiguration(deviceModel);
            setProxyDevExtConfiguration(deviceModel);
            setXdsDevExtConfiguration(deviceModel);
        } catch (ConfigurationException ce) {
            log.error(this.getClass().toString() + ": " + "Error retrieving device data: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new ModalWindowRuntimeException(ce.getLocalizedMessage());
        }
    }

    private void setTypeModel(final DeviceModel deviceModel) throws ConfigurationException {
        Iterator<DeviceExtension> iter = deviceModel.getDevice().listDeviceExtensions().iterator();
        while (iter.hasNext()) {
            DeviceExtension ext = iter.next();
            if (ext instanceof ProxyDeviceExtension) {
                typeModel = Model.of(ConfigTreeProvider.ConfigurationType.Proxy);
                break;
            }
            else if (ext instanceof XCAiInitiatingGWCfg 
                    || ext instanceof XCAInitiatingGWCfg
                    || ext instanceof XCAiRespondingGWCfg 
                    || ext instanceof XCARespondingGWCfg
                    || ext instanceof XdsRegistry 
                    || ext instanceof XdsSource
                    || ext instanceof StorageConfiguration
                    || ext instanceof XdsRepository) {
                typeModel = Model.of(ConfigTreeProvider.ConfigurationType.XDS);
                break;
            }
        }
        if (typeModel == null)
            typeModel = Model.of(ConfigTreeProvider.ConfigurationType.Basic);
    }

    private void setXdsDevExtConfiguration(DeviceModel deviceModel) throws ConfigurationException {
        if (!typeModel.getObject().equals(ConfigurationType.XDS))
                return;

        xcaiInitiatingGatewayModel = Model.of(deviceModel.getDevice().getDeviceExtension(XCAiInitiatingGWCfg.class) != null);
        xcaiRespondingGatewayModel = Model.of(deviceModel.getDevice().getDeviceExtension(XCAiRespondingGWCfg.class) != null);
        xcaInitiatingGatewayModel = Model.of(deviceModel.getDevice().getDeviceExtension(XCAInitiatingGWCfg.class) != null);
        xcaRespondingGatewayModel = Model.of(deviceModel.getDevice().getDeviceExtension(XCARespondingGWCfg.class) != null);
        xdsRegistryModel = Model.of(deviceModel.getDevice().getDeviceExtension(XdsRegistry.class) != null);
        xdsRepositoryModel = Model.of(deviceModel.getDevice().getDeviceExtension(XdsRepository.class) != null);
        xdsSourceModel = Model.of(deviceModel.getDevice().getDeviceExtension(XdsSource.class) != null);
        xdsStorageModel = Model.of(deviceModel.getDevice().getDeviceExtension(StorageConfiguration.class) != null);
        xdsiSourceModel = Model.of(deviceModel.getDevice().getDeviceExtension(XDSiSourceCfg.class) != null);
    }

    private void setDeviceConfiguration(final DeviceModel deviceModel) throws ConfigurationException {
        deviceNameModel = Model.of(deviceModel.getDevice().getDeviceName());
        installedModel = Model.of(deviceModel.getDevice().isInstalled());
        descriptionModel = Model.of(deviceModel.getDevice().getDescription());
        deviceSerialNumberModel = Model.of(deviceModel.getDevice().getDeviceSerialNumber());
        institutionAddressModel = new StringArrayModel(deviceModel.getDevice().getInstitutionAddresses());
        institutionCodeModel = deviceModel.getDevice().getInstitutionCodes().length > 0 
                ? new InstitutionCodeModel(deviceModel.getDevice().getInstitutionCodes()[0])
                        : new InstitutionCodeModel(null);
        institutionalDepartmentNameModel = new StringArrayModel(deviceModel.getDevice().getInstitutionalDepartmentNames());
        institutionNameModel = new StringArrayModel(deviceModel.getDevice().getInstitutionNames());
        issuerOfAccessionNumberModel = deviceModel.getDevice().getIssuerOfAccessionNumber() == null
                ? new Model<String>()
                        : Model.of(deviceModel.getDevice().getIssuerOfAccessionNumber().toString());
        issuerOfAdmissionIDModel = deviceModel.getDevice().getIssuerOfAdmissionID() == null
                ? new Model<String>()
                        : Model.of(deviceModel.getDevice().getIssuerOfAdmissionID().toString());
        issuerOfContainerIdentifierModel = deviceModel.getDevice().getIssuerOfContainerIdentifier() == null
                ? new Model<String>()
                        : Model.of(deviceModel.getDevice().getIssuerOfContainerIdentifier().toString());
        issuerOfPatientIDModel = deviceModel.getDevice().getIssuerOfPatientID() == null
                ? new Model<String>()
                        : Model.of(deviceModel.getDevice().getIssuerOfPatientID().toString());
        issuerOfServiceEpisodeIDModel = deviceModel.getDevice().getIssuerOfServiceEpisodeID() == null
                ? new Model<String>()
                        : Model.of(deviceModel.getDevice().getIssuerOfServiceEpisodeID().toString());
        issuerOfSpecimenIdentifierModel = deviceModel.getDevice().getIssuerOfSpecimenIdentifier() == null
                ? new Model<String>()
                        : Model.of(deviceModel.getDevice().getIssuerOfSpecimenIdentifier().toString());
        manufacturerModel = Model.of(deviceModel.getDevice().getManufacturer());
        manufacturerModelNameModel = Model.of(deviceModel.getDevice().getManufacturerModelName());
        orderFillerIdentifierModel = deviceModel.getDevice().getOrderFillerIdentifier() == null
                ? new Model<String>()
                        : Model.of(deviceModel.getDevice().getOrderFillerIdentifier().toString());
        orderPlacerIdentifierModel = deviceModel.getDevice().getOrderPlacerIdentifier() == null
                ? new Model<String>()
                        : Model.of(deviceModel.getDevice().getOrderPlacerIdentifier().toString());
        primaryDeviceTypesModel = new StringArrayModel(deviceModel.getDevice().getPrimaryDeviceTypes());
        relatedDeviceRefsModel = new StringArrayModel(deviceModel.getDevice().getRelatedDeviceRefs());
        softwareVersionsModel = new StringArrayModel(deviceModel.getDevice().getSoftwareVersions());
        stationNameModel = Model.of(deviceModel.getDevice().getStationName());
        vendorDataModel = Model.of("size " + deviceModel.getDevice().getVendorData().length);
        trustStoreURLModel = Model.of(deviceModel.getDevice().getTrustStoreURL());
        trustStoreTypeModel = Model.of(deviceModel.getDevice().getTrustStoreType());
        trustStorePinModel = Model.of(deviceModel.getDevice().getTrustStorePinProperty() != null 
                ? deviceModel.getDevice().getTrustStorePinProperty() 
                        : deviceModel.getDevice().getTrustStorePin());
        useTrustStorePinProperty = Model.of(deviceModel.getDevice().getTrustStorePinProperty() != null);
        keyStoreURLModel = Model.of(deviceModel.getDevice().getKeyStoreURL());
        keyStoreTypeModel = Model.of(deviceModel.getDevice().getKeyStoreType());
        keyStorePinModel = Model.of(deviceModel.getDevice().getKeyStorePinProperty() != null 
                ? deviceModel.getDevice().getKeyStorePinProperty() 
                        : deviceModel.getDevice().getKeyStorePin());
        useKeyStorePinProperty = Model.of(deviceModel.getDevice().getKeyStorePinProperty() != null);
        keyStoreKeyPinModel = Model.of(deviceModel.getDevice().getKeyStoreKeyPinProperty() != null 
                ? deviceModel.getDevice().getKeyStoreKeyPinProperty() 
                        : deviceModel.getDevice().getKeyStoreKeyPin());
        useKeyStoreKeyPinProperty = Model.of(deviceModel.getDevice().getKeyStoreKeyPinProperty() != null);
    }

    private void setProxyDevExtConfiguration(final DeviceModel deviceModel) throws ConfigurationException {
        if (!typeModel.getObject().equals(ConfigurationType.Proxy))
            return;

        ProxyDeviceExtension proxyDevExt = deviceModel.getDevice().getDeviceExtension(ProxyDeviceExtension.class);
        schedulerIntervalModel = Model.of(proxyDevExt.getSchedulerInterval());
        forwardThreadsModel = Model.of(proxyDevExt.getForwardThreads());
        staleTimeoutModel = Model.of(proxyDevExt.getConfigurationStaleTimeout());
    }

    private void initNewConfigurationTypeModel() {
        initBasicDeviceConfigurationTypeModel();
        initProxyConfigurationTypeModel();
        initXdsConfigurationTypeModel();
    }

    private void initXdsConfigurationTypeModel() {
        xcaiInitiatingGatewayModel = Model.of(true);
        xcaInitiatingGatewayModel = Model.of(true);
        xcaiRespondingGatewayModel = Model.of(true);
        xcaRespondingGatewayModel = Model.of(true);
        xdsRegistryModel = Model.of(true);
        xdsRepositoryModel = Model.of(true);
        xdsSourceModel = Model.of(true);
        xdsStorageModel = Model.of(true);
        xdsiSourceModel = Model.of(true);
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

    private void addSaveButton(final ModalWindow window, final DeviceModel deviceModel, final ExtendedForm form) {
        form.add(new IndicatingAjaxButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    Device device = deviceModel != null
                            ? deviceModel.getDevice()
                                    : initDeviceExtensions();
                    setDeviceAttributes(device);
                    setProxyDeviceAttributes(device);
                    setXdsDeviceAttributes(device);
                    if (deviceModel == null)
                        ConfigTreeProvider.get().persistDevice(device);
                    else
                        ConfigTreeProvider.get().mergeDevice(device);
                    window.close(target);
                } catch (Exception e) {
                    log.error(this.getClass().toString() + ": " + "Error modifying device: " + e.getMessage());
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
    }

    private void setProxyDeviceAttributes(Device device) {
        ProxyDeviceExtension proxyDevExt = device.getDeviceExtension(ProxyDeviceExtension.class);
        if (proxyDevExt == null)
            return;

        // mandatory
        proxyDevExt.setSchedulerInterval(schedulerIntervalModel.getObject());

        // optional
        if (staleTimeoutModel.getObject() != null)
            proxyDevExt.setConfigurationStaleTimeout(staleTimeoutModel.getObject());
        if (forwardThreadsModel.getObject() != null)
            proxyDevExt.setForwardThreads(forwardThreadsModel.getObject());
    }

    private void setXdsDeviceAttributes(Device device) {
        if (!typeModel.getObject().equals(ConfigTreeProvider.ConfigurationType.XDS))
            return;

        XCAiInitiatingGWCfg xcaiInit = device.getDeviceExtension(XCAiInitiatingGWCfg.class);
        Boolean xcaiInitCheck = xcaiInitiatingGatewayModel.getObject();
        if (xcaiInitCheck && xcaiInit == null) {
            XCAiInitiatingGWCfg xcai = new XCAiInitiatingGWCfg();
            xcai.setApplicationName("SAMPLE^APPLICATION");
            xcai.setHomeCommunityID("SAMPLE-ID");
            device.addDeviceExtension(xcai);
        }
        else if (xcaiInit != null && !xcaiInitCheck)
                device.removeDeviceExtension(xcaiInit);

        XCAInitiatingGWCfg xcaInit = device.getDeviceExtension(XCAInitiatingGWCfg.class);
        Boolean xcaInitCheck = xcaInitiatingGatewayModel.getObject();
        if (xcaInitCheck && xcaInit == null) {
            XCAInitiatingGWCfg xca = new XCAInitiatingGWCfg();
            xca.setApplicationName("SAMPLE^APPLICATION");
            xca.setHomeCommunityID("SAMPLE-ID");
            device.addDeviceExtension(xca);
        }
        else if (xcaInit != null && !xcaInitCheck)
                device.removeDeviceExtension(xcaInit);

        XCAiRespondingGWCfg xcaiResp = device.getDeviceExtension(XCAiRespondingGWCfg.class);
        Boolean xcaiRespCheck = xcaiRespondingGatewayModel.getObject();
        if (xcaiRespCheck && xcaiResp == null) {
            XCAiRespondingGWCfg xcai = new XCAiRespondingGWCfg();
            xcai.setApplicationName("SAMPLE^APPLICATION");
            xcai.setHomeCommunityID("SAMPLE-ID");
            device.addDeviceExtension(xcai);
        }
        else if (xcaiResp != null && !xcaiRespCheck)
                device.removeDeviceExtension(xcaiResp);

        XCARespondingGWCfg xcaResp = device.getDeviceExtension(XCARespondingGWCfg.class);
        Boolean xcaRespCheck = xcaRespondingGatewayModel.getObject();
        if (xcaRespCheck && xcaResp == null) {
            XCARespondingGWCfg xca = new XCARespondingGWCfg();
            xca.setApplicationName("SAMPLE^APPLICATION");
            xca.setHomeCommunityID("SAMPLE-ID");
            device.addDeviceExtension(xca);
        }
        else if (xcaResp != null && !xcaRespCheck)
                device.removeDeviceExtension(xcaResp);

        XdsRegistry xdsReg = device.getDeviceExtension(XdsRegistry.class);
        Boolean xdsRegCheck = xdsRegistryModel.getObject();
        if (xdsRegCheck && xdsReg == null) {
            XdsRegistry xds = new XdsRegistry();
            xds.setApplicationName("SAMPLE^APPLICATION");
            xds.setAffinityDomain(new String[] {"SAMPLE-DOMAIN"});
            xds.setAffinityDomainConfigDir("SAMPLE-DIR");
            device.addDeviceExtension(xds);
        }
        else if (xdsReg != null && !xdsRegCheck)
                device.removeDeviceExtension(xdsReg);

        XdsRepository xdsRep = device.getDeviceExtension(XdsRepository.class);
        Boolean xdsRepCheck = xdsRepositoryModel.getObject();
        if (xdsRepCheck && xdsRep == null) {
            XdsRepository xds = new XdsRepository();
            xds.setApplicationName("SAMPLE^APPLICATION");
            xds.setRepositoryUID("SAMPLE-UID");
            device.addDeviceExtension(xds);
        }
        else if (xdsRep != null && !xdsRepCheck)
                device.removeDeviceExtension(xdsRep);

        XdsSource xdsSrc = device.getDeviceExtension(XdsSource.class);
        Boolean xdsSrcCheck = xdsSourceModel.getObject();
        if (xdsSrcCheck && xdsSrc == null) {
            XdsSource xds = new XdsSource();
            device.addDeviceExtension(xds);
        }
        else if (xdsSrc != null && !xdsSrcCheck)
                device.removeDeviceExtension(xdsSrc);


        StorageConfiguration xdsStorage = device.getDeviceExtension(StorageConfiguration.class);
        Boolean xdsStorageCheck = xdsStorageModel.getObject();
        if (xdsStorageCheck && xdsStorage == null) {
            StorageConfiguration xds = new StorageConfiguration();
            device.addDeviceExtension(xds);
        }
        else if (xdsStorage != null && !xdsStorageCheck)
            device.removeDeviceExtension(xdsStorage);        
        
        XDSiSourceCfg xdsiSrc = device.getDeviceExtension(XDSiSourceCfg.class);
        Boolean xdsiSrcCheck = xdsiSourceModel.getObject();
        if (xdsiSrcCheck && xdsiSrc == null) {
            XDSiSourceCfg xds = new XDSiSourceCfg();
            device.addDeviceExtension(xds);
        }
        else if (xdsiSrc != null && !xdsiSrcCheck)
                device.removeDeviceExtension(xdsiSrc);
    }

    private Device initDeviceExtensions() {
        Device device = new Device(deviceNameModel.getObject());
        if (typeModel.getObject().equals(ConfigurationType.Proxy))
            initProxyDeviceExtension(device);
        if (typeModel.getObject().equals(ConfigurationType.AuditRecordRepository))
            initARRExtension(device);
        return device;
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

    private void setDeviceAttributes(Device device) {
        // mandatory
        device.setInstalled(installedModel.getObject());

        // optional
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
