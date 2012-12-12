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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
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
import org.dcm4chee.wizard.common.behavior.FocusOnLoadBehavior;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ExtendedWebPage;
import org.dcm4chee.wizard.war.common.component.ExtendedSecureWebPage;
import org.dcm4chee.wizard.war.configuration.common.custom.ConfigManager;
import org.dcm4chee.wizard.war.configuration.common.custom.CustomComponent;
import org.dcm4chee.wizard.war.configuration.common.custom.CustomComponentPanel;
import org.dcm4chee.wizard.war.configuration.common.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.war.configuration.common.tree.ConfigTreeProvider.ConfigurationType;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.DeviceModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.InstitutionCodeModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.StringArrayModel;
import org.dcm4chee.wizard.war.configuration.simple.validator.CodeValidator;
import org.dcm4chee.wizard.war.configuration.simple.validator.DeviceNameValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditDevicePage extends CustomCreateOrEditPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditDevicePage.class);
    
    private static final ResourceReference baseCSS = new CssResourceReference(ExtendedWebPage.class, "base-style.css");
    
	private Model<ConfigurationType> typeModel;
    private Model<String> deviceNameModel;
    
    Device device;
    boolean create;
	
    public CreateOrEditDevicePage(final ModalWindow window, final DeviceModel deviceModel, final String configuration) {
        super(window, deviceModel, configuration);

        add(new WebMarkupContainer("create-device-title").setVisible(deviceModel == null));
        add(new WebMarkupContainer("edit-device-title").setVisible(deviceModel != null));

//        setOutputMarkupId(true);
//        final ExtendedForm form = new ExtendedForm("form");
//        form.setResourceIdPrefix("dicom.edit.device.");
//        add(form);

        try {
        	if (deviceModel == null) {
        		create = true;
        		typeModel = Model.of(ConfigTreeProvider.ConfigurationType.Basic);
    			deviceNameModel = Model.of();
        	} else {
        		create = false;
        		typeModel = Model.of(deviceModel.getDevice() instanceof ProxyDevice ? 
	        			ConfigTreeProvider.ConfigurationType.Proxy : ConfigTreeProvider.ConfigurationType.Basic);
				deviceNameModel = Model.of(deviceModel.getDevice().getDeviceName());				
				device = deviceModel.getDevice();
        	}
		} catch (ConfigurationException ce) {
			log.error(this.getClass().toString() + ": " + "Error retrieving device data: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new RuntimeException(ce);
		}

        form.add(new Label("type.label", new ResourceModel("dicom.edit.device.type.label")));
        ArrayList<ConfigTreeProvider.ConfigurationType> configurationTypeList = 
        		new ArrayList<ConfigTreeProvider.ConfigurationType>();
        configurationTypeList.add(ConfigTreeProvider.ConfigurationType.Basic); 
        configurationTypeList.add(ConfigTreeProvider.ConfigurationType.Proxy);
        configurationTypeList.add(ConfigTreeProvider.ConfigurationType.Archive);
        final DropDownChoice<ConfigTreeProvider.ConfigurationType> typeDropDown = 
        		new DropDownChoice<ConfigTreeProvider.ConfigurationType>("typeSelection", typeModel, configurationTypeList);
		form.add(typeDropDown
				.setNullValid(false)
				.setEnabled(deviceModel == null)
				.add(new AjaxFormComponentUpdatingBehavior("onchange") {
					
						private static final long serialVersionUID = 1L;
						
						@Override
						protected void onUpdate(AjaxRequestTarget target) {
							boolean visible = !typeDropDown.getModelObject()
									.equals(ConfigTreeProvider.ConfigurationType.Basic);
							target.add(typeContainer.setVisible(visible));
							target.add(optionalTypeContainer.setVisible(visible));
						}
				}));

		boolean visible = !typeDropDown.getModelObject()
				.equals(ConfigTreeProvider.ConfigurationType.Basic);
		typeContainer.setVisible(visible);
		optionalTypeContainer.setVisible(visible);

        try {
			form.add(new Label("title.label", new ResourceModel("dicom.edit.device.title.label")))
			.add(new TextField<String>("title", deviceNameModel)
					.add(new DeviceNameValidator(
							getDicomConfigurationManager().listDevices(), 
							deviceNameModel.getObject()))
			        .setRequired(true).add(FocusOnLoadBehavior.newFocusAndSelectBehaviour())
			        .setEnabled(deviceModel == null));
		} catch (ConfigurationException ce) {
			log.error(this.getClass().toString() + ": " + "Error listing devices: " + ce.getMessage());
            log.debug("Exception", ce);
            throw new RuntimeException(ce);
		}
    }
    
    public Serializable onBeforeSave() {
    	if (device == null)
    		device = new Device(deviceNameModel.getObject());
    	return device;
    }

    public void onAfterSave() throws ConfigurationException, IOException {
        if (create)
        	ConfigTreeProvider.get().persistDevice(device);
        else
        	ConfigTreeProvider.get().mergeDevice(device);                   
    }

    @Override
    public void renderHead(IHeaderResponse response) {
    	if (CreateOrEditDevicePage.baseCSS != null) 
    		response.render(CssHeaderItem.forReference(CreateOrEditDevicePage.baseCSS));
    }
 }
