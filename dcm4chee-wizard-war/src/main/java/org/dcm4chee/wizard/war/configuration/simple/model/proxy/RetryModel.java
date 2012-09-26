package org.dcm4chee.wizard.war.configuration.simple.model.proxy;

import java.io.Serializable;

import org.dcm4chee.proxy.conf.Retry;
import org.dcm4chee.wizard.war.configuration.simple.model.ConfigNodeModel;

public class RetryModel implements Serializable, ConfigNodeModel {

	private static final long serialVersionUID = 1L;

	public static String cssClass = "retry";
	public static String toolTip = "retry";

	private Retry retry;
	
	public RetryModel(Retry retry) {
		this.retry = retry;
	}
	
	public Retry getRetry() {
		return retry;
	}
}
