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

package org.dcm4chee.wizard.war;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.conf.api.DicomConfiguration;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class DicomConfigurationManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(DicomConfigurationManager.class);

    private DicomConfiguration dicomConfiguration;
    private HashMap<String, Device> deviceMap;
    private Date lastModificationTime;

    public DicomConfigurationManager(String dicomConfigurationClass) {
        try {
            dicomConfiguration = (DicomConfiguration) Class.forName(dicomConfigurationClass, false,
                    Thread.currentThread().getContextClassLoader()).newInstance();
        } catch (Exception e) {
            log.error("Error instanciating DicomConfigurationManager", e);
            throw new RuntimeException(e);
        }
        getDeviceMap();
        lastModificationTime = new Date();
    }

    public DicomConfiguration getDicomConfiguration() {
        return dicomConfiguration;
    }

    public synchronized HashMap<String, Device> getDeviceMap() {
        if (deviceMap == null)
            try {
                deviceMap = new HashMap<String, Device>();
                for (String deviceName : Arrays.asList(dicomConfiguration.listDeviceNames()))
                    deviceMap.put(deviceName, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        return deviceMap;
    }

    public synchronized ArrayList<String> listDevices() throws ConfigurationException {
        deviceMap = null;
        ArrayList<String> deviceNameList = new ArrayList<String>(getDeviceMap().keySet());
        Collections.sort(deviceNameList);
        return deviceNameList;
    }

    public synchronized Device getDevice(String deviceName) throws ConfigurationException {
        Device device = getDeviceMap().get(deviceName);
        if (device == null) {
            device = dicomConfiguration.findDevice(deviceName);
            getDeviceMap().put(deviceName, device);
        }
        return device;
    }

    public synchronized void save(Device device, Date date) throws ConfigurationException {
        if (getDeviceMap().containsKey(device.getDeviceName()))
            dicomConfiguration.merge(device);
        else
            dicomConfiguration.persist(device);
        getDeviceMap().put(device.getDeviceName(), device);
        lastModificationTime = date;
    }

    public synchronized void remove(String deviceName) throws ConfigurationException {
        dicomConfiguration.removeDevice(deviceName);
        getDeviceMap().remove(deviceName);
    }
    
    public ApplicationEntity getApplicationEntity(String aet) throws ConfigurationException {
    	return dicomConfiguration.findApplicationEntity(aet);
    }

	public synchronized Date getLastModificationTime() {
		return lastModificationTime;
	}   
}
