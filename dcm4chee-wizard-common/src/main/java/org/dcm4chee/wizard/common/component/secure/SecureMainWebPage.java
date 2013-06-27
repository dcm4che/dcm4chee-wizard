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

package org.dcm4chee.wizard.common.component.secure;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.resource.loader.PackageStringResourceLoader;
import org.dcm4chee.wizard.common.component.MainWebPage;
import org.dcm4chee.wizard.common.component.ModuleSelectorPanel;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class SecureMainWebPage extends SecureSessionCheckPage {

	private static final long serialVersionUID = 1L;

	private static final org.apache.wicket.request.resource.ResourceReference baseCSS = new CssResourceReference(MainWebPage.class, "base-style.css");
    
    private ModuleSelectorPanel selectorPanel;
    
    public SecureMainWebPage() {
        super();
        initLayout();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
    	if (SecureMainWebPage.baseCSS != null)
    		response.render(CssHeaderItem.forReference(SecureMainWebPage.baseCSS));
    }

    private void initLayout() {

        add( new Label("app_browser_title", new AbstractReadOnlyModel<Object>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Object getObject() {
                return getBrowserTitle();
            }
        } ));

        add(selectorPanel = new ModuleSelectorPanel("modules"));
    }

    public ModuleSelectorPanel getModuleSelectorPanel() {
        return selectorPanel;
    }

    protected String getBrowserTitle() {
        Class<?> clazz = Application.get().getHomePage();
        String s = new ClassStringResourceLoader(clazz)
        	.loadStringResource(this.getClass(), "application.browser_title", getSession().getLocale(), null, null);
        if (s==null) {
            s = new PackageStringResourceLoader()
            	.loadStringResource(this.getClass(), "application.browser_title", getSession().getLocale(), null, null); 
        }
        return s == null ? "DCM4CHEE" : s;
    }
}
