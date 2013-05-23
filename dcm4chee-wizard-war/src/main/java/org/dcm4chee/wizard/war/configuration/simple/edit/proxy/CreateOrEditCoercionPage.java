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

package org.dcm4chee.wizard.war.configuration.simple.edit.proxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.dcm4che.conf.api.AttributeCoercion;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.TransferCapability.Role;
import org.dcm4chee.proxy.conf.ProxyAEExtension;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.MainWebPage;
import org.dcm4chee.wizard.common.component.ModalWindowRuntimeException;
import org.dcm4chee.wizard.common.component.secure.SecureSessionCheckPage;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.ApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.StringArrayModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.CoercionModel;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeNode;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.war.configuration.simple.validator.CoercionValidator;
import org.dcm4chee.wizard.war.configuration.simple.validator.SOPClassValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditCoercionPage extends SecureSessionCheckPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditCoercionPage.class);

    private static final ResourceReference baseCSS = new CssResourceReference(MainWebPage.class, "base-style.css");
    
    // mandatory
    private Model<String> commonNameModel;
    private Model<Dimse> dimseModel;
    private Model<Role> transferRoleModel;
    private Model<String> labeledURIModel;
    
    // optional
    private Model<String> aeTitleModel;
    private StringArrayModel sopClassesModel;
    
    public CreateOrEditCoercionPage(final ModalWindow window, final CoercionModel coercionModel, 
    		final ConfigTreeNode aeNode) {
    	super();

    	ApplicationEntity applicationEntity = null;
		try {
			applicationEntity = ((ApplicationEntityModel) aeNode.getModel()).getApplicationEntity();
		} catch (ConfigurationException ce) {
			log.error(this.getClass().toString() + ": " + "Error modifying retry: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new ModalWindowRuntimeException(ce.getLocalizedMessage());
		}

        add(new WebMarkupContainer("create-coercion-title").setVisible(coercionModel == null));
        add(new WebMarkupContainer("edit-coercion-title").setVisible(coercionModel != null));

        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");
        form.setResourceIdPrefix("dicom.edit.coercion.");
        add(form);

    	aeTitleModel = Model.of();
        if (coercionModel == null) {
        	commonNameModel = Model.of();
            dimseModel = Model.of(org.dcm4che.net.Dimse.C_STORE_RQ);
            transferRoleModel = Model.of(org.dcm4che.net.TransferCapability.Role.SCP);
            labeledURIModel = Model.of();
        	sopClassesModel = new StringArrayModel(null);
        } else {
        	AttributeCoercion coercion = coercionModel.getCoercion();
        	commonNameModel = Model.of(coercion.getCommonName());
        	dimseModel = Model.of(coercion.getDIMSE());
        	transferRoleModel = Model.of(coercion.getRole());
        	labeledURIModel = Model.of(coercion.getURI());
        	if (coercion.getAETitles() != null && coercion.getAETitles().length > 0)
        		aeTitleModel = Model.of(coercion.getAETitles()[0]);
			sopClassesModel = new StringArrayModel(coercion.getSOPClasses());
        }
        
		form.add(new Label("commonName.label", new ResourceModel("dicom.edit.coercion.commonName.label")))
		.add(new TextField<String>("commonName", commonNameModel).setRequired(true)
				.add(new CoercionValidator(commonNameModel.getObject(), applicationEntity)));

        form.add(new Label("dimse.label", new ResourceModel("dicom.edit.coercion.dimse.label")));
        ArrayList<Dimse> dimseList = 
        		new ArrayList<Dimse>();
        dimseList.add(Dimse.C_STORE_RQ);
        dimseList.add(Dimse.C_GET_RQ);
        dimseList.add(Dimse.C_MOVE_RQ);
        dimseList.add(Dimse.C_FIND_RQ);
        DropDownChoice<Dimse> dimseDropDown = 
        		new DropDownChoice<Dimse>("dimse", dimseModel, dimseList);
        form.add(dimseDropDown
        		.setNullValid(false));

        form.add(new Label("transferRole.label", new ResourceModel("dicom.edit.coercion.transferRole.label")));
        ArrayList<org.dcm4che.net.TransferCapability.Role> transferSyntaxList = 
        		new ArrayList<org.dcm4che.net.TransferCapability.Role>();
        transferSyntaxList.add(org.dcm4che.net.TransferCapability.Role.SCP); 
        transferSyntaxList.add(org.dcm4che.net.TransferCapability.Role.SCU);
        DropDownChoice<org.dcm4che.net.TransferCapability.Role> roleDropDown = 
        		new DropDownChoice<org.dcm4che.net.TransferCapability.Role>("transferRole", transferRoleModel, transferSyntaxList);
        form.add(roleDropDown
        		.setNullValid(false));

        form.add(new Label("labeledURI.label", new ResourceModel("dicom.edit.coercion.labeledURI.label")))
        .add(new TextField<String>("labeledURI", labeledURIModel)
        		.setRequired(true));

        final WebMarkupContainer optionalContainer = new WebMarkupContainer("optional");
        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));

        List<String> uniqueAETitles = null;
		try {
			uniqueAETitles = Arrays.asList(ConfigTreeProvider.get().getUniqueAETitles());
		} catch (ConfigurationException ce) {
			log.error(this.getClass().toString() + ": " + "Error retrieving unique ae titles: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new ModalWindowRuntimeException(ce.getLocalizedMessage());
		}
        Collections.sort(uniqueAETitles);
        
		final DropDownChoice<String> aeTitleDropDownChoice = 
				new DropDownChoice<String>("aeTitle", aeTitleModel, uniqueAETitles);
		optionalContainer.add(new Label("aeTitle.label", new ResourceModel("dicom.edit.coercion.optional.aeTitle.label")))
        .add(aeTitleDropDownChoice
        		.setNullValid(true).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));

		final TextField<String> aeTitleTextField = 
				new TextField<String>("aeTitle.freetext", aeTitleModel);
		optionalContainer.add(aeTitleTextField
				.setVisible(false).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));

	    final Model<Boolean> toggleAETitleModel = Model.of(false);
		if (coercionModel != null && aeTitleModel.getObject() != null 
				&& !uniqueAETitles.contains(aeTitleModel.getObject())) {
			toggleAETitleModel.setObject(true);
			aeTitleTextField.setVisible(true);
			aeTitleDropDownChoice.setVisible(false);
		}

        optionalContainer.add(new AjaxCheckBox("toggleAETitle", toggleAETitleModel) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				aeTitleDropDownChoice.setVisible(!toggleAETitleModel.getObject());
				aeTitleTextField.setVisible(toggleAETitleModel.getObject());
				target.add(aeTitleDropDownChoice);
				target.add(aeTitleTextField);
			}
        });

        optionalContainer.add(new Label("sopClasses.label", new ResourceModel("dicom.edit.coercion.optional.sopClasses.label")))
        .add(new TextArea<String>("sopClasses", sopClassesModel)
        		.add(new SOPClassValidator()));

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
                	AttributeCoercion coercion = 
                			new AttributeCoercion(commonNameModel.getObject(), 
                					sopClassesModel.getArray(), 
                					dimseModel.getObject(), 
                					transferRoleModel.getObject(), 
                					aeTitleModel.getObject() == null ? 
                							null : new String[] {aeTitleModel.getObject()}, 
                					labeledURIModel.getObject());

                	ProxyAEExtension proxyAEExtension = 
                			((ApplicationEntityModel) aeNode.getModel()).getApplicationEntity()
                			.getAEExtension(ProxyAEExtension.class);

            		if (coercionModel != null)
            			proxyAEExtension.getAttributeCoercions()
            				.remove(coercionModel.getCoercion());
            		proxyAEExtension.getAttributeCoercions().add(coercion);

            		ConfigTreeProvider.get().mergeDevice(
            				((ApplicationEntityModel) aeNode.getModel()).getApplicationEntity().getDevice());
                    window.close(target);
                } catch (Exception e) {
        			log.error(this.getClass().toString() + ": " + "Error modifying coercion: " + e.getMessage());
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
    }

	@Override
    public void renderHead(IHeaderResponse response) {
		if (CreateOrEditCoercionPage.baseCSS != null) 
			response.render(CssHeaderItem.forReference(CreateOrEditCoercionPage.baseCSS));
    }
}
