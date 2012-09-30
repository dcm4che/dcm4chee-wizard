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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.conf.api.DicomConfiguration;
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
    private static Map<String, String> sopClassTypeMap;

    public DicomConfigurationManager(String dicomConfigurationClass) {
        try {
            dicomConfiguration = (DicomConfiguration) Class.forName(dicomConfigurationClass, false,
                    Thread.currentThread().getContextClassLoader()).newInstance();
        } catch (Exception e) {
            log.error("Error instanciating DicomConfigurationManager", e);
            throw new RuntimeException(e);
        }
        getDeviceMap();
    }

    public DicomConfiguration getDicomConfiguration() {
        return dicomConfiguration;
    }

    public HashMap<String, Device> getDeviceMap() {
        if (deviceMap == null)
            try {
                deviceMap = new HashMap<String, Device>();
                for (String deviceName : Arrays.asList(dicomConfiguration.listDeviceNames()))
                    deviceMap.put(deviceName, null);// dicomConfiguration.findDevice(deviceName));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        return deviceMap;
    }

    public Map<String, String> getTransferCapabilityTypes() {

        if (sopClassTypeMap == null) {
            String configPath = System.getProperty("dcm4chee-wizard.cfg.path", "conf/dcm4chee-wizard/");
            File typesFile = new File(configPath + "transfer-capability-types.json");
            if (!typesFile.isAbsolute())
                typesFile = new File(System.getProperty("jboss.server.home.dir"), typesFile.getPath());

            sopClassTypeMap = new HashMap<String, String>();
            String line;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(typesFile));
                while ((line = reader.readLine()) != null) {
                    JSONObject jsonObject = JSONObject.fromObject(line);
                    sopClassTypeMap.put(jsonObject.getString("sopClass"), jsonObject.getString("type"));
                }
            } catch (IOException e) {
                log.error("Error processing transfer capability type mapping file", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
        return sopClassTypeMap;
    }

    public ArrayList<String> listDevices() throws ConfigurationException {
        deviceMap = null;
        ArrayList<String> deviceNameList = new ArrayList<String>(getDeviceMap().keySet());
        Collections.sort(deviceNameList);
        return deviceNameList;
    }

    public Device getDevice(String deviceName) throws ConfigurationException {
        Device device = getDeviceMap().get(deviceName);
        if (device == null) {
            device = dicomConfiguration.findDevice(deviceName);
            getDeviceMap().put(deviceName, device);
        }
        return device;
    }

    public void save(Device device) throws ConfigurationException {
        if (getDeviceMap().containsKey(device.getDeviceName()))
            dicomConfiguration.merge(device);
        else
            dicomConfiguration.persist(device);
    }

    public void remove(String deviceName) throws ConfigurationException {
        dicomConfiguration.removeDevice(deviceName);
        getDeviceMap().remove(deviceName);
    }

    // public ApplicationEntity getApplicationEntity(String aeTitle) throws
    // ConfigurationException {
    // return dicomConfiguration.findApplicationEntity(aeTitle);
    // }
    //
    // public void removeApplicationEntity(String aeTitle) throws
    // ConfigurationException {
    // Device device =
    // dicomConfiguration.findApplicationEntity(aeTitle).getDevice();
    // device.removeApplicationEntity(aeTitle);
    // dicomConfiguration.merge(device);
    // getDeviceMap().put(device.getDeviceName(), device);
    // }
    //
    // public void removeTransferCapability(String deviceName, String aeTitle,
    // int identityHashcode)
    // throws ConfigurationException {
    // boolean found = false;
    // Device device = getDeviceMap().get(deviceName);
    // ApplicationEntity ae = device.getApplicationEntity(aeTitle);
    // Iterator<TransferCapability> iterator =
    // ae.getTransferCapabilities().iterator();
    // while (iterator.hasNext()) {
    // TransferCapability tc = iterator.next();
    // if (System.identityHashCode(tc) == identityHashcode) {
    // ae.removeTransferCapability(tc);
    // dicomConfiguration.merge(device);
    // getDeviceMap().put(device.getDeviceName(), device);
    // found = true;
    // break;
    // }
    // }
    // if (!found)
    // throw new
    // ConfigurationException("Error: Failed to locate transfer capability for deletion");
    // }
    //
    // public void removeConnection(Device device, Connection connection) throws
    // ConfigurationException {
    // device.removeConnection(connection);
    // dicomConfiguration.merge(device);
    // getDeviceMap().put(device.getDeviceName(), device);
    // }

    // public Map<String,String> getTransferCapabilityTypes() {
    // return sopClassTypeMap;
    // }

    // public boolean registerAETitle(String aeTitle) throws
    // ConfigurationException {
    // return dicomConfiguration.registerAETitle(aeTitle);
    // }
    //
    // public void unregisterAETitle(String aeTitle) throws
    // ConfigurationException {
    // dicomConfiguration.unregisterAETitle(aeTitle);
    // }
}
