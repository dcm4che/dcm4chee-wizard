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

package org.dcm4chee.wizard.war.configuration.advanced.edit;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ExtendedWebPage;
import org.dcm4chee.wizard.war.common.component.ExtendedSecureWebPage;
import org.dcm4chee.wizard.war.configuration.common.custom.ConfigManager;
import org.dcm4chee.wizard.war.configuration.common.custom.CustomComponent;
import org.dcm4chee.wizard.war.configuration.common.custom.CustomComponentPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CustomCreateOrEditPage extends ExtendedSecureWebPage {
    
	private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CustomCreateOrEditPage.class);
    
    private static final ResourceReference baseCSS = new CssResourceReference(ExtendedWebPage.class, "base-style.css");
    
	List<CustomComponent> customComponents;
	Map<String, IModel> models;

	final ExtendedForm form = new ExtendedForm("form");
	final Form<?> optionalContainer = new Form<Object>("optional");
	
	WebMarkupContainer typeContainer;
	WebMarkupContainer optionalTypeContainer;
	
    public CustomCreateOrEditPage(final ModalWindow window, final Serializable model, final String configuration) {
        super();
        init(window, model, configuration);
    }
    
    @Override
    public void renderHead(IHeaderResponse response) {
    	if (CustomCreateOrEditPage.baseCSS != null) 
    		response.render(CssHeaderItem.forReference(CustomCreateOrEditPage.baseCSS));
    }
    
    void init(final ModalWindow window, final Serializable model, String configuration) {

    	setOutputMarkupId(true);
        add(form);

        customComponents =
				ConfigManager.getConfigurationFor(configuration).getComponents();     

        if (customComponents.size() > 0 )
        	form.setResourceIdPrefix(customComponents.get(0).getNamePrefix());
        
        models = new HashMap<String,IModel>();
        for (CustomComponent customComponent : customComponents) {
			models.put(customComponent.getName(), Model.of());
		}
        
        form.add(new CustomComponentPanel(
        		ConfigManager.filter(customComponents, CustomComponent.Container.Mandatory, true), 
        		models, this));
        
        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));

        optionalContainer.add(new CustomComponentPanel(
        		ConfigManager.filter(customComponents, CustomComponent.Container.Optional, true), 
        				models, this));
        
        form.add((typeContainer = 
    	        new WebMarkupContainer("type"))
    	        .setOutputMarkupId(true)
    	        .setOutputMarkupPlaceholderTag(true));

        typeContainer.add(new CustomComponentPanel(
        		ConfigManager.filter(customComponents, CustomComponent.Container.Mandatory, false), 
        		models, this));
        
        optionalContainer.add((optionalTypeContainer = 
    	        new WebMarkupContainer("type"))
    	        .setOutputMarkupId(true)
    	        .setOutputMarkupPlaceholderTag(true));
        
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

                	
//                	Device device = null;
//                	if (deviceModel != null) 
//                		device = deviceModel.getDevice();
//                	else 
//                		device = typeModel.getObject().equals(ConfigurationType.Proxy.toString()) ? 
//                				new ProxyDevice(deviceNameModel.getObject()) : 
//                         		new Device(deviceNameModel.getObject());
//                    device.setInstalled(installedModel.getObject());
//                    if (device instanceof ProxyDevice) 
//                    	((ProxyDevice) device).setSchedulerInterval(schedulerIntervalModel.getObject());

                	
                	// extract class from storeTo

                	// list values of models
                	for (Iterator i = models.keySet().iterator(); i.hasNext(); )
                		System.out.println("Model value: " + models.get(i.next()).getObject());
                	
                	
                	
//                	Class params[] = {};
                    Object paramsObj[] = {};
                    
                	for (CustomComponent customComponent : customComponents) {
                        try {
//                		System.out.println("CALLING CLASS: " + customComponent.getStoreClass());

//                			System.out.println("CALLING METHOD: " + customComponent.getStoreMethod(customComponent.getDataClass()));

//                			Object object = null;
//							object = model == null ? 
//									customComponent.getStoreClass().newInstance() : model;
							Method method = null;
							try {
System.out.println("Data class is: " + customComponent.getDataClass());
								method = customComponent.getStoreMethod(customComponent.getDataClass());
System.out.println("Tried method with class parameter " + customComponent.getDataClass() + 
		", resulted in " + method);
//							} catch (NoSuchMethodException e) {
//								method = getMethodWithPrimitive(customComponent);
							} catch (Exception e) {
								System.out.println("OTHER EXCEPTION HAPPENED: " + e.getMessage() + " " + e.getClass());
								
							}
System.out.println("Method fetching succeeded"); 
                			Object object = null;
							object = model == null ? 
									customComponent.getStoreClass().newInstance() : model;
							method.invoke(object, paramsObj);

                	
                	// extract method from storeTo
                	
                	
                	// check object from model, cast to class if not null
                	// else create new instance

                    // call method to store value from model
                    window.close(target);
//                	} catch (NoSuchMethodException e) {
//                	} catch (SecurityException e) {
//                	} catch (ClassNotFoundException e) {
//                	} catch (InstantiationException e) {
//                	} catch (IllegalAccessException e) {
//					} catch (InvocationTargetException e) {
					} catch (Exception e) {
	        			log.error(this.getClass().toString() + ": " + 
	        					"Error reflecting on: " + customComponent.getName() + ": " + e.getMessage());
	                    log.debug("Exception", e);
//	                    throw new RuntimeException(e);
	        		}
                }
        	}
            
            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                if (target != null)
                    target.add(form);
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
    }
            
//    private Method getMethodWithPrimitive(CustomComponent customComponent) throws ClassNotFoundException {
//		Method method = null;
//		
//System.out.println("getMethodWithPrimitive: " + customComponent.getDataClass());
//		
//		if (customComponent.getDataClass().equals(Boolean.class))
//			try {
//				method = customComponent.getStoreMethod(boolean.class);
//System.out.println("getMethodWithPrimitive: " + method);
//			} catch (Exception e) {
//    			log.error(this.getClass().toString()
//    					+ ": " + "Error getting method signature for " 
//    					+ customComponent.getName() + ": " + e.getMessage());
//                log.debug("Exception", e);
//                throw new RuntimeException(e);
//			}
//		return method;
//    }
 }
