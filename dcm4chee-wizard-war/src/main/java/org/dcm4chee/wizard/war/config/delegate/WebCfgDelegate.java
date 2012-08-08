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

package org.dcm4chee.wizard.war.config.delegate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4chee.web.common.delegate.BaseCfgDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @author Robert David <robert.david@agfa.com>
 */
public class WebCfgDelegate extends BaseCfgDelegate {

    protected static WebCfgDelegate singleton;

    protected static Logger log = LoggerFactory.getLogger(WebCfgDelegate.class);

    protected WebCfgDelegate() {
        init();
    }

    public static WebCfgDelegate getInstance() {
        if (singleton == null)
            singleton = new WebCfgDelegate();
        return singleton;
    }

    public String getDicomSecurityServletUrl() {
        return getString("dicomSecurityServletUrl");
    }

    public boolean getManageUsers() {
        return getBoolean("manageUsers", true);
    }

    public String getIgnoreEditTimeLimitRolename() {
        return getString("ignoreEditTimeLimitRolename");
    }

    public String getRetentionTime() {
        return getString("retentionTime");
    }

    public String getEmptyTrashInterval() {
        return getString("emptyTrashInterval");
    }

    public String getStudyPermissionsAllRolename() {
        return getString("studyPermissionsAllRolename");
    }

    public String getStudyPermissionsOwnRolename() {
        return getString("studyPermissionsOwnRolename");
    }

    public boolean getManageStudyPermissions() {
        return getBoolean("manageStudyPermissions", true);
    }

    public boolean getUseStudyPermissions() {
        return getBoolean("useStudyPermissions", true);
    }

    public String getWadoBaseURL() {
        return noneAsNull(getString("WadoBaseURL"));
    }

    public String getRIDBaseURL() {
        return noneAsNull(getString("RIDBaseURL"));
    }

    @SuppressWarnings("unchecked")
    public List<String> getRIDMimeTypes(String cuid) {
        try {
            return (List<String>) server.invoke(serviceObjectName,
                    "getRIDMimeTypesForCuid", new Object[] { cuid },
                    new String[] { String.class.getName() });
        } catch (Exception x) {
            log.warn("Cant invoke getRIDMimeTypes! Ignored by return null!", x);
            return null;
        }
    }

    public List<String> getInstalledWebViewerNameList() {
        return getStringList("getInstalledWebViewerNameList");
    }

    public List<String> getWebviewerNameList() {
        return getStringList("getWebviewerNameList");
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getWebviewerBaseUrlMap() {
        if (server != null) {
            try {
                return (Map<String, String>) server.invoke(serviceObjectName,
                        "getWebviewerBaseUrlMap", new Object[] {},
                        new String[] {});
            } catch (Exception x) {
                log.warn(
                        "Cant invoke 'getWebviewerBaseUrlMap'! Return null as default!",
                        x);
            }
        }
        return null;
    }

    public List<String> getModalityList() {
        List<String> mods = getStringList("getModalityList");
        mods.add(0, "*");
        return mods;
    }

    public List<String> getAETTypes() {
        return getStringList("getAETTypesList");
    }

    public String getAEManagementDefault() {
        return getString("aeManagementDefault");
    }

    public List<String> getStationNameList() {
        List<String> names = getStringList("getStationNameList");
        names.add(0, "*");
        return names;
    }

    public List<Integer> getAutoExpandLevelChoiceList() {
        return getIntegerList("getAutoExpandLevelChoiceList", Arrays.asList(-1));
    }

    public int getAutoWildcard() {
        if (server != null) {
            try {
                return (Integer) server.getAttribute(serviceObjectName,
                        "AutoWildcard");
            } catch (Exception ignore) {
                log.debug("Failed to get AutoWildcard attribute!", ignore);
            }
        }
        log.warn("Cant get AutoWildcard attribute! return 1 (only Patient) as default!");
        return 1;

    }

    @SuppressWarnings("unchecked")
    public List<Integer> getPagesizeList() {
        if (server == null)
            return Arrays.asList(10, 25, 50);
        try {
            return (List<Integer>) server.invoke(serviceObjectName,
                    "getPagesizeList", new Object[] {}, new String[] {});
        } catch (Exception x) {
            log.warn(
                    "Cant invoke 'getPagesizeList'! Return default list (10,25,50)!",
                    x);
            return Arrays.asList(10, 25, 50);
        }
    }

    public Integer getDefaultFolderPagesize() {
        if (server == null)
            return 10;
        try {
            return (Integer) server.getAttribute(serviceObjectName,
                    "DefaultFolderPagesize");
        } catch (Exception x) {
            log.warn(
                    "Cant get DefaultFolderPagesize attribute! return 10 as default!",
                    x);
            return 10;
        }
    }

    public Integer getDefaultMWLPagesize() {
        if (server == null)
            return 10;
        try {
            return (Integer) server.getAttribute(serviceObjectName,
                    "DefaultMWLPagesize");
        } catch (Exception x) {
            log.warn(
                    "Cant get DefaultMWLPagesize attribute! return 10 as default!",
                    x);
            return 10;
        }
    }

    public boolean isQueryAfterPagesizeChange() {
        return getBoolean("QueryAfterPagesizeChange", true);
    }

    public boolean useFamilyAndGivenNameQueryFields() {
        return getBoolean("useFamilyAndGivenNameQueryFields", false);
    }

    public boolean forcePatientExpandableForPatientQuery() {
        return getBoolean("forcePatientExpandableForPatientQuery", true);
    }

    public String getMpps2mwlPresetPatientname() {
        return getString("Mpps2mwlPresetPatientname");
    }

    public String getMpps2mwlPresetStartDate() {
        return getString("Mpps2mwlPresetStartDate");
    }

    public String getMpps2mwlPresetModality() {
        return getString("Mpps2mwlPresetModality");
    }

    public boolean isMpps2mwlAutoQuery() {
        return getBoolean("Mpps2mwlAutoQuery", true);
    }

    public String getTCKeywordCataloguesPath() {
        return getString("TCKeywordCataloguesPath");
    }

    public List<String> getTCRestrictedSourceAETList() {
        return getStringList("getTCRestrictedSourceAETList");
    }
    
    public Map<String, String> getTCKeywordCataloguesAsString() {
        return getStringMap("getTCKeywordCataloguesMap");
    }

    public Map<String, KeywordCatalogue> getTCKeywordCatalogues() {
        Map<String, String> map = getStringMap("getTCKeywordCataloguesMap");
        Map<String, KeywordCatalogue> parsedMap = new HashMap<String, KeywordCatalogue>(
                map.size());
        for (Map.Entry<String, String> me : map.entrySet()) {
            try {
                parsedMap.put(me.getKey(),
                        KeywordCatalogue.create(me.getValue()));
            } catch (Exception e) {
                log.error("Parsing TC keyword catalogue failed! Skipped...", e);
            }
        }
        return parsedMap;
    }

    public int checkCUID(String cuid) {
        if (server == null)
            return -1;
        try {
            return (Integer) server.invoke(serviceObjectName, "checkCUID",
                    new Object[] { cuid },
                    new String[] { String.class.getName() });
        } catch (Exception x) {
            log.warn("Cant invoke checkCUID! Ignored by return -1!", x);
            return -1;
        }
    }

    public String getZipEntryTemplate() {
        return getString("ZipEntryTemplate");
    }
    
    public int getBuffersize() {
        if (server == null)
            return 8192;
        try {
            return (Integer) server.getAttribute(serviceObjectName,
                    "Buffersize");
        } catch (Exception x) {
            log.warn("Cant get DefaultFolderPagesize attribute! return 10 as default!", x);
            return 8192;
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, String> getStringMap(String name) {
        if (server != null) {
            try {
                return (Map<String, String>) server.invoke(serviceObjectName,
                        name, new Object[0], new String[0]);
            } catch (Exception e) {
                log.warn("Can't invoke '" + name + "', returning empty map!", e);
            }
        }

        return Collections.emptyMap();
    }

    private String noneAsNull(String s) {
        return "NONE".equals(s) ? null : s;
    }

    public static class KeywordCatalogue {
        private String designator;

        private String id;

        private String version;

        private KeywordCatalogue(String designator, String id, String version) {
            this.designator = designator.trim();
            this.id = id.trim();
            this.version = version != null ? version.trim() : null;
        }

        public static KeywordCatalogue create(String s) throws Exception {
            String[] parts = s.split(",");

            return new KeywordCatalogue(parts[0], parts[1],
                    parts.length > 2 ? parts[2] : null);
        }

        public String getDesignator() {
            return designator;
        }

        public String getId() {
            return id;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(designator);
            sb.append(",");
            sb.append(id);

            if (version != null) {
                sb.append(",");
                sb.append(version);
            }
            return sb.toString();
        }
    }
}
