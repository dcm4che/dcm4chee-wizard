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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.wizard.war.configuration.simple.edit;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.TransferCapability;
import org.dcm4che.net.TransferCapability.Role;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ModalWindowRuntimeException;
import org.dcm4chee.wizard.war.common.component.DicomConfigurationWebPage;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.ApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.StringArrayModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.TransferCapabilityModel;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeNode;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.war.configuration.simple.validator.SOPClassValidator;
import org.dcm4chee.wizard.war.configuration.simple.validator.TransferCapabilityValidator;
import org.dcm4chee.wizard.war.configuration.simple.validator.TransferSyntaxValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditTransferCapabilityPage extends DicomConfigurationWebPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditTransferCapabilityPage.class);

    // mandatory
	private Model<String> sopClassModel;
    private Model<Role> roleModel;
	private StringArrayModel transferSyntaxModel;
	
	// optional
    private Model<String> commonNameModel;
    
    public CreateOrEditTransferCapabilityPage(final ModalWindow window, final TransferCapabilityModel transferCapabilityModel, 
    		final ConfigTreeNode aeNode) {
    	super();

		try {
			final ApplicationEntity applicationEntity = 
					((ApplicationEntityModel) aeNode.getModel()).getApplicationEntity();
    	
        add(new WebMarkupContainer("create-transferCapability-title").setVisible(transferCapabilityModel == null));
        add(new WebMarkupContainer("edit-transferCapability-title").setVisible(transferCapabilityModel != null));

        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");
        form.setResourceIdPrefix("dicom.edit.transferCapability.");
        add(form);

        if (transferCapabilityModel == null) {
        	sopClassModel = Model.of();
        	roleModel = Model.of(org.dcm4che.net.TransferCapability.Role.SCP);
        	transferSyntaxModel = new StringArrayModel(null);
        	commonNameModel = Model.of();
        } else {
        	TransferCapability transferCapability = transferCapabilityModel.getTransferCapability();
        	sopClassModel = Model.of(transferCapability.getSopClass());
        	roleModel = Model.of(transferCapability.getRole());
        	transferSyntaxModel = new StringArrayModel(transferCapability.getTransferSyntaxes());
        	commonNameModel = Model.of(transferCapability.getCommonName());
        }
        
        form.add(new Label("sopClass.label", new ResourceModel("dicom.edit.transferCapability.sopClass.label")));
        TextField<String> sopClassTextField = new TextField<String>("sopClass", sopClassModel);
        sopClassTextField.setRequired(true);
        sopClassTextField.add(new SOPClassValidator());
        form.add(sopClassTextField);

        form.add(new Label("role.label", new ResourceModel("dicom.edit.transferCapability.role.label")));
        ArrayList<org.dcm4che.net.TransferCapability.Role> transferSyntaxList = 
        		new ArrayList<org.dcm4che.net.TransferCapability.Role>();
        transferSyntaxList.add(org.dcm4che.net.TransferCapability.Role.SCP); 
        transferSyntaxList.add(org.dcm4che.net.TransferCapability.Role.SCU);
        DropDownChoice<org.dcm4che.net.TransferCapability.Role> roleDropDown = 
        		new DropDownChoice<org.dcm4che.net.TransferCapability.Role>("role", roleModel, transferSyntaxList);
        form.add(roleDropDown
        		.setNullValid(false));

        form.add(new Label("transferSyntax.label", new ResourceModel("dicom.edit.transferCapability.transferSyntax.label")))
        .add(new TextArea<String>("transferSyntax", transferSyntaxModel)
        		.add(new TransferSyntaxValidator())
        		.setRequired(true));

        TextField<String> commonNameTextField = new TextField<String>("commonName", commonNameModel);
        
		form
			.add(new TransferCapabilityValidator(
					applicationEntity, 
					sopClassTextField, roleDropDown, commonNameTextField));

        final WebMarkupContainer optionalContainer = new WebMarkupContainer("optional");
        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));

        optionalContainer.add(new Label("commonName.label", new ResourceModel("dicom.edit.transferCapability.optional.commonName.label")))
        .add(commonNameTextField);

        form.add(new Label("toggleOptional.label", new ResourceModel("dicom.edit.toggleOptional.label")))
        .add(new AjaxCheckBox("toggleOptional", new Model<Boolean>()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(optionalContainer.setVisible(this.getModelObject()));
			}
        });

        form.add(new IndicatingAjaxButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
					TransferCapability transferCapability = transferCapabilityModel == null ? 
                    		new TransferCapability(commonNameModel.getObject(), 
                    				sopClassModel.getObject(),
                    				roleModel.getObject(), 
                    				transferSyntaxModel.getArray()) : 
                    			transferCapabilityModel.getTransferCapability();
                    
                    if (transferCapabilityModel != null) {
                    	applicationEntity.removeTransferCapabilityFor(
                    			transferCapability.getSopClass(),
                    			transferCapability.getRole());
                		transferCapability.setSopClass(sopClassModel.getObject());
                		transferCapability.setRole(roleModel.getObject());
                		transferCapability.setTransferSyntaxes(transferSyntaxModel.getArray());
                		transferCapability.setCommonName(commonNameModel.getObject());                   	
                    }
                    applicationEntity.addTransferCapability(transferCapability);

                    ConfigTreeProvider.get().mergeDevice(applicationEntity.getDevice());
                    window.close(target);
                } catch (Exception e) {
        			log.error(this.getClass().toString() + ": " + "Error modifying transfer capability: " + e.getMessage());
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
			}}.setDefaultFormProcessing(false));
		} catch (ConfigurationException e) {
			log.error("Error creating TransferCapabilityValidator for sopClass TextField", e);
		}
    }
 }
