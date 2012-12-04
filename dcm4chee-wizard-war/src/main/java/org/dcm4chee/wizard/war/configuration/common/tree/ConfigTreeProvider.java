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

package org.dcm4chee.wizard.war.configuration.common.tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableTreeProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4che.conf.api.AttributeCoercion;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Device;
import org.dcm4chee.proxy.conf.ProxyApplicationEntity;
import org.dcm4chee.proxy.conf.ProxyDevice;
import org.dcm4chee.wizard.war.DicomConfigurationManager;
import org.dcm4chee.wizard.war.WizardApplication;
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
import org.dcm4chee.wizard.war.profile.transfercapability.xml.Group;

/**
 * @author Robert David
 */
public class ConfigTreeProvider extends SortableTreeProvider<ConfigTreeNode,String> {

	private static final long serialVersionUID = 1L;
	
	public enum ConfigurationType { Basic, Proxy, Archive };

	private List<ConfigTreeNode> deviceNodeList;

	private Component forComponent;
	
	private boolean resync;
	private Date lastModificationTime;

	private ConfigTreeProvider(Component forComponent) throws ConfigurationException {
		this.forComponent = forComponent;
		loadDeviceList();
	}

	public void loadDeviceList() throws ConfigurationException {
		
		deviceNodeList = new ArrayList<ConfigTreeNode>();
		
		List<String> deviceList = 
				getDicomConfigurationManager().listDevices();		
		Collections.sort(deviceList);

		for (String deviceName : deviceList) {

			// CREATE DEVICE NODE AND MODEL
			DeviceModel deviceModel = new DeviceModel(deviceName);
			ConfigTreeNode deviceNode = 
					new ConfigTreeNode(null, deviceModel.getDeviceName(), 
							ConfigTreeNode.TreeNodeType.DEVICE, 
							null, deviceModel);
			deviceNodeList.add(deviceNode);
			new ConfigTreeNode(deviceNode, null, null, null, null);
		}
		lastModificationTime = new Date();
		resync = true;
	}

	public void loadDevice(ConfigTreeNode deviceNode) throws ConfigurationException {

		Device device = getDicomConfigurationManager().getDevice(deviceNode.getName());

		// CREATE DEVICE NODE
		DeviceModel deviceModel
			= (this.getConfigurationType(device).equals(ConfigurationType.Proxy)) ? 
					new ProxyDeviceModel((ProxyDevice) device) : new DeviceModel(device);

		deviceNode.setConfigurationType(getConfigurationType(device));
		deviceNode.setModel(deviceModel);
		
		deviceNode.removeChildren();
		addDeviceSubnodes(deviceNode);
		
		// CREATE CONNECTION NODES
		deviceNode.getContainer(ConfigTreeNode.CONTAINER_CONNECTIONS).removeChildren();
		for (ConnectionModel connectionModel : deviceModel.getConnections())
			new ConfigTreeNode(deviceNode.getContainer(ConfigTreeNode.CONTAINER_CONNECTIONS), 
					connectionModel.getConnection().getCommonName() == null ? 
							connectionModel.getConnection().getHostname() + ":" + 
							connectionModel.getConnection().getPort() : 
								connectionModel.getConnection().getCommonName(), 
					ConfigTreeNode.TreeNodeType.CONNECTION, connectionModel);
		Collections.sort(deviceNode.getContainer(
				ConfigTreeNode.CONTAINER_CONNECTIONS).getChildren());

		// CREATE AE NODES
		deviceNode.getContainer(ConfigTreeNode.CONTAINER_APPLICATION_ENTITIES).removeChildren();
		for (ApplicationEntityModel applicationEntityModel : deviceModel.getApplicationEntities().values()) {
			ConfigTreeNode aeNode = 
					new ConfigTreeNode(deviceNode.getContainer(ConfigTreeNode.CONTAINER_APPLICATION_ENTITIES), 
							applicationEntityModel.getApplicationEntity().getAETitle(), 
							ConfigTreeNode.TreeNodeType.APPLICATION_ENTITY, 
							this.getConfigurationType(applicationEntityModel.getApplicationEntity()), 
							applicationEntityModel);

			addApplicationEntitySubnodes(aeNode);
			
			// CREATE TRANSFER CAPABILITY NODES
			Map<String, Group> groupMap = 
					((WizardApplication) forComponent.getApplication()).getTransferCapabilityProfiles().asMap();
			
			for (String groupName : groupMap.keySet()) {
				ConfigTreeNode groupNode = 
					new ConfigTreeNode(aeNode.getContainer(ConfigTreeNode.CONTAINER_TRANSFER_CAPABILITIES), 
							groupName, 
							ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITY_TYPE, null);

				for (TransferCapabilityModel transferCapabilityModel : applicationEntityModel.getTransferCapabilities())
					if (groupMap.get(groupName).asMap().containsKey(transferCapabilityModel.getTransferCapability().getCommonName())) {
						transferCapabilityModel.setGroup(true);
						new ConfigTreeNode(groupNode,
								transferCapabilityModel.getTransferCapability().getRole() + " " + (
								transferCapabilityModel.getTransferCapability().getCommonName() != null ? 
										transferCapabilityModel.getTransferCapability().getCommonName() :  
										transferCapabilityModel.getTransferCapability().getSopClass() + " " + 
										transferCapabilityModel.getTransferCapability().getRole()),
										ConfigTreeNode.TreeNodeType.TRANSFER_CAPABILITY, 
										transferCapabilityModel);
					}
				if (!groupNode.hasChildren())
					groupNode.remove();
			}
			ConfigTreeNode groupNode = 
					new ConfigTreeNode(aeNode.getContainer(ConfigTreeNode.CONTAINER_TRANSFER_CAPABILITIES), 
							new ResourceModel("dicom.list.transferCapabilities.custom.label")
								.wrapOnAssignment(forComponent).getObject(), 
							ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITY_TYPE, null);

			for (TransferCapabilityModel transferCapabilityModel : applicationEntityModel.getTransferCapabilities())
				if (!transferCapabilityModel.hasGroup())
					new ConfigTreeNode(groupNode,
							transferCapabilityModel.getTransferCapability().getRole() + " " + (
							transferCapabilityModel.getTransferCapability().getCommonName() != null ? 
									transferCapabilityModel.getTransferCapability().getCommonName() :  
									transferCapabilityModel.getTransferCapability().getSopClass() + " " + 
									transferCapabilityModel.getTransferCapability().getRole()),
									ConfigTreeNode.TreeNodeType.TRANSFER_CAPABILITY, 
									transferCapabilityModel);
			if (!groupNode.hasChildren())
				groupNode.remove();

			Collections.sort(aeNode.getContainer(ConfigTreeNode.CONTAINER_TRANSFER_CAPABILITIES).getChildren());
			
			if (this.getConfigurationType(applicationEntityModel.getApplicationEntity())
					.equals(ConfigurationType.Proxy)) {
				
				// CREATE FORWARD RULE NODES
				for (ForwardRuleModel forwardRuleModel : 
					((ProxyApplicationEntityModel) applicationEntityModel).getForwardRules()) {
					new ConfigTreeNode(aeNode.getContainer(ConfigTreeNode.CONTAINER_FORWARD_RULES), 
							forwardRuleModel.getForwardRule().getCommonName(), 
							ConfigTreeNode.TreeNodeType.FORWARD_RULE, forwardRuleModel);
				}

				// CREATE FORWARD SCHEDULE NODES
				for (ForwardScheduleModel forwardScheduleModel : 
					((ProxyApplicationEntityModel) applicationEntityModel).getForwardSchedules()) {
					new ConfigTreeNode(aeNode.getContainer(ConfigTreeNode.CONTAINER_FORWARD_SCHEDULES), 
							forwardScheduleModel.getDestinationAETitle(), 
							ConfigTreeNode.TreeNodeType.FORWARD_SCHEDULE, forwardScheduleModel);
				}

				// CREATE RETRY NODES
				for (RetryModel retryModel : 
					((ProxyApplicationEntityModel) applicationEntityModel).getRetries()) {
					new ConfigTreeNode(aeNode.getContainer(ConfigTreeNode.CONTAINER_RETRIES), 
							retryModel.getRetry().retryObject.getRetryNote(), 
							ConfigTreeNode.TreeNodeType.RETRY, retryModel);
				}
				
				// CREATE COERCION NODES
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
		return ((WizardApplication) Application.get()).getDicomConfigurationManager();
	}

	public static ConfigTreeProvider get() {
		return (ConfigTreeProvider) Session.get().getAttribute("configTreeProvider");
	}

	public static ConfigTreeProvider init(Component forComponent) throws ConfigurationException {
		ConfigTreeProvider configTreeProvider = new ConfigTreeProvider(forComponent);
		Session.get().setAttribute("configTreeProvider", configTreeProvider);
		return configTreeProvider;
	}

	public void registerAETitle(String aeTitle) throws ConfigurationException {
		getDicomConfigurationManager().getDicomConfiguration().registerAETitle(aeTitle);
	}

	public void unregisterAETitle(String aeTitle) throws ConfigurationException {
		getDicomConfigurationManager().getDicomConfiguration().unregisterAETitle(aeTitle);
	}

	public String[] getUniqueAETitles() throws ConfigurationException {
		return getDicomConfigurationManager().getDicomConfiguration().listRegisteredAETitles();
	}
	
	public ApplicationEntity getApplicationEntity(String aet) throws ConfigurationException {
		return this.getDicomConfigurationManager().getApplicationEntity(aet);
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
		return deviceNodeList;
	}

	public void persistDevice(Device device) throws ConfigurationException {
		getDicomConfigurationManager().save(device, (lastModificationTime = new Date()));
		ConfigTreeNode deviceNode = 
				new ConfigTreeNode(null, device.getDeviceName(), 
						ConfigTreeNode.TreeNodeType.DEVICE,
						getConfigurationType(device),  
						null);
		deviceNodeList.add(deviceNode);
		Collections.sort(deviceNodeList);
		addDeviceSubnodes(deviceNode);
		Session.get().setAttribute("configTreeProvider", this);
	}

	public void mergeDevice(Device device) throws IOException, ConfigurationException {
		deviceNodeList = 
				((ConfigTreeProvider) Session.get().getAttribute("configTreeProvider"))
					.getNodeList();
		getDicomConfigurationManager().save(device, (lastModificationTime = new Date()));
		for (ConfigTreeNode node : deviceNodeList)
			if (node.getName().equals(device.getDeviceName()))
				node.setModel(null);
		Session.get().setAttribute("configTreeProvider", this);
	}

	public void removeDevice(ConfigTreeNode deviceNode) throws ConfigurationException {
		loadDevice(deviceNode);
		for (ApplicationEntity applicationEntity : ((DeviceModel) deviceNode.getModel()).getDevice().getApplicationEntities())
			unregisterAETitle(applicationEntity.getAETitle());
		getDicomConfigurationManager().remove(((DeviceModel) deviceNode.getModel()).getDeviceName());
		deviceNodeList.remove(deviceNode);
		resync = true;
		Session.get().setAttribute("configTreeProvider", this);
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
		if (resync) {
			resync = false;
			return true;
		} else return false;
	}
	
	public Date getLastModificationTime() {
		return lastModificationTime;
	}
}