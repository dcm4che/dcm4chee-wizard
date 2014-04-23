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

package org.dcm4chee.wizard.edit.xds;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.net.Device;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ModalWindowRuntimeException;
import org.dcm4chee.wizard.common.component.secure.SecureSessionCheckPage;
import org.dcm4chee.wizard.model.DeviceModel;
import org.dcm4chee.wizard.model.StringArrayModel;
import org.dcm4chee.wizard.model.xds.XDSRegistryModel;
import org.dcm4chee.wizard.tree.ConfigTreeNode;
import org.dcm4chee.wizard.tree.ConfigTreeProvider;
import org.dcm4chee.xds2.conf.XdsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class XDSRegistryEditPage extends SecureSessionCheckPage{

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(XDSRegistryEditPage.class);

    private List<Boolean> booleanChoice = Arrays.asList(new Boolean[] { true, false });

    // mandatory
    private Model<String> xdsApplicationNameModel;
    private StringArrayModel xdsAffinityDomainModel;
    private Model<String> xdsAffinityDomainConfigDirModel;
    private Model<String> xdsQueryURLModel;
    private Model<String> xdsRegisterURLModel;    

    // optional
    private StringArrayModel xdsAcceptedMimeTypesModel;
    private Model<String> xdsSoapMsgLogDirModel;
    private Model<Boolean> xdsCreateMissingPIDsModel;
    private Model<Boolean> xdsCreateMissingCodesModel;
    private Model<Boolean> xdsDontSaveCodeClassificationsModel;
    private Model<Boolean> xdsCheckAffinityDomainModel;
    private Model<Boolean> xdsCheckMimetypeModel;
    private Model<Boolean> xdsPreMetadataCheckModel;

    public XDSRegistryEditPage(final ModalWindow window, XDSRegistryModel model,
            final ConfigTreeNode deviceNode) {
        super();
        try {
            add(new WebMarkupContainer("edit-xdsregistry-title").setVisible(model != null));
            setOutputMarkupId(true);
            final ExtendedForm form = new ExtendedForm("form");
            form.setResourceIdPrefix("dicom.edit.xds.");
            Device device = ((DeviceModel) deviceNode.getModel()).getDevice();
            initAttributes(device.getDeviceExtension(XdsRegistry.class));
            addMandatoryFormAttributes(form);
            addOptionalFormAttributes(form);
            addSaveButton(window, deviceNode, form);
            addCancelButton(window, form);
            add(form);
        } catch (ConfigurationException e) {
            log.error("{}: Error modifying XDS Registry: {}", this, e);
            if (log.isDebugEnabled())
                e.printStackTrace();
            throw new ModalWindowRuntimeException(e.getLocalizedMessage());
        }
    }

    private void addOptionalFormAttributes(ExtendedForm form) {
        final Form<?> optionalContainer = new Form<Object>("optional");
        optionalContainer.setOutputMarkupId(true);
        optionalContainer.setOutputMarkupPlaceholderTag(true);
        optionalContainer.setVisible(false);
        form.add(optionalContainer);
        addToggleOptionalCheckBox(form, optionalContainer);

        optionalContainer.add(new Label("xdsAcceptedMimeTypes.label", new ResourceModel(
                "dicom.edit.xds.optional.xdsAcceptedMimeTypes.label")).setOutputMarkupPlaceholderTag(true));
        optionalContainer.add(new TextArea<String>("xdsAcceptedMimeTypes", xdsAcceptedMimeTypesModel)
                .setType(String.class));

        optionalContainer.add(new Label("xdsSoapMsgLogDir.label", new ResourceModel(
                "dicom.edit.xds.optional.xdsSoapMsgLogDir.label")).setOutputMarkupPlaceholderTag(true));
        optionalContainer.add(new TextField<String>("xdsSoapMsgLogDir", xdsSoapMsgLogDirModel).setType(String.class));

        optionalContainer.add(new Label("xdsCreateMissingPIDs.label", new ResourceModel(
                "dicom.edit.xds.optional.xdsCreateMissingPIDs.label")).setOutputMarkupPlaceholderTag(true));
        optionalContainer.add(new DropDownChoice<>("xdsCreateMissingPIDs", xdsCreateMissingPIDsModel, booleanChoice)
                .setNullValid(false));

        optionalContainer.add(new Label("xdsCreateMissingCodes.label", new ResourceModel(
                "dicom.edit.xds.optional.xdsCreateMissingCodes.label")).setOutputMarkupPlaceholderTag(true));
        optionalContainer.add(new DropDownChoice<>("xdsCreateMissingCodes", xdsCreateMissingCodesModel, booleanChoice)
                .setNullValid(false));

        optionalContainer.add(new Label("xdsDontSaveCodeClassifications.label", new ResourceModel(
                "dicom.edit.xds.optional.xdsDontSaveCodeClassifications.label")).setOutputMarkupPlaceholderTag(true));
        optionalContainer.add(new DropDownChoice<>("xdsDontSaveCodeClassifications",
                xdsDontSaveCodeClassificationsModel, booleanChoice).setNullValid(false));

        optionalContainer.add(new Label("xdsCheckAffinityDomain.label", new ResourceModel(
                "dicom.edit.xds.optional.xdsCheckAffinityDomain.label")).setOutputMarkupPlaceholderTag(true));
        optionalContainer
                .add(new DropDownChoice<>("xdsCheckAffinityDomain", xdsCheckAffinityDomainModel, booleanChoice)
                        .setNullValid(false));

        optionalContainer.add(new Label("xdsCheckMimetype.label", new ResourceModel(
                "dicom.edit.xds.optional.xdsCheckMimetype.label")).setOutputMarkupPlaceholderTag(true));
        optionalContainer.add(new DropDownChoice<>("xdsCheckMimetype", xdsCheckMimetypeModel, booleanChoice)
                .setNullValid(false));

        optionalContainer.add(new Label("xdsPreMetadataCheck.label", new ResourceModel(
                "dicom.edit.xds.optional.xdsPreMetadataCheck.label")).setOutputMarkupPlaceholderTag(true));
        optionalContainer.add(new DropDownChoice<>("xdsPreMetadataCheck", xdsPreMetadataCheckModel, booleanChoice)
                .setNullValid(false));
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

    private void addMandatoryFormAttributes(ExtendedForm form) {
        Label applicationNameLabel = new Label("xdsApplicationName.label", new ResourceModel(
                "dicom.edit.xds.xdsApplicationName.label"));
        form.add(applicationNameLabel);
        FormComponent<String> applicationNameTextField = new TextField<String>("xdsApplicationName",
                xdsApplicationNameModel);
        applicationNameTextField.setType(String.class);
        applicationNameTextField.setRequired(true);
        form.add(applicationNameTextField);

        Label affinityDomainLabel = new Label("xdsAffinityDomain.label", new ResourceModel(
                "dicom.edit.xds.xdsAffinityDomain.label"));
        form.add(affinityDomainLabel);
        FormComponent<String> affinityDomainTextArea = new TextArea<String>("xdsAffinityDomain",
                xdsAffinityDomainModel);
        affinityDomainTextArea.setType(String.class);
        affinityDomainTextArea.setRequired(true);
        form.add(affinityDomainTextArea);

        Label affinityDomainConfigDirLabel = new Label("xdsAffinityDomainConfigDir.label", new ResourceModel(
                "dicom.edit.xds.xdsAffinityDomainConfigDir.label"));
        form.add(affinityDomainConfigDirLabel);
        
        FormComponent<String> affinityDomainConfigDirTextField = new TextField<String>("xdsAffinityDomainConfigDir",
                xdsAffinityDomainConfigDirModel);
        affinityDomainConfigDirTextField.setType(String.class);
        affinityDomainConfigDirTextField.setRequired(true);
        form.add(affinityDomainConfigDirTextField);

        form.add(new Label("xdsQueryURL.label", new ResourceModel("dicom.edit.xds.xdsQueryURL.label")));
        FormComponent<String> xdsQueryURLField = new TextField<String>("xdsQueryURL",
                xdsQueryURLModel);
        xdsQueryURLField.setType(String.class);
        xdsQueryURLField.setRequired(true);
        form.add(xdsQueryURLField);

        form.add(new Label("xdsRegisterURL.label", new ResourceModel("dicom.edit.xds.xdsRegisterURL.label")));
        FormComponent<String> xdsRegisterURLField = new TextField<String>("xdsRegisterURL",
                xdsRegisterURLModel);
        xdsRegisterURLField.setType(String.class);
        xdsRegisterURLField.setRequired(true);
        form.add(xdsRegisterURLField);
        
    }

    private void initAttributes(XdsRegistry xds) {
        if (xds == null) {
            xdsApplicationNameModel = Model.of();
            xdsAffinityDomainModel = new StringArrayModel(null);
            xdsAffinityDomainConfigDirModel = Model.of();
            xdsAcceptedMimeTypesModel = new StringArrayModel(null);
            xdsSoapMsgLogDirModel = Model.of();
            xdsCreateMissingPIDsModel = Model.of();
            xdsCreateMissingCodesModel = Model.of();
            xdsDontSaveCodeClassificationsModel = Model.of();
            xdsCheckAffinityDomainModel = Model.of();
            xdsCheckMimetypeModel = Model.of();
            xdsPreMetadataCheckModel = Model.of();
            xdsQueryURLModel = Model.of();
            xdsRegisterURLModel = Model.of();
        } else {
            xdsApplicationNameModel = Model.of(xds.getApplicationName());
            xdsAffinityDomainModel = new StringArrayModel(xds.getAffinityDomain());
            xdsAffinityDomainConfigDirModel = Model.of(xds.getAffinityDomainConfigDir());
            xdsAcceptedMimeTypesModel = new StringArrayModel(xds.getAcceptedMimeTypes());
            xdsSoapMsgLogDirModel = Model.of(xds.getSoapLogDir());
            xdsCreateMissingPIDsModel = Model.of(xds.isCreateMissingPIDs());
            xdsCreateMissingCodesModel = Model.of(xds.isCreateMissingCodes());
            xdsDontSaveCodeClassificationsModel = Model.of(xds.isDontSaveCodeClassifications());
            xdsCheckAffinityDomainModel = Model.of(xds.isCheckAffinityDomain());
            xdsCheckMimetypeModel = Model.of(xds.isCheckMimetype());
            xdsPreMetadataCheckModel = Model.of(xds.isPreMetadataCheck());
            xdsQueryURLModel = Model.of(xds.getQueryUrl());
            xdsRegisterURLModel = Model.of(xds.getRegisterUrl());            
        }
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

    private void addSaveButton(final ModalWindow window, final ConfigTreeNode deviceNode, final ExtendedForm form) {
        form.add(new IndicatingAjaxButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    Device device = ((DeviceModel) deviceNode.getModel()).getDevice();
                    XdsRegistry xds = device.getDeviceExtension(XdsRegistry.class);
                    // mandatory
                    xds.setApplicationName(xdsApplicationNameModel.getObject());
                    xds.setAffinityDomain(xdsAffinityDomainModel.getArray());
                    xds.setAffinityDomainConfigDir(xdsAffinityDomainConfigDirModel.getObject());
                    
                    xds.setQueryUrl(xdsQueryURLModel.getObject());
                    xds.setRegisterUrl(xdsRegisterURLModel.getObject());
                    
                    // optional
                    if (xdsAcceptedMimeTypesModel.getArray().length > 0)
                        xds.setAcceptedMimeTypes(xdsAcceptedMimeTypesModel.getArray());
                    if (xdsSoapMsgLogDirModel.getObject() != null)
                        xds.setSoapLogDir(xdsSoapMsgLogDirModel.getObject());
                    if (xdsCreateMissingPIDsModel.getObject() != null)
                        xds.setCreateMissingPIDs(xdsCreateMissingPIDsModel.getObject());
                    if (xdsCreateMissingCodesModel.getObject() != null)
                        xds.setCreateMissingCodes(xdsCreateMissingCodesModel.getObject());
                    if (xdsDontSaveCodeClassificationsModel.getObject() != null)
                        xds.setDontSaveCodeClassifications(xdsDontSaveCodeClassificationsModel.getObject());
                    if (xdsCheckAffinityDomainModel.getObject() != null)
                        xds.setCheckAffinityDomain(xdsCheckAffinityDomainModel.getObject());
                    if (xdsCheckMimetypeModel.getObject() != null)
                        xds.setCheckMimetype(xdsCheckMimetypeModel.getObject());
                    if (xdsPreMetadataCheckModel.getObject() != null)
                        xds.setPreMetadataCheck(xdsPreMetadataCheckModel.getObject());
                    ConfigTreeProvider.get().mergeDevice(device);
                    window.close(target);
                } catch (Exception e) {
                    log.error("{}: Error modifying XDS Registry: {}", this, e);
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
}
