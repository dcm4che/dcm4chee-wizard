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

package org.dcm4chee.wizard.war.common.component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.dcm4chee.web.common.markup.modal.ConfirmationWindow;
import org.dcm4chee.web.common.model.ProgressProvider;
import org.dcm4chee.web.common.secure.SecureAjaxTabbedPanel;
import org.dcm4chee.web.common.secure.SecureSession;
import org.dcm4chee.web.common.util.CloseRequestSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.security.swarm.SwarmWebApplication;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since July 12, 2009
 */

public class ModuleSelectorPanel extends SecureAjaxTabbedPanel {

    private static final long serialVersionUID = 1L;
    
    public boolean showLogout = true;
    
    private static Logger log = LoggerFactory.getLogger(ModuleSelectorPanel.class);
    
    private static final long LAST_REFRESHED_TIMEOUT = 5000l;

    private boolean popupsClosed = false;
    
    ConfirmationWindow<List<ProgressProvider>> confirmLogout = new ConfirmationWindow<List<ProgressProvider>>("confirmLogout") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onConfirmation(AjaxRequestTarget target, List<ProgressProvider> providers) {

            if (popupsClosed) {
                getSession().invalidate();
                return;
            }
            
            if (closePopups(providers)) {
                popupsClosed = true;
                throw new IllegalStateException(ModuleSelectorPanel.this.getString("logout.logout"));
            }
            else if (isPopupOpen(providers)) {
                throw new IllegalStateException(ModuleSelectorPanel.this.getString("logout.waiting"));
            }
        }
    };

    public ModuleSelectorPanel(String id) {
        super(id);
        boolean found = false;
        List<Cookie> cookies = ((WebRequest) RequestCycle.get().getRequest()).getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) 
                if (cookie.getName().equals("WEB3LOCALE")) {
                    getSession().setLocale(new Locale(cookie.getValue()));
                    found = true;
                    break;
                }

        if (!found) {
            Cookie cookie = new Cookie("WEB3LOCALE", getSession().getLocale().getCountry().toLowerCase());
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
                List<ProgressProvider> providers = null;
                Session s = getSession();
                if (s instanceof SecureSession) {
                    providers = ((SecureSession) s).getProgressProviders();
                    if (providers.size() > 0) {
                        confirmLogout.confirm(target, 
                                new ResourceModel("logout.confirmPendingTasks").wrapOnAssignment(this), providers);
                        return;
                    }
                }
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

        List<String> languages = new ArrayList<String>();
        languages.add("en");
        languages.add("de");
        languages.add("ja");

        final DropDownChoice<String> languageSelector = 
            new DropDownChoice<String>("language", new Model<String>(), languages, new ChoiceRenderer<String>() {

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
                Cookie c = new Cookie("WEB3LOCALE", newSelection);
                c.setMaxAge(Integer.MAX_VALUE);
                ((WebResponse) RequestCycle.get().getResponse()).addCookie(c);
                getSession().setLocale(new Locale(newSelection));
            }
        };
        languageSelector.setDefaultModelObject(getSession().getLocale().getLanguage());
        languageSelector.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            private static final long serialVersionUID = 1L;

            protected void onUpdate(AjaxRequestTarget target) {
                languageSelector.onSelectionChanged();
                target.add(getPage().setOutputMarkupId(true));
            }
        });
        add(languageSelector);

        add(new Image("img_logo", new PackageResourceReference(ModuleSelectorPanel.class, "images/logo.gif"))
        );
    }

    public void addModule(final Class<? extends Panel> clazz) {
        super.addModule(clazz, null);
// TODO: 
//        if (clazz.getResource("base-style.css") != null)
//            add(CssPackageResource.getHeaderContribution(clazz, "base-style.css"));
    }

    public void addInstance(Panel instance) {
        addInstance(instance, null);
    }

    public void addInstance(Panel instance, IModel<String> titleModel) {
        super.addModule(instance.getClass(), titleModel);
// TODO:
//        if (instance.getClass().getResource("base-style.css") != null)
//            add(CSSPackageResource.getHeaderContribution(instance.getClass(), "base-style.css"));
    }

    public ModuleSelectorPanel setShowLogoutLink(boolean show) {
        showLogout = show;
        return this;
    }
    
    private boolean closePopups(List<ProgressProvider> providers) {
        boolean b = false;
        if (providers != null) {
            synchronized (providers) {
                Integer pageID;
                for (int i = 0, len = providers.size() ; i < len ; i++) {
                    pageID = providers.get(i).getPopupPageId();
                    log.info("Provider has status: " + providers.get(i).getStatus());
                    if (pageID != null) {
                        IManageablePage p = ModuleSelectorPanel.this.getSession().getPageManager().getPage(pageID);
                        log.info("Found open popup page:"+p);
                        if (p != null && (p instanceof CloseRequestSupport)) {
                            log.debug("Set close request for popup page:"+p);
                            if (!((CloseRequestSupport) p).isCloseRequested()) {
                                ((CloseRequestSupport) p).setCloseRequest();
                                b = true;
                            }
                        }
                    }
                }
            }
        }
        return b;
    }
    
    private boolean isPopupOpen(List<ProgressProvider> providers) {
        if (providers != null) {
            synchronized (providers) {
                Integer pageID;
                for (int i = 0, len = providers.size() ; i < len ; i++) {
                    pageID = providers.get(i).getPopupPageId();
                    if (pageID != null) {
                        IManageablePage p = ModuleSelectorPanel.this.getSession().getPageManager().getPage(pageID);
                        if (p != null && (p instanceof CloseRequestSupport) && !((CloseRequestSupport)p).isClosed()) {
                            //check refresh timeout in case popup is closed without removing page in pagemap.(e.g. window close button)
                            return (System.currentTimeMillis() - providers.get(i).getLastRefreshedTimeInMillis() < LAST_REFRESHED_TIMEOUT);
                                
                        }
                    }
                }
            }
        }
        return false;
    }
}
