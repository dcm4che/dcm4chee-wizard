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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.dcm4che.conf.api.AttributeCoercion;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.TransferCapability.Role;
import org.dcm4chee.proxy.conf.ProxyApplicationEntity;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.markup.modal.MessageWindow;
import org.dcm4chee.wizard.war.common.SimpleBaseForm;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.CoercionModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.ProxyApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeNode;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.war.configuration.simple.validator.SOPClassValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.security.components.SecureWebPage;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditCoercionPage extends SecureWebPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditCoercionPage.class);

    private static final ResourceReference BaseCSS = new CssResourceReference(BaseWicketPage.class, "base-style.css");
    
    private MessageWindow msgWin = new MessageWindow("msgWin");
    
    // mandatory
    private Model<Dimse> dimseModel;
    private Model<Role> transferRoleModel;
    private Model<String> labeledURIModel;
    
    // optional
    private Model<String> aeTitleModel;
    private Model<String> sopClassModel;
    
    public CreateOrEditCoercionPage(final ModalWindow window, final CoercionModel coercionModel, 
    		final ConfigTreeNode aeNode) {
    	super();

        msgWin.setTitle("");
        add(msgWin);
        add(new WebMarkupContainer("create-coercion-title").setVisible(coercionModel == null));
        add(new WebMarkupContainer("edit-coercion-title").setVisible(coercionModel != null));

        setOutputMarkupId(true);
        final SimpleBaseForm form = new SimpleBaseForm("form");
        form.setResourceIdPrefix("dicom.edit.coercion.");
        add(form);

        if (coercionModel == null) {
            dimseModel = Model.of(org.dcm4che.net.Dimse.C_STORE_RQ);
            transferRoleModel = Model.of(org.dcm4che.net.TransferCapability.Role.SCP);
            labeledURIModel = Model.of();
        	aeTitleModel = Model.of();
        	sopClassModel = Model.of();
        } else {
        	AttributeCoercion coercion = coercionModel.getCoercion();
        	dimseModel = Model.of(coercion.getDimse());
        	transferRoleModel = Model.of(coercion.getRole());
        	labeledURIModel = Model.of(coercion.getURI());
        	aeTitleModel = Model.of(coercion.getAETitle());
			sopClassModel = Model.of(coercion.getSopClass());
        }
        
        form.add(new Label("dimse.label", new ResourceModel("dicom.edit.coercion.dimse.label")));
        ArrayList<Dimse> dimseList = 
        		new ArrayList<Dimse>();
        dimseList.addAll(Arrays.asList(Dimse.values())); 
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

        final WebMarkupContainer optionalContainer = new WebMarkupContainer("optionalContainer");
        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));

        List<String> uniqueAETitles = null;
		try {
			uniqueAETitles = Arrays.asList(ConfigTreeProvider.get().getUniqueAETitles());
		} catch (ConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        Collections.sort(uniqueAETitles);
        
        optionalContainer.add(new Label("aeTitle.label", new ResourceModel("dicom.edit.coercion.aeTitle.label")))
        .add(new DropDownChoice<String>("aeTitle", aeTitleModel, uniqueAETitles));

        optionalContainer.add(new Label("sopClass.label", new ResourceModel("dicom.edit.forwardRule.sopClass.label")))
        .add(new TextField<String>("sopClass", sopClassModel)
        		.add(new SOPClassValidator()));

        form.add(new Label("optional.label", new ResourceModel("dicom.edit.optional.label")))
        .add(new AjaxCheckBox("optional", new Model<Boolean>()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(optionalContainer.setVisible(this.getModelObject()));
			}
        });

        form.add(new AjaxFallbackButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                	AttributeCoercion coercion = 
                			new AttributeCoercion(sopClassModel.getObject(), 
                					dimseModel.getObject(), 
                					transferRoleModel.getObject(), 
                					aeTitleModel.getObject(), 
                					labeledURIModel.getObject());

                	ProxyApplicationEntity proxyApplicationEntity = 
                			((ProxyApplicationEntityModel) aeNode.getModel()).getApplicationEntity();

            		if (coercionModel != null)
            			proxyApplicationEntity.getAttributeCoercions()
            				.remove(coercionModel.getCoercion());
            		proxyApplicationEntity.getAttributeCoercions().add(coercion);

            		ConfigTreeProvider.get().mergeDevice(proxyApplicationEntity.getDevice());
                    window.close(target);
                } catch (Exception e) {
                	log.error("Error modifying coercion", e);
                    msgWin.show(target, new ResourceModel(coercionModel == null ? 
                    		"dicom.edit.coercion.create.failed" : "dicom.edit.coercion.update.failed")
                    		.wrapOnAssignment(this));
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
        if (CreateOrEditCoercionPage.BaseCSS != null)
        	response.renderCSSReference(CreateOrEditCoercionPage.BaseCSS);
    }
}
