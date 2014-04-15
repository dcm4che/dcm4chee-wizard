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

package org.dcm4chee.wizard.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.dcm4che3.conf.api.AttributeCoercion;
import org.dcm4che3.conf.api.AttributeCoercions;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.TransferCapability;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class ApplicationEntityModel extends ConfigNodeModel implements Serializable {

    private static final long serialVersionUID = 1L;

    public static String cssClass = "application-entity";
    public static String toolTip = "Application Entity";

    private String aeTitle;

    private ApplicationEntity applicationEntity;

    private List<TransferCapabilityModel> transferCapabilityModels;

    private List<CoercionModel> coercionModels;

    public void setCoercions(AttributeCoercions attributeCoercions) {
        this.coercionModels = new ArrayList<CoercionModel>();
        for (Iterator<AttributeCoercion> i = attributeCoercions.iterator(); i.hasNext();)
            this.coercionModels.add(new CoercionModel(i.next()));
    }

    public List<CoercionModel> getCoercions() {
        return coercionModels;
    }

    public ApplicationEntityModel(ApplicationEntity applicationEntity) {
        this.aeTitle = applicationEntity.getAETitle();
        this.applicationEntity = applicationEntity;
        setTransferCapabilities(applicationEntity.getTransferCapabilities());
    }

    public ApplicationEntity getApplicationEntity() throws ConfigurationException {
        return applicationEntity;
    }

    private void setTransferCapabilities(Collection<TransferCapability> transferCapabilities) {
        transferCapabilityModels = new ArrayList<TransferCapabilityModel>();
        for (TransferCapability transferCapability : transferCapabilities)
            transferCapabilityModels.add(new TransferCapabilityModel(transferCapability, aeTitle));
        Collections.sort(transferCapabilityModels, new Comparator<TransferCapabilityModel>() {

            @Override
            public int compare(TransferCapabilityModel model1, TransferCapabilityModel model2) {
                String commonName1 = model1.getTransferCapability().getCommonName();
                String commonName2 = model2.getTransferCapability().getCommonName();
                if (commonName1 == null)
                    return commonName2 == null ? 0 : 1;
                else {
                    if (commonName2 == null)
                        return -1;
                    else {
                        if (commonName1.startsWith("SCU ") || commonName1.startsWith("SCP "))
                            commonName1 = commonName1.substring(4);
                        if (commonName2.startsWith("SCU ") || commonName2.startsWith("SCP "))
                            commonName2 = commonName2.substring(4);
                        return commonName1.toLowerCase().compareTo(commonName2.toLowerCase());
                    }
                }
            }
        });
    }

    public List<TransferCapabilityModel> getTransferCapabilities() {
        return transferCapabilityModels;
    }

    @Override
    public String getDescription() {
        return applicationEntity.getDescription() == null ? toolTip : applicationEntity.getDescription();
    }
}
