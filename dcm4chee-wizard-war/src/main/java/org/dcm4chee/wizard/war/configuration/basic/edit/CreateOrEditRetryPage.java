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

package org.dcm4chee.wizard.war.configuration.basic.edit;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.dcm4chee.proxy.conf.Retry;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.modal.MessageWindow;
import org.dcm4chee.wizard.war.configuration.basic.model.ProxyApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.basic.model.RetryModel;
import org.dcm4chee.wizard.war.configuration.basic.tree.TreeNode;
import org.dcm4chee.wizard.war.configuration.basic.tree.DeviceTreeProvider;
import org.dcm4chee.wizard.war.configuration.basic.validator.RetrySuffixValidator;
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
	private Model<String> suffixModel;
    private Model<Integer> delayModel;
	private Model<Integer> retriesModel;
    
    public CreateOrEditRetryPage(final ModalWindow window, final RetryModel retryModel, 
    		final TreeNode rtsNode, final TreeNode rtNode) {
    	super();

        msgWin.setTitle("");
        add(msgWin);
        add(new WebMarkupContainer("create-retry-title").setVisible(retryModel == null));
        add(new WebMarkupContainer("edit-retry-title").setVisible(retryModel != null));

        setOutputMarkupId(true);
        final BaseForm form = new BaseForm("form");
        form.setResourceIdPrefix("dicom.edit.retry.");
        add(form);

		suffixModel = Model.of(retryModel != null ? retryModel.getRetry().getSuffix() : null);
		delayModel = Model.of(retryModel != null ? retryModel.getRetry().getDelay() : null);
		retriesModel = Model.of(retryModel != null ? retryModel.getRetry().getNumberOfRetries() : null);

        form.add(new Label("suffix.label", new ResourceModel("dicom.edit.retry.suffix.label")))
        .add(new TextField<String>("suffix", suffixModel).setRequired(true)
        		.add(new RetrySuffixValidator(
        				suffixModel.getObject(), 
        				((ProxyApplicationEntityModel) rtsNode.getParent().getModel()).getApplicationEntity().getRetries())));
        
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

                    if (retryModel == null)
                    	DeviceTreeProvider.get().addRetry(rtsNode, retry);
                    else 
                    	DeviceTreeProvider.get().editRetry(rtsNode, rtNode, retry);

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
