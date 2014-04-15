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

package org.dcm4chee.wizard.edit.proxy;

import java.util.ArrayList;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4chee.proxy.common.RetryObject;
import org.dcm4chee.proxy.conf.ProxyAEExtension;
import org.dcm4chee.proxy.conf.Retry;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ModalWindowRuntimeException;
import org.dcm4chee.wizard.common.component.secure.SecureSessionCheckPage;
import org.dcm4chee.wizard.model.ApplicationEntityModel;
import org.dcm4chee.wizard.model.proxy.RetryModel;
import org.dcm4chee.wizard.model.proxy.TimeIntervalModel;
import org.dcm4chee.wizard.tree.ConfigTreeNode;
import org.dcm4chee.wizard.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.validator.RetrySuffixValidator;
import org.dcm4chee.wizard.validator.TimeIntervalValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditRetryPage extends SecureSessionCheckPage {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditRetryPage.class);

    // mandatory
    private IModel<RetryObject> suffixModel;
    private TimeIntervalModel delayModel;
    private Model<Integer> retriesModel;
    private Model<Boolean> deleteAfterFinalRetryModel;

    public CreateOrEditRetryPage(final ModalWindow window, final RetryModel retryModel, final ConfigTreeNode aeNode) {
        super();

        ProxyAEExtension proxyAEExtension = null;
        try {
            proxyAEExtension = ((ApplicationEntityModel) aeNode.getModel()).getApplicationEntity().getAEExtension(
                    ProxyAEExtension.class);
        } catch (ConfigurationException ce) {
            log.error(this.getClass().toString() + ": " + "Error modifying retry: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new ModalWindowRuntimeException(ce.getLocalizedMessage());
        }

        add(new WebMarkupContainer("create-retry-title").setVisible(retryModel == null));
        add(new WebMarkupContainer("edit-retry-title").setVisible(retryModel != null));

        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");
        form.setResourceIdPrefix("dicom.edit.retry.");
        add(form);

        if (retryModel == null) {
            suffixModel = Model.of(RetryObject.AAssociateRJ);
            delayModel = new TimeIntervalModel(Retry.DEFAULT_DELAY);
            retriesModel = Model.of(Retry.DEFAULT_RETRIES);
            deleteAfterFinalRetryModel = Model.of(false);
        } else {
            suffixModel = Model.of(retryModel.getRetry().getRetryObject());
            delayModel = new TimeIntervalModel(retryModel.getRetry().getDelay());
            retriesModel = Model.of(retryModel.getRetry().getNumberOfRetries());
            deleteAfterFinalRetryModel = Model.of(retryModel.getRetry().isDeleteAfterFinalRetry());
        }

        form.add(new Label("suffix.label", new ResourceModel("dicom.edit.retry.suffix.label")));
        final ArrayList<RetryObject> suffixValueList = new ArrayList<RetryObject>();
        ArrayList<String> suffixDisplayList = new ArrayList<String>();
        for (RetryObject suffix : RetryObject.values()) {
            suffixValueList.add(suffix);
            suffixDisplayList.add(suffix.getRetryNote());
        }
        DropDownChoice<RetryObject> suffixDropDown = new DropDownChoice<RetryObject>("suffix", suffixModel,
                suffixValueList, new IChoiceRenderer<Object>() {

                    private static final long serialVersionUID = 1L;

                    public String getDisplayValue(Object object) {
                        return ((RetryObject) object).getRetryNote();
                    }

                    public String getIdValue(Object object, int index) {
                        return object.toString();
                    }
                });
        form.add(suffixDropDown.setNullValid(false).add(
                new RetrySuffixValidator(
                        retryModel == null ? null : retryModel.getRetry().getRetryObject().getSuffix(),
                        proxyAEExtension.getRetries())));

        form.add(new Label("delay.label", new ResourceModel("dicom.edit.retry.delay.label"))).add(
                new TextField<String>("delay", delayModel).setRequired(true).add(new TimeIntervalValidator())
                        .add(new AttributeModifier("title", new ResourceModel("dicom.edit.retry.delay.tooltip"))));

        form.add(new Label("retries.label", new ResourceModel("dicom.edit.retry.retries.label"))).add(
                new TextField<Integer>("retries", retriesModel).setType(Integer.class).setRequired(true));

        form.add(
                new Label("deleteAfterFinalRetry.label", new ResourceModel(
                        "dicom.edit.retry.deleteAfterFinalRetry.label"))).add(
                new CheckBox("deleteAfterFinalRetry", deleteAfterFinalRetryModel));

        form.add(new IndicatingAjaxButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    Retry retry = new Retry(suffixModel.getObject(), delayModel.getSeconds(), retriesModel.getObject(),
                            deleteAfterFinalRetryModel.getObject());

                    ProxyAEExtension proxyAEExtension = ((ApplicationEntityModel) aeNode.getModel())
                            .getApplicationEntity().getAEExtension(ProxyAEExtension.class);

                    if (retryModel != null)
                        proxyAEExtension.getRetries().remove(retryModel.getRetry());
                    proxyAEExtension.getRetries().add(retry);

                    ConfigTreeProvider.get().mergeDevice(
                            ((ApplicationEntityModel) aeNode.getModel()).getApplicationEntity().getDevice());
                    window.close(target);
                } catch (Exception e) {
                    log.error(this.getClass().toString() + ": " + "Error modifying retry: " + e.getMessage());
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
            }
        }.setDefaultFormProcessing(false));
    }
}
