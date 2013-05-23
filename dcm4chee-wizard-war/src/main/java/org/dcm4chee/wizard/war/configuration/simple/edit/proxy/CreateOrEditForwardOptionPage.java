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
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4chee.proxy.conf.ForwardOption;
import org.dcm4chee.proxy.conf.ProxyAEExtension;
import org.dcm4chee.proxy.conf.Schedule;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.MainWebPage;
import org.dcm4chee.wizard.common.component.ModalWindowRuntimeException;
import org.dcm4chee.wizard.common.component.secure.SecureSessionCheckPage;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.ApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.ForwardOptionModel;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeNode;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.war.configuration.simple.validator.DestinationAETitleValidator;
import org.dcm4chee.wizard.war.configuration.simple.validator.ScheduleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditForwardOptionPage extends SecureSessionCheckPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditForwardOptionPage.class);

    private static final ResourceReference baseCSS = new CssResourceReference(MainWebPage.class, "base-style.css");
    
    // mandatory
	private Model<String> destinationAETitleModel;
	
	// optional
	private Model<String> scheduleDaysModel;
	private Model<String> scheduleHoursModel;
	private Model<String> descriptionModel;
	private Model<Boolean> convertEmf2SfModel;
    
    public CreateOrEditForwardOptionPage(final ModalWindow window, final ForwardOptionModel forwardOptionModel, 
    		final ConfigTreeNode aeNode) {
    	super();

    	ProxyAEExtension proxyAEExtension = null;
		try {
			proxyAEExtension = ((ApplicationEntityModel) aeNode.getModel()).getApplicationEntity()
			.getAEExtension(ProxyAEExtension.class);
		} catch (ConfigurationException ce) {
			log.error(this.getClass().toString() + ": " + "Error modifying retry: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new ModalWindowRuntimeException(ce.getLocalizedMessage());
		}

        add(new WebMarkupContainer("create-forwardOption-title").setVisible(forwardOptionModel == null));
        add(new WebMarkupContainer("edit-forwardOption-title").setVisible(forwardOptionModel != null));

        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");
        form.setResourceIdPrefix("dicom.edit.forwardOption.");
        add(form);

        List<String> uniqueAETitles = null;
		try {
			uniqueAETitles = Arrays.asList(ConfigTreeProvider.get().getUniqueAETitles());
		} catch (ConfigurationException ce) {
			log.error(this.getClass().toString() + ": " + "Error retrieving unique ae titles: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new ModalWindowRuntimeException(ce.getLocalizedMessage());
		}
        Collections.sort(uniqueAETitles);

        if (forwardOptionModel == null) {        
			destinationAETitleModel = Model.of(uniqueAETitles.get(0));
	        scheduleDaysModel = Model.of();
	        scheduleHoursModel = Model.of();
	        descriptionModel = Model.of();
	        convertEmf2SfModel = Model.of(false);
        } else {
        	if (!uniqueAETitles.contains(forwardOptionModel.getDestinationAETitle())) {
				uniqueAETitles = new ArrayList<String>(uniqueAETitles);
        		uniqueAETitles.add(forwardOptionModel.getDestinationAETitle());
        		Collections.sort(uniqueAETitles);
        	}
        	destinationAETitleModel = Model.of(forwardOptionModel.getDestinationAETitle());
	        scheduleDaysModel = Model.of(forwardOptionModel.getForwardOption().getSchedule().getDays());
	        scheduleHoursModel = Model.of(forwardOptionModel.getForwardOption().getSchedule().getHours());
	        descriptionModel = Model.of(forwardOptionModel.getForwardOption().getDescription());
	        convertEmf2SfModel = Model.of(forwardOptionModel.getForwardOption().isConvertEmf2Sf());
        }
        
        form.add(new Label("destinationAETitle.label", new ResourceModel("dicom.edit.forwardOption.destinationAETitle.label")))
        .add(new DropDownChoice<String>("destinationAETitle", destinationAETitleModel, uniqueAETitles)
        		.setNullValid(false)
        		.setRequired(true)
        		.add(new DestinationAETitleValidator(
        				destinationAETitleModel.getObject(), proxyAEExtension.getForwardOptions())));
        
        final WebMarkupContainer optionalContainer = new WebMarkupContainer("optional");
        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));

        optionalContainer.add(new Label("scheduleDays.label", new ResourceModel("dicom.edit.forwardOption.optional.scheduleDays.label")))
        .add(new TextField<String>("scheduleDays", scheduleDaysModel)
        		.add(new ScheduleValidator(ScheduleValidator.Type.DAYS)));

        optionalContainer.add(new Label("scheduleHours.label", new ResourceModel("dicom.edit.forwardOption.optional.scheduleHours.label")))
        .add(new TextField<String>("scheduleHours", scheduleHoursModel)
        		.add(new ScheduleValidator(ScheduleValidator.Type.HOURS)));

        optionalContainer.add(new Label("description.label", new ResourceModel("dicom.edit.forwardOption.optional.description.label")))
        .add(new TextField<String>("description", descriptionModel));

        optionalContainer.add(new Label("convertEmf2Sf.label", new ResourceModel("dicom.edit.forwardOption.optional.convertEmf2Sf.label")))
        .add(new CheckBox("convertEmf2Sf", convertEmf2SfModel));

        form.add(new Label("toggleOptional.label", new ResourceModel("dicom.edit.toggleOptional.label")))
        .add(new AjaxCheckBox("toggleOptional", new Model<Boolean>(true)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(optionalContainer.setVisible(this.getModelObject()));
			}
        });
        optionalContainer.setVisible(true);
        		
        form.add(new IndicatingAjaxButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                	Schedule schedule = new Schedule();
                	schedule.setDays(scheduleDaysModel.getObject());
                	schedule.setHours(scheduleHoursModel.getObject());

                	ForwardOption forwardOption = new ForwardOption();
                	forwardOption.setSchedule(schedule);
                	forwardOption.setDescription(descriptionModel.getObject());
                	forwardOption.setConvertEmf2Sf(convertEmf2SfModel.getObject());
                	
                	ProxyAEExtension proxyAEExtension = 
                			((ApplicationEntityModel) aeNode.getModel()).getApplicationEntity()
        					.getAEExtension(ProxyAEExtension.class);

                    if (forwardOptionModel != null)
                    	proxyAEExtension.getForwardOptions()
                    		.remove(forwardOptionModel.getDestinationAETitle());
                    
                    proxyAEExtension.getForwardOptions()
                    	.put(destinationAETitleModel.getObject(), forwardOption);
                    
            		ConfigTreeProvider.get().mergeDevice(
            				((ApplicationEntityModel) aeNode.getModel()).getApplicationEntity().getDevice());
                    window.close(target);
                } catch (Exception e) {
        			log.error(this.getClass().toString() + ": " + "Error modifying forward option: " + e.getMessage());
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
    	if (CreateOrEditForwardOptionPage.baseCSS != null) 
    		response.render(CssHeaderItem.forReference(CreateOrEditForwardOptionPage.baseCSS));
    }
 }
