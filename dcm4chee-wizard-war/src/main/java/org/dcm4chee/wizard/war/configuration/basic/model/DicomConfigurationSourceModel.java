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

package org.dcm4chee.wizard.war.configuration.basic.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class DicomConfigurationSourceModel implements Serializable, Comparable<DicomConfigurationSourceModel> {

    private static final long serialVersionUID = 1L;

    private String uuid;
    private String name;
    private String type;
    private String host;
    private String port;
    private String cn;
    private String dc;
    private String password;  	    
    private String description;
    
    public DicomConfigurationSourceModel() {
        this.uuid = UUID.randomUUID().toString();
    }
    
    public DicomConfigurationSourceModel(String name, String type, String host, String port, String cn, String dc, String password, String description) {
        this();
        this.name = name;
        this.type = type;
        this.host = host;
        this.port = port;
        this.cn = cn;
        this.dc = dc;
        this.password = password;
        this.description = description;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }
    
    public void setPort(String port) {
        this.port = port;
    }

    public String getCn() {
        return cn;
    }
    
    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getDc() {
        return dc;
    }
    
    public void setDc(String dc) {
        this.dc = dc;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getName();   
    }

    public int compareTo(DicomConfigurationSourceModel configSource) {
        int i = name.toUpperCase().compareTo(configSource.getName().toUpperCase());
        return i == 0 ? name.compareTo(configSource.getName()) : i;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof DicomConfigurationSourceModel) {
            String n = ((DicomConfigurationSourceModel)o).name;
            return name == null ? n == null : name.equals(n);
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}	
