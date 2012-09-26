package org.dcm4chee.wizard.war.common;

import org.apache.wicket.markup.html.panel.Panel;
import org.dcm4chee.wizard.war.DicomConfigurationManager;
import org.dcm4chee.wizard.war.WicketApplication;

public class ExtendedPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public ExtendedPanel(String id) {
		super(id);
	}
	
    public DicomConfigurationManager getDicomConfigurationManager() {
    	return ((WicketApplication) getApplication()).getDicomConfigurationManager();
    }
}
