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
import java.util.Map;

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
import org.apache.wicket.validation.IValidator;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.net.Device;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ModalWindowRuntimeException;
import org.dcm4chee.wizard.common.component.secure.SecureSessionCheckPage;
import org.dcm4chee.wizard.model.DeviceModel;
import org.dcm4chee.wizard.model.GenericConfigNodeModel;
import org.dcm4chee.wizard.model.StringArrayModel;
import org.dcm4chee.wizard.model.xds.XDSRepositoryModel;
import org.dcm4chee.wizard.tree.ConfigTreeNode;
import org.dcm4chee.wizard.tree.ConfigTreeProvider;
import org.dcm4chee.xds2.conf.XCAiInitiatingGWCfg;
import org.dcm4chee.xds2.conf.XdsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class XDSRepositoryEditPage extends SecureSessionCheckPage{

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(XDSRepositoryEditPage.class);

    private List<Boolean> booleanChoice = Arrays.asList(new Boolean[] { true, false });

    // mandatory
    private Model<String> xdsApplicationNameModel;
    private Model<String> xdsRepositoryUIDModel;
    private Model<String> xdsRetrieveURLModel;
    private Model<String> xdsProvideURLModel;
    private Model<Boolean> xdsDeactivatedModel;
    // optional
    private StringArrayModel xdsAcceptedMimeTypesModel;
    private Model<Boolean> xdsCheckMimetypeModel;
    private Model<String> xdsSoapMsgLogDirModel;
    private Model<String> xdsAllowedCipherHostnameModel;
    private StringArrayModel xdsLogFullMessageHostsModel;
    private GenericConfigNodeModel<XdsRepository> xdsSources;
    private GenericConfigNodeModel<XdsRepository> xdsFileSystemGroupIDs;


    public XDSRepositoryEditPage(final ModalWindow window, XDSRepositoryModel model,
            final ConfigTreeNode deviceNode) {
        super();
        try {
            add(new WebMarkupContainer("edit-xdsrepository-title").setVisible(model != null));
            setOutputMarkupId(true);
            final ExtendedForm form = new ExtendedForm("form");
            form.setResourceIdPrefix("dicom.edit.xds.");
            Device device = ((DeviceModel) deviceNode.getModel()).getDevice();
            initAttributes(device.getDeviceExtension(XdsRepository.class));
            addMandatoryFormAttributes(form);
            addOptionalFormAttributes(form);
            addSaveButton(window, deviceNode, form);
            addCancelButton(window, form);
            add(form);
        } catch (ConfigurationException e) {
            log.error("{}: Error modifying XDS Repository: {}", this, e);
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

        optionalContainer.add(new Label("xdsCheckMimetype.label", new ResourceModel(
                "dicom.edit.xds.optional.xdsCheckMimetype.label")).setOutputMarkupPlaceholderTag(true));
        optionalContainer.add(new DropDownChoice<>("xdsCheckMimetype", xdsCheckMimetypeModel, booleanChoice)
                .setNullValid(false));

        optionalContainer.add(new Label("xdsSoapMsgLogDir.label", new ResourceModel(
                "dicom.edit.xds.optional.xdsSoapMsgLogDir.label")).setOutputMarkupPlaceholderTag(true));
        optionalContainer.add(new TextField<String>("xdsSoapMsgLogDir", xdsSoapMsgLogDirModel).setType(String.class));

        optionalContainer.add(new Label("xdsAllowedCipherHostname.label", new ResourceModel(
                "dicom.edit.xds.optional.xdsAllowedCipherHostname.label")).setOutputMarkupPlaceholderTag(true));
        optionalContainer.add(new TextField<String>("xdsAllowedCipherHostname", xdsAllowedCipherHostnameModel)
                .setType(String.class));

        optionalContainer.add(new Label("xdsLogFullMessageHosts.label", new ResourceModel(
                "dicom.edit.xds.optional.xdsLogFullMessageHosts.label")).setOutputMarkupPlaceholderTag(true));
        optionalContainer.add(new TextArea<String>("xdsLogFullMessageHosts", xdsLogFullMessageHostsModel)
                .setType(String.class));
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

        form.addComponent(
                new Label("xdsDeactivated.label",
                        new ResourceModel("dicom.edit.xds.xdsDeactivated.label"))
                        .setOutputMarkupPlaceholderTag(true));
        form.add(
                new DropDownChoice<>("xdsDeactivated", xdsDeactivatedModel, booleanChoice).setNullValid(false));

        Label xdsRepositoryUIDLabel = new Label("xdsRepositoryUID.label", new ResourceModel(
                "dicom.edit.xds.xdsRepositoryUID.label"));
        form.add(xdsRepositoryUIDLabel);
        FormComponent<String> xdsRepositoryUIDTextField = new TextField<String>("xdsRepositoryUID",
                xdsRepositoryUIDModel);
        xdsRepositoryUIDTextField.setType(String.class);
        xdsRepositoryUIDTextField.setRequired(true);
        form.add(xdsRepositoryUIDTextField);
        
        form.add(new Label("xdsRetrieveURL.label", new ResourceModel("dicom.edit.xds.xdsRetrieveURL.label")));
        FormComponent<String> xdsRetrieveURLField = new TextField<String>("xdsRetrieveURL",
                xdsRetrieveURLModel);
        xdsRetrieveURLField.setType(String.class);
        xdsRetrieveURLField.setRequired(true);
        form.add(xdsRetrieveURLField);

        form.add(new Label("xdsProvideURL.label", new ResourceModel("dicom.edit.xds.xdsProvideURL.label")));
        FormComponent<String> xdsProvideURLField = new TextField<String>("xdsProvideURL",
                xdsProvideURLModel);
        xdsProvideURLField.setType(String.class);
        xdsProvideURLField.setRequired(true);
        form.add(xdsProvideURLField);        
        
        form.add(new Label("xdsSources.label", new ResourceModel("dicom.edit.xds.xdsSources.label")));
        FormComponent<String> sources = new TextArea<String>("xdsSources",
                xdsSources);
        sources.setType(String.class);
        sources.setRequired(true);
        sources.add((IValidator<String>) xdsSources);
        form.add(sources);

        form.add(new Label("xdsFileSystemGroupIds.label", new ResourceModel("dicom.edit.xds.xdsFileSystemGroupIds.label")));
        FormComponent<String> fileGroups = new TextArea<String>("xdsFileSystemGroupIds",
                xdsFileSystemGroupIDs);
        fileGroups.setType(String.class);
        fileGroups.setRequired(true);
        fileGroups.add((IValidator<String>) xdsFileSystemGroupIDs);
        form.add(fileGroups);
    }

    private void initAttributes(XdsRepository xds) {
        if (xds == null) {
            xdsApplicationNameModel = Model.of();
            xdsRepositoryUIDModel = Model.of();
            xdsAcceptedMimeTypesModel = new StringArrayModel(null);
            xdsCheckMimetypeModel = Model.of();
            xdsSoapMsgLogDirModel = Model.of();
            xdsAllowedCipherHostnameModel = Model.of();
            xdsLogFullMessageHostsModel = new StringArrayModel(null);
            xdsRetrieveURLModel = Model.of();
            xdsProvideURLModel = Model.of();
            xdsSources = new GenericConfigNodeModel<XdsRepository>(new XdsRepository(), "xdsSource", Map.class);
            xdsFileSystemGroupIDs = new GenericConfigNodeModel<XdsRepository>(new XdsRepository(), "xdsFileSystemGroupID", Map.class);
            xdsDeactivatedModel = Model.of();
        } else {
            xdsApplicationNameModel = Model.of(xds.getApplicationName());
            xdsRepositoryUIDModel = Model.of(xds.getRepositoryUID());
            xdsAcceptedMimeTypesModel = new StringArrayModel(xds.getAcceptedMimeTypes());
            xdsCheckMimetypeModel = Model.of(xds.isCheckMimetype());
            xdsSoapMsgLogDirModel = Model.of(xds.getSoapLogDir());
            xdsAllowedCipherHostnameModel = Model.of(xds.getAllowedCipherHostname());
            xdsLogFullMessageHostsModel = new StringArrayModel(xds.getLogFullMessageHosts());
            xdsRetrieveURLModel = Model.of(xds.getRetrieveUrl());
            xdsProvideURLModel = Model.of(xds.getProvideUrl());
            xdsSources = new GenericConfigNodeModel<XdsRepository>(xds, "xdsSource", Map.class);
            xdsFileSystemGroupIDs = new GenericConfigNodeModel<XdsRepository>(xds, "xdsFileSystemGroupID", Map.class);
            xdsDeactivatedModel = Model.of(xds.isDeactivated());
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
                    XdsRepository xds = device.getDeviceExtension(XdsRepository.class);
                    // mandatory
                    xds.setApplicationName(xdsApplicationNameModel.getObject());
                    xds.setRepositoryUID(xdsRepositoryUIDModel.getObject());
                    xds.setProvideUrl(xdsProvideURLModel.getObject());
                    xds.setRetrieveUrl(xdsRetrieveURLModel.getObject());
                    xds.setDeactivated(xdsDeactivatedModel.getObject());

                    try {
                        xds.setSrcDevicebySrcIdMap(xdsSources.getModifiedConfigObj().getSrcDevicebySrcIdMap());
                    } catch (NullPointerException e) {
                        // thats fine, this means nothing has changed
                    }

                    try {
                        xds.setFsGroupIDbyAffinity(xdsFileSystemGroupIDs.getModifiedConfigObj().getFsGroupIDbyAffinity());
                    } catch (NullPointerException e) {
                        // thats fine, this means nothing has changed
                    }

                    // optional
                    if (xdsAcceptedMimeTypesModel.getArray().length > 0)
                        xds.setAcceptedMimeTypes(xdsAcceptedMimeTypesModel.getArray());
                    if (xdsCheckMimetypeModel.getObject() != null)
                        xds.setCheckMimetype(xdsCheckMimetypeModel.getObject());
                    xds.setSoapLogDir(xdsSoapMsgLogDirModel.getObject());
                    xds.setAllowedCipherHostname(xdsAllowedCipherHostnameModel.getObject());
                    if (xdsLogFullMessageHostsModel.getArray().length > 0)
                        xds.setLogFullMessageHosts(xdsLogFullMessageHostsModel.getArray());
                    ConfigTreeProvider.get().mergeDevice(device);
                    window.close(target);
                } catch (Exception e) {
                    log.error("{}: Error modifying XDS Repository: {}", this, e);
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
