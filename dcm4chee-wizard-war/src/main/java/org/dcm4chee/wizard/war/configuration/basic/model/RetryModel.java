package org.dcm4chee.wizard.war.configuration.basic.model;

import java.io.Serializable;

import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4chee.proxy.conf.ProxyApplicationEntity;
import org.dcm4chee.proxy.conf.Retry;
import org.dcm4chee.wizard.war.configuration.basic.tree.DeviceTreeProvider;

public class RetryModel implements Serializable, ConfigurationNodeModel {

	private static final long serialVersionUID = 1L;

	public static String cssClass = "retry";
	public static String toolTip = "retry";

	private String aeTitle;
	private String suffix;
	
	private transient ProxyApplicationEntity applicationEntity;
	private transient Retry retry;
	
	public RetryModel(ProxyApplicationEntity applicationEntity, Retry retry) {
		this.suffix = retry.getSuffix();
		this.aeTitle = applicationEntity.getAETitle();
		this.applicationEntity = applicationEntity;
		this.retry = retry;
	}
	
	public ApplicationEntity getApplicationEntity() throws ConfigurationException {
		rebuild(this.applicationEntity);
		return this.applicationEntity;
	}

	public Retry getRetry() {
		return retry;
	}

	private void rebuild(ProxyApplicationEntity applicationEntity) throws ConfigurationException {
		if (applicationEntity == null)
			applicationEntity = (ProxyApplicationEntity) DeviceTreeProvider.get().getDicomConfigurationProxy()
				.getApplicationEntity(aeTitle);
		if (retry == null)
			for (Retry retry : applicationEntity.getRetries())
				if (retry.getSuffix().equals(suffix))
					this.retry = retry;
	}
}
