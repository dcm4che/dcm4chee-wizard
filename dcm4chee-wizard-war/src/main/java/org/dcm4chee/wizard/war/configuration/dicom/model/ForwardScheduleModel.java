package org.dcm4chee.wizard.war.configuration.dicom.model;

import java.io.Serializable;

import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4chee.proxy.conf.ProxyApplicationEntity;
import org.dcm4chee.proxy.conf.Schedule;
import org.dcm4chee.wizard.war.configuration.dicom.DeviceTreeProvider;

public class ForwardScheduleModel implements Serializable, ConfigurationNodeModel {

	private static final long serialVersionUID = 1L;

	public static String cssClass = "forward_schedule";
	public static String toolTip = "forward_schedule";

	private String aeTitle;
	
	private transient ProxyApplicationEntity applicationEntity;
	private java.lang.String destinationAETitle;
	private transient Schedule schedule;
	
	public ForwardScheduleModel(ProxyApplicationEntity applicationEntity, String destinationAETitle, Schedule schedule) {
		this.aeTitle = applicationEntity.getAETitle();
		this.applicationEntity = applicationEntity;
		this.destinationAETitle = destinationAETitle;
		this.schedule = schedule;
	}
	
	public ApplicationEntity getApplicationEntity() throws ConfigurationException {
		rebuild(this.applicationEntity);
		return this.applicationEntity;
	}

	public String getDestinationAETitle() {
		return destinationAETitle;
	}

	public void setDestinationAETitle(java.lang.String destinationAETitle) {
		this.destinationAETitle = destinationAETitle;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	private void rebuild(ProxyApplicationEntity applicationEntity) throws ConfigurationException {
		if (applicationEntity == null)
			applicationEntity = (ProxyApplicationEntity) DeviceTreeProvider.get().getDicomConfigurationProxy()
				.getApplicationEntity(aeTitle);
		if (schedule == null)
			schedule = applicationEntity.getForwardSchedules().get(destinationAETitle);
	}
}

