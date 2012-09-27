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

package org.dcm4chee.wizard.war.configuration.simple.tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4che.conf.api.AttributeCoercion;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Device;
import org.dcm4che.net.TransferCapability;
import org.dcm4chee.proxy.conf.ForwardRule;
import org.dcm4chee.proxy.conf.ProxyApplicationEntity;
import org.dcm4chee.proxy.conf.ProxyDevice;
import org.dcm4chee.proxy.conf.Retry;
import org.dcm4chee.proxy.conf.Schedule;
import org.dcm4chee.wizard.war.DicomConfigurationManager;
import org.dcm4chee.wizard.war.Utils;
import org.dcm4chee.wizard.war.WicketApplication;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.ApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.ConnectionModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.DeviceModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.TransferCapabilityModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.CoercionModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.ForwardRuleModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.ForwardScheduleModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.ProxyApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.ProxyDeviceModel;
import org.dcm4chee.wizard.war.configuration.simple.model.proxy.RetryModel;

import wickettree.util.SortableTreeProvider;

/**
 * @author Robert David
 */
public class ConfigTreeProvider extends SortableTreeProvider<ConfigTreeNode> {

	private static final long serialVersionUID = 1L;
	
	public enum ConfigurationType {Basic, Proxy, Archive};

	private List<ConfigTreeNode> deviceNodeList = new ArrayList<ConfigTreeNode>();
	private Set<String> uniqueAETitles = new HashSet<String>();

	private Component forComponent;
	
	private boolean resync = false;

	private ConfigTreeProvider(Component forComponent) throws ConfigurationException {
		this.forComponent = forComponent;
		loadDeviceList();
	}

	private void loadDeviceList() throws ConfigurationException {
		List<String> deviceList = 
				getDicomConfigurationManager().listDevices();

		if (deviceList == null)
			return;
		
		Collections.sort(deviceList);
		for (String deviceName : deviceList) {

			// CREATE DEVICE NODE AND MODEL
			DeviceModel deviceModel
//				= (this.getConfigurationType(device).equals(ConfigurationType.Proxy)) ? 
//						new ProxyDeviceModel((ProxyDevice) device) : new DeviceModel(device);
			= new DeviceModel(deviceName);
			
//				this.deviceModelList.put(device.getDeviceName(), deviceModel);
			ConfigTreeNode deviceNode = 
					new ConfigTreeNode(null, deviceModel.getDeviceName(), 
							ConfigTreeNode.TreeNodeType.DEVICE, 
							ConfigurationType.Basic, deviceModel);
			deviceNodeList.add(deviceNode);
			addDeviceSubnodes(deviceNode);
		}
	}

	public void loadDevice(ConfigTreeNode deviceNode) throws ConfigurationException {

		Device device = getDicomConfigurationManager().getDevice(deviceNode.getName());
				
		// CREATE DEVICE NODE AND MODEL
		DeviceModel deviceModel
			= (this.getConfigurationType(device).equals(ConfigurationType.Proxy)) ? 
					new ProxyDeviceModel((ProxyDevice) device) : new DeviceModel(device);
		
		deviceNode.setConfigurationType(getConfigurationType(device));
		deviceNode.setModel(deviceModel);
		
		// CREATE CONNECTION NODE AND MODEL
		deviceNode.getContainer(ConfigTreeNode.CONTAINER_CONNECTIONS).removeChildren();
		for (ConnectionModel connectionModel : deviceModel.getConnections())
			new ConfigTreeNode(deviceNode.getContainer(ConfigTreeNode.CONTAINER_CONNECTIONS), 
					connectionModel.getConnection().getCommonName() == null ? 
							connectionModel.getConnection().getHostname() : 
								connectionModel.getConnection().getCommonName(), 
					ConfigTreeNode.TreeNodeType.CONNECTION, connectionModel);
		Collections.sort(deviceNode.getContainer(
				ConfigTreeNode.CONTAINER_CONNECTIONS).getChildren());

		// CREATE AE NODE AND MODEL
		deviceNode.getContainer(ConfigTreeNode.CONTAINER_APPLICATION_ENTITIES).removeChildren();
		for (ApplicationEntityModel applicationEntityModel : deviceModel.getApplicationEntities().values()) {
			ConfigTreeNode aeNode = 
					new ConfigTreeNode(deviceNode.getContainer(ConfigTreeNode.CONTAINER_APPLICATION_ENTITIES), 
							applicationEntityModel.getApplicationEntity().getAETitle(), 
							ConfigTreeNode.TreeNodeType.APPLICATION_ENTITY, 
							this.getConfigurationType(applicationEntityModel.getApplicationEntity()), 
							applicationEntityModel);

			uniqueAETitles.add(applicationEntityModel.getApplicationEntity().getAETitle());
			
			this.addApplicationEntitySubnodes(aeNode);
			
			Map<String, ConfigTreeNode> typeNodes = 
					new HashMap<String, ConfigTreeNode>();

			ConfigTreeNode otherNode = null;
			if (applicationEntityModel.getTransferCapabilities().size() > 0)
				otherNode = 
					new ConfigTreeNode(aeNode.getContainer(ConfigTreeNode.CONTAINER_TRANSFER_CAPABILITIES), 
						new ResourceModel("dicom.list.transferCapabilities.other.label").wrapOnAssignment(forComponent).getObject(), 
						ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITY_TYPE, null);
			
			for (TransferCapabilityModel transferCapabilityModel : applicationEntityModel.getTransferCapabilities()) {

				ConfigTreeNode typeNode = null;
				if (DicomConfigurationManager.getTransferCapabilityTypes()
						.containsKey(transferCapabilityModel.getTransferCapability().getSopClass())) {

					String type = DicomConfigurationManager.getTransferCapabilityTypes()
							.get(transferCapabilityModel.getTransferCapability().getSopClass());
					
					if (typeNodes.containsKey(type)) 
						typeNode = typeNodes.get(type);	
					else {
						typeNode = new ConfigTreeNode(aeNode.getContainer(ConfigTreeNode.CONTAINER_TRANSFER_CAPABILITIES), 
								type, 
								ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITY_TYPE, null);
						typeNodes.put(type, typeNode);
					}
				} else 
					typeNode = otherNode;								
				new ConfigTreeNode(typeNode, 
						transferCapabilityModel.getTransferCapability().getCommonName() != null ? 
								transferCapabilityModel.getTransferCapability().getCommonName() :  
								transferCapabilityModel.getTransferCapability().getSopClass() + " " + 
								transferCapabilityModel.getTransferCapability().getRole(),
								ConfigTreeNode.TreeNodeType.TRANSFER_CAPABILITY, 
								transferCapabilityModel);
			}
			if (otherNode != null)
				Collections.sort(otherNode.getChildren());
			for (ConfigTreeNode typeNode: typeNodes.values())
				Collections.sort(typeNode.getChildren());
			Collections.sort(aeNode.getContainer(ConfigTreeNode.CONTAINER_TRANSFER_CAPABILITIES).getChildren());
			
			if (this.getConfigurationType(applicationEntityModel.getApplicationEntity())
					.equals(ConfigurationType.Proxy)) {
				
				for (ForwardRuleModel forwardRuleModel : 
					((ProxyApplicationEntityModel) applicationEntityModel).getForwardRules()) {
					new ConfigTreeNode(aeNode.getContainer(ConfigTreeNode.CONTAINER_FORWARD_RULES), 
							forwardRuleModel.getForwardRule().getCommonName(), 
							ConfigTreeNode.TreeNodeType.FORWARD_RULE, forwardRuleModel);
				}

				for (ForwardScheduleModel forwardScheduleModel : 
					((ProxyApplicationEntityModel) applicationEntityModel).getForwardSchedules()) {
					new ConfigTreeNode(aeNode.getContainer(ConfigTreeNode.CONTAINER_FORWARD_SCHEDULES), 
							forwardScheduleModel.getDestinationAETitle(), 
							ConfigTreeNode.TreeNodeType.FORWARD_SCHEDULE, forwardScheduleModel);
				}

				for (RetryModel retryModel : 
					((ProxyApplicationEntityModel) applicationEntityModel).getRetries()) {
					new ConfigTreeNode(aeNode.getContainer(ConfigTreeNode.CONTAINER_RETRIES), 
							retryModel.getRetry().getSuffix(), 
							ConfigTreeNode.TreeNodeType.RETRY, retryModel);
				}
				
				for (CoercionModel coercionModel : 
					((ProxyApplicationEntityModel) applicationEntityModel).getCoercions()) {
					AttributeCoercion coercion = coercionModel.getCoercion();
					new ConfigTreeNode(aeNode.getContainer(ConfigTreeNode.CONTAINER_COERCION), 
							coercion.getDimse().toString() + " " +
							coercion.getRole().toString() + " " + 
							(coercion.getAETitle() != null ? 
									coercion.getAETitle() + " " :  "") + 
							(coercion.getSopClass() != null ? 
									coercion.getSopClass() + " " :  ""),  
							ConfigTreeNode.TreeNodeType.COERCION, coercionModel);
				}
			}
		}
	}
	
	private DicomConfigurationManager getDicomConfigurationManager() {
		return ((WicketApplication) Application.get()).getDicomConfigurationManager();
	}

	public static ConfigTreeProvider get() {
		return (ConfigTreeProvider) Session.get().getAttribute("deviceTreeProvider");
	}

	public static ConfigTreeProvider set(Component forComponent) throws ConfigurationException {
		ConfigTreeProvider deviceTreeProvider = new ConfigTreeProvider(forComponent);
		Session.get().setAttribute("deviceTreeProvider", deviceTreeProvider);
		return deviceTreeProvider;
	}

	private void saveToSession() {
		Session.get().setAttribute("deviceTreeProvider", this);
	}

	public void registerAETitle(String aeTitle) throws ConfigurationException {
		getDicomConfigurationManager().getDicomConfiguration().registerAETitle(aeTitle);
        uniqueAETitles.add(aeTitle);
	}

	public void unregisterAETitle(String aeTitle) throws ConfigurationException {
		getDicomConfigurationManager().getDicomConfiguration().unregisterAETitle(aeTitle);
        uniqueAETitles.remove(aeTitle);
	}

	public Set<String> getUniqueAETitles() {
		return uniqueAETitles;
	}
	
	private void addDeviceSubnodes(ConfigTreeNode deviceNode) {
		
		// CREATE CONNECTIONS FOLDER
		new ConfigTreeNode(deviceNode, 
				new ResourceModel("dicom.list.connections.label").wrapOnAssignment(forComponent).getObject(), 
				ConfigTreeNode.TreeNodeType.CONTAINER_CONNECTIONS, null);
		
		// CREATE AE FOLDER
		new ConfigTreeNode(deviceNode, 
				new ResourceModel("dicom.list.applicationEntities.label").wrapOnAssignment(forComponent).getObject(), 
				ConfigTreeNode.TreeNodeType.CONTAINER_APPLICATION_ENTITIES, null);
	}

	private void addApplicationEntitySubnodes(ConfigTreeNode aeNode) throws ConfigurationException {
		
		// CREATE TC FOLDER
		new ConfigTreeNode(aeNode, 
			new ResourceModel("dicom.list.transferCapabilities.label").wrapOnAssignment(forComponent).getObject(),
			ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITIES, null);
		
		if (this.getConfigurationType(((ApplicationEntityModel) aeNode.getModel()).getApplicationEntity())
				.equals(ConfigurationType.Proxy)) {
			
			// CREATE FORWARD RULES FOLDER
			new ConfigTreeNode(aeNode, 
				new ResourceModel("dicom.list.forwardRules.label").wrapOnAssignment(forComponent).getObject(),
				ConfigTreeNode.TreeNodeType.CONTAINER_FORWARD_RULES, null);
	
			// CREATE FORWARD SCHEDULES FOLDER
			new ConfigTreeNode(aeNode, 
				new ResourceModel("dicom.list.forwardSchedules.label").wrapOnAssignment(forComponent).getObject(),
				ConfigTreeNode.TreeNodeType.CONTAINER_FORWARD_SCHEDULES, null);
			
			// CREATE RETRIES FOLDER
			new ConfigTreeNode(aeNode, 
				new ResourceModel("dicom.list.retries.label").wrapOnAssignment(forComponent).getObject(),
				ConfigTreeNode.TreeNodeType.CONTAINER_RETRIES, null);
			
			// CREATE COERCIONS FOLDER
			new ConfigTreeNode(aeNode, 
				new ResourceModel("dicom.list.coercions.label").wrapOnAssignment(forComponent).getObject(),
				ConfigTreeNode.TreeNodeType.CONTAINER_COERCIONS, null);
		}
	}
	
	public Iterator<? extends ConfigTreeNode> getRoots() {
		return deviceNodeList.iterator();
	}

	public boolean hasChildren(ConfigTreeNode treeNode) {
		return treeNode.hasChildren();
	}

	public Iterator<? extends ConfigTreeNode> getChildren(ConfigTreeNode treeNode) {
		return treeNode.getChildren().iterator();
	}

	public IModel<ConfigTreeNode> model(ConfigTreeNode treeNode) {
		return Model.of(treeNode);
	}

	public List<ConfigTreeNode> getNodeList() {
		for (ConfigTreeNode node : deviceNodeList)
			Utils.prettyPrintln(node);
		return deviceNodeList;
	}

	public ConfigTreeNode persistDevice(Device device) throws ConfigurationException {
		getDicomConfigurationManager().save(device);
		ConfigTreeNode deviceNode = 
				new ConfigTreeNode(null, device.getDeviceName(), 
						ConfigTreeNode.TreeNodeType.DEVICE,
						getConfigurationType(device),  
						new DeviceModel(device));
		deviceNodeList.add(deviceNode);
		Collections.sort(deviceNodeList);
		addDeviceSubnodes(deviceNode);
		return deviceNode;
	}

	public void removeDevice(ConfigTreeNode deviceNode) throws ConfigurationException {
		getDicomConfigurationManager().remove(((DeviceModel) deviceNode.getModel()).getDeviceName());
		deviceNodeList.remove(deviceNode);
		resync = true;
	}

//	public void addConnection(Connection connection) throws ConfigurationException, IOException {
//        getDicomConfigurationManager().persist(connection.getDevice());
//        Utils.prettyPrintln("Added Connection");
//        saveToSession();
//	}
//
//	public void editConnection(Connection connection) throws IOException, ConfigurationException {
//        getDicomConfigurationManager().persist(connection.getDevice());
//        Utils.prettyPrintln("Edited Connection");
//        saveToSession();
//	}

	public void mergeDevice(Device device) throws IOException, ConfigurationException {
		getDicomConfigurationManager().save(device);
		Utils.prettyPrintln("Persisted device");
		saveToSession();
	}

	public void removeConnection(ConfigTreeNode connectionNode) throws ConfigurationException {
		Device device = ((DeviceModel) connectionNode.getParent().getParent().getModel()).getDevice();
        device.removeConnection(((ConnectionModel) connectionNode.getModel()).getConnection());
        getDicomConfigurationManager().save(device);
//		
//		((DeviceModel) connectionNode.getParent().getParent().getModel())
//			.getConnections().remove(connectionNode.getModel());
//		
//		ConfigTreeNode connectionsNode = connectionNode.getParent();
//		connectionNode.remove();
//		Collections.sort(connectionsNode.getChildren());
	}

	public void addApplicationEntity(ConfigTreeNode aesNode, ApplicationEntity applicationEntity) throws ConfigurationException {
		Device device = ((DeviceModel) aesNode.getParent().getModel()).getDevice();
        device.addApplicationEntity(applicationEntity);
        getDicomConfigurationManager().save(device);
        
        registerAETitle(applicationEntity.getAETitle());
        
        ConfigTreeNode aeNode = new ConfigTreeNode(aesNode, applicationEntity.getAETitle(), 
				ConfigTreeNode.TreeNodeType.APPLICATION_ENTITY, 
				getConfigurationType(applicationEntity), 
				new ApplicationEntityModel(applicationEntity));
		Collections.sort(aesNode.getChildren());
		addApplicationEntitySubnodes(aeNode);
	}

	public void editApplicationEntity(ConfigTreeNode aesNode, ApplicationEntity applicationEntity, 
			String oldAETitle) throws ConfigurationException {
		unregisterAETitle(oldAETitle);
        getDicomConfigurationManager().save(applicationEntity.getDevice());
        registerAETitle(applicationEntity.getAETitle());

        Utils.prettyPrintln("Edited AE, setting model of node " + aesNode.getParent().getName() + " to null");
        aesNode.getParent().setModel(null);
//        for (ConfigTreeNode aeNode : aesNode.getChildren())
//        	if (aeNode.getName().equals(oldAETitle)) {
//        		aeNode.setModel(new ApplicationEntityModel(applicationEntity));
//        		aeNode.setName(applicationEntity.getAETitle());
//        	}
//		Collections.sort(aesNode.getChildren());
	}
	
	public void removeApplicationEntity(ConfigTreeNode aeNode) throws ConfigurationException {
		Device device = ((DeviceModel) aeNode.getParent().getParent().getModel()).getDevice();
		ApplicationEntity applicationEntity = ((ApplicationEntityModel) aeNode.getModel())
				.getApplicationEntity();
        device.removeApplicationEntity(applicationEntity);
        getDicomConfigurationManager().save(device);

        unregisterAETitle(applicationEntity.getAETitle());
        
		((DeviceModel) aeNode.getParent().getParent().getModel())
			.getApplicationEntities().remove(aeNode.getModel());
		
		ConfigTreeNode aesNode = aeNode.getParent();
		aeNode.remove();
		Collections.sort(aesNode.getChildren());
	}

	public void addTransferCapability(ConfigTreeNode tcsNode,
			TransferCapability transferCapability) throws ConfigurationException {
		ApplicationEntity applicationEntity = 
				((ApplicationEntityModel) tcsNode.getParent().getModel()).getApplicationEntity();
		applicationEntity.addTransferCapability(transferCapability);
		getDicomConfigurationManager().save(applicationEntity.getDevice());

        String typeName = null;
		ConfigTreeNode typeNode = null;
		getDicomConfigurationManager();
		if (DicomConfigurationManager.getTransferCapabilityTypes()
				.containsKey(transferCapability.getSopClass())) {
			getDicomConfigurationManager();
			typeName = DicomConfigurationManager.getTransferCapabilityTypes()
					.get(transferCapability.getSopClass());
		} else 
			typeName = new ResourceModel("dicom.list.tcs.other.label").wrapOnAssignment(forComponent).getObject();
			
		for (ConfigTreeNode node : tcsNode.getChildren())
			if (node.getName().equals(typeName))
				typeNode = node;
		
		if (typeNode == null)
			typeNode = new ConfigTreeNode(tcsNode, typeName, 
					ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITY_TYPE, null);
			
		new ConfigTreeNode(typeNode, 
				transferCapability.getCommonName() != null ? transferCapability.getCommonName() : 
					transferCapability.getSopClass() + " " + transferCapability.getRole(), 
				ConfigTreeNode.TreeNodeType.TRANSFER_CAPABILITY, 
				new TransferCapabilityModel(transferCapability, applicationEntity.getAETitle()));
		Collections.sort(typeNode.getChildren());
	}

	public void editTransferCapability(ConfigTreeNode tcsNode, ConfigTreeNode tcNode,  
			TransferCapability transferCapability) throws ConfigurationException {
		ApplicationEntity applicationEntity = 
				((ApplicationEntityModel) tcsNode.getParent().getModel()).getApplicationEntity();
		getDicomConfigurationManager().save(applicationEntity.getDevice());

		ConfigTreeNode typeNode = tcNode.getParent();
		tcNode.remove();

        String typeName = null;
		typeNode = null;
		getDicomConfigurationManager();
		if (DicomConfigurationManager.getTransferCapabilityTypes()
				.containsKey(transferCapability.getSopClass())) {
			getDicomConfigurationManager();
			typeName = DicomConfigurationManager.getTransferCapabilityTypes()
					.get(transferCapability.getSopClass());
		} else 
			typeName = new ResourceModel("dicom.list.tcs.other.label").wrapOnAssignment(forComponent).getObject();
			
		for (ConfigTreeNode node : tcsNode.getChildren())
			if (node.getName().equals(typeName))
				typeNode = node;
		
		if (typeNode == null)
			typeNode = new ConfigTreeNode(tcsNode, typeName, 
					ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITY_TYPE, null);
			
		new ConfigTreeNode(typeNode, 
				transferCapability.getCommonName() != null ? transferCapability.getCommonName() : 
					transferCapability.getSopClass() + " " + transferCapability.getRole(), 
				ConfigTreeNode.TreeNodeType.TRANSFER_CAPABILITY, 
				new TransferCapabilityModel(transferCapability, applicationEntity.getAETitle()));
		Collections.sort(typeNode.getChildren());
	}

	public void removeTransferCapability(ConfigTreeNode tcNode) throws ConfigurationException {
		ApplicationEntity applicationEntity = 
				((ApplicationEntityModel) tcNode.getParent().getParent().getParent().getModel()).getApplicationEntity();
		applicationEntity.removeTransferCapability(((TransferCapabilityModel) tcNode.getModel()).getTransferCapability());
		getDicomConfigurationManager().save(applicationEntity.getDevice());

		((ApplicationEntityModel) tcNode.getParent().getParent().getParent().getModel())
			.getTransferCapabilities().remove(tcNode.getModel());
        
		ConfigTreeNode typeNode = tcNode.getParent();
		tcNode.remove();
		Collections.sort(typeNode.getChildren());
	}
	
	public void addForwardRule(ConfigTreeNode frsNode, ForwardRule forwardRule) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity) 
				((ApplicationEntityModel) frsNode.getParent().getModel()).getApplicationEntity();
		List<ForwardRule> forwardRules = proxyApplicationEntity.getForwardRules();
		forwardRules.add(forwardRule);
		proxyApplicationEntity.setForwardRules(forwardRules);
		getDicomConfigurationManager().save(proxyApplicationEntity.getDevice());

		new ConfigTreeNode(frsNode, 
				forwardRule.getCommonName(), 
				ConfigTreeNode.TreeNodeType.FORWARD_RULE, 
				new ForwardRuleModel(forwardRule));
		Collections.sort(frsNode.getChildren());
	}

	public void editForwardRule(ConfigTreeNode frsNode,
			ConfigTreeNode frNode, ForwardRule forwardRule) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) frNode.getParent().getParent().getModel()).getApplicationEntity();
		getDicomConfigurationManager().save(proxyApplicationEntity.getDevice());

		frNode.remove();
		new ConfigTreeNode(frsNode, 
				forwardRule.getCommonName(), 
				ConfigTreeNode.TreeNodeType.FORWARD_RULE, 
				new ForwardRuleModel(forwardRule));
		Collections.sort(frsNode.getChildren());
	}

	public void removeForwardRule(ConfigTreeNode frNode) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) frNode.getParent().getParent().getModel()).getApplicationEntity();
		List<ForwardRule> forwardRules = proxyApplicationEntity.getForwardRules();
		forwardRules.remove(((ForwardRuleModel) frNode.getModel()).getForwardRule());
		 getDicomConfigurationManager().save(proxyApplicationEntity.getDevice());

		ConfigTreeNode frsNode = frNode.getParent();
		frNode.remove();
		Collections.sort(frsNode.getChildren());
	}

	public void addForwardSchedule(ConfigTreeNode fssNode, 
			String destinationAETitle, Schedule schedule) throws ConfigurationException {
    	ProxyApplicationEntity proxyApplicationEntity = 
    			((ProxyApplicationEntityModel) fssNode.getParent().getModel()).getApplicationEntity();
    	proxyApplicationEntity.getForwardSchedules().put(destinationAETitle, schedule);
        getDicomConfigurationManager().save(proxyApplicationEntity.getDevice());
        
		new ConfigTreeNode(fssNode, 
				destinationAETitle, 
				ConfigTreeNode.TreeNodeType.FORWARD_SCHEDULE, 
				new ForwardScheduleModel(destinationAETitle, schedule));
		Collections.sort(fssNode.getChildren());
	}

	public void editForwardSchedule(ConfigTreeNode fssNode, ConfigTreeNode fsNode, 
			String destinationAETitle, Schedule schedule) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) fsNode.getParent().getParent().getModel()).getApplicationEntity();
        getDicomConfigurationManager().save(proxyApplicationEntity.getDevice());

		fsNode.remove();
		new ConfigTreeNode(fssNode, 
				destinationAETitle, 
				ConfigTreeNode.TreeNodeType.FORWARD_SCHEDULE, 
				new ForwardScheduleModel(destinationAETitle, schedule));
		Collections.sort(fssNode.getChildren());
	}

	public void removeForwardSchedule(ConfigTreeNode fsNode) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) fsNode.getParent().getParent().getModel()).getApplicationEntity();
		proxyApplicationEntity.getForwardSchedules()
			.remove(((ForwardScheduleModel) fsNode.getModel()).getDestinationAETitle());
		getDicomConfigurationManager().save(proxyApplicationEntity.getDevice());

		ConfigTreeNode fssNode = fsNode.getParent();
		fsNode.remove();
		Collections.sort(fssNode.getChildren());
	}

	public void addRetry(ConfigTreeNode rtsNode, Retry retry) throws ConfigurationException {
    	ProxyApplicationEntity proxyApplicationEntity = 
    			((ProxyApplicationEntityModel) rtsNode.getParent().getModel()).getApplicationEntity();
    	proxyApplicationEntity.getRetries().add(retry);    	   	
        getDicomConfigurationManager().save(proxyApplicationEntity.getDevice());
        
		new ConfigTreeNode(rtsNode, 
				retry.getSuffix(), 
				ConfigTreeNode.TreeNodeType.RETRY, 
				new RetryModel(retry));
		Collections.sort(rtsNode.getChildren());
	}

	public void editRetry(ConfigTreeNode rtsNode,
			ConfigTreeNode rtNode, Retry retry) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) rtNode.getParent().getParent().getModel()).getApplicationEntity();
        getDicomConfigurationManager().save(proxyApplicationEntity.getDevice());
        
		rtNode.remove();        
		new ConfigTreeNode(rtsNode, 
				retry.getSuffix(), 
				ConfigTreeNode.TreeNodeType.RETRY, 
				new RetryModel(retry));
		Collections.sort(rtsNode.getChildren());
	}

	public void removeRetry(ConfigTreeNode rtNode) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) rtNode.getParent().getParent().getModel()).getApplicationEntity();
		List<Retry> retries = proxyApplicationEntity.getRetries();
		retries.remove(((RetryModel) rtNode.getModel()).getRetry());
		getDicomConfigurationManager().save(proxyApplicationEntity.getDevice());

		ConfigTreeNode rtsNode = rtNode.getParent();
		rtNode.remove();
		Collections.sort(rtsNode.getChildren());
	}

	public void addCoercion(ConfigTreeNode csNode, AttributeCoercion coercion) throws ConfigurationException {
    	ProxyApplicationEntity proxyApplicationEntity = 
    			((ProxyApplicationEntityModel) csNode.getParent().getModel()).getApplicationEntity();
    	proxyApplicationEntity.getAttributeCoercions().add(coercion);
    	getDicomConfigurationManager().save(proxyApplicationEntity.getDevice());
        
		new ConfigTreeNode(csNode, 
				coercion.getDimse().toString() + " " +
				coercion.getRole().toString() + " " + 
				(coercion.getAETitle() != null ? 
						coercion.getAETitle() + " " :  "") + 
				(coercion.getSopClass() != null ? 
						coercion.getSopClass() + " " :  ""),  
				ConfigTreeNode.TreeNodeType.COERCION, 
				new CoercionModel(coercion));
		Collections.sort(csNode.getChildren());
	}

	public void editCoercion(ConfigTreeNode csNode, 
			ConfigTreeNode cNode, AttributeCoercion coercion) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) cNode.getParent().getParent().getModel()).getApplicationEntity();
        getDicomConfigurationManager().save(proxyApplicationEntity.getDevice());

		cNode.remove();
		new ConfigTreeNode(csNode, 
				coercion.getDimse().toString() + " " +
				coercion.getRole().toString() + " " + 
				(coercion.getAETitle() != null ? 
						coercion.getAETitle() + " " :  "") + 
				(coercion.getSopClass() != null ? 
						coercion.getSopClass() + " " :  ""),  
				ConfigTreeNode.TreeNodeType.COERCION, 
				new CoercionModel(coercion));
		Collections.sort(csNode.getChildren());
	}

	public void removeCoercion(ConfigTreeNode cNode) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) cNode.getParent().getParent().getModel()).getApplicationEntity();
		proxyApplicationEntity.getAttributeCoercions()
			.remove(((CoercionModel) cNode.getModel()).getCoercion());
		getDicomConfigurationManager().save(proxyApplicationEntity.getDevice());

		ConfigTreeNode csNode = cNode.getParent();
		cNode.remove();
		Collections.sort(csNode.getChildren());
	}

	public ConfigurationType getConfigurationType(Device device) {
		if (device instanceof ProxyDevice)
			return ConfigurationType.Proxy;
		if (device instanceof Device)
			return ConfigurationType.Basic;
		return null;
	}
	
	public ConfigurationType getConfigurationType(ApplicationEntity applicationEntity) {
		if (applicationEntity instanceof ProxyApplicationEntity)
			return ConfigurationType.Proxy;
		if (applicationEntity instanceof ApplicationEntity)
			return ConfigurationType.Basic;
		return null;
	}

	public boolean resync() {
Utils.prettyPrintln("resync: " + resync);
		if (resync) {
			resync = false;
			return true;
		} else return false;
	}
}