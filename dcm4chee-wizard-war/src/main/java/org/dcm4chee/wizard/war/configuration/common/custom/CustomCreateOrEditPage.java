/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either expresqs or implied. See the License
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

package org.dcm4chee.wizard.war.configuration.common.custom;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4chee.wizard.common.behavior.MarkInvalidBehavior;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ExtendedWebPage;
import org.dcm4chee.wizard.war.common.component.ExtendedSecureWebPage;
import org.dcm4chee.wizard.war.configuration.common.tree.ConfigTreeProvider.ConfigurationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public abstract class CustomCreateOrEditPage extends ExtendedSecureWebPage {
    
	private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CustomCreateOrEditPage.class);
    
    private static final ResourceReference baseCSS = new CssResourceReference(ExtendedWebPage.class, "base-style.css");
    
	List<CustomComponent> customComponents;

	@SuppressWarnings("rawtypes")
	Map<String, IModel> models;

	protected final ExtendedForm form = new ExtendedForm("form");
	protected final Form<?> optionalContainer = new Form<Object>("optional");
	
	protected WebMarkupContainer typeContainer;
	protected WebMarkupContainer optionalTypeContainer;
	
	protected Label storeError;
	
    @SuppressWarnings({ "rawtypes" })
	public CustomCreateOrEditPage(final ModalWindow window, final Serializable model, final String configuration) {
        super();
        
    	setOutputMarkupId(true);
        add(form);

        customComponents =
				ConfigManager.getConfigurationFor(configuration).getComponents();     

        if (customComponents.size() > 0 )
        	form.setResourceIdPrefix(customComponents.get(0).getNamePrefix());

        models = new HashMap<String,IModel>();
        
        Serializable storeObject = getStoreObject(model);
System.out.println("Constructor: " + storeObject);
        for (CustomComponent customComponent : customComponents) {
        	try {
        		if (storeObject == null)
    	        	models.put(customComponent.getName(), Model.of());
        		else {
	        		Serializable value = 
		        		(Serializable) storeObject.getClass()
						.getDeclaredMethod(customComponent.getGetFrom(), new Class[] {})
						.invoke(getStoreObject(model), new Object[] {});
	        		models.put(customComponent.getName(), value == null ? Model.of() : Model.of(value));
        		}			
        	} catch (NoSuchMethodException nsme) {
        		log.warn("Failed to get value for component: " + customComponent.getName());
        		models.put(customComponent.getName(), Model.of());
        	} catch (Exception e) {
    			log.error(this.getClass().toString() + ": " + 
    					"Error reflecting on value for component: " 
    					+ customComponent.getName() + ": " + e.getMessage());
                log.debug("Exception", e);
//				throw new RuntimeException(e);
			}
		}
        
        form.add((typeContainer = 
    	        new WebMarkupContainer("type"))
    	        .setOutputMarkupId(true)
    	        .setOutputMarkupPlaceholderTag(true));

        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));

        optionalContainer.add((optionalTypeContainer = 
    	        new WebMarkupContainer("type"))
    	        .setOutputMarkupId(true)
    	        .setOutputMarkupPlaceholderTag(true));

        form.add(new CustomComponentPanel(
        		ConfigManager.filter(customComponents, CustomComponent.Container.Mandatory, true), 
        		models, this));

        optionalContainer.add(new CustomComponentPanel(
        		ConfigManager.filter(customComponents, CustomComponent.Container.Optional, true), 
        				models, this));

        typeContainer.add(new CustomComponentPanel(
        		ConfigManager.filter(customComponents, CustomComponent.Container.Mandatory, false), 
        		models, this));

        optionalTypeContainer.add(new CustomComponentPanel(
        		ConfigManager.filter(customComponents, CustomComponent.Container.Optional, false), 
        				models, this));

        form.add(new Label("toggleOptional.label", new ResourceModel("dicom.edit.toggleOptional.label")))
        .add(new AjaxCheckBox("toggleOptional", new Model<Boolean>()) {
        	
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(optionalContainer.setVisible(this.getModelObject()));
			}
        });

        form.add(new AjaxButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				try {
					storeError.setVisible(false);
					Serializable storeObject = getStoreObject(model);
	
					for (CustomComponent customComponent : customComponents) {
						if (!visible(customComponent))
							continue;

						storeObject.getClass().getDeclaredMethod(
								customComponent.getStoreTo(), customComponent.getDataClass())
								.invoke(storeObject, 
										new Object[] {models.get(customComponent.getName()).getObject()});
	                }
					save(storeObject);
	                window.close(target);
				} catch (Exception e) {
					storeError.setDefaultModelObject("Error: " + e.getMessage())
					.setVisible(true);
					target.add(form);
				}
        	}
            
            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            	storeError.setVisible(false);
            	for (FeedbackMessage feedbackMessage : new FeedbackCollector(getPage()).collect())
					feedbackMessage.getReporter().add(new MarkInvalidBehavior());
				ExtendedForm.addInvalidComponentsToAjaxRequestTarget(target, form);
            }
        });

        form.add(new AjaxButton("cancel", new ResourceModel("cancelBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                window.close(target);
            }

			@Override
			protected void onError(AjaxRequestTarget arg0, Form<?> arg1) {
			}
        }.setDefaultFormProcessing(false));
        
        form.add((storeError = new Label("storeError", new Model<String>("")))
        	.setOutputMarkupId(true)
        	.setOutputMarkupPlaceholderTag(true)
        	.add(new AttributeAppender("class", " invalid")));
    }
    
    @Override
    public void renderHead(IHeaderResponse response) {
    	if (CustomCreateOrEditPage.baseCSS != null) 
    		response.render(CssHeaderItem.forReference(CustomCreateOrEditPage.baseCSS));
    }

    public abstract Serializable getStoreObject(Object model);
    
    public abstract void save(Serializable object) throws ConfigurationException, IOException;

    private boolean visible(CustomComponent customComponent) {
    	if (!customComponent.getConfigurationType()
    			.equals(ConfigurationType.Basic)) {
    		if (!typeContainer.isVisible())
    			return false;
    		if (customComponent.getContainer().equals(CustomComponent.Container.Optional)
    				&& !optionalTypeContainer.isVisible())
    			return false;
    	} else {
    		if (customComponent.getContainer().equals(CustomComponent.Container.Optional)
    				&& !optionalContainer.isVisible())
    			return false;
    	}
    	return true;
    }
 }
