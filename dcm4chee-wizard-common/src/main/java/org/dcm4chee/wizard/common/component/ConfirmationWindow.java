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

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.dcm4chee.wizard.common.behavior.MaskingAjaxCallDecorator;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @author Robert David <robert.david@agfa.com>
 */
public abstract class ConfirmationWindow<T> extends ModalWindow {
    
    private static final long serialVersionUID = 1L;

    public static final String FOCUS_ON_CONFIRM = "content:confirm";
    public static final String FOCUS_ON_DECLINE = "content:decline";
    public static final String FOCUS_ON_CANCEL = "content:cancel";

    public static final int UNCONFIRMED = 0;
    public static final int CONFIRMED = 1;
    public static final int DECLINED = 2;
    public static final int CANCELED = 3;

    private T userObject;
    private String focusElementId;

    private IModel<?> remark, confirm, decline, cancel;
    
    protected boolean hasStatus;
    private boolean showCancel = false;
    private int state = UNCONFIRMED;
    
    public MessageWindowPanel messageWindowPanel;
    
    private static final ResourceReference baseCSS = new PackageResourceReference(MainWebPage.class, "base-style.css");
	
    public ConfirmationWindow(String id, String titleResource) {
        this(id);
        setTitle(new ResourceModel(titleResource));
    }
    
    public ConfirmationWindow(String id) {
        
        this(id, new ResourceModel("yesBtn"), new ResourceModel("noBtn"), new ResourceModel("cancelBtn"));

        setCloseButtonCallback(new CloseButtonCallback() {

            private static final long serialVersionUID = 1L;

            public boolean onCloseButtonClicked(AjaxRequestTarget target) {
                messageWindowPanel.msg = null;
                close(target);
                return true;
            }
        });
        setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
            
            private static final long serialVersionUID = 1L;

            public void onClose(AjaxRequestTarget target) {
                getPage().setOutputMarkupId(true);
                target.add(getPage());
            }
        });
    }

    public ConfirmationWindow(String id, IModel<?> confirm, IModel<?> decline, IModel<?> cancel) {
        super(id);
        this.confirm = confirm;
        this.decline = decline;
        this.cancel = cancel;
        initContent();
    }

    protected void initContent() {
        setInitialWidth(400);
        setInitialHeight(300);
        
        messageWindowPanel = new MessageWindowPanel("panel");
        
        setPageCreator(new ModalWindow.PageCreator() {
            
            private static final long serialVersionUID = 1L;
              
            public Page createPage() {
                return new ConfirmPage();
            }
        });
        add(new DisableDefaultConfirmBehavior());
    }

    public abstract void onConfirmation(AjaxRequestTarget target, T userObject);
    public void onDecline(AjaxRequestTarget target, T userObject) {}
    public void onCancel(AjaxRequestTarget target, T userObject) {}
    public void onOk(AjaxRequestTarget target) {}
    
    @Override
    public void show(final AjaxRequestTarget target) {
        hasStatus = false;
        super.show(target);
        if (focusElementId != null)
            target.focusComponent(this.get(focusElementId));
    }
    
    public void confirm(AjaxRequestTarget target, IModel<?> msg, T userObject) {
        confirm(target, msg, userObject, FOCUS_ON_DECLINE);
    }
    public void confirm(AjaxRequestTarget target, IModel<?> msg, T userObject, String focusElementId) {
        confirm(target, msg, userObject, focusElementId, false);
    }
    public void confirm(AjaxRequestTarget target, IModel<?> msg, T userObject, String focusElementId, boolean showCancel) {
        this.messageWindowPanel.msg = msg;
        this.userObject = userObject;
        this.focusElementId = focusElementId;
        this.showCancel = showCancel;
        show(target);
    }

    public void confirmWithCancel(AjaxRequestTarget target, IModel<?> msg, T userObject) {
        confirm(target, msg, userObject, FOCUS_ON_CANCEL, true);
    }
    
    public void setStatus(IModel<?> statusMsg) {
        messageWindowPanel.msg = statusMsg;
        hasStatus = true;
    }

    public void setRemark(IModel<?> remark) {
        this.remark = remark;
    }
    
    public T getUserObject() {
        return userObject;
    }    
    
    public int getState() {
        return state;
    }

    public class ConfirmPage extends WebPage {
    	
		private static final long serialVersionUID = 1L;

		public ConfirmPage() {
            add(messageWindowPanel);
        }
		
	    @Override
	    public void renderHead(IHeaderResponse response) {
	    	if (ConfirmationWindow.baseCSS != null)
	    		response.render(CssHeaderItem.forReference(ConfirmationWindow.baseCSS));
	    }
    }
    
    public class MessageWindowPanel extends Panel {
        
        private static final long serialVersionUID = 1L;
        
        private IndicatingAjaxLink<Object> confirmBtn;
        private AjaxLink<Object> okBtn;
        
        private IModel<?> msg;
        private Label msgLabel;
        private Label remarkLabel;
        
        private boolean logout = false;
        
        public MessageWindowPanel(String id) {
            super(id);
            
//            final MaskingAjaxCallBehavior macb = new MaskingAjaxCallBehavior();
//            add(macb);

            add((msgLabel = new Label("msg", new AbstractReadOnlyModel<Object>() {

                private static final long serialVersionUID = 1L;

                @Override
                public Object getObject() {
                    return msg == null ? null : msg.getObject();
                }
            })).setOutputMarkupId(true)
            .setEscapeModelStrings(false));
            
            add((remarkLabel = new Label("remark", new AbstractReadOnlyModel<Object>() {

                private static final long serialVersionUID = 1L;

                @Override
                public Object getObject() {
                    return remark == null ? null : remark.getObject();
                }
            }){
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return !hasStatus;
                }

            }).setOutputMarkupId(true));

            confirmBtn = new IndicatingAjaxLink<Object>("confirm") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    try {
                        onConfirmation(target, userObject);
                        state = CONFIRMED;
                        if (hasStatus) {
                            target.add(MessageWindowPanel.this);
                        } else {
                            msg = null;
                            close(target);
                        }
                    } catch (Exception x) {
                        logout = true;
                        setStatus(new Model<String>(x.getMessage()));
                        target.add(MessageWindowPanel.this);
                    }
                }
                
                @Override
                public boolean isVisible() {
                    return !hasStatus;
                }

//                @Override
//                protected IAjaxCallDecorator getAjaxCallDecorator() {
//                    try {
//                        return macb.getAjaxCallDecorator();
//                    } catch (Exception e) {
//                        log.error("Failed to get IAjaxCallDecorator", e);
//                    }
//                    return null;
//                }
            };
            confirmBtn.add(new MaskingAjaxCallDecorator());
            confirmBtn.add(new Label("confirmLabel", confirm));
            confirmBtn.setOutputMarkupId(true);
            add(confirmBtn);
            
            add(new AjaxLink<Object>("decline"){

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    onDecline(target, userObject);
                    state = DECLINED;
                    if (hasStatus) {
                        target.add(MessageWindowPanel.this);
                    } else {
                        msg = null;
                        close(target);
                    }
                }
                @Override
                public boolean isVisible() {
                    return !hasStatus;
                }
            }.add(new Label("declineLabel", decline)));
            
            add(new AjaxLink<Object>("cancel"){
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    onCancel(target, userObject);
                    state = CANCELED;
                    msg = null;
                    close(target);
                }
                @Override
                public boolean isVisible() {
                    return !hasStatus && showCancel;
                }
            }.add(new Label("cancelLabel", cancel)) );
            
            add(okBtn = new IndicatingAjaxLink<Object>("ok") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    if (logout)   
                        onConfirmation(target, userObject);
                    else 
                        onOk(target);
                    msg = null;
                    close(target);
                }
                
                @Override
                public boolean isVisible() {
                    return hasStatus;
                }
                
//                @Override
//                protected IAjaxCallDecorator getAjaxCallDecorator() {
//                    try {
//                        return macb.getAjaxCallDecorator();
//                    } catch (Exception e) {
//                        log.error("Failed to get IAjaxCallDecorator", e);
//                    }
//                    return null;
//                }
            });
            okBtn.add(new MaskingAjaxCallDecorator());
            getOkBtn().add(new Label("okLabel", new ResourceModel("okBtn")));
            getOkBtn()
            .setOutputMarkupId(true)
            .setOutputMarkupPlaceholderTag(true);
            this.setOutputMarkupId(true);
        }

        /**
         * Return always true because ModalWindow.beforeRender set visibility of content to false!
         */
        @Override
        public boolean isVisible() {
            return true;
        }

        public AjaxLink<Object> getOkBtn() {
            return okBtn;
        }

        public Label getMsgLabel() {
            return msgLabel;
        }

        public Label getRemarkLabel() {
            return remarkLabel;
        }
    }
    
    public MessageWindowPanel getMessageWindowPanel() {
        return messageWindowPanel;
    }    

    public class DisableDefaultConfirmBehavior extends Behavior implements IHeaderContributor {

        private static final long serialVersionUID = 1L;

        public void renderHead(IHeaderResponse response) {
            response.render(OnDomReadyHeaderItem
            		.forScript("Wicket.Window.unloadConfirmation = false"));
        }
    }
}
