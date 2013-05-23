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

package org.dcm4chee.wizard.common.component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.dcm4chee.wizard.common.behavior.TooltipBehavior;
import org.dcm4chee.wizard.common.component.secure.SecureAjaxTabbedPanel;
import org.dcm4chee.wizard.common.login.secure.SecureSession;
import org.wicketstuff.security.swarm.SwarmWebApplication;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @author Robert David <robert.david@agfa.com>
 */
public class ModuleSelectorPanel extends SecureAjaxTabbedPanel {

    private static final long serialVersionUID = 1L;
    
    public boolean showLogout = true;
    
    ConfirmationWindow<?> confirmLogout = new ConfirmationWindow<Object>("confirmLogout") {

        private static final long serialVersionUID = 1L;

		@Override
		public void onConfirmation(AjaxRequestTarget target, Object userObject) {
            getSession().invalidate();
            return;
		}
    };

	final ModalWindow aboutWindow = new ModalWindow("aboutWindow");

	AjaxLink<Object> aboutLink = new AjaxLink<Object>("aboutLink") {
        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
        	aboutWindow.setTitle("").show(target);
        }
    };

    public ModuleSelectorPanel(String id) {
        super(id);
        
        Set<String> languages = new HashSet<String>();
        languages.add("de");
        languages.add("en");
        
        Set<String> customLanguages = new HashSet<String>();
        String languageProperty = System.getProperty("org.dcm4chee.wizard.config.languages");
        if (languageProperty != null) {
        	for (String language : languageProperty.split("\\|"))
        		if (languages.contains(language))
        			customLanguages.add(language);
        	languages = customLanguages;
        }

        boolean found = false;
        List<Cookie> cookies = ((WebRequest) RequestCycle.get().getRequest()).getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) 
                if (cookie.getName().equals("WIZARDLOCALE")) {
                    getSession().setLocale(new Locale(cookie.getValue()));
                    found = true;
                    break;
                }

        if (languages.size() == 1)
        	getSession().setLocale(new Locale(languages.iterator().next()));
        	
        if (!found) {
            Cookie cookie = new Cookie("WIZARDLOCALE", getSession().getLocale().getLanguage());
            cookie.setMaxAge(Integer.MAX_VALUE);
            ((WebResponse) RequestCycle.get().getResponse()).addCookie(cookie);
        }

        add(confirmLogout);

        try {
            InputStream is = ((SwarmWebApplication) getApplication()).getServletContext().getResourceAsStream("/WEB-INF/web.xml");
            XMLReader parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
            
            DefaultHandler dh = new DefaultHandler() {
                
                private StringBuffer current;
    
                @Override
                public void characters (char ch[], int start, int length) throws SAXException {
                    current = new StringBuffer().append(ch, start, length);
                }
    
                @Override
                public void endElement (String uri, String localName, String qName) throws SAXException {
                    if(qName.equals("auth-method"))
                        if (current.toString().equals("BASIC")) 
                            showLogout = false;
                }
            };
            parser.setContentHandler(dh);
            parser.parse(new InputSource(is));
        } catch (Exception ignore) {
        }
        
        add(new AjaxFallbackLink<Object>("logout") {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick(final AjaxRequestTarget target) {
                getSession().invalidate();
                setResponsePage(getApplication().getHomePage());
            }

            @Override
            public boolean isVisible() {
                return showLogout;
            }
        }.add(new Label("logoutLabel", 
            new StringResourceModel("logout", ModuleSelectorPanel.this, null, 
                    new Object[] { 
                        ((SecureSession) Session.get()).getUsername()
                    })
        )));
       
        final DropDownChoice<String> languageSelector = 
            new DropDownChoice<String>("language", new Model<String>(), new ArrayList<String>(languages), new ChoiceRenderer<String>() {

            private static final long serialVersionUID = 1L;
            
            @Override
            public String getDisplayValue(String object) {
                Locale l = new Locale(object);
                return l.getDisplayName(l);
            }
        }) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSelectionChanged(String newSelection) {
                Cookie cookie = new Cookie("WIZARDLOCALE", newSelection);
                cookie.setMaxAge(Integer.MAX_VALUE);
                ((WebResponse) RequestCycle.get().getResponse()).addCookie(cookie);
                getSession().setLocale(new Locale(newSelection));
            }
        };
        
        if (languages.size() > 1) {
	        languageSelector.setDefaultModelObject(getSession().getLocale().getLanguage());
	        languageSelector.add(new AjaxFormComponentUpdatingBehavior("onchange") {
	            private static final long serialVersionUID = 1L;
	
	            protected void onUpdate(AjaxRequestTarget target) {
	                languageSelector.onSelectionChanged();
	                target.add(getPage().setOutputMarkupId(true));
	            }
	        });
        } else
            languageSelector.setVisible(false);
        
        add(languageSelector);

    	add(aboutWindow.setInitialWidth(600).setInitialHeight(400));

        add(aboutLink
        		.add(new Image("img_logo", new PackageResourceReference(ModuleSelectorPanel.class, "images/logo.gif")))
        		.add(new TooltipBehavior("dicom."))
        		.setEnabled(false));
    }

    public void addModule(final Class<? extends Panel> clazz) {
        super.addModule(clazz, null);
    }

    public void addInstance(Panel instance) {
        addInstance(instance, null);
    }

    public void addInstance(Panel instance, IModel<String> titleModel) {
        super.addModule(instance.getClass(), titleModel);
    }

    public ModuleSelectorPanel setShowLogoutLink(boolean show) {
        showLogout = show;
        return this;
    }
    
    public ModalWindow getAboutWindow() {
    	aboutLink.setEnabled(true);
    	return aboutWindow;
    }    
}
