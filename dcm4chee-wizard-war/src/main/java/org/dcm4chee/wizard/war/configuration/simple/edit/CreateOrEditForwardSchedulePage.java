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
import org.dcm4chee.proxy.conf.ProxyApplicationEntity;
import org.dcm4chee.proxy.conf.Schedule;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.modal.MessageWindow;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.ForwardScheduleModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.ProxyApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeNode;
import org.dcm4chee.wizard.war.configuration.simple.validator.DestinationAETitleValidator;
import org.dcm4chee.wizard.war.configuration.simple.validator.ScheduleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.security.components.SecureWebPage;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditForwardSchedulePage extends SecureWebPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditForwardSchedulePage.class);

    private static final ResourceReference BaseCSS = new CssResourceReference(BaseWicketPage.class, "base-style.css");
    
    private MessageWindow msgWin = new MessageWindow("msgWin");
    
    // mandatory
	private Model<String> destinationAETitleModel;
	
	// optional
	private Model<String> scheduleDaysModel;
	private Model<String> scheduleHoursModel;
    
    public CreateOrEditForwardSchedulePage(final ModalWindow window, final ForwardScheduleModel forwardScheduleModel, 
    		final ConfigTreeNode aeNode) {
    	super();

    	final ProxyApplicationEntity proxyApplicationEntity = 
    			((ProxyApplicationEntityModel) aeNode.getModel()).getApplicationEntity();

        msgWin.setTitle("");
        add(msgWin);
        add(new WebMarkupContainer("create-forwardSchedule-title").setVisible(forwardScheduleModel == null));
        add(new WebMarkupContainer("edit-forwardSchedule-title").setVisible(forwardScheduleModel != null));

        setOutputMarkupId(true);
        final BaseForm form = new BaseForm("form");
        form.setResourceIdPrefix("dicom.edit.forwardSchedule.");
        add(form);

        List<String> uniqueAETitles = new ArrayList<String>(ConfigTreeProvider.get().getUniqueAETitles());
        Collections.sort(uniqueAETitles);

		destinationAETitleModel = Model.of(forwardScheduleModel != null ? 
				forwardScheduleModel.getDestinationAETitle() : uniqueAETitles.get(0));
        scheduleDaysModel = Model.of(forwardScheduleModel != null ? forwardScheduleModel.getSchedule().getDays() : null);
        scheduleHoursModel = Model.of(forwardScheduleModel != null ? forwardScheduleModel.getSchedule().getHours() : null);
        
        form.add(new Label("destinationAETitle.label", new ResourceModel("dicom.edit.forwardSchedule.destinationAETitle.label")))
        .add(new DropDownChoice<String>("destinationAETitle", destinationAETitleModel, uniqueAETitles)
        		.setNullValid(false)
        		.setRequired(true)
        		.add(new DestinationAETitleValidator(
        				destinationAETitleModel.getObject(), proxyApplicationEntity.getForwardSchedules())));
        
        final WebMarkupContainer optionalContainer = new WebMarkupContainer("optionalContainer");
        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));

        optionalContainer.add(new Label("scheduleDays.label", new ResourceModel("dicom.edit.forwardSchedule.scheduleDays.label")))
        .add(new TextField<String>("scheduleDays", scheduleDaysModel)
        		.add(new ScheduleValidator(ScheduleValidator.Type.DAYS)));

        optionalContainer.add(new Label("scheduleHours.label", new ResourceModel("dicom.edit.forwardSchedule.scheduleHours.label")))
        .add(new TextField<String>("scheduleHours", scheduleHoursModel)
        		.add(new ScheduleValidator(ScheduleValidator.Type.HOURS)));

        form.add(new Label("optional.label", new ResourceModel("dicom.edit.optional.label")))
        .add(new AjaxCheckBox("optional", new Model<Boolean>(true)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(optionalContainer.setVisible(this.getModelObject()));
			}
        });
        optionalContainer.setVisible(true);
        		
        form.add(new AjaxFallbackButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                	
                	Schedule schedule = forwardScheduleModel == null ? 
                			new Schedule() : forwardScheduleModel.getSchedule();
                	schedule.setDays(scheduleDaysModel.getObject());
                	schedule.setHours(scheduleHoursModel.getObject());

                    if (forwardScheduleModel != null)
                    	proxyApplicationEntity.getForwardSchedules()
                    		.remove(forwardScheduleModel.getDestinationAETitle());
                    
                    proxyApplicationEntity.getForwardSchedules()
                    	.put(destinationAETitleModel.getObject(), schedule);

            		ConfigTreeProvider.get().mergeDevice(proxyApplicationEntity.getDevice());
            		aeNode.getAncestor(2).setModel(null);

                    window.close(target);
                } catch (Exception e) {
                	log.error("Error modifying forward schedule", e);
                    msgWin.show(target, new ResourceModel(forwardScheduleModel == null ? 
                    		"dicom.edit.forwardSchedule.create.failed" : "dicom.edit.forwardSchedule.update.failed")
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
        if (CreateOrEditForwardSchedulePage.BaseCSS != null)
        	response.renderCSSReference(CreateOrEditForwardSchedulePage.BaseCSS);
    }
 }
