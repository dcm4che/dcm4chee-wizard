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

package org.dcm4chee.wizard.war.configuration.basic.tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4che.conf.api.AttributeCoercion;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.TransferCapability;
import org.dcm4chee.proxy.conf.ForwardRule;
import org.dcm4chee.proxy.conf.ProxyApplicationEntity;
import org.dcm4chee.proxy.conf.ProxyDevice;
import org.dcm4chee.proxy.conf.Retry;
import org.dcm4chee.proxy.conf.Schedule;
import org.dcm4chee.wizard.war.configuration.basic.model.ApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.basic.model.CoercionModel;
import org.dcm4chee.wizard.war.configuration.basic.model.ConnectionModel;
import org.dcm4chee.wizard.war.configuration.basic.model.DeviceModel;
import org.dcm4chee.wizard.war.configuration.basic.model.ForwardRuleModel;
import org.dcm4chee.wizard.war.configuration.basic.model.ForwardScheduleModel;
import org.dcm4chee.wizard.war.configuration.basic.model.ProxyApplicationEntityModel;
import org.dcm4chee.wizard.war.configuration.basic.model.ProxyDeviceModel;
import org.dcm4chee.wizard.war.configuration.basic.model.RetryModel;
import org.dcm4chee.wizard.war.configuration.basic.model.TransferCapabilityModel;
import org.dcm4chee.wizard.war.configuration.basic.proxy.DicomConfigurationProxy;

import wickettree.util.SortableTreeProvider;

/**
 * @author Robert David
 */
public class DeviceTreeProvider extends SortableTreeProvider<TreeNode> {

	private static final long serialVersionUID = 1L;
	
	public enum ConfigurationType {Basic, Proxy, Archive};

	private List<TreeNode> treeNodeList = new ArrayList<TreeNode>();
	private Set<String> uniqueAETitles = new HashSet<String>();
	
	private Component forComponent;

	DicomConfigurationProxy dicomConfigurationProxy;
	
	private DeviceTreeProvider(Component forComponent) {
		
		this.forComponent = forComponent;

		try {
			dicomConfigurationProxy = new DicomConfigurationProxy();

			List<Device> deviceList = dicomConfigurationProxy.listDevices();

			if (deviceList == null)
				return;
			
			Collections.sort(deviceList, new Comparator<Device>() {

				public int compare(Device device1, Device device2) {
					return device1.getDeviceName().compareToIgnoreCase(device2.getDeviceName());
				}
			});
			
			for (Device device : deviceList) {
				
				// CREATE DEVICE NODE AND MODEL
				DeviceModel deviceModel
					= (this.getConfigurationType(device).equals(ConfigurationType.Proxy)) ? 
							new ProxyDeviceModel((ProxyDevice) device) : new DeviceModel(device);
				
//				this.deviceModelList.put(device.getDeviceName(), deviceModel);
				TreeNode deviceNode = 
						new TreeNode(null, device.getDeviceName(), TreeNode.TreeNodeType.DEVICE, 
								getConfigurationType(device), deviceModel);
				treeNodeList.add(deviceNode);

				addDeviceSubnodes(deviceNode);
				
				// CREATE CONNECTION NODE AND MODEL
				for (ConnectionModel connectionModel : deviceModel.getConnections())
					new TreeNode(deviceNode.getContainer(TreeNode.CONTAINER_CONNECTIONS), 
							connectionModel.getConnection().getCommonName() == null ? 
									connectionModel.getConnection().getHostname() : 
										connectionModel.getConnection().getCommonName(), 
							TreeNode.TreeNodeType.CONNECTION, connectionModel);
				Collections.sort(deviceNode.getContainer(
						TreeNode.CONTAINER_CONNECTIONS).getChildren());
				
				// CREATE AE NODE AND MODEL
				for (ApplicationEntityModel applicationEntityModel : deviceModel.getApplicationEntities().values()) {
					TreeNode aeNode = 
							new TreeNode(deviceNode.getContainer(TreeNode.CONTAINER_APPLICATION_ENTITIES), 
									applicationEntityModel.getApplicationEntity().getAETitle(), 
									TreeNode.TreeNodeType.APPLICATION_ENTITY, 
									this.getConfigurationType(applicationEntityModel.getApplicationEntity()), 
									applicationEntityModel);

					uniqueAETitles.add(applicationEntityModel.getApplicationEntity().getAETitle());
					
					this.addApplicationEntitySubnodes(aeNode);
					
					Map<String, TreeNode> typeNodes = 
							new HashMap<String, TreeNode>();

					TreeNode otherNode = null;
					if (applicationEntityModel.getTransferCapabilities().size() > 0)
						otherNode = 
							new TreeNode(aeNode.getContainer(TreeNode.CONTAINER_TRANSFER_CAPABILITIES), 
								new ResourceModel("dicom.list.transferCapabilities.other.label").wrapOnAssignment(forComponent).getObject(), 
								TreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITY_TYPE, null);
					
					for (TransferCapabilityModel transferCapabilityModel : applicationEntityModel.getTransferCapabilities()) {

						TreeNode typeNode = null;
						if (dicomConfigurationProxy.getTransferCapabilityTypes()
								.containsKey(transferCapabilityModel.getTransferCapability().getSopClass())) {

							String type = dicomConfigurationProxy.getTransferCapabilityTypes()
									.get(transferCapabilityModel.getTransferCapability().getSopClass());
							
							if (typeNodes.containsKey(type)) 
								typeNode = typeNodes.get(type);	
							else {
								typeNode = new TreeNode(aeNode.getContainer(TreeNode.CONTAINER_TRANSFER_CAPABILITIES), 
										type, 
										TreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITY_TYPE, null);
								typeNodes.put(type, typeNode);
							}
						} else 
							typeNode = otherNode;								
						new TreeNode(typeNode, 
								transferCapabilityModel.getTransferCapability().getCommonName() != null ? 
										transferCapabilityModel.getTransferCapability().getCommonName() :  
										transferCapabilityModel.getTransferCapability().getSopClass() + " " + 
										transferCapabilityModel.getTransferCapability().getRole(),
										TreeNode.TreeNodeType.TRANSFER_CAPABILITY, 
										transferCapabilityModel);
					}
					if (otherNode != null)
						Collections.sort(otherNode.getChildren());
					for (TreeNode typeNode: typeNodes.values())
						Collections.sort(typeNode.getChildren());
					Collections.sort(aeNode.getContainer(TreeNode.CONTAINER_TRANSFER_CAPABILITIES).getChildren());
					
					if (this.getConfigurationType(applicationEntityModel.getApplicationEntity())
							.equals(ConfigurationType.Proxy)) {
						
						for (ForwardRuleModel forwardRuleModel : 
							((ProxyApplicationEntityModel) applicationEntityModel).getForwardRules()) {
							new TreeNode(aeNode.getContainer(TreeNode.CONTAINER_FORWARD_RULES), 
									forwardRuleModel.getCommonName(), 
									TreeNode.TreeNodeType.FORWARD_RULE, forwardRuleModel);
						}

						for (ForwardScheduleModel forwardScheduleModel : 
							((ProxyApplicationEntityModel) applicationEntityModel).getForwardSchedules()) {
							new TreeNode(aeNode.getContainer(TreeNode.CONTAINER_FORWARD_SCHEDULES), 
									forwardScheduleModel.getDestinationAETitle(), 
									TreeNode.TreeNodeType.FORWARD_SCHEDULE, forwardScheduleModel);
						}

						for (RetryModel retryModel : 
							((ProxyApplicationEntityModel) applicationEntityModel).getRetries()) {
							new TreeNode(aeNode.getContainer(TreeNode.CONTAINER_RETRIES), 
									retryModel.getRetry().getSuffix(), 
									TreeNode.TreeNodeType.RETRY, retryModel);
						}
						
						for (CoercionModel coercionModel : 
							((ProxyApplicationEntityModel) applicationEntityModel).getCoercions()) {
							new TreeNode(aeNode.getContainer(TreeNode.CONTAINER_COERCION), 
									coercionModel.getDimse().toString() + " " +
									coercionModel.getRole().toString() + " " + 
									(coercionModel.getAETitle() != null ? 
											coercionModel.getAETitle() + " " :  "") + 
									(coercionModel.getSOPClass() != null ? 
											coercionModel.getSOPClass() + " " :  ""),  
									TreeNode.TreeNodeType.COERCION, coercionModel);
						}				
					}
				}				
			}
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static DeviceTreeProvider get() {
		return (DeviceTreeProvider) Session.get().getAttribute("deviceTreeProvider");
	}
	
	public static DeviceTreeProvider set(Component forComponent) {
		DeviceTreeProvider deviceTreeProvider = new DeviceTreeProvider(forComponent);
		Session.get().setAttribute("deviceTreeProvider", deviceTreeProvider);
		return deviceTreeProvider;
	}

	public DicomConfigurationProxy getDicomConfigurationProxy() {
		return dicomConfigurationProxy;
	}
	
	public Set<String> getUniqueAETitles() {
		return uniqueAETitles;
	}
	
	private void addDeviceSubnodes(TreeNode deviceNode) {
		
		// CREATE CONNECTIONS FOLDER
		new TreeNode(deviceNode, 
				new ResourceModel("dicom.list.connections.label").wrapOnAssignment(forComponent).getObject(), 
				TreeNode.TreeNodeType.CONTAINER_CONNECTIONS, null);
		
		// CREATE AE FOLDER
		new TreeNode(deviceNode, 
				new ResourceModel("dicom.list.applicationEntities.label").wrapOnAssignment(forComponent).getObject(), 
				TreeNode.TreeNodeType.CONTAINER_APPLICATION_ENTITIES, null);
	}

	private void addApplicationEntitySubnodes(TreeNode aeNode) throws ConfigurationException {
		
		// CREATE TC FOLDER
		new TreeNode(aeNode, 
			new ResourceModel("dicom.list.transferCapabilities.label").wrapOnAssignment(forComponent).getObject(),
			TreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITIES, null);
		
		if (this.getConfigurationType(((ApplicationEntityModel) aeNode.getModel()).getApplicationEntity())
				.equals(ConfigurationType.Proxy)) {
			
			// CREATE FORWARD RULES FOLDER
			new TreeNode(aeNode, 
				new ResourceModel("dicom.list.forwardRules.label").wrapOnAssignment(forComponent).getObject(),
				TreeNode.TreeNodeType.CONTAINER_FORWARD_RULES, null);
	
			// CREATE FORWARD SCHEDULES FOLDER
			new TreeNode(aeNode, 
				new ResourceModel("dicom.list.forwardSchedules.label").wrapOnAssignment(forComponent).getObject(),
				TreeNode.TreeNodeType.CONTAINER_FORWARD_SCHEDULES, null);
			
			// CREATE RETRIES FOLDER
			new TreeNode(aeNode, 
				new ResourceModel("dicom.list.retries.label").wrapOnAssignment(forComponent).getObject(),
				TreeNode.TreeNodeType.CONTAINER_RETRIES, null);
			
			// CREATE COERCIONS FOLDER
			new TreeNode(aeNode, 
				new ResourceModel("dicom.list.coercions.label").wrapOnAssignment(forComponent).getObject(),
				TreeNode.TreeNodeType.CONTAINER_COERCIONS, null);
		}
	}
	
	public Iterator<? extends TreeNode> getRoots() {
		return treeNodeList.iterator();
	}

	public boolean hasChildren(TreeNode treeNode) {
		return treeNode.hasChildren();
	}

	public Iterator<? extends TreeNode> getChildren(TreeNode treeNode) {
		return treeNode.getChildren().iterator();
	}

	public IModel<TreeNode> model(TreeNode treeNode) {
		return Model.of(treeNode);
	}

	public void addDevice(Device device) throws ConfigurationException {
		dicomConfigurationProxy.persistDevice(device);
        
		TreeNode deviceNode = 
				new TreeNode(null, device.getDeviceName(), 
						TreeNode.TreeNodeType.DEVICE,
						getConfigurationType(device),  
						new DeviceModel(device));
		treeNodeList.add(deviceNode);
		Collections.sort(treeNodeList);
		addDeviceSubnodes(deviceNode);
	}

	public void editDevice(Device device) throws ConfigurationException {
		dicomConfigurationProxy.persistDevice(device);
        
		for (TreeNode node : treeNodeList)
			if (node.getName().equals(device.getDeviceName())) {
				treeNodeList.remove(node);
				TreeNode deviceNode = 
						new TreeNode(null, device.getDeviceName(), 
								TreeNode.TreeNodeType.DEVICE, 
								getConfigurationType(device), 
								new DeviceModel(device));
				treeNodeList.add(deviceNode);
				Collections.sort(treeNodeList);
				addDeviceSubnodes(deviceNode);
				return;
			}
	}

	public void removeDevice(TreeNode deviceNode) throws ConfigurationException {
		dicomConfigurationProxy.removeDevice(((DeviceModel) deviceNode.getModel()).getDeviceName());
		treeNodeList.remove(deviceNode);
	}

	public List<TreeNode> getNodeList() {
		return treeNodeList;
	}

	public void addConnection(TreeNode connectionsNode, Connection connection) throws ConfigurationException, IOException {
		Device device = ((DeviceModel) connectionsNode.getParent().getModel()).getDevice();
        device.addConnection(connection);
		dicomConfigurationProxy.persistDevice(device);
        		
		new TreeNode(connectionsNode, connection.getCommonName() + " - " + (connectionsNode.getChildren().size() - 1), 
				TreeNode.TreeNodeType.CONNECTION, new ConnectionModel(connection, connectionsNode.getChildren().size()));
		Collections.sort(connectionsNode.getChildren());
	}

	public void editConnection(TreeNode connectionsNode, Connection connection) throws IOException, ConfigurationException {
        dicomConfigurationProxy.persistDevice(connection.getDevice());
        
		connectionsNode.getChildren().clear();
		for (Connection currentConnection : connection.getDevice().listConnections())
			new TreeNode(connectionsNode, currentConnection.getCommonName(), 
					TreeNode.TreeNodeType.CONNECTION, new ConnectionModel(currentConnection, connectionsNode.getChildren().size()));
		Collections.sort(connectionsNode.getChildren());
	}
	
	public void removeConnection(TreeNode connectionNode) throws ConfigurationException {
		Device device = ((DeviceModel) connectionNode.getParent().getParent().getModel()).getDevice();
        device.removeConnection(((ConnectionModel) connectionNode.getModel()).getConnection());
        dicomConfigurationProxy.persistDevice(device);
		
		((DeviceModel) connectionNode.getParent().getParent().getModel())
			.getConnections().remove(connectionNode.getModel());
		
		TreeNode connectionsNode = connectionNode.getParent();
		connectionNode.remove();
		Collections.sort(connectionsNode.getChildren());
	}

	public void addApplicationEntity(TreeNode aesNode, ApplicationEntity applicationEntity) throws ConfigurationException {
		Device device = ((DeviceModel) aesNode.getParent().getModel()).getDevice();
        device.addApplicationEntity(applicationEntity);
        dicomConfigurationProxy.persistDevice(device);
        
        registerAETitle(applicationEntity.getAETitle());
        
        TreeNode aeNode = new TreeNode(aesNode, applicationEntity.getAETitle(), 
				TreeNode.TreeNodeType.APPLICATION_ENTITY, 
				getConfigurationType(applicationEntity), 
				new ApplicationEntityModel(applicationEntity));
		Collections.sort(aesNode.getChildren());
		addApplicationEntitySubnodes(aeNode);
	}

	public void editApplicationEntity(TreeNode aesNode, ApplicationEntity applicationEntity, 
			String oldAETitle) throws ConfigurationException {
		unregisterAETitle(oldAETitle);
        dicomConfigurationProxy.persistDevice(applicationEntity.getDevice());
        registerAETitle(applicationEntity.getAETitle());

        for (TreeNode aeNode : aesNode.getChildren())
        	if (aeNode.getName().equals(oldAETitle)) {
        		aeNode.setModel(new ApplicationEntityModel(applicationEntity));
        		aeNode.setName(applicationEntity.getAETitle());
        	}
		Collections.sort(aesNode.getChildren());
	}
	
	public void removeApplicationEntity(TreeNode aeNode) throws ConfigurationException {
		Device device = ((DeviceModel) aeNode.getParent().getParent().getModel()).getDevice();
		ApplicationEntity applicationEntity = ((ApplicationEntityModel) aeNode.getModel())
				.getApplicationEntity();
        device.removeApplicationEntity(applicationEntity);
        dicomConfigurationProxy.persistDevice(device);

        unregisterAETitle(applicationEntity.getAETitle());
        
		((DeviceModel) aeNode.getParent().getParent().getModel())
			.getApplicationEntities().remove(aeNode.getModel());
		
		TreeNode aesNode = aeNode.getParent();
		aeNode.remove();
		Collections.sort(aesNode.getChildren());
	}

	public void registerAETitle(String aeTitle) throws ConfigurationException {
		dicomConfigurationProxy.registerAETitle(aeTitle);
        uniqueAETitles.add(aeTitle);
	}

	public void unregisterAETitle(String aeTitle) throws ConfigurationException {
		dicomConfigurationProxy.unregisterAETitle(aeTitle);
        uniqueAETitles.remove(aeTitle);
	}

	public void addTransferCapability(TreeNode tcsNode,
			TransferCapability transferCapability) throws ConfigurationException {
		ApplicationEntity applicationEntity = 
				((ApplicationEntityModel) tcsNode.getParent().getModel()).getApplicationEntity();
		applicationEntity.addTransferCapability(transferCapability);
		dicomConfigurationProxy.persistDevice(applicationEntity.getDevice());

        String typeName = null;
		TreeNode typeNode = null;
		if (dicomConfigurationProxy.getTransferCapabilityTypes()
				.containsKey(transferCapability.getSopClass())) {
			typeName = dicomConfigurationProxy.getTransferCapabilityTypes()
					.get(transferCapability.getSopClass());
		} else 
			typeName = new ResourceModel("dicom.list.tcs.other.label").wrapOnAssignment(forComponent).getObject();
			
		for (TreeNode node : tcsNode.getChildren())
			if (node.getName().equals(typeName))
				typeNode = node;
		
		if (typeNode == null)
			typeNode = new TreeNode(tcsNode, typeName, 
					TreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITY_TYPE, null);
			
		new TreeNode(typeNode, 
				transferCapability.getCommonName() != null ? transferCapability.getCommonName() : 
					transferCapability.getSopClass() + " " + transferCapability.getRole(), 
				TreeNode.TreeNodeType.TRANSFER_CAPABILITY, 
				new TransferCapabilityModel(transferCapability, applicationEntity.getAETitle()));
		Collections.sort(typeNode.getChildren());
	}

	public void editTransferCapability(TreeNode tcsNode, TreeNode tcNode,  
			TransferCapability transferCapability) throws ConfigurationException {
		ApplicationEntity applicationEntity = 
				((ApplicationEntityModel) tcsNode.getParent().getModel()).getApplicationEntity();
		dicomConfigurationProxy.persistDevice(applicationEntity.getDevice());

		TreeNode typeNode = tcNode.getParent();
		tcNode.remove();

        String typeName = null;
		typeNode = null;
		if (dicomConfigurationProxy.getTransferCapabilityTypes()
				.containsKey(transferCapability.getSopClass())) {
			typeName = dicomConfigurationProxy.getTransferCapabilityTypes()
					.get(transferCapability.getSopClass());
		} else 
			typeName = new ResourceModel("dicom.list.tcs.other.label").wrapOnAssignment(forComponent).getObject();
			
		for (TreeNode node : tcsNode.getChildren())
			if (node.getName().equals(typeName))
				typeNode = node;
		
		if (typeNode == null)
			typeNode = new TreeNode(tcsNode, typeName, 
					TreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITY_TYPE, null);
			
		new TreeNode(typeNode, 
				transferCapability.getCommonName() != null ? transferCapability.getCommonName() : 
					transferCapability.getSopClass() + " " + transferCapability.getRole(), 
				TreeNode.TreeNodeType.TRANSFER_CAPABILITY, 
				new TransferCapabilityModel(transferCapability, applicationEntity.getAETitle()));
		Collections.sort(typeNode.getChildren());
	}

	public void removeTransferCapability(TreeNode tcNode) throws ConfigurationException {
		ApplicationEntity applicationEntity = 
				((ApplicationEntityModel) tcNode.getParent().getParent().getParent().getModel()).getApplicationEntity();
		applicationEntity.removeTransferCapability(((TransferCapabilityModel) tcNode.getModel()).getTransferCapability());
		dicomConfigurationProxy.persistDevice(applicationEntity.getDevice());

		((ApplicationEntityModel) tcNode.getParent().getParent().getParent().getModel())
			.getTransferCapabilities().remove(tcNode.getModel());
        
		TreeNode typeNode = tcNode.getParent();
		tcNode.remove();
		Collections.sort(typeNode.getChildren());
	}
	
	public void addForwardRule(TreeNode frsNode, ForwardRule forwardRule) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity) 
				((ApplicationEntityModel) frsNode.getParent().getModel()).getApplicationEntity();
		List<ForwardRule> forwardRules = proxyApplicationEntity.getForwardRules();
		forwardRules.add(forwardRule);
		proxyApplicationEntity.setForwardRules(forwardRules);
		dicomConfigurationProxy.persistDevice(proxyApplicationEntity.getDevice());

		new TreeNode(frsNode, 
				forwardRule.getCommonName(), 
				TreeNode.TreeNodeType.FORWARD_RULE, 
				new ForwardRuleModel(forwardRule, proxyApplicationEntity.getAETitle()));
		Collections.sort(frsNode.getChildren());
	}

	public void editForwardRule(TreeNode frsNode,
			TreeNode frNode, ForwardRule forwardRule) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) frNode.getParent().getParent().getModel()).getApplicationEntity();
		dicomConfigurationProxy.persistDevice(proxyApplicationEntity.getDevice());

		frNode.remove();
		new TreeNode(frsNode, 
				forwardRule.getCommonName(), 
				TreeNode.TreeNodeType.FORWARD_RULE, 
				new ForwardRuleModel(forwardRule, proxyApplicationEntity.getAETitle()));
		Collections.sort(frsNode.getChildren());
	}

	public void removeForwardRule(TreeNode frNode) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) frNode.getParent().getParent().getModel()).getApplicationEntity();
		List<ForwardRule> forwardRules = proxyApplicationEntity.getForwardRules();
		forwardRules.remove(((ForwardRuleModel) frNode.getModel()).getForwardRule());
		 dicomConfigurationProxy.persistDevice(proxyApplicationEntity.getDevice());

		TreeNode frsNode = frNode.getParent();
		frNode.remove();
		Collections.sort(frsNode.getChildren());
	}

	public void addForwardSchedule(TreeNode fssNode, 
			String destinationAETitle, Schedule schedule) throws ConfigurationException {
    	ProxyApplicationEntity proxyApplicationEntity = 
    			((ProxyApplicationEntityModel) fssNode.getParent().getModel()).getApplicationEntity();
    	proxyApplicationEntity.getForwardSchedules().put(destinationAETitle, schedule);
        dicomConfigurationProxy.persistDevice(proxyApplicationEntity.getDevice());
        
		new TreeNode(fssNode, 
				destinationAETitle, 
				TreeNode.TreeNodeType.FORWARD_SCHEDULE, 
				new ForwardScheduleModel(proxyApplicationEntity, destinationAETitle, schedule));
		Collections.sort(fssNode.getChildren());
	}

	public void editForwardSchedule(TreeNode fssNode, TreeNode fsNode, 
			String destinationAETitle, Schedule schedule) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) fsNode.getParent().getParent().getModel()).getApplicationEntity();
        dicomConfigurationProxy.persistDevice(proxyApplicationEntity.getDevice());

		fsNode.remove();
		new TreeNode(fssNode, 
				destinationAETitle, 
				TreeNode.TreeNodeType.FORWARD_SCHEDULE, 
				new ForwardScheduleModel(proxyApplicationEntity, destinationAETitle, schedule));
		Collections.sort(fssNode.getChildren());
	}

	public void removeForwardSchedule(TreeNode fsNode) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) fsNode.getParent().getParent().getModel()).getApplicationEntity();
		proxyApplicationEntity.getForwardSchedules()
			.remove(((ForwardScheduleModel) fsNode.getModel()).getDestinationAETitle());
		dicomConfigurationProxy.persistDevice(proxyApplicationEntity.getDevice());

		TreeNode fssNode = fsNode.getParent();
		fsNode.remove();
		Collections.sort(fssNode.getChildren());
	}

	public void addRetry(TreeNode rtsNode, Retry retry) throws ConfigurationException {
    	ProxyApplicationEntity proxyApplicationEntity = 
    			((ProxyApplicationEntityModel) rtsNode.getParent().getModel()).getApplicationEntity();
    	proxyApplicationEntity.getRetries().add(retry);    	   	
        dicomConfigurationProxy.persistDevice(proxyApplicationEntity.getDevice());
        
		new TreeNode(rtsNode, 
				retry.getSuffix(), 
				TreeNode.TreeNodeType.RETRY, 
				new RetryModel(proxyApplicationEntity, retry));
		Collections.sort(rtsNode.getChildren());
	}

	public void editRetry(TreeNode rtsNode,
			TreeNode rtNode, Retry retry) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) rtNode.getParent().getParent().getModel()).getApplicationEntity();
        dicomConfigurationProxy.persistDevice(proxyApplicationEntity.getDevice());
        
		rtNode.remove();        
		new TreeNode(rtsNode, 
				retry.getSuffix(), 
				TreeNode.TreeNodeType.RETRY, 
				new RetryModel(proxyApplicationEntity, retry));
		Collections.sort(rtsNode.getChildren());
	}

	public void removeRetry(TreeNode rtNode) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) rtNode.getParent().getParent().getModel()).getApplicationEntity();
		List<Retry> retries = proxyApplicationEntity.getRetries();
		retries.remove(((RetryModel) rtNode.getModel()).getRetry());
		dicomConfigurationProxy.persistDevice(proxyApplicationEntity.getDevice());

		TreeNode rtsNode = rtNode.getParent();
		rtNode.remove();
		Collections.sort(rtsNode.getChildren());
	}

	public void addCoercion(TreeNode csNode, AttributeCoercion coercion) throws ConfigurationException {
    	ProxyApplicationEntity proxyApplicationEntity = 
    			((ProxyApplicationEntityModel) csNode.getParent().getModel()).getApplicationEntity();
    	proxyApplicationEntity.getAttributeCoercions().add(coercion);
        dicomConfigurationProxy.persistDevice(proxyApplicationEntity.getDevice());
        
		new TreeNode(csNode, 
				coercion.getDimse().toString() + " " +
				coercion.getRole().toString() + " " + 
				(coercion.getAETitle() != null ? 
						coercion.getAETitle() + " " :  "") + 
				(coercion.getSopClass() != null ? 
						coercion.getSopClass() + " " :  ""),  
				TreeNode.TreeNodeType.COERCION, 
				new CoercionModel(proxyApplicationEntity, coercion));
		Collections.sort(csNode.getChildren());
	}

	public void editCoercion(TreeNode csNode, 
			TreeNode cNode, AttributeCoercion coercion) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) cNode.getParent().getParent().getModel()).getApplicationEntity();
        dicomConfigurationProxy.persistDevice(proxyApplicationEntity.getDevice());

		cNode.remove();
		new TreeNode(csNode, 
				coercion.getDimse().toString() + " " +
				coercion.getRole().toString() + " " + 
				(coercion.getAETitle() != null ? 
						coercion.getAETitle() + " " :  "") + 
				(coercion.getSopClass() != null ? 
						coercion.getSopClass() + " " :  ""),  
				TreeNode.TreeNodeType.COERCION, 
				new CoercionModel(proxyApplicationEntity, coercion));
		Collections.sort(csNode.getChildren());
	}

	public void removeCoercion(TreeNode cNode) throws ConfigurationException {
		ProxyApplicationEntity proxyApplicationEntity = (ProxyApplicationEntity)
				((ApplicationEntityModel) cNode.getParent().getParent().getModel()).getApplicationEntity();
		proxyApplicationEntity.getAttributeCoercions()
			.remove(((CoercionModel) cNode.getModel()).getCoercion());
		dicomConfigurationProxy.persistDevice(proxyApplicationEntity.getDevice());

		TreeNode csNode = cNode.getParent();
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
}