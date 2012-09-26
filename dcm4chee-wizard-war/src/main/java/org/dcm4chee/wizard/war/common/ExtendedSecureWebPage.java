package org.dcm4chee.wizard.war.common;

import org.dcm4chee.wizard.war.DicomConfigurationManager;
import org.dcm4chee.wizard.war.WicketApplication;
import org.wicketstuff.security.components.SecureWebPage;

public class ExtendedSecureWebPage extends SecureWebPage {

	private static final long serialVersionUID = 1L;

    public DicomConfigurationManager getDicomConfigurationManager() {
    	return ((WicketApplication) getApplication()).getDicomConfigurationManager();
    }
}
