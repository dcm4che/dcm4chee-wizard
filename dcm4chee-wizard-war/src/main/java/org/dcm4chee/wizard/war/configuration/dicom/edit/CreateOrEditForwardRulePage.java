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

package org.dcm4chee.wizard.war.configuration.dicom.edit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.DefaultCssAutoCompleteTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.Dimse;
import org.dcm4chee.proxy.conf.ForwardRule;
import org.dcm4chee.proxy.conf.ProxyApplicationEntity;
import org.dcm4chee.proxy.conf.Schedule;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.modal.MessageWindow;
import org.dcm4chee.wizard.war.configuration.dicom.ConfigurationTreeNode;
import org.dcm4chee.wizard.war.configuration.dicom.DeviceTreeProvider;
import org.dcm4chee.wizard.war.configuration.dicom.model.ApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.dicom.model.ForwardRuleModel;
import org.dcm4chee.wizard.war.configuration.dicom.validator.CommonNameValidator;
import org.dcm4chee.wizard.war.configuration.dicom.validator.DestinationURIValidator;
import org.dcm4chee.wizard.war.configuration.dicom.validator.SOPClassValidator;
import org.dcm4chee.wizard.war.configuration.dicom.validator.ScheduleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.security.components.SecureWebPage;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditForwardRulePage extends SecureWebPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditForwardRulePage.class);

    private static final ResourceReference BaseCSS = new CssResourceReference(BaseWicketPage.class, "base-style.css");
    
    private MessageWindow msgWin = new MessageWindow("msgWin");
    
    // mandatory
	private Model<String> commonNameModel;
    private Model<String> destinationURIModel;
	
	// optional
    private Model<String> callingAETitleModel;
    private Model<Dimse> dimseModel;
    private Model<Boolean> exclusiveUseDefinedTCModel;
    private Model<String> scheduleDaysModel;
    private Model<String> scheduleHoursModel;
    private Model<String> useCallingAETitleModel;
    private Model<String> sopClassModel;
    
    public CreateOrEditForwardRulePage(final ModalWindow window, final ForwardRuleModel frModel, 
    		final ConfigurationTreeNode frsNode, final ConfigurationTreeNode frNode) {
    	super();

        msgWin.setTitle("");
        add(msgWin);
        add(new WebMarkupContainer("create-forwardRule-title").setVisible(frModel == null));
        add(new WebMarkupContainer("edit-forwardRule-title").setVisible(frModel != null));

        setOutputMarkupId(true);
        final BaseForm form = new BaseForm("form");
        form.setResourceIdPrefix("dicom.edit.forwardRule.");
        add(form);

		if (frModel == null) {
			commonNameModel = Model.of();
		    destinationURIModel = Model.of();
		    callingAETitleModel = Model.of();
		    dimseModel = Model.of();
		    exclusiveUseDefinedTCModel = Model.of(false);
		    scheduleDaysModel = Model.of();
		    scheduleHoursModel = Model.of();
		    useCallingAETitleModel = Model.of();
		    sopClassModel = Model.of();
		} else {
			ForwardRule forwardRule = frModel.getForwardRule();
	        commonNameModel = Model.of(forwardRule.getCommonName());
	        destinationURIModel = Model.of(forwardRule.getDestinationURI());
	        callingAETitleModel = Model.of(forwardRule.getCallingAET());
	        dimseModel = Model.of(forwardRule.getDimse());
	        exclusiveUseDefinedTCModel = Model.of(forwardRule.isExclusiveUseDefinedTC());
		    scheduleDaysModel = Model.of(forwardRule.getReceiveSchedule().getDays());
		    scheduleHoursModel = Model.of(forwardRule.getReceiveSchedule().getHours());
		    useCallingAETitleModel = Model.of(forwardRule.getUseCallingAET());
			sopClassModel = Model.of(forwardRule.getSopClass());		
		}

        try {
			form.add(new Label("commonName.label", new ResourceModel("dicom.edit.forwardRule.commonName.label")))
			.add(new TextField<String>("commonName", commonNameModel).setRequired(true)
					.add(new CommonNameValidator(commonNameModel.getObject(), 
							(ProxyApplicationEntity) ((ApplicationEntityModel) frsNode.getParent().getModel()).getApplicationEntity())));
		} catch (ConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        form.add(new Label("destinationURI.label", new ResourceModel("dicom.edit.forwardRule.destinationURI.label")))
        .add(new TextField<String>("destinationURI", destinationURIModel).setRequired(true)
        		.add(new DestinationURIValidator(destinationURIModel.getObject())));

        final WebMarkupContainer optionalContainer = new WebMarkupContainer("optionalContainer");
        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));

        final List<String> uniqueAETitles = new ArrayList<String>(DeviceTreeProvider.get().getUniqueAETitles());       
        optionalContainer.add(new Label("callingAETitle.label", new ResourceModel("dicom.edit.forwardRule.callingAETitle.label")))
                .add(new DefaultCssAutoCompleteTextField<String>("callingAETitle", callingAETitleModel) {

					private static final long serialVersionUID = 1L;

					ArrayList<String> choices = new ArrayList<String>();
					
					@Override
					protected Iterator<String> getChoices(String input) {
						choices.clear();
						for (String aeTitle : uniqueAETitles) 
							if (aeTitle.startsWith(input))
								choices.add(aeTitle);
						return choices.iterator();
					}
                });
        
        optionalContainer.add(new Label("dimse.label", new ResourceModel("dicom.edit.forwardRule.dimse.label")));
        ArrayList<Dimse> dimseList = 
        		new ArrayList<Dimse>();
        dimseList.addAll(Arrays.asList(Dimse.values())); 
        DropDownChoice<Dimse> dimseDropDown = 
        		new DropDownChoice<Dimse>("dimse", dimseModel, dimseList);
        optionalContainer.add(dimseDropDown
        		.setNullValid(false));

        optionalContainer.add(new Label("exclusiveUseDefinedTC.label", new ResourceModel("dicom.edit.forwardRule.exclusiveUseDefinedTC.label")))
        .add(new CheckBox("exclusiveUseDefinedTC", exclusiveUseDefinedTCModel));

        optionalContainer.add(new Label("scheduleDays.label", new ResourceModel("dicom.edit.forwardRule.scheduleDays.label")))
        .add(new TextField<String>("scheduleDays", scheduleDaysModel)
        		.add(new ScheduleValidator(ScheduleValidator.Type.DAYS)));

        optionalContainer.add(new Label("scheduleHours.label", new ResourceModel("dicom.edit.forwardRule.scheduleHours.label")))
        .add(new TextField<String>("scheduleHours", scheduleHoursModel)
        		.add(new ScheduleValidator(ScheduleValidator.Type.HOURS)));

        optionalContainer.add(new Label("useCallingAETitle.label", new ResourceModel("dicom.edit.forwardRule.useCallingAETitle.label")))
        .add(new TextField<String>("useCallingAETitle", useCallingAETitleModel));
                		
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
                	ForwardRule forwardRule = frModel == null ? 
                    		new ForwardRule() : frModel.getForwardRule();

            		forwardRule.setCommonName(commonNameModel.getObject());
            		forwardRule.setDestinationURI(destinationURIModel.getObject());
            		forwardRule.setCallingAET(callingAETitleModel.getObject());
            		forwardRule.setDimse(dimseModel.getObject());
            		forwardRule.setExclusiveUseDefinedTC(exclusiveUseDefinedTCModel.getObject());
            		Schedule schedule = new Schedule();
            		schedule.setDays(scheduleDaysModel.getObject());
            		schedule.setHours(scheduleHoursModel.getObject());
            		forwardRule.setReceiveSchedule(schedule);
            		forwardRule.setUseCallingAET(useCallingAETitleModel.getObject());
            		forwardRule.setSopClass(sopClassModel.getObject());
                    		
                    if (frModel == null)
                    	DeviceTreeProvider.get().addForwardRule(frsNode, forwardRule);
                    else 
                    	DeviceTreeProvider.get().editForwardRule(frsNode, frNode, forwardRule);

                    window.close(target);
                } catch (Exception e) {
                	log.error("Error modifying forward rule", e);
                    msgWin.show(target, new ResourceModel(frModel == null ? 
                    		"dicom.edit.forwardRule.create.failed" : "dicom.edit.forwardRule.update.failed")
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
        if (CreateOrEditForwardRulePage.BaseCSS != null)
        	response.renderCSSReference(CreateOrEditForwardRulePage.BaseCSS);
    }
 }
