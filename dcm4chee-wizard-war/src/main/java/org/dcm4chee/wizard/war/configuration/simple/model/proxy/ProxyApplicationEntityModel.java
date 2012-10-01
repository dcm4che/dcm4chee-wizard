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

package org.dcm4chee.wizard.war.configuration.simple.model.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dcm4che.conf.api.AttributeCoercion;
import org.dcm4che.conf.api.AttributeCoercions;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4chee.proxy.conf.ForwardRule;
import org.dcm4chee.proxy.conf.ProxyApplicationEntity;
import org.dcm4chee.proxy.conf.Retry;
import org.dcm4chee.proxy.conf.Schedule;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.ApplicationEntityModel;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class ProxyApplicationEntityModel extends ApplicationEntityModel {

	private static final long serialVersionUID = 1L;
	
    private ProxyApplicationEntity applicationEntity;
    
    private List<ForwardRuleModel> forwardRuleModels;
    private List<ForwardScheduleModel> forwardScheduleModels;
    private List<RetryModel> retryModels;
	private List<CoercionModel> coercionModels;
    
	public ProxyApplicationEntityModel(ProxyApplicationEntity applicationEntity) throws ConfigurationException {
		super(applicationEntity);
		this.applicationEntity = applicationEntity;
		setForwardRules(applicationEntity.getForwardRules());
		setForwardSchedules(applicationEntity.getForwardSchedules());
		setRetries(applicationEntity.getRetries());
		setCoercions(applicationEntity.getAttributeCoercions());
	}

	@Override
	public ProxyApplicationEntity getApplicationEntity() {
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

	private void setForwardSchedules(HashMap<String, Schedule> forwardSchedules) {
		forwardScheduleModels = new ArrayList<ForwardScheduleModel>();
		Iterator<String> iterator = forwardSchedules.keySet().iterator();
		while (iterator.hasNext()) { 
			String destinationAETitle = iterator.next();
			forwardScheduleModels
				.add(new ForwardScheduleModel(
						destinationAETitle, 
						forwardSchedules.get(destinationAETitle)));
		}
	}

	public List<ForwardScheduleModel> getForwardSchedules() {
		return forwardScheduleModels;
	}
	
	private void setRetries(List<Retry> retries) {
		retryModels = new ArrayList<RetryModel>();
		for (Retry retry : retries) 
			retryModels.add(new RetryModel(retry));
	}

	public List<RetryModel> getRetries() {
		return retryModels;
	}

	private void setCoercions(AttributeCoercions attributeCoercions) {
		this.coercionModels = new ArrayList<CoercionModel>();
		for (AttributeCoercion attributeCoercion : attributeCoercions.getAll())
			this.coercionModels.add(new CoercionModel(attributeCoercion));
	}

	public List<CoercionModel> getCoercions() {
		return coercionModels;
	}
}
