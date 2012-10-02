package org.dcm4chee.wizard.war.configuration.simple.model.proxy;

import java.io.Serializable;

import org.dcm4chee.proxy.conf.Schedule;
import org.dcm4chee.wizard.war.configuration.simple.model.ConfigNodeModel;

public class ForwardScheduleModel implements Serializable, ConfigNodeModel {

	private static final long serialVersionUID = 1L;

	public static String cssClass = "forward_schedule";
	public static String toolTip = "forward_schedule";

	private String destinationAETitle;
	private Schedule schedule;
	
	public ForwardScheduleModel(String destinationAETitle, Schedule schedule) {
		this.destinationAETitle = destinationAETitle;
		this.schedule = schedule;
	}
	
	public String getDestinationAETitle() {
		return destinationAETitle;
	}

	public Schedule getSchedule() {
		return schedule;
	}
}

