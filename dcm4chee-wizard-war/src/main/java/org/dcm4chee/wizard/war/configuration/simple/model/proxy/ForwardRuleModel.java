package org.dcm4chee.wizard.war.configuration.simple.model.proxy;

import java.io.Serializable;

import org.dcm4chee.proxy.conf.ForwardRule;
import org.dcm4chee.wizard.war.configuration.simple.model.ConfigNodeModel;

public class ForwardRuleModel extends ConfigNodeModel implements Serializable {

	private static final long serialVersionUID = 1L;

	public static String cssClass = "forward_rule";
	public static String toolTip = "Forward Rule";
	
	private ForwardRule forwardRule;
	
	public ForwardRuleModel(ForwardRule forwardRule) {
		this.forwardRule = forwardRule;
	}

	public ForwardRule getForwardRule() {
		return forwardRule;
	}
	
	@Override
	public String getDescription() {
		return forwardRule.getDescription() == null ? toolTip : forwardRule.getDescription();
	}
}
