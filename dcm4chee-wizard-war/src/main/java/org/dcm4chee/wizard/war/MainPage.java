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

package org.dcm4chee.wizard.war;

import java.util.Properties;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.dcm4chee.web.common.base.BaseWicketApplication;
import org.dcm4chee.web.common.base.ModuleSelectorPanel;
import org.dcm4chee.web.common.secure.SecureWicketPage;
import org.dcm4chee.wizard.war.configuration.advanced.AdvancedConfigurationPanel;
import org.dcm4chee.wizard.war.configuration.profile.ProfilePanel;
import org.dcm4chee.wizard.war.configuration.simple.tree.BasicConfigurationPanel;
import org.dcm4chee.wizard.war.configuration.source.DicomConfigurationSourcePanel;
import org.dcm4chee.wizard.war.configuration.wizard.WizardPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class MainPage extends SecureWicketPage {
    
	private static final long serialVersionUID = 1L;
	
	protected static Logger log = LoggerFactory.getLogger(MainPage.class);
    
	private static final ResourceReference wizardCSS = new PackageResourceReference(MainPage.class, "wizard-style.css");
	private static final ResourceReference tableTreeCSS = new PackageResourceReference(MainPage.class, "table-tree.css");

    public MainPage() {
        super();
        addModules(getModuleSelectorPanel());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
    	if (MainPage.wizardCSS != null)
    		response.renderCSSReference(MainPage.wizardCSS);
    	if (MainPage.tableTreeCSS != null)
    		response.renderCSSReference(MainPage.tableTreeCSS);
    }

    private void addModules(ModuleSelectorPanel selectorPanel) {
        
        selectorPanel.addModule(BasicConfigurationPanel.class);
        selectorPanel.addModule(AdvancedConfigurationPanel.class);
        selectorPanel.addModule(WizardPanel.class);
        selectorPanel.addModule(ProfilePanel.class);
        selectorPanel.addModule(DicomConfigurationSourcePanel.class);
        
        try {
            Properties properties = new Properties();
            properties.load(((BaseWicketApplication) getApplication()).getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF"));
            selectorPanel.get("img_logo").add(new AttributeModifier("title", 
                    new Model<String>(
                            properties.getProperty("Implementation-Title", "")
                            + " : " + properties.getProperty("Implementation-Build", "")
                            + " (" + properties.getProperty("SCM-Revision", "?")+")"
                            )));            
        } catch (Exception ignore) {}
    }    
}
