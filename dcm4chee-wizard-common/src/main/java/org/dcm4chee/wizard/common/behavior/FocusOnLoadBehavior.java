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

package org.dcm4chee.wizard.common.behavior;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 14, 2009
 */

public class FocusOnLoadBehavior extends Behavior {

    private static final long serialVersionUID = 1L;
    
    private FocusStrategy focusStrategy;
    
    public FocusOnLoadBehavior() {}

    public FocusOnLoadBehavior(FocusStrategy strategy) {
        focusStrategy = strategy;
    }
    
    public static FocusOnLoadBehavior newSimpleFocusBehaviour() {
        FocusOnLoadBehavior fb = new FocusOnLoadBehavior();
        fb.focusStrategy = fb.new SimpleFocusStrategy();
        return fb;
    }
    public static FocusOnLoadBehavior newFocusAndSelectBehaviour() {
        FocusOnLoadBehavior fb = new FocusOnLoadBehavior();
        fb.focusStrategy = fb.new FocusAndSelectTextStrategy();
        return fb;
    }
    
    public void bind( Component component ) {
        if ( focusStrategy == null ) {
            if (component instanceof FormComponent<?>) {
                focusStrategy = new EmptyFocusStrategy();
            } else if (component instanceof Form<?>) {
                focusStrategy = new FirstEmptyTextfieldFocusStrategy();
            }
        }
        component.setOutputMarkupId(true);
    }

    @Override
    public void renderHead(Component component, IHeaderResponse headerResponse ) {
        super.renderHead(component, headerResponse);
        focusStrategy.focus(headerResponse, component);
    }

    public boolean isTemporary() {
        return false;
    }
    
    private boolean setFocusOnEmpty(IHeaderResponse headerResponse, Component component) {
        Object object = component.getDefaultModelObject();
        if (object == null || object.toString().length() < 1) {
            headerResponse.renderOnLoadJavaScript(getJavaScriptString(component));
            return true;
        }
        return false;
    }
    private String getJavaScriptString(Component component) {
        return "self.focus();var elem=document.getElementById('"+
                component.getMarkupId() + "');elem.focus()";
    }

    public interface FocusStrategy extends Serializable {
        void focus(IHeaderResponse headerResponse, Component component);
    }
    
    public class SimpleFocusStrategy implements FocusStrategy {

        private static final long serialVersionUID = 1L;

        public void focus(IHeaderResponse headerResponse, Component component) {
            headerResponse.renderOnLoadJavaScript(getJavaScriptString(component));
        }
    }

    public class EmptyFocusStrategy implements FocusStrategy {

        private static final long serialVersionUID = 1L;

        public void focus(IHeaderResponse headerResponse, Component component) {
            setFocusOnEmpty(headerResponse, component);
        }
    }

    public class FocusAndSelectTextStrategy implements FocusStrategy {

        private static final long serialVersionUID = 1L;

        public void focus(IHeaderResponse headerResponse, Component component) {
            if (component instanceof TextField<?>) {
                headerResponse.renderOnLoadJavaScript(getJavaScriptString(component)+";elem.select()");
            }
        }
    }
    
    public class FirstEmptyTextfieldFocusStrategy implements FocusStrategy {

        private static final long serialVersionUID = 1L;

        public void focus(IHeaderResponse headerResponse, Component formComponent) {
            Form<?> form = (Form<?>)formComponent;
            Component component;
            for ( int i=0 ; i<form.size() ; i++) {
                component = form.get(i);
                if (component instanceof TextField<?>) {
                    if ( setFocusOnEmpty(headerResponse, component) ) {
                        component.setOutputMarkupId(true);
                        break;
                    }
                }
            }
        }
    }
}
