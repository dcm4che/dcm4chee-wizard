package org.dcm4chee.wizard.war.configuration.simple.model.proxy;

import java.io.Serializable;

import org.dcm4chee.proxy.conf.ForwardOption;
import org.dcm4chee.wizard.war.configuration.simple.model.ConfigNodeModel;

public class ForwardOptionModel extends ConfigNodeModel implements Serializable {

	private static final long serialVersionUID = 1L;

	public static String cssClass = "forward_schedule";
	public static String toolTip = "Forward Option";

	private String destinationAETitle;
	private ForwardOption forwardOption;
	
	public ForwardOptionModel(String destinationAETitle, ForwardOption forwardOption) {
		this.destinationAETitle = destinationAETitle;
		this.forwardOption = forwardOption;
	}
	
	public String getDestinationAETitle() {
		return destinationAETitle;
	}

	public ForwardOption getForwardOption() {
		return forwardOption;
	}
	
	@Override
	public String getDescription() {
		return forwardOption.getDescription() == null ? toolTip : forwardOption.getDescription();
	}
}

