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

package org.dcm4chee.wizard.model.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4chee.proxy.conf.ForwardOption;
import org.dcm4chee.proxy.conf.ForwardRule;
import org.dcm4chee.proxy.conf.ProxyAEExtension;
import org.dcm4chee.proxy.conf.Retry;
import org.dcm4chee.wizard.model.ApplicationEntityModel;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class ProxyApplicationEntityModel extends ApplicationEntityModel {

    private static final long serialVersionUID = 1L;

    private ApplicationEntity applicationEntity;

    private List<ForwardRuleModel> forwardRuleModels;
    private List<ForwardOptionModel> forwardOptionModels;
    private List<RetryModel> retryModels;

    public ProxyApplicationEntityModel(ApplicationEntity applicationEntity) throws ConfigurationException {
        super(applicationEntity);
        this.applicationEntity = applicationEntity;
        ProxyAEExtension proxyAEExtension = applicationEntity.getAEExtension(ProxyAEExtension.class);
        setForwardRules(proxyAEExtension.getForwardRules());
        setForwardOptions(proxyAEExtension.getForwardOptions());
        setRetries(proxyAEExtension.getRetries());
        setCoercions(proxyAEExtension.getAttributeCoercions());
    }

    @Override
    public ApplicationEntity getApplicationEntity() {
        return applicationEntity;
    }

    private void setForwardRules(Collection<ForwardRule> forwardRules) throws ConfigurationException {
        forwardRuleModels = new ArrayList<ForwardRuleModel>();
        for (ForwardRule forwardRule : forwardRules)
            forwardRuleModels.add(new ForwardRuleModel(forwardRule));
    }

    public List<ForwardRuleModel> getForwardRules() {
        return forwardRuleModels;
    }

    private void setForwardOptions(HashMap<String, ForwardOption> hashMap) {
        forwardOptionModels = new ArrayList<ForwardOptionModel>();
        Iterator<String> iterator = hashMap.keySet().iterator();
        while (iterator.hasNext()) {
            String destinationAETitle = iterator.next();
            forwardOptionModels.add(new ForwardOptionModel(destinationAETitle, hashMap.get(destinationAETitle)));
        }
    }

    public List<ForwardOptionModel> getForwardOptions() {
        return forwardOptionModels;
    }

    private void setRetries(List<Retry> retries) {
        retryModels = new ArrayList<RetryModel>();
        for (Retry retry : retries)
            retryModels.add(new RetryModel(retry));
    }

    public List<RetryModel> getRetries() {
        return retryModels;
    }
}
