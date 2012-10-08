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

package org.dcm4chee.wizard.war.configuration.simple.edit;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
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
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.behaviours.FocusOnLoadBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.modal.MessageWindow;
import org.dcm4chee.wizard.war.common.ExtendedSecureWebPage;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.DeviceModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.InstitutionCodeModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.StringArrayModel;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeProvider.ConfigurationType;
import org.dcm4chee.wizard.war.configuration.simple.validator.CodeValidator;
import org.dcm4chee.wizard.war.configuration.simple.validator.DeviceNameValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditDevicePage extends ExtendedSecureWebPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditDevicePage.class);
    
    private static final ResourceReference BaseCSS = new CssResourceReference(BaseWicketPage.class, "base-style.css");
    
    private MessageWindow msgWin = new MessageWindow("msgWin");

    // configuration type selection
	private Model<ConfigurationType> typeModel;
	
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
	
    public CreateOrEditDevicePage(final ModalWindow window, final DeviceModel deviceModel) {
        super();

        msgWin.setTitle("");
        add(msgWin);
        add(new WebMarkupContainer("create-device-title").setVisible(deviceModel == null));
        add(new WebMarkupContainer("edit-device-title").setVisible(deviceModel != null));

        setOutputMarkupId(true);
        final BaseForm form = new BaseForm("form");
        form.setResourceIdPrefix("dicom.edit.device.");
        add(form);

        try {
        	if (deviceModel == null) {
        		typeModel = Model.of(ConfigTreeProvider.ConfigurationType.Basic);
    			deviceNameModel = Model.of();
    			installedModel = Model.of(false);
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
	        			ConfigTreeProvider.ConfigurationType.Proxy : ConfigTreeProvider.ConfigurationType.Basic);
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

        form.add(new Label("type.label", new ResourceModel("dicom.edit.device.type.label")));
        ArrayList<ConfigTreeProvider.ConfigurationType> configurationTypeList = 
        		new ArrayList<ConfigTreeProvider.ConfigurationType>();
        configurationTypeList.add(ConfigTreeProvider.ConfigurationType.Basic); 
        configurationTypeList.add(ConfigTreeProvider.ConfigurationType.Proxy);
        configurationTypeList.add(ConfigTreeProvider.ConfigurationType.Archive);
        DropDownChoice<ConfigTreeProvider.ConfigurationType> typeDropDown = 
        		new DropDownChoice<ConfigTreeProvider.ConfigurationType>("type", typeModel, configurationTypeList);
		form.add(typeDropDown
				.setNullValid(false)
				.setEnabled(deviceModel == null)
				.add(new AjaxFormComponentUpdatingBehavior("onchange") {
					
						private static final long serialVersionUID = 1L;
						
						@Override
						protected void onUpdate(AjaxRequestTarget target) {
							target.add(form.get("proxyContainer"));
						}
				}));

        form.add(new Label("title.label", new ResourceModel("dicom.edit.device.title.label")))
        .add(new TextField<String>("title", deviceNameModel)
        		.add(new DeviceNameValidator(
        				getDicomConfigurationManager().listDevices(), 
        				deviceNameModel.getObject()))
                .setRequired(true).add(FocusOnLoadBehaviour.newFocusAndSelectBehaviour())
                .setEnabled(deviceModel == null));

		} catch (ConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        form.add(new Label("installed.label", new ResourceModel("dicom.edit.device.installed.label")))
        .add(new CheckBox("installed", installedModel));

        try {
            form.add(new WebMarkupContainer("proxyContainer") {
            	
    			private static final long serialVersionUID = 1L;

    			@Override
    			public boolean isVisible() {
    				return typeModel.getObject().equals(ConfigurationType.Proxy);
    			}        	
            }.add(new Label("schedulerInterval.label", new ResourceModel("dicom.edit.device.schedulerInterval.label")))
			.add(new TextField<Integer>("schedulerInterval", schedulerIntervalModel)
            		.setType(Integer.class)
            		.setRequired(deviceModel != null &&
	            			deviceModel.getDevice() instanceof ProxyDevice ? true : false))
	            			.setOutputMarkupPlaceholderTag(true));
		} catch (ConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        final Form<?> optionalContainer = new Form<Object>("optionalContainer");
        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));

        optionalContainer.add(new Label("description.label", new ResourceModel("dicom.edit.device.description.label")))
        .add(new TextField<String>("description", descriptionModel));

        form.add(new Label("optional.label", new ResourceModel("dicom.edit.optional.label")))
        .add(new AjaxCheckBox("optional", new Model<Boolean>()) {
        	
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(optionalContainer.setVisible(this.getModelObject()));
				
			}
        });
        
        optionalContainer.add(new Label("deviceSerialNumber.label", 
        		new ResourceModel("dicom.edit.device.deviceSerialNumber.label")))
        .add(new TextField<String>("deviceSerialNumber", deviceSerialNumberModel));

        optionalContainer.add(new Label("institutionAddress.label", 
        		new ResourceModel("dicom.edit.device.institutionAddress.label")))
        .add(new TextArea<String>("institutionAddress", institutionAddressModel));
        
        TextField<String> codeValueTextField = new TextField<String>("institutionCodeValue", institutionCodeModel.getCodeFieldModel(0));
        optionalContainer.add(new Label("institutionCodeValue.label", 
        		new ResourceModel("dicom.edit.device.institutionCodeValue.label")))
        .add(codeValueTextField);

        TextField<String> codingSchemeDesignatorTextField = new TextField<String>("institutionCodingSchemeDesignator", institutionCodeModel.getCodeFieldModel(1));
        optionalContainer.add(new Label("institutionCodingSchemeDesignator.label", 
        		new ResourceModel("dicom.edit.device.institutionCodingSchemeDesignator.label")))
        .add(codingSchemeDesignatorTextField);

        TextField<String> codingSchemeVersionTextField = new TextField<String>("institutionCodingSchemeVersion", institutionCodeModel.getCodeFieldModel(2));
        optionalContainer.add(new Label("institutionCodingSchemeVersion.label", 
        		new ResourceModel("dicom.edit.device.institutionCodingSchemeVersion.label")))
        .add(codingSchemeVersionTextField);
        
        TextField<String> codeMeaningTextField = new TextField<String>("institutionCodeMeaning", institutionCodeModel.getCodeFieldModel(3));
        optionalContainer.add(new Label("institutionCodeMeaning.label", 
        		new ResourceModel("dicom.edit.device.institutionCodeMeaning.label")))
        .add(codeMeaningTextField);
        
        optionalContainer.add(new CodeValidator(codeValueTextField, codingSchemeDesignatorTextField, 
        		codingSchemeVersionTextField, codeMeaningTextField));
       
        optionalContainer.add(new Label("institutionalDepartmentName.label", 
        		new ResourceModel("dicom.edit.device.institutionalDepartmentName.label")))
        .add(new TextArea<String>("institutionalDepartmentName", institutionalDepartmentNameModel));

        optionalContainer.add(new Label("institutionName.label", 
        		new ResourceModel("dicom.edit.device.institutionName.label")))
        .add(new TextArea<String>("institutionName", institutionNameModel));

        optionalContainer.add(new Label("issuerOfAccessionNumber.label", 
        		new ResourceModel("dicom.edit.device.issuerOfAccessionNumber.label")))
        .add(new TextField<String>("issuerOfAccessionNumber", issuerOfAccessionNumberModel));

        optionalContainer.add(new Label("issuerOfAdmissionID.label", 
        		new ResourceModel("dicom.edit.device.issuerOfAdmissionID.label")))
        .add(new TextField<String>("issuerOfAdmissionID", issuerOfAdmissionIDModel));

        optionalContainer.add(new Label("issuerOfContainerIdentifier.label", 
        		new ResourceModel("dicom.edit.device.issuerOfContainerIdentifier.label")))
        .add(new TextField<String>("issuerOfContainerIdentifier", issuerOfContainerIdentifierModel));
        
        optionalContainer.add(new Label("issuerOfPatientID.label", 
        		new ResourceModel("dicom.edit.device.issuerOfPatientID.label")))
        .add(new TextField<String>("issuerOfPatientID", issuerOfPatientIDModel));

        optionalContainer.add(new Label("issuerOfServiceEpisodeID.label", 
        		new ResourceModel("dicom.edit.device.issuerOfServiceEpisodeID.label")))
        .add(new TextField<String>("issuerOfServiceEpisodeID", issuerOfServiceEpisodeIDModel));

        optionalContainer.add(new Label("issuerOfSpecimenIdentifier.label", 
        		new ResourceModel("dicom.edit.device.issuerOfSpecimenIdentifier.label")))
        .add(new TextField<String>("issuerOfSpecimenIdentifier", issuerOfSpecimenIdentifierModel));
        
        optionalContainer.add(new Label("manufacturer.label", 
        		new ResourceModel("dicom.edit.device.manufacturer.label")))
        .add(new TextField<String>("manufacturer", manufacturerModel));

        optionalContainer.add(new Label("manufacturerModelName.label", 
        		new ResourceModel("dicom.edit.device.manufacturerModelName.label")))
        .add(new TextField<String>("manufacturerModelName", manufacturerModelNameModel));

        optionalContainer.add(new Label("orderFillerIdentifier.label", 
        		new ResourceModel("dicom.edit.device.orderFillerIdentifier.label")))
        .add(new TextField<String>("orderFillerIdentifier", orderFillerIdentifierModel));

        optionalContainer.add(new Label("orderPlacerIdentifier.label", 
        		new ResourceModel("dicom.edit.device.orderPlacerIdentifier.label")))
        .add(new TextField<String>("orderPlacerIdentifier", orderPlacerIdentifierModel));
        
        optionalContainer.add(new Label("primaryDeviceTypes.label", 
        		new ResourceModel("dicom.edit.device.primaryDeviceTypes.label")))
        .add(new TextArea<String>("primaryDeviceTypes", primaryDeviceTypesModel));

        optionalContainer.add(new Label("softwareVersions.label", 
        		new ResourceModel("dicom.edit.device.softwareVersions.label")))
        .add(new TextArea<String>("softwareVersions", softwareVersionsModel));

        optionalContainer.add(new Label("stationName.label", 
        		new ResourceModel("dicom.edit.device.stationName.label")))
        .add(new TextField<String>("stationName", stationNameModel));

        optionalContainer.add(new Label("forwardThreads.label", new ResourceModel("dicom.edit.device.forwardThreads.label")) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return typeModel.getObject().equals(ConfigurationType.Proxy);
			}
		}.setOutputMarkupPlaceholderTag(true))
		.add(new TextField<Integer>("forwardThreads", forwardThreadsModel) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return typeModel.getObject().equals(ConfigurationType.Proxy);
			}
		}.setType(Integer.class)
		.add(new RangeValidator<Integer>(1,256)));

// TODO:         
        WebMarkupContainer relatedDeviceRefsContainer = 
        		new WebMarkupContainer("relatedDeviceRefsContainer");
        optionalContainer.add(relatedDeviceRefsContainer);
        relatedDeviceRefsContainer.add(new Label("relatedDeviceRefs.label", 
        		new ResourceModel("dicom.edit.device.relatedDeviceRefs.label")))
        .add(new TextArea<String>("relatedDeviceRefs", relatedDeviceRefsModel)
        		.setEnabled(false));
        relatedDeviceRefsContainer.setVisible(relatedDeviceRefsModel.getArray().length > 0);

        WebMarkupContainer vendorDataContainer = 
        		new WebMarkupContainer("vendorDataContainer");
        optionalContainer.add(vendorDataContainer);
        vendorDataContainer.add(new Label("vendorData.label", 
    			new ResourceModel("dicom.edit.applicationEntity.vendorData.label")))
    	.add(new Label("vendorData", vendorDataModel));      
			vendorDataContainer.setVisible(!vendorDataModel.getObject().equals("size 0"));
    	
        form.add(new AjaxButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                	Device device = null;
                	if (deviceModel != null) 
                		device = deviceModel.getDevice();
                	else 
                		device = typeModel.getObject().equals(ConfigurationType.Proxy) ? 
                				new ProxyDevice(deviceNameModel.getObject()) : 
                         		new Device(deviceNameModel.getObject());
                    device.setInstalled(installedModel.getObject());
                    if (device instanceof ProxyDevice) 
                    	((ProxyDevice) device).setSchedulerInterval(schedulerIntervalModel.getObject());

//                    for (String s : authorizedNodeCertificateReferenceModel.getArray()) {
//                    	for (X509Certificate c : device..getAllAuthorizedNodeCertificates()) {
//                    		if c.getSigAlgName().equals(s)
//                    			device.setAuthorizedNodeCertificates(ref, certs)
//                    	}
//                    device.setAuthorizedNodeCertificates(authorizedNodeCertificateReferenceModel.getArray());
//                    }
                    
//                    device.setThisNodeCertificates(ref, certs)
//                    device.re
                    
                    // this node cert -> eigenes

                    if (optionalContainer.isVisible()) {
	    				device.setDescription(descriptionModel.getObject());
	    				device.setDeviceSerialNumber(deviceSerialNumberModel.getObject());
	    				device.setInstitutionAddresses(institutionAddressModel.getArray());
	    				device.setInstitutionCodes(institutionCodeModel.getCode() == null ? 
	    						new Code[] {} : new Code[] {institutionCodeModel.getCode()});
		    			device.setInstitutionalDepartmentNames(institutionalDepartmentNameModel.getArray());
	    				device.setInstitutionNames(institutionNameModel.getArray());    				
	    				device.setIssuerOfAccessionNumber(issuerOfAccessionNumberModel.getObject() == null ? 
	    						null : new Issuer(issuerOfAccessionNumberModel.getObject()));
	    				device.setIssuerOfAdmissionID(issuerOfAdmissionIDModel.getObject() == null ? 
	    						null : new Issuer(issuerOfAdmissionIDModel.getObject()));
	    				device.setIssuerOfContainerIdentifier(issuerOfContainerIdentifierModel.getObject() == null ? 
	    						null : new Issuer(issuerOfContainerIdentifierModel.getObject()));
	    				device.setIssuerOfPatientID(issuerOfPatientIDModel.getObject() == null ? 
	    						null : new Issuer(issuerOfPatientIDModel.getObject()));
	    				device.setIssuerOfServiceEpisodeID(issuerOfServiceEpisodeIDModel.getObject() == null ? 
	    						null : new Issuer(issuerOfServiceEpisodeIDModel.getObject()));
	    				device.setIssuerOfSpecimenIdentifier(issuerOfSpecimenIdentifierModel.getObject() == null ? 
	    						null : new Issuer(issuerOfSpecimenIdentifierModel.getObject()));
	    				device.setManufacturer(manufacturerModel.getObject());
	    				device.setManufacturerModelName(manufacturerModelNameModel.getObject());
	    				device.setOrderFillerIdentifier(orderFillerIdentifierModel.getObject() == null ? 
	    						null : new Issuer(orderFillerIdentifierModel.getObject()));
	    				device.setOrderPlacerIdentifier(orderPlacerIdentifierModel.getObject() == null ? 
	    						null : new Issuer(orderPlacerIdentifierModel.getObject()));
	    				device.setPrimaryDeviceTypes(primaryDeviceTypesModel.getArray());
	    				device.setRelatedDeviceRefs(relatedDeviceRefsModel.getArray());
	    				device.setSoftwareVersions(softwareVersionsModel.getArray());
	    				device.setStationName(stationNameModel.getObject());

	                    if (device instanceof ProxyDevice && forwardThreadsModel.getObject() != null)
	                    	((ProxyDevice) device).setForwardThreads(forwardThreadsModel.getObject());
                    }
                    
                    if (deviceModel == null)
                    	ConfigTreeProvider.get().persistDevice(device).setModel(null);
                    else
                    	ConfigTreeProvider.get().mergeDevice(device);                   
                    window.close(target);
                } catch (Exception e) {
                	log.error("Error modifying device", e);
                    msgWin.show(target, new ResourceModel(deviceModel == null ? 
                    		"dicom.edit.device.create.failed" : "dicom.edit.device.update.failed")
                    		.wrapOnAssignment(this));
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
        if (CreateOrEditDevicePage.BaseCSS != null)
        	response.renderCSSReference(CreateOrEditDevicePage.BaseCSS);
    }
 }
