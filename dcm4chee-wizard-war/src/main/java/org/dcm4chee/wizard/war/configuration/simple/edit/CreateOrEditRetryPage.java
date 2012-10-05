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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.dcm4chee.proxy.conf.ProxyApplicationEntity;
import org.dcm4chee.proxy.conf.Retry;
import org.dcm4chee.proxy.conf.RetryObject;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.modal.MessageWindow;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.ProxyApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.RetryModel;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeNode;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.security.components.SecureWebPage;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditRetryPage extends SecureWebPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditRetryPage.class);

    private static final ResourceReference BaseCSS = new CssResourceReference(BaseWicketPage.class, "base-style.css");
    
    private MessageWindow msgWin = new MessageWindow("msgWin");
    
    // mandatory
	private IModel<RetryObject> suffixModel;
    private Model<Integer> delayModel;
	private Model<Integer> retriesModel;
    
    public CreateOrEditRetryPage(final ModalWindow window, final RetryModel retryModel, 
    		final ConfigTreeNode aeNode) {
    	super();

    	final ProxyApplicationEntity proxyApplicationEntity = 
    			((ProxyApplicationEntityModel) aeNode.getModel()).getApplicationEntity();

        msgWin.setTitle("");
        add(msgWin);
        add(new WebMarkupContainer("create-retry-title").setVisible(retryModel == null));
        add(new WebMarkupContainer("edit-retry-title").setVisible(retryModel != null));

        setOutputMarkupId(true);
        final BaseForm form = new BaseForm("form");
        form.setResourceIdPrefix("dicom.edit.retry.");
        add(form);

        if (retryModel == null) {
    		suffixModel = Model.of(RetryObject.AAssociateRJ);
    		delayModel = Model.of();
    		retriesModel = Model.of();
        } else {
			suffixModel = Model.of(retryModel.getRetry().getRetryObject());
			delayModel = Model.of(retryModel.getRetry().getDelay());
			retriesModel = Model.of(retryModel.getRetry().getNumberOfRetries());
        }

        form.add(new Label("suffix.label", new ResourceModel("dicom.edit.retry.suffix.label")));
        final ArrayList<RetryObject> suffixValueList = new ArrayList<RetryObject>();
        ArrayList<String> suffixDisplayList = new ArrayList<String>();
        for (RetryObject suffix : RetryObject.values()) {
        	suffixValueList.add(suffix);
        	suffixDisplayList.add(suffix.getRetryNote());
        }       	
		DropDownChoice<RetryObject> suffixDropDown = 
        		new DropDownChoice<RetryObject>("suffix", suffixModel, suffixValueList, new IChoiceRenderer<Object>() {
 
					private static final long serialVersionUID = 1L;

					public String getDisplayValue(Object object) {
						return ((RetryObject) object).getRetryNote();
					}

					public String getIdValue(Object object, int index) {
						return object.toString();
					}
        		});
        form.add(suffixDropDown
        		.setNullValid(false));

        form.add(new Label("delay.label", new ResourceModel("dicom.edit.retry.delay.label")))
        .add(new TextField<Integer>("delay", delayModel)
        		.setType(Integer.class)
        		.setRequired(true)
        		.add(new AttributeModifier("title", new ResourceModel("dicom.edit.retry.delay.tooltip"))));

        form.add(new Label("retries.label", new ResourceModel("dicom.edit.retry.retries.label")))
        .add(new TextField<Integer>("retries", retriesModel)
        		.setType(Integer.class).setRequired(true));

        form.add(new AjaxFallbackButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                	Retry retry = new Retry(
                			suffixModel.getObject(),
                			delayModel.getObject(),
                			retriesModel.getObject());

            		if (retryModel != null)
            			proxyApplicationEntity.getRetries().remove(retryModel.getRetry());
            		proxyApplicationEntity.getRetries().add(retry);

            		ConfigTreeProvider.get().mergeDevice(proxyApplicationEntity.getDevice());
            		aeNode.getAncestor(2).setModel(null);

                    window.close(target);
                } catch (Exception e) {
                	log.error("Error modifying forward schedule", e);
                    msgWin.show(target, new ResourceModel(retryModel == null ? 
                    		"dicom.edit.retry.create.failed" : "dicom.edit.retry.update.failed")
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
        if (CreateOrEditRetryPage.BaseCSS != null)
        	response.renderCSSReference(CreateOrEditRetryPage.BaseCSS);
    }
 }
