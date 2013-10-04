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
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2012
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
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

package org.dcm4chee.wizard.tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableTreeProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.dcm4che.conf.api.AttributeCoercion;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Device;
import org.dcm4che.net.DeviceExtension;
import org.dcm4che.net.audit.AuditRecordRepository;
import org.dcm4che.net.hl7.HL7DeviceExtension;
import org.dcm4chee.proxy.conf.ProxyAEExtension;
import org.dcm4chee.proxy.conf.ProxyDeviceExtension;
import org.dcm4chee.wizard.DicomConfigurationManager;
import org.dcm4chee.wizard.WizardApplication;
import org.dcm4chee.wizard.model.ApplicationEntityModel;
import org.dcm4chee.wizard.model.AuditLoggerModel;
import org.dcm4chee.wizard.model.CoercionModel;
import org.dcm4chee.wizard.model.ConnectionModel;
import org.dcm4chee.wizard.model.DeviceModel;
import org.dcm4chee.wizard.model.TransferCapabilityModel;
import org.dcm4chee.wizard.model.hl7.HL7ApplicationModel;
import org.dcm4chee.wizard.model.hl7.HL7DeviceModel;
import org.dcm4chee.wizard.model.proxy.ForwardOptionModel;
import org.dcm4chee.wizard.model.proxy.ForwardRuleModel;
import org.dcm4chee.wizard.model.proxy.ProxyApplicationEntityModel;
import org.dcm4chee.wizard.model.proxy.ProxyDeviceModel;
import org.dcm4chee.wizard.model.proxy.RetryModel;
import org.dcm4chee.wizard.model.xds.XdsDeviceModel;
import org.dcm4chee.wizard.tcxml.Group;
import org.dcm4chee.xds2.conf.XCAInitiatingGWCfg;
import org.dcm4chee.xds2.conf.XCARespondingGWCfg;
import org.dcm4chee.xds2.conf.XCAiInitiatingGWCfg;
import org.dcm4chee.xds2.conf.XCAiRespondingGWCfg;
import org.dcm4chee.xds2.conf.XdsRegistry;
import org.dcm4chee.xds2.conf.XdsRepository;

/**
 * @author Robert David
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class ConfigTreeProvider extends SortableTreeProvider<ConfigTreeNode, String> {

    private static final long serialVersionUID = 1L;

    public enum ConfigurationType {
        Basic, 
        Proxy, 
        Archive,
        AuditRecordRepository,
        XDS
    };

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
        List<String> deviceList = getDicomConfigurationManager().listDevices();
        Collections.sort(deviceList);
        for (String deviceName : deviceList) {
            // CREATE DEVICE NODE AND MODEL
            DeviceModel deviceModel = new DeviceModel(deviceName);
            ConfigTreeNode deviceNode = new ConfigTreeNode(null, 
                    deviceModel.getDeviceName(),
                    ConfigTreeNode.TreeNodeType.DEVICE, 
                    null, 
                    deviceModel);
            deviceNodeList.add(deviceNode);
            new ConfigTreeNode(deviceNode, null, ConfigTreeNode.TreeNodeType.CONTAINER_CONNECTIONS, null, null);
            loadDevice(deviceNode);
        }
        lastModificationTime = new Date();
        resync = true;
    }

    public void loadDevice(ConfigTreeNode deviceNode) throws ConfigurationException {
        Device device = getDicomConfigurationManager().getDevice(deviceNode.getName());
        ConfigurationType type = this.getConfigurationType(device);
        HL7DeviceModel deviceModel = createDeviceNode(deviceNode, device, type);
        createConnectionNodes(deviceNode, deviceModel);
        createHL7ApplicationNodes(deviceNode, deviceModel);
        if (type.equals(ConfigurationType.Proxy) || type.equals(ConfigurationType.XDS))
            createAuditLoggerNodes(deviceNode, deviceModel);
        createAENodes(deviceNode, deviceModel);
    }

    private void createAENodes(ConfigTreeNode deviceNode, DeviceModel deviceModel) throws ConfigurationException {
        deviceNode.getContainer(ConfigTreeNode.CONTAINER_APPLICATION_ENTITIES).removeChildren();
        for (ApplicationEntityModel applicationEntityModel : deviceModel.getApplicationEntities().values()) {
            ConfigurationType configType = this.getConfigurationType(applicationEntityModel.getApplicationEntity());
            ConfigTreeNode aeNode = new ConfigTreeNode(
                    deviceNode.getContainer(ConfigTreeNode.CONTAINER_APPLICATION_ENTITIES), 
                    applicationEntityModel.getApplicationEntity().getAETitle(), 
                    ConfigTreeNode.TreeNodeType.APPLICATION_ENTITY,
                    configType, 
                    applicationEntityModel);
            addApplicationEntitySubnodes(aeNode);
            createTransferCapabilityNodes(applicationEntityModel, aeNode);
            if (configType.equals(ConfigurationType.Proxy))
                createProxyNodes(new ProxyApplicationEntityModel(applicationEntityModel.getApplicationEntity()), aeNode);
        }
    }

    private void createProxyNodes(ProxyApplicationEntityModel proxyModel, ConfigTreeNode aeNode) {
        createForwardRuleNodes(proxyModel, aeNode);
        createForwardScheduleNodes(proxyModel, aeNode);
        createRetryNodes(proxyModel, aeNode);
        createCoercionNodes(proxyModel, aeNode);
    }

    private void createRetryNodes(ProxyApplicationEntityModel proxyModel, ConfigTreeNode aeNode) {
        for (RetryModel retryModel : proxyModel.getRetries()) {
            new ConfigTreeNode(
                    aeNode.getContainer(ConfigTreeNode.CONTAINER_RETRIES),
                    retryModel.getRetry().retryObject.getRetryNote(), 
                    ConfigTreeNode.TreeNodeType.RETRY, 
                    retryModel);
        }
    }

    private void createForwardScheduleNodes(ProxyApplicationEntityModel proxyModel, ConfigTreeNode aeNode) {
        for (ForwardOptionModel forwardScheduleModel : proxyModel.getForwardOptions()) {
            new ConfigTreeNode(
                    aeNode.getContainer(ConfigTreeNode.CONTAINER_FORWARD_OPTIONS),
                    forwardScheduleModel.getDestinationAETitle(), 
                    ConfigTreeNode.TreeNodeType.FORWARD_OPTION,
                    forwardScheduleModel);
        }
    }

    private void createForwardRuleNodes(ProxyApplicationEntityModel proxyModel, ConfigTreeNode aeNode) {
        for (ForwardRuleModel forwardRuleModel : proxyModel.getForwardRules()) {
            new ConfigTreeNode(
                    aeNode.getContainer(ConfigTreeNode.CONTAINER_FORWARD_RULES), 
                    forwardRuleModel.getForwardRule().getCommonName(), 
                    ConfigTreeNode.TreeNodeType.FORWARD_RULE, 
                    forwardRuleModel);
        }
    }

    private void createCoercionNodes(ApplicationEntityModel applicationEntityModel, ConfigTreeNode aeNode) {
        for (CoercionModel coercionModel : applicationEntityModel.getCoercions()) {
            AttributeCoercion coercion = coercionModel.getCoercion();
            new ConfigTreeNode(
                    aeNode.getContainer(ConfigTreeNode.CONTAINER_COERCION), 
                    coercion.getCommonName(),
                    ConfigTreeNode.TreeNodeType.COERCION, 
                    coercionModel);
        }
    }

    private void createTransferCapabilityNodes(ApplicationEntityModel applicationEntityModel, ConfigTreeNode aeNode) {
        Map<String, Group> groupMap = ((WizardApplication) forComponent.getApplication())
                .getTransferCapabilityProfiles().asMap();

        for (String groupName : groupMap.keySet()) {
            ConfigTreeNode groupNode = new ConfigTreeNode(
                    aeNode.getContainer(ConfigTreeNode.CONTAINER_TRANSFER_CAPABILITIES), groupName,
                    ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITY_TYPE, null);

            for (TransferCapabilityModel transferCapabilityModel : applicationEntityModel.getTransferCapabilities())
                if (groupMap.get(groupName).asMap()
                        .containsKey(transferCapabilityModel.getTransferCapability().getSopClass())) {
                    transferCapabilityModel.setGroup(true);
                    new ConfigTreeNode(
                            groupNode,
                            transferCapabilityModel.getTransferCapability().getCommonName() != null ? transferCapabilityModel
                                    .getTransferCapability().getCommonName().length() > 64 ? transferCapabilityModel
                                    .getTransferCapability().getCommonName().substring(0, 64) : transferCapabilityModel
                                    .getTransferCapability().getCommonName() : transferCapabilityModel
                                    .getTransferCapability().getRole()
                                    + " "
                                    + transferCapabilityModel.getTransferCapability().getSopClass(),
                            ConfigTreeNode.TreeNodeType.TRANSFER_CAPABILITY, transferCapabilityModel);
                }
            if (!groupNode.hasChildren())
                groupNode.remove();
        }

        ConfigTreeNode groupNode = new ConfigTreeNode(
                aeNode.getContainer(
                        ConfigTreeNode.CONTAINER_TRANSFER_CAPABILITIES), 
                        new StringResourceModel("dicom.list.transferCapabilities.custom.label", forComponent, null),
                ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITY_TYPE, null);

        for (TransferCapabilityModel transferCapabilityModel : applicationEntityModel.getTransferCapabilities())
            if (!transferCapabilityModel.hasGroup())
                new ConfigTreeNode(
                        groupNode,
                        transferCapabilityModel.getTransferCapability().getCommonName() != null ? transferCapabilityModel
                                .getTransferCapability().getCommonName().length() > 64 ? transferCapabilityModel
                                .getTransferCapability().getCommonName().substring(0, 64) : transferCapabilityModel
                                .getTransferCapability().getCommonName() : transferCapabilityModel
                                .getTransferCapability().getRole()
                                + " "
                                + transferCapabilityModel.getTransferCapability().getSopClass(),
                        ConfigTreeNode.TreeNodeType.TRANSFER_CAPABILITY, transferCapabilityModel);
        if (!groupNode.hasChildren())
            groupNode.remove();

        Collections.sort(aeNode.getContainer(ConfigTreeNode.CONTAINER_TRANSFER_CAPABILITIES).getChildren());
    }

    private void createAuditLoggerNodes(ConfigTreeNode deviceNode, HL7DeviceModel deviceModel) {
        deviceNode.getContainer(ConfigTreeNode.CONTAINER_AUDIT_LOGGERS).removeChildren();
        AuditLoggerModel auditLoggerModel = deviceModel.getAuditLoggerModel();
        if (auditLoggerModel != null)
            new ConfigTreeNode(deviceNode.getContainer(ConfigTreeNode.CONTAINER_AUDIT_LOGGERS),
                    auditLoggerModel.getText(), ConfigTreeNode.TreeNodeType.AUDIT_LOGGER, auditLoggerModel);
    }

    private void createHL7ApplicationNodes(ConfigTreeNode deviceNode, DeviceModel deviceModel) {
        deviceNode.getContainer(ConfigTreeNode.CONTAINER_HL7_APPLICATIONS).removeChildren();
        for (HL7ApplicationModel hl7ApplicationModel : ((HL7DeviceModel) deviceModel).getHL7Applications().values()) {
            new ConfigTreeNode(deviceNode.getContainer(ConfigTreeNode.CONTAINER_HL7_APPLICATIONS), hl7ApplicationModel
                    .getHL7Application().getApplicationName(), ConfigTreeNode.TreeNodeType.HL7_APPLICATION,
                    hl7ApplicationModel);
        }
    }

    private void createConnectionNodes(ConfigTreeNode deviceNode, DeviceModel deviceModel) throws ConfigurationException {
        deviceNode.getContainer(ConfigTreeNode.CONTAINER_CONNECTIONS).removeChildren();
        for (ConnectionModel connectionModel : deviceModel.getConnections())
            new ConfigTreeNode(deviceNode.getContainer(ConfigTreeNode.CONTAINER_CONNECTIONS), connectionModel
                    .getConnection().getCommonName() == null ? connectionModel.getConnection().getHostname() + ":"
                    + connectionModel.getConnection().getPort() : connectionModel.getConnection().getCommonName(),
                    ConfigTreeNode.TreeNodeType.CONNECTION, connectionModel);
        Collections.sort(deviceNode.getContainer(ConfigTreeNode.CONTAINER_CONNECTIONS).getChildren());
    }

    private HL7DeviceModel createDeviceNode(ConfigTreeNode deviceNode, Device device, ConfigurationType type)
            throws ConfigurationException {
        HL7DeviceModel deviceModel = type.equals(ConfigurationType.Proxy) 
                ? new ProxyDeviceModel(device)
                : type.equals(ConfigurationType.XDS)
                    ? new XdsDeviceModel(device)
                    : new HL7DeviceModel(device);
        deviceNode.setConfigurationType(getConfigurationType(device));
        deviceNode.setModel(deviceModel);
        deviceNode.removeChildren();
        addDeviceSubnodes(deviceNode);
        return deviceModel;
    }

    public void unloadDevice(ConfigTreeNode node) {
        getDicomConfigurationManager().resetDevice(node.getName());
    }

    public static DicomConfigurationManager getDicomConfigurationManager() {
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

    public void registerHL7Application(String hl7Application) throws ConfigurationException {
        getDicomConfigurationManager().getHL7Configuration().registerHL7Application(hl7Application);
    }

    public void unregisterHL7Application(String hl7Application) throws ConfigurationException {
        getDicomConfigurationManager().getHL7Configuration().unregisterHL7Application(hl7Application);
    }

    public String[] getUniqueHL7ApplicationNames() throws ConfigurationException {
        return getDicomConfigurationManager().getHL7Configuration().listRegisteredHL7ApplicationNames();
    }

    public ApplicationEntity getApplicationEntity(String aet) throws ConfigurationException {
        return getDicomConfigurationManager().getApplicationEntity(aet);
    }

    private void addDeviceSubnodes(ConfigTreeNode deviceNode) throws ConfigurationException {

        // CREATE CONNECTIONS FOLDER
        new ConfigTreeNode(deviceNode, new StringResourceModel("dicom.list.connections.label", forComponent, null),
                ConfigTreeNode.TreeNodeType.CONTAINER_CONNECTIONS, null);

        // CREATE AE FOLDER
        new ConfigTreeNode(deviceNode, new StringResourceModel("dicom.list.applicationEntities.label", forComponent,
                null), ConfigTreeNode.TreeNodeType.CONTAINER_APPLICATION_ENTITIES, null);

        // CREATE HL7 APPLICATIONS FOLDER
        new ConfigTreeNode(deviceNode, new StringResourceModel("dicom.list.hl7Applications.label", forComponent, null),
                ConfigTreeNode.TreeNodeType.CONTAINER_HL7_APPLICATIONS, null);

        ConfigurationType configurationType = deviceNode.getConfigurationType();
        if (configurationType.equals(ConfigurationType.Proxy) || configurationType.equals(ConfigurationType.XDS)) {
            // CREATE AUDIT LOGGERS FOLDER
            new ConfigTreeNode(deviceNode,
                    new StringResourceModel("dicom.list.auditLoggers.label", forComponent, null),
                    ConfigTreeNode.TreeNodeType.CONTAINER_AUDIT_LOGGERS, null);
        }
    }

    private void addApplicationEntitySubnodes(ConfigTreeNode aeNode) throws ConfigurationException {

        // CREATE TC FOLDER
        new ConfigTreeNode(aeNode,
                new StringResourceModel("dicom.list.transferCapabilities.label", forComponent, null),
                ConfigTreeNode.TreeNodeType.CONTAINER_TRANSFER_CAPABILITIES, null);

        if (this.getConfigurationType(((ApplicationEntityModel) aeNode.getModel()).getApplicationEntity()).equals(
                ConfigurationType.Proxy)) {

            // CREATE FORWARD RULES FOLDER
            new ConfigTreeNode(aeNode, new StringResourceModel("dicom.list.forwardRules.label", forComponent, null),
                    ConfigTreeNode.TreeNodeType.CONTAINER_FORWARD_RULES, null);

            // CREATE FORWARD OPTIONS FOLDER
            new ConfigTreeNode(aeNode, new StringResourceModel("dicom.list.forwardOptions.label", forComponent, null),
                    ConfigTreeNode.TreeNodeType.CONTAINER_FORWARD_OPTIONS, null);

            // CREATE RETRIES FOLDER
            new ConfigTreeNode(aeNode, new StringResourceModel("dicom.list.retries.label", forComponent, null),
                    ConfigTreeNode.TreeNodeType.CONTAINER_RETRIES, null);

            // CREATE COERCIONS FOLDER
            new ConfigTreeNode(aeNode, new StringResourceModel("dicom.list.coercions.label", forComponent, null),
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
        ConfigTreeNode deviceNode = new ConfigTreeNode(null, device.getDeviceName(),
                ConfigTreeNode.TreeNodeType.DEVICE, getConfigurationType(device), null);
        deviceNodeList.add(deviceNode);
        Collections.sort(deviceNodeList);
        addDeviceSubnodes(deviceNode);
        Session.get().setAttribute("configTreeProvider", this);
        ((WizardApplication) Session.get().getApplication()).getDicomConfigurationManager().setReload(
                device.getDeviceName());
    }

    public void mergeDevice(Device device) throws IOException, ConfigurationException {
        deviceNodeList = ((ConfigTreeProvider) Session.get().getAttribute("configTreeProvider")).getNodeList();
        getDicomConfigurationManager().save(device, (lastModificationTime = new Date()));
        for (ConfigTreeNode node : deviceNodeList)
            if (node.getName().equals(device.getDeviceName()))
                node.setModel(null);
        Session.get().setAttribute("configTreeProvider", this);
        ((WizardApplication) Session.get().getApplication()).getDicomConfigurationManager().setReload(
                device.getDeviceName());
    }

    public void removeDevice(ConfigTreeNode deviceNode) throws ConfigurationException {
        loadDevice(deviceNode);
        for (ApplicationEntity applicationEntity : ((DeviceModel) deviceNode.getModel()).getDevice()
                .getApplicationEntities())
            unregisterAETitle(applicationEntity.getAETitle());
        if (((DeviceModel) deviceNode.getModel()).getDevice().getDeviceExtension(HL7DeviceExtension.class) != null) {
            LinkedHashMap<String, HL7ApplicationModel> hl7Applications = ((HL7DeviceModel) deviceNode.getModel())
                    .getHL7Applications();
            for (String key : hl7Applications.keySet())
                unregisterHL7Application(hl7Applications.get(key).getHL7Application().getApplicationName());
        }
        getDicomConfigurationManager().remove(((DeviceModel) deviceNode.getModel()).getDeviceName());
        deviceNodeList.remove(deviceNode);
        resync = true;
        Session.get().setAttribute("configTreeProvider", this);
    }

    public ConfigurationType getConfigurationType(Device device) {
        Iterator<DeviceExtension> iter = device.listDeviceExtensions().iterator();
        if (!iter.hasNext())
            return ConfigurationType.Basic;

        DeviceExtension ext = device.listDeviceExtensions().iterator().next();
        if (ext instanceof ProxyDeviceExtension)
            return ConfigurationType.Proxy;

        else if (ext instanceof XCAiInitiatingGWCfg 
                || ext instanceof XCAInitiatingGWCfg
                || ext instanceof XCAiRespondingGWCfg 
                || ext instanceof XCARespondingGWCfg
                || ext instanceof XdsRegistry 
                || ext instanceof XdsRepository)
            return ConfigurationType.XDS;

        else if (ext instanceof AuditRecordRepository)
            return ConfigurationType.AuditRecordRepository;

        return ConfigurationType.Basic;
    }

    public ConfigurationType getConfigurationType(ApplicationEntity applicationEntity) {
        if (applicationEntity.getAEExtension(ProxyAEExtension.class) != null)
            return ConfigurationType.Proxy;
        return ConfigurationType.Basic;
    }

    public boolean resync() {
        if (resync) {
            resync = false;
            return true;
        } else
            return false;
    }

    public Date getLastModificationTime() {
        return lastModificationTime;
    }
}