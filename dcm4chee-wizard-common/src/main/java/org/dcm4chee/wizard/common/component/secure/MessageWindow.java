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

package org.dcm4chee.wizard.common.component.secure;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.dcm4chee.wizard.common.component.MainWebPage;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public abstract class MessageWindow extends ModalWindow {

    private static final long serialVersionUID = 1L;

    private static final ResourceReference baseCSS = new PackageResourceReference(MainWebPage.class, "base-style.css");

    public MessageWindow(String id, final IModel<?> message) {
        super(id);

        setInitialWidth(400);
        setInitialHeight(300);

        setPageCreator(new ModalWindow.PageCreator() {

            private static final long serialVersionUID = 1L;

            public Page createPage() {
                return new MessagePage(message);
            }
        });
    }

    public abstract void onOk(AjaxRequestTarget target);

    public class MessagePage extends SecureSessionCheckPage {

        private static final long serialVersionUID = 1L;

        private IndicatingAjaxLink<Object> okBtn;

        public MessagePage(final IModel<?> message) {

            add(new Label("msg", new AbstractReadOnlyModel<Object>() {

                private static final long serialVersionUID = 1L;

                @Override
                public Object getObject() {
                    return message == null ? null : message.getObject();
                }
            }).setOutputMarkupId(true).setEscapeModelStrings(false));

            okBtn = new IndicatingAjaxLink<Object>("ok") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    onOk(target);
                    close(target);
                }
            };
            add(okBtn.add(new Label("okLabel", new ResourceModel("okBtn"))).setOutputMarkupId(true)
                    .setOutputMarkupPlaceholderTag(true));
            this.setOutputMarkupId(true);
        }

        @Override
        public void renderHead(IHeaderResponse response) {
            super.renderHead(response);
            if (MessageWindow.baseCSS != null)
                response.render(CssHeaderItem.forReference(MessageWindow.baseCSS));
        }
    }
}
