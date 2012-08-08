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

package org.dcm4chee.wizard.war.configuration.dicom.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4chee.wizard.war.configuration.dicom.DeviceTreeProvider;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class DeviceModel implements Serializable, ConfigurationNodeModel {
	
	private static final long serialVersionUID = 1L;

	public static String cssClass = "device";
	public static String toolTip = "device";
	
	private java.lang.String deviceName;
	transient Device device;
	
	private List<ConnectionModel> connections;
	LinkedHashMap<String, ApplicationEntityModel> applicationEntities;

	public DeviceModel(Device device) {
		this.deviceName = device.getDeviceName();
		this.device = device;
		
		setConnections(device.listConnections()); 
		setApplicationEntities(device.getApplicationEntities());		
	}

	public java.lang.String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(java.lang.String deviceName) {
		this.deviceName = deviceName;
	}

	public Device getDevice() throws ConfigurationException {
		if (device == null) 
			rebuild(deviceName);
		return device;
	}

	private void rebuild(String deviceName) throws ConfigurationException {
		device = DeviceTreeProvider.get().getDicomConfigurationProxy().getDevice(deviceName);
		for (ConnectionModel connectionModel : this.connections)
			connectionModel.rebuild(device);
		for (ApplicationEntityModel applicationEntityModel : this.applicationEntities.values())
			applicationEntityModel.rebuild(device);
	}

	public List<ConnectionModel> getConnections() {
		return connections;
	}

	public void setConnections(List<Connection> connections) {
		this.connections = new ArrayList<ConnectionModel>();
		
		int i = 0;
		for (Connection connection : connections) 
			this.connections.add(new ConnectionModel(connection, i++));
	}

	public LinkedHashMap<String, ApplicationEntityModel> getApplicationEntities() {
		return applicationEntities;
	}

	public void setApplicationEntities(Collection<ApplicationEntity> applicationEntities) {
		this.applicationEntities = new LinkedHashMap<String,ApplicationEntityModel>();
		Iterator<ApplicationEntity> i = applicationEntities.iterator();
		while (i.hasNext()) {
			ApplicationEntity applicationEntity = i.next();
			this.applicationEntities.put(
					applicationEntity.getAETitle(), new ApplicationEntityModel(applicationEntity));
		}
	}
}
