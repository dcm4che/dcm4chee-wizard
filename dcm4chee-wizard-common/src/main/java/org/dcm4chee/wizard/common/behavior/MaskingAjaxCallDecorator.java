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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.IComponentAwareHeaderContributor;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.dcm4chee.wizard.common.component.MainWebPage;

/**
 * Ajax Call Decorator to show and hide a mask to prevent user input while ajax call is running.
 * Note: adds web3-utils.js via the IHeaderContributor 
 * which must be loaded for the MaskingAjaxCallDecorator to work. 
 *  
 * @author Franz Willer <franz.willer@gmail.com>
 * @author Robert David <robert.david@agfa.com>
 */
public class MaskingAjaxCallDecorator extends Behavior implements IAjaxCallListener, IComponentAwareHeaderContributor {

    private static final long serialVersionUID = 1L;
    
    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(
        		new JavaScriptResourceReference(MainWebPage.class, "web3-utils.js")));
    }

	@Override
	public CharSequence getBeforeHandler(Component component) {
		// TODO: 
		System.out.println("AjaxCallListener: getBeforeHandler: for: " + component);
		return "if(typeof showMask == 'function') { showMask(); };";
	}

	@Override
	public CharSequence getSuccessHandler(Component component) {
		// TODO: 
		System.out.println("AjaxCallListener: getBeforeHandler: for: " + component);
		return "hideMask();";
	}

	@Override
	public CharSequence getFailureHandler(Component component) {
		// TODO: 
		System.out.println("AjaxCallListener: getBeforeHandler: for: " + component);
		return "hideMask();";
	}

	@Override
	public CharSequence getAfterHandler(Component arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharSequence getBeforeSendHandler(Component arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharSequence getCompleteHandler(Component arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharSequence getPrecondition(Component arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
