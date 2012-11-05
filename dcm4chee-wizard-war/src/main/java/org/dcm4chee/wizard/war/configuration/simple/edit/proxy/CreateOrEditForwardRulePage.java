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
import java.util.Iterator;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.DefaultCssAutoCompleteTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
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
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ExtendedWebPage;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.StringArrayModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.ForwardRuleModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.ProxyApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeNode;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.war.configuration.simple.validator.CommonNameValidator;
import org.dcm4chee.wizard.war.configuration.simple.validator.DestinationURIValidator;
import org.dcm4chee.wizard.war.configuration.simple.validator.SOPClassValidator;
import org.dcm4chee.wizard.war.configuration.simple.validator.ScheduleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.security.components.SecureWebPage;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditForwardRulePage extends SecureWebPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditForwardRulePage.class);

    private static final ResourceReference baseCSS = new CssResourceReference(ExtendedWebPage.class, "base-style.css");

    // mandatory
	private Model<String> commonNameModel;
    private StringArrayModel destinationURIModel;
	
	// optional
    private Model<String> callingAETitleModel;
    private Model<Dimse> dimseModel;
    private Model<Boolean> exclusiveUseDefinedTCModel;
    private Model<String> scheduleDaysModel;
    private Model<String> scheduleHoursModel;
    private Model<String> useCallingAETitleModel;
    private StringArrayModel sopClassModel;
    
    public CreateOrEditForwardRulePage(final ModalWindow window, final ForwardRuleModel forwardRuleModel, 
    		final ConfigTreeNode aeNode) {
    	super();

    	final ProxyApplicationEntity proxyApplicationEntity = 
    			((ProxyApplicationEntityModel) aeNode.getModel()).getApplicationEntity();

        add(new WebMarkupContainer("create-forwardRule-title").setVisible(forwardRuleModel == null));
        add(new WebMarkupContainer("edit-forwardRule-title").setVisible(forwardRuleModel != null));

        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");
        form.setResourceIdPrefix("dicom.edit.forwardRule.");
        add(form);

		if (forwardRuleModel == null) {
			commonNameModel = Model.of();
		    destinationURIModel = new StringArrayModel(null);
		    callingAETitleModel = Model.of();
		    dimseModel = Model.of();
		    exclusiveUseDefinedTCModel = Model.of(false);
		    scheduleDaysModel = Model.of();
		    scheduleHoursModel = Model.of();
		    useCallingAETitleModel = Model.of();
		    sopClassModel = new StringArrayModel(null);
		} else {
			ForwardRule forwardRule = forwardRuleModel.getForwardRule();
	        commonNameModel = Model.of(forwardRule.getCommonName());
	        destinationURIModel = new StringArrayModel(forwardRule.getDestinationURI()
	        		.toArray(new String[0]));
	        callingAETitleModel = Model.of(forwardRule.getCallingAET());
	        dimseModel = Model.of(forwardRule.getDimse());
	        exclusiveUseDefinedTCModel = Model.of(forwardRule.isExclusiveUseDefinedTC());
		    scheduleDaysModel = Model.of(forwardRule.getReceiveSchedule().getDays());
		    scheduleHoursModel = Model.of(forwardRule.getReceiveSchedule().getHours());
		    useCallingAETitleModel = Model.of(forwardRule.getUseCallingAET());
			sopClassModel = new StringArrayModel(forwardRule.getSopClass()
					.toArray(new String[0]));		
		}

        form.add(new Label("commonName.label", new ResourceModel("dicom.edit.forwardRule.commonName.label")))
		.add(new TextField<String>("commonName", commonNameModel).setRequired(true)
				.add(new CommonNameValidator(commonNameModel.getObject(), proxyApplicationEntity)));

        form.add(new Label("destinationURI.label", new ResourceModel("dicom.edit.forwardRule.destinationURI.label")))
        .add(new TextArea<String>("destinationURI", destinationURIModel).setRequired(true)
        		.add(new DestinationURIValidator()));

        final WebMarkupContainer optionalContainer = new WebMarkupContainer("optional");
        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));

        optionalContainer.add(new Label("callingAETitle.label", new ResourceModel("dicom.edit.forwardRule.optional.callingAETitle.label")))
                .add(new DefaultCssAutoCompleteTextField<String>("callingAETitle", callingAETitleModel) {

					private static final long serialVersionUID = 1L;

					ArrayList<String> choices = new ArrayList<String>();
					
					@Override
					protected Iterator<String> getChoices(String input) {
						choices.clear();
						try {
							for (String aeTitle : ConfigTreeProvider.get().getUniqueAETitles()) 
								if (aeTitle.startsWith(input))
									choices.add(aeTitle);
						} catch (ConfigurationException ce) {
		        			log.error(this.getClass().toString() + ": " + "Error retrieving unique ae titles: " + ce.getMessage());
		                    log.debug("Exception", ce);
		                    throw new RuntimeException(ce);
						}
						return choices.iterator();
					}
                });
        
        optionalContainer.add(new Label("dimse.label", new ResourceModel("dicom.edit.forwardRule.optional.dimse.label")));
        ArrayList<Dimse> dimseList = 
        		new ArrayList<Dimse>();
        dimseList.addAll(Arrays.asList(Dimse.values())); 
        DropDownChoice<Dimse> dimseDropDown = 
        		new DropDownChoice<Dimse>("dimse", dimseModel, dimseList);
        optionalContainer.add(dimseDropDown
        		.setNullValid(false));

        optionalContainer.add(new Label("exclusiveUseDefinedTC.label", new ResourceModel("dicom.edit.forwardRule.optional.exclusiveUseDefinedTC.label")))
        .add(new CheckBox("exclusiveUseDefinedTC", exclusiveUseDefinedTCModel));

        optionalContainer.add(new Label("scheduleDays.label", new ResourceModel("dicom.edit.forwardRule.optional.scheduleDays.label")))
        .add(new TextField<String>("scheduleDays", scheduleDaysModel)
        		.add(new ScheduleValidator(ScheduleValidator.Type.DAYS)));

        optionalContainer.add(new Label("scheduleHours.label", new ResourceModel("dicom.edit.forwardRule.optional.scheduleHours.label")))
        .add(new TextField<String>("scheduleHours", scheduleHoursModel)
        		.add(new ScheduleValidator(ScheduleValidator.Type.HOURS)));

        optionalContainer.add(new Label("useCallingAETitle.label", new ResourceModel("dicom.edit.forwardRule.optional.useCallingAETitle.label")))
        .add(new TextField<String>("useCallingAETitle", useCallingAETitleModel));
                		
        optionalContainer.add(new Label("sopClass.label", new ResourceModel("dicom.edit.forwardRule.optional.sopClass.label")))
        .add(new TextArea<String>("sopClass", sopClassModel)
        		.add(new SOPClassValidator()));

        form.add(new Label("toggleOptional.label", new ResourceModel("dicom.edit.toggleOptional.label")))
        .add(new AjaxCheckBox("toggleOptional", new Model<Boolean>()) {
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
                	ForwardRule forwardRule = forwardRuleModel == null ? 
                    		new ForwardRule() : forwardRuleModel.getForwardRule();

            		forwardRule.setCommonName(commonNameModel.getObject());
            		forwardRule.setDestinationURIs(Arrays.asList(destinationURIModel.getArray()));
            		forwardRule.setCallingAET(callingAETitleModel.getObject());
            		forwardRule.setDimse(dimseModel.getObject());
            		forwardRule.setExclusiveUseDefinedTC(exclusiveUseDefinedTCModel.getObject());
            		Schedule schedule = new Schedule();
            		schedule.setDays(scheduleDaysModel.getObject());
            		schedule.setHours(scheduleHoursModel.getObject());
            		forwardRule.setReceiveSchedule(schedule);
            		forwardRule.setUseCallingAET(useCallingAETitleModel.getObject());
            		forwardRule.setSopClass(Arrays.asList(sopClassModel.getArray()));
                    		
            		if (forwardRuleModel == null)
            			proxyApplicationEntity.getForwardRules().add(forwardRule);

            		ConfigTreeProvider.get().mergeDevice(proxyApplicationEntity.getDevice());
                    window.close(target);
                } catch (Exception e) {
        			log.error(this.getClass().toString() + ": " + "Error modifying forward rule: " + e.getMessage());
                    log.debug("Exception", e);
                    throw new RuntimeException(e);
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
        if (CreateOrEditForwardRulePage.baseCSS != null)
    		response.render(CssHeaderItem.forReference(CreateOrEditForwardRulePage.baseCSS));
    }
 }
