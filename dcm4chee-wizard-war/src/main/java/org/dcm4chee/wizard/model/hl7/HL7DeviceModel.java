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

package org.dcm4chee.wizard.model.hl7;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.Device;
import org.dcm4che.net.audit.AuditLogger;
import org.dcm4che.net.hl7.HL7Application;
import org.dcm4che.net.hl7.HL7DeviceExtension;
import org.dcm4chee.wizard.model.ApplicationEntityModel;
import org.dcm4chee.wizard.model.AuditLoggerModel;
import org.dcm4chee.wizard.model.DeviceModel;

/**
 * @author Robert David <robert.david@agfa.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class HL7DeviceModel extends DeviceModel {

    private static final long serialVersionUID = 1L;

    LinkedHashMap<String, ApplicationEntityModel> applicationEntities;
    LinkedHashMap<String, HL7ApplicationModel> hl7Applications;
    AuditLoggerModel auditLoggerModel;

    public LinkedHashMap<String, HL7ApplicationModel> getHL7Applications() {
        return hl7Applications;
    }

    private void setHL7Applications(Collection<HL7Application> hl7Applications) throws ConfigurationException {
        this.hl7Applications = new LinkedHashMap<String, HL7ApplicationModel>();
        Iterator<HL7Application> i = hl7Applications.iterator();
        while (i.hasNext()) {
            HL7Application hl7Application = i.next();
            this.hl7Applications.put(hl7Application.getApplicationName(), new HL7ApplicationModel(hl7Application));
        }
    }

    public AuditLoggerModel getAuditLoggerModel() {
        return auditLoggerModel;
    }

    public void setAuditLogger(AuditLogger auditLogger) throws ConfigurationException {
        if (auditLogger != null)
            this.auditLoggerModel = new AuditLoggerModel(auditLogger);
    }

    public HL7DeviceModel(Device device) throws ConfigurationException {
        super(device);
        hl7Applications = new LinkedHashMap<String, HL7ApplicationModel>();
        HL7DeviceExtension hl7DeviceExtension = device.getDeviceExtension(HL7DeviceExtension.class);
        if (hl7DeviceExtension != null)
            setHL7Applications(hl7DeviceExtension.getHL7Applications());
    }
}
