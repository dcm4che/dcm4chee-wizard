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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.validation.validator.RangeValidator;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.data.Code;
import org.dcm4che.data.Issuer;
import org.dcm4che.net.Device;
import org.dcm4chee.proxy.conf.ProxyDevice;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ExtendedWebPage;
import org.dcm4chee.wizard.war.common.component.ExtendedSecureWebPage;
import org.dcm4chee.wizard.war.configuration.common.custom.ConfigManager;
import org.dcm4chee.wizard.war.configuration.common.custom.CustomComponent;
import org.dcm4chee.wizard.war.configuration.common.custom.CustomComponentsPanel;
import org.dcm4chee.wizard.war.configuration.common.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.war.configuration.common.tree.ConfigTreeProvider.ConfigurationType;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.DeviceModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.InstitutionCodeModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.StringArrayModel;
import org.dcm4chee.wizard.war.configuration.simple.validator.CodeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CustomCreateOrEditPage extends ExtendedSecureWebPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CustomCreateOrEditPage.class);
    
    private static final ResourceReference baseCSS = new CssResourceReference(ExtendedWebPage.class, "base-style.css");
    
    // configuration type selection
//	private Model<ConfigurationType> typeModel;
	private Model<String> typeModel;
	
    // mandatory
    private Model<String> deviceNameModel;
	private IModel<Boolean> installedModel;
	// ProxyDevice only
	private Model<Integer> schedulerIntervalModel;
	
	// optional
	private Model<String> descriptionModel;
	private Model<String> deviceSerialNumberModel;
	private StringArrayModel institutionAddressModel;
	private InstitutionCodeModel institutionCodeModel;
	private StringArrayModel institutionalDepartmentNameModel;
	private StringArrayModel institutionNameModel;
	private Model<String> issuerOfAccessionNumberModel;
	private Model<String> issuerOfAdmissionIDModel;
	private Model<String> issuerOfContainerIdentifierModel;
	private Model<String> issuerOfPatientIDModel;
	private Model<String> issuerOfServiceEpisodeIDModel;
	private Model<String> issuerOfSpecimenIdentifierModel;
	private Model<String> manufacturerModel;
	private Model<String> manufacturerModelNameModel;
	private Model<String> orderFillerIdentifierModel;
	private Model<String> orderPlacerIdentifierModel;
	private StringArrayModel primaryDeviceTypesModel;
	private StringArrayModel relatedDeviceRefsModel;
	private StringArrayModel softwareVersionsModel;
	private Model<String> stationNameModel;
	private Model<String> vendorDataModel;
	// ProxyDevice only
	private Model<Integer> forwardThreadsModel;
	
    public CustomCreateOrEditPage(final ModalWindow window, final DeviceModel deviceModel, final String configuration) {
        super();

        add(new WebMarkupContainer("create-device-title").setVisible(deviceModel == null));
        add(new WebMarkupContainer("edit-device-title").setVisible(deviceModel != null));

        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");
        form.setResourceIdPrefix("dicom.edit.device.");
        add(form);

        try {
        	if (deviceModel == null) {
        		typeModel = Model.of(ConfigTreeProvider.ConfigurationType.Basic.toString());
    			deviceNameModel = Model.of();
    			installedModel = Model.of(true);
    			schedulerIntervalModel = Model.of(ProxyDevice.DEFAULT_SCHEDULER_INTERVAL);
    			descriptionModel = Model.of();
    			deviceSerialNumberModel = Model.of();
    			institutionAddressModel = new StringArrayModel(null);
    			institutionCodeModel = new InstitutionCodeModel(null);
    			institutionalDepartmentNameModel = new StringArrayModel(null);
    			institutionNameModel = new StringArrayModel(null);
    			issuerOfAccessionNumberModel = Model.of();
    			issuerOfAdmissionIDModel = Model.of();
    			issuerOfContainerIdentifierModel = Model.of();
    			issuerOfPatientIDModel = Model.of();
    			issuerOfServiceEpisodeIDModel = Model.of();
    			issuerOfSpecimenIdentifierModel = Model.of();
    			manufacturerModel = Model.of();
    			manufacturerModelNameModel = Model.of();
    			orderFillerIdentifierModel = Model.of();
    			orderPlacerIdentifierModel = Model.of();
    			primaryDeviceTypesModel = new StringArrayModel(null);
    			relatedDeviceRefsModel = new StringArrayModel(null);
    			softwareVersionsModel = new StringArrayModel(null);
    			stationNameModel = Model.of();
    			vendorDataModel = Model.of("size 0");
    			forwardThreadsModel = Model.of(ProxyDevice.DEFAULT_FORWARD_THREADS);
        	} else {
        		typeModel = Model.of(deviceModel.getDevice() instanceof ProxyDevice ? 
	        			ConfigTreeProvider.ConfigurationType.Proxy.toString() : 
	        				ConfigTreeProvider.ConfigurationType.Basic.toString());
				deviceNameModel = Model.of(deviceModel.getDevice().getDeviceName());
				installedModel = Model.of(deviceModel.getDevice().isInstalled());
				schedulerIntervalModel = Model.of(deviceModel.getDevice() instanceof ProxyDevice ? 
	        					((ProxyDevice) deviceModel.getDevice()).getSchedulerInterval() : null);
				descriptionModel = Model.of(deviceModel.getDevice().getDescription());
				deviceSerialNumberModel = Model.of(deviceModel.getDevice().getDeviceSerialNumber());
				institutionAddressModel = new StringArrayModel(deviceModel.getDevice().getInstitutionAddresses());
				
				Code code = null;
				if (deviceModel.getDevice().getInstitutionCodes().length > 0)
					code = deviceModel.getDevice().getInstitutionCodes()[0];
	
				institutionCodeModel = new InstitutionCodeModel(code);		
				institutionalDepartmentNameModel = new StringArrayModel(deviceModel.getDevice().getInstitutionalDepartmentNames());
				institutionNameModel = new StringArrayModel(deviceModel.getDevice().getInstitutionNames());			
				if (deviceModel.getDevice().getIssuerOfAccessionNumber() == null)
					issuerOfAccessionNumberModel = Model.of();
				else
					issuerOfAccessionNumberModel = Model.of(deviceModel.getDevice().getIssuerOfAccessionNumber().toString());
				if (deviceModel.getDevice().getIssuerOfAdmissionID() == null)
					issuerOfAdmissionIDModel = Model.of();
				else
					issuerOfAdmissionIDModel = Model.of(deviceModel.getDevice().getIssuerOfAdmissionID().toString());				
				if (deviceModel.getDevice().getIssuerOfContainerIdentifier() == null)
					issuerOfContainerIdentifierModel = Model.of();
				else
					issuerOfContainerIdentifierModel = Model.of(deviceModel.getDevice().getIssuerOfContainerIdentifier().toString());
				if (deviceModel.getDevice().getIssuerOfPatientID() == null)
					issuerOfPatientIDModel = Model.of();
				else
					issuerOfPatientIDModel = Model.of(deviceModel.getDevice().getIssuerOfPatientID().toString());				
				if (deviceModel.getDevice().getIssuerOfServiceEpisodeID() == null)
					issuerOfServiceEpisodeIDModel = Model.of();
				else
					issuerOfServiceEpisodeIDModel = Model.of(deviceModel.getDevice().getIssuerOfServiceEpisodeID().toString());
				if (deviceModel.getDevice().getIssuerOfSpecimenIdentifier() == null)
					issuerOfSpecimenIdentifierModel = Model.of();
				else
					issuerOfSpecimenIdentifierModel = Model.of(deviceModel.getDevice().getIssuerOfSpecimenIdentifier().toString());
				manufacturerModel = Model.of(deviceModel.getDevice().getManufacturer());
				manufacturerModelNameModel = Model.of(deviceModel.getDevice().getManufacturerModelName());
				if (deviceModel.getDevice().getOrderFillerIdentifier() == null)
					orderFillerIdentifierModel = Model.of();
				else
					orderFillerIdentifierModel = Model.of(deviceModel.getDevice().getOrderFillerIdentifier().toString());
				if (deviceModel.getDevice().getOrderPlacerIdentifier() == null)
					orderPlacerIdentifierModel = Model.of();
				else
					orderPlacerIdentifierModel = Model.of(deviceModel.getDevice().getOrderPlacerIdentifier().toString());
				primaryDeviceTypesModel = new StringArrayModel(deviceModel.getDevice().getPrimaryDeviceTypes());
				relatedDeviceRefsModel = new StringArrayModel(deviceModel.getDevice().getRelatedDeviceRefs());
				softwareVersionsModel = new StringArrayModel(deviceModel.getDevice().getSoftwareVersions());
				stationNameModel = Model.of(deviceModel.getDevice().getStationName());
				vendorDataModel = Model.of("size " + deviceModel.getDevice().getVendorData().length);
				forwardThreadsModel = Model.of(deviceModel.getDevice() instanceof ProxyDevice ? 
     					((ProxyDevice) deviceModel.getDevice()).getForwardThreads() : null);
        	}
		} catch (ConfigurationException ce) {
			log.error(this.getClass().toString() + ": " + "Error retrieving device data: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new RuntimeException(ce);
		}

        List<CustomComponent> customComponents =
				ConfigManager.getConfigurationFor(configuration).getComponents();

        Map<String, IModel> models = new HashMap<String,IModel>();
        for (CustomComponent customComponent : customComponents) {
			models.put(customComponent.getName(), Model.of());
		}
        
        // check for storage class
        
//        models.put("dicom.edit.device.type", typeModel);
//        models.put("dicom.edit.device.title", deviceNameModel);
//        models.put("dicom.edit.device.installed", installedModel);

        form.add(new CustomComponentsPanel(
        		ConfigManager.filter(customComponents, CustomComponent.Container.MANDATORY), models));

        final Form<?> optionalContainer = new Form<Object>("optional");
        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));

        optionalContainer.add(new CustomComponentsPanel(
        		ConfigManager.filter(customComponents, CustomComponent.Container.OPTIONAL), models));

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
                	// extract method from storeTo
                	
                	
                	// check object from model, cast to class if not null
                	// else create new instance
                    
                    // call method to store value from model

//                    if (optionalContainer.isVisible()) {
//	    				device.setDescription(descriptionModel.getObject());
//	    				device.setDeviceSerialNumber(deviceSerialNumberModel.getObject());
//	    				device.setInstitutionAddresses(institutionAddressModel.getArray());
//	    				device.setInstitutionCodes(institutionCodeModel.getCode() == null ? 
//	    						new Code[] {} : new Code[] {institutionCodeModel.getCode()});
//		    			device.setInstitutionalDepartmentNames(institutionalDepartmentNameModel.getArray());
//	    				device.setInstitutionNames(institutionNameModel.getArray());    				
//	    				device.setIssuerOfAccessionNumber(issuerOfAccessionNumberModel.getObject() == null ? 
//	    						null : new Issuer(issuerOfAccessionNumberModel.getObject()));
//	    				device.setIssuerOfAdmissionID(issuerOfAdmissionIDModel.getObject() == null ? 
//	    						null : new Issuer(issuerOfAdmissionIDModel.getObject()));
//	    				device.setIssuerOfContainerIdentifier(issuerOfContainerIdentifierModel.getObject() == null ? 
//	    						null : new Issuer(issuerOfContainerIdentifierModel.getObject()));
//	    				device.setIssuerOfPatientID(issuerOfPatientIDModel.getObject() == null ? 
//	    						null : new Issuer(issuerOfPatientIDModel.getObject()));
//	    				device.setIssuerOfServiceEpisodeID(issuerOfServiceEpisodeIDModel.getObject() == null ? 
//	    						null : new Issuer(issuerOfServiceEpisodeIDModel.getObject()));
//	    				device.setIssuerOfSpecimenIdentifier(issuerOfSpecimenIdentifierModel.getObject() == null ? 
//	    						null : new Issuer(issuerOfSpecimenIdentifierModel.getObject()));
//	    				device.setManufacturer(manufacturerModel.getObject());
//	    				device.setManufacturerModelName(manufacturerModelNameModel.getObject());
//	    				device.setOrderFillerIdentifier(orderFillerIdentifierModel.getObject() == null ? 
//	    						null : new Issuer(orderFillerIdentifierModel.getObject()));
//	    				device.setOrderPlacerIdentifier(orderPlacerIdentifierModel.getObject() == null ? 
//	    						null : new Issuer(orderPlacerIdentifierModel.getObject()));
//	    				device.setPrimaryDeviceTypes(primaryDeviceTypesModel.getArray());
//	    				device.setRelatedDeviceRefs(relatedDeviceRefsModel.getArray());
//	    				device.setSoftwareVersions(softwareVersionsModel.getArray());
//	    				device.setStationName(stationNameModel.getObject());
//
//	                    if (device instanceof ProxyDevice && forwardThreadsModel.getObject() != null)
//	                    	((ProxyDevice) device).setForwardThreads(forwardThreadsModel.getObject());
//                    }
//                    
//                    if (deviceModel == null)
//                    	ConfigTreeProvider.get().persistDevice(device);
//                    else
//                    	ConfigTreeProvider.get().mergeDevice(device);                   
                    window.close(target);
        		} catch (Exception e) {
        			log.error(this.getClass().toString() + ": " + "Error modifying device: " + e.getMessage());
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
    
    @Override
    public void renderHead(IHeaderResponse response) {
    	if (CustomCreateOrEditPage.baseCSS != null) 
    		response.render(CssHeaderItem.forReference(CustomCreateOrEditPage.baseCSS));
    }
 }
