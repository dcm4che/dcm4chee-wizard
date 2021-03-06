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
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2012
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
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

package org.dcm4chee.wizard.page;

import java.io.IOException;
import java.util.Properties;

import org.apache.wicket.Page;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.dcm4chee.wizard.ProxyManifest;
import org.dcm4chee.wizard.common.component.ModuleSelectorPanel;
import org.dcm4chee.wizard.common.component.secure.SecureMainWebPage;
import org.dcm4chee.wizard.common.component.secure.SecureSessionCheckPage;
import org.dcm4chee.wizard.common.component.secure.SecureWebApplication;
import org.dcm4chee.wizard.panel.BasicConfigurationPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class MainPage extends SecureMainWebPage {

    private static final long serialVersionUID = 1L;

    protected static Logger log = LoggerFactory.getLogger(MainPage.class);

    private static final ResourceReference tableTreeCSS = new CssResourceReference(MainPage.class, "table-tree.css");

    public MainPage() {
        super();
        addModules(getModuleSelectorPanel());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        if (MainPage.tableTreeCSS != null)
            response.render(CssHeaderItem.forReference(MainPage.tableTreeCSS));
    }

    private void addModules(ModuleSelectorPanel selectorPanel) {

        selectorPanel.addModule(BasicConfigurationPanel.class);
        // selectorPanel.addModule(AdvancedConfigurationPanel.class);
        // selectorPanel.addModule(WizardPanel.class);
        // selectorPanel.addModule(ProfilePanel.class);
        // selectorPanel.addModule(DicomConfigurationSourcePanel.class);

        selectorPanel.getAboutWindow().setPageCreator(new ModalWindow.PageCreator() {

            private static final long serialVersionUID = 1L;

            @Override
            public Page createPage() {
                return new AboutPage();
            }
        });
    }

    private class AboutPage extends SecureSessionCheckPage {

        private static final long serialVersionUID = 1L;

        public AboutPage() {

            Properties wizardProperties = new Properties();
            try {
                wizardProperties.load(((SecureWebApplication) getApplication()).getServletContext()
                        .getResourceAsStream("/META-INF/MANIFEST.MF"));
            } catch (IOException e) {
                log.error("Could not retrieve properties from /META-INF/MANIFEST.MF", e);
            }

            add(new Label("content", new StringResourceModel("template", this, null, new Object[] {
                    wizardProperties.getProperty("Implementation-Title"),
                    wizardProperties.getProperty("Implementation-Version"),
                    wizardProperties.getProperty("Implementation-Vendor-Id"),
                    wizardProperties.getProperty("Implementation-Build"),
                    ProxyManifest.get("proxyJarName").get("Implementation-Title"),
                    ProxyManifest.get("proxyJarName").get("Implementation-Version"),
                    ProxyManifest.get("proxyJarName").get("Implementation-Vendor-Id"),
                    ProxyManifest.get("proxyJarName").get("Proxy-Implementation-Build"),
                    ProxyManifest.get("xdsConfigJarName").get("Implementation-Build")
            })).setEscapeModelStrings(false));
        }
    }
}
