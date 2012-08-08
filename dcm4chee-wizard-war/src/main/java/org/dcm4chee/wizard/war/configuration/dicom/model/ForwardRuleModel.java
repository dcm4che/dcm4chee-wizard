package org.dcm4chee.wizard.war.configuration.dicom.model;

import java.io.Serializable;

import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4chee.proxy.conf.ForwardRule;
import org.dcm4chee.proxy.conf.ProxyApplicationEntity;
import org.dcm4chee.wizard.war.configuration.dicom.DeviceTreeProvider;

public class ForwardRuleModel implements Serializable, ConfigurationNodeModel {

	private static final long serialVersionUID = 1L;

	public static String cssClass = "forward_rule";
	public static String toolTip = "forward_rule";
	
	private String aeTitle;
	
	private java.lang.String commonName;
	private transient ForwardRule forwardRule;
	
	public ForwardRuleModel(ForwardRule forwardRule, String aeTitle) {
		this.aeTitle = aeTitle;
		this.commonName = forwardRule.getCommonName();
		this.forwardRule = forwardRule;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(java.lang.String commonName) {
		this.commonName = commonName;
	}

	public ForwardRule getForwardRule() {
		return forwardRule;
	}
	
	public void rebuild(ProxyApplicationEntity applicationEntity) throws ConfigurationException {
		if (applicationEntity == null)
			applicationEntity = (ProxyApplicationEntity) DeviceTreeProvider.get().getDicomConfigurationProxy()
				.getApplicationEntity(aeTitle);
		if (forwardRule == null) {
			for (ForwardRule forwardRule : applicationEntity.getForwardRules()) {
				if (this.commonName.equals(forwardRule.getCommonName())) {
					this.forwardRule = forwardRule;
					return;
				}
			}
		}
	}
}
