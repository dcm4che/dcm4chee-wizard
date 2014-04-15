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

package org.dcm4chee.wizard;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.hl7.HL7Configuration;
import org.dcm4che3.conf.ldap.LdapDicomConfiguration;
import org.dcm4che3.conf.ldap.audit.LdapAuditLoggerConfiguration;
import org.dcm4che3.conf.ldap.audit.LdapAuditRecordRepositoryConfiguration;
import org.dcm4che3.conf.ldap.generic.LdapGenericConfigExtension;
import org.dcm4che3.conf.ldap.hl7.LdapHL7Configuration;
import org.dcm4che3.conf.prefs.PreferencesDicomConfiguration;
import org.dcm4che3.conf.prefs.audit.PreferencesAuditLoggerConfiguration;
import org.dcm4che3.conf.prefs.audit.PreferencesAuditRecordRepositoryConfiguration;
import org.dcm4che3.conf.prefs.generic.PreferencesGenericConfigExtension;
import org.dcm4che3.conf.prefs.hl7.PreferencesHL7Configuration;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.proxy.conf.ldap.LdapProxyConfigurationExtension;
import org.dcm4chee.proxy.conf.prefs.PreferencesProxyConfigurationExtension;
import org.dcm4chee.xds2.conf.XCAInitiatingGWCfg;
import org.dcm4chee.xds2.conf.XCARespondingGWCfg;
import org.dcm4chee.xds2.conf.XCAiInitiatingGWCfg;
import org.dcm4chee.xds2.conf.XCAiRespondingGWCfg;
import org.dcm4chee.xds2.conf.XdsRegistry;
import org.dcm4chee.xds2.conf.XdsRepository;
import org.dcm4chee.xds2.conf.XdsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class DicomConfigurationManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(DicomConfigurationManager.class);

    private DicomConfiguration dicomConfiguration;
    private HashMap<String, Device> deviceMap;
    private Date lastModificationTime;
    private Set<String> reloadList;
    private HL7Configuration hl7Configuration;
    private HashMap<String, String> connectedDeviceUrls;

    public DicomConfigurationManager(String ldapPropertiesURL) {
        try {
            ldapPropertiesURL = StringUtils.replaceSystemProperties(
                    System.getProperty("org.dcm4chee.proxy.ldapPropertiesURL", ldapPropertiesURL)).replace('\\', '/');
            InputStream ldapConf = null;
            try {
                ldapConf = new URL(ldapPropertiesURL).openStream();
                Properties p = new Properties();
                p.load(ldapConf);
                LdapDicomConfiguration ldapConfig = new LdapDicomConfiguration(p);
                LdapHL7Configuration hl7Config = new LdapHL7Configuration();
                ldapConfig.addDicomConfigurationExtension(hl7Config);
                ldapConfig.addDicomConfigurationExtension(new LdapProxyConfigurationExtension());
                ldapConfig.addDicomConfigurationExtension(new LdapAuditLoggerConfiguration());
                ldapConfig.addDicomConfigurationExtension(new LdapAuditRecordRepositoryConfiguration());
                ldapConfig.addDicomConfigurationExtension(new LdapGenericConfigExtension<XdsRegistry>(XdsRegistry.class));
                ldapConfig.addDicomConfigurationExtension(new LdapGenericConfigExtension<XdsRepository>(XdsRepository.class));
                ldapConfig.addDicomConfigurationExtension(new LdapGenericConfigExtension<XCARespondingGWCfg>(XCARespondingGWCfg.class));
                ldapConfig.addDicomConfigurationExtension(new LdapGenericConfigExtension<XCAInitiatingGWCfg>(XCAInitiatingGWCfg.class));
                ldapConfig.addDicomConfigurationExtension(new LdapGenericConfigExtension<XCAiRespondingGWCfg>(XCAiRespondingGWCfg.class));
                ldapConfig.addDicomConfigurationExtension(new LdapGenericConfigExtension<XCAiInitiatingGWCfg>(XCAiInitiatingGWCfg.class));
                ldapConfig.addDicomConfigurationExtension(new LdapGenericConfigExtension<XdsSource>(XdsSource.class));                
                dicomConfiguration = ldapConfig;
                hl7Configuration = hl7Config;
            } catch (FileNotFoundException e) {
                String pf = System.getProperty("java.util.prefs.PreferencesFactory");
                if (pf != null && pf.equals("org.dcm4che.jdbc.prefs.PreferencesFactoryImpl"))
                    log.info("Using JDBC Preferences as Configuration Backend");
                else
                    log.info("Using Java Preferences as Configuration Backend");
                PreferencesDicomConfiguration prefsConfig = new PreferencesDicomConfiguration();
                PreferencesHL7Configuration hl7Config = new PreferencesHL7Configuration();
                prefsConfig.addDicomConfigurationExtension(hl7Config);
                prefsConfig.addDicomConfigurationExtension(new PreferencesProxyConfigurationExtension());
                prefsConfig.addDicomConfigurationExtension(new PreferencesAuditLoggerConfiguration());
                prefsConfig.addDicomConfigurationExtension(new PreferencesAuditRecordRepositoryConfiguration());
                prefsConfig.addDicomConfigurationExtension(new PreferencesGenericConfigExtension<XdsRegistry>(XdsRegistry.class));
                prefsConfig.addDicomConfigurationExtension(new PreferencesGenericConfigExtension<XdsRepository>(XdsRepository.class));
                prefsConfig.addDicomConfigurationExtension(new PreferencesGenericConfigExtension<XCARespondingGWCfg>(XCARespondingGWCfg.class));
                prefsConfig.addDicomConfigurationExtension(new PreferencesGenericConfigExtension<XCAInitiatingGWCfg>(XCAInitiatingGWCfg.class));
                prefsConfig.addDicomConfigurationExtension(new PreferencesGenericConfigExtension<XCAiRespondingGWCfg>(XCAiRespondingGWCfg.class));
                prefsConfig.addDicomConfigurationExtension(new PreferencesGenericConfigExtension<XCAiInitiatingGWCfg>(XCAiInitiatingGWCfg.class));
                prefsConfig.addDicomConfigurationExtension(new PreferencesGenericConfigExtension<XdsSource>(XdsSource.class));   
                dicomConfiguration = prefsConfig;
                hl7Configuration = hl7Config;
            } finally {
                SafeClose.close(ldapConf);
            }
                for (String deviceName : getDeviceMap().keySet())
                    getDevice(deviceName);
            lastModificationTime = new Date();
            reloadList = new HashSet<String>();
        } catch (Exception e) {
            if (dicomConfiguration != null)
                dicomConfiguration.close();
            log.error("Error loading device configuration: " + e.getMessage());
            if (log.isDebugEnabled())
                e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public DicomConfiguration getDicomConfiguration() {
        return dicomConfiguration;
    }

    public HL7Configuration getHL7Configuration() {
        return hl7Configuration;
    }

    public synchronized HashMap<String, Device> getDeviceMap() {
        if (deviceMap == null)
            try {
                deviceMap = new HashMap<String, Device>();
                for (String deviceName : Arrays.asList(dicomConfiguration.listDeviceNames()))
                    deviceMap.put(deviceName, null);
            } catch (Exception e) {
                log.error("Error loading device list", e);
                throw new RuntimeException(e);
            }
        return deviceMap;
    }

    public synchronized void resetDeviceMap() {
        deviceMap = null;
    }

    public synchronized ArrayList<String> listDevices() throws ConfigurationException {
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

    public synchronized void resetDevice(String deviceName) {
        getDeviceMap().put(deviceName, null);
    }

    public Map<String, String> getConnectedDeviceUrls() {
        if (connectedDeviceUrls == null) {
            connectedDeviceUrls = new HashMap<String, String>();
            try {
                for (String deviceName : listDevices()) {
                    String connectedDeviceUrl = System.getProperty("org.dcm4chee.device." + deviceName);
                    if (connectedDeviceUrl != null) {
                        try {
                            if (!connectedDeviceUrl.startsWith("http")) {
                                Url url = ((WebRequest) RequestCycle.get().getRequest()).getUrl();
                                connectedDeviceUrl = url.getProtocol().concat("://")
                                        .concat(url.getHost()).concat(":")
                                        .concat(url.getPort().toString())
                                        .concat(connectedDeviceUrl);
                            }
                            connectedDeviceUrls
                                    .put(deviceName, StringUtils.replaceSystemProperties(connectedDeviceUrl));
                        } catch (Exception e) {
                            log.error("Error processing URL " + connectedDeviceUrl + " for connected device "
                                    + deviceName, e);
                        }
                    }
                }
            } catch (ConfigurationException ce) {
                log.error("Error retrieving connected device URLs", ce);
            }
        }
        return connectedDeviceUrls;
    }

    public ApplicationEntity getApplicationEntity(String aet) throws ConfigurationException {
        return dicomConfiguration.findApplicationEntity(aet);
    }

    public synchronized Date getLastModificationTime() {
        return lastModificationTime;
    }

    public synchronized boolean isReload(String deviceName) {
        return reloadList.contains(deviceName);
    }

    public synchronized void setReload(String deviceName) {
        reloadList.add(deviceName);
    }

    public synchronized void clearReload(String deviceName) {
        reloadList.remove(deviceName);
    }
}
