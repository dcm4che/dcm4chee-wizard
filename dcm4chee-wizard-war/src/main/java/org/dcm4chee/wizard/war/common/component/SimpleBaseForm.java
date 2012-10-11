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
 ***** END LICENSE BLOCK ***** */

package org.dcm4chee.wizard.war.common.component;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.dcm4chee.wizard.war.common.behavior.MarkInvalidBehavior;
import org.dcm4chee.wizard.war.common.behavior.TooltipBehavior;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @author Robert David <robert.david@agfa.com>
 */
public class SimpleBaseForm extends Form<Object> {

    private static final long serialVersionUID = 0L;
    public static final String LABEL_ID_EXTENSION = ".label";

    private String resourceIdPrefix;
    private WebMarkupContainer parent;
    private boolean rendered;
    
    MarkInvalidBehavior markInvalidBehaviour = new MarkInvalidBehavior();
        
    public SimpleBaseForm(String id) {
        super(id);
    }
    
    public SimpleBaseForm(String id, IModel<Object> model) {
        super(id, model);
    }

    public void setResourceIdPrefix(String resourceIdPrefix) {
        this.resourceIdPrefix = resourceIdPrefix;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        if (!rendered) {
            visitChildren(new FormVisitor());
            rendered = true;
        }
    }

    public void clearParent() {
        this.parent = null;
    }
    
    public WebMarkupContainer createAjaxParent(String id) {
        super.add(this.parent = new WebMarkupContainer(id));
        this.parent.setOutputMarkupId(true);
        this.parent.setOutputMarkupPlaceholderTag(true);
        return this.parent;
    }

    public MarkupContainer addComponent(Component child) {
        if (parent == null)
            super.add(child);
        else
            parent.add(child);
        return this;
    }
    
    /**
     * Add a Label and a TextField with text from ResourceModel.
     * <p>
     * The text for the label is defined in the ResourceModel with key &lt;module&gt;.&lt;id&gt;Label<br/>
     * The TextField use a CompoundPropertyModel with given <code>id</code><br/>
     * 
     * @param id        Id of TextField.
     * 
     * @return The TextField (to allow adding Validators,.. )
     */
//    public TextField<String> addLabeledTextField(String id) {
//        TextField<String> tf = new TextField<String>(id);
//        addInternalLabel(id);
//        this.addComponent(tf);
//        return tf;
//    }
//
//    public TextField<Integer> addLabeledNumberTextField(String id) {
//        TextField<Integer> tf = new TextField<Integer>(id);
//        addInternalLabel(id);
//        this.addComponent(tf);
//        return tf;
//    }
//
//    public TextField<String> addTextField(String id, final IModel<Boolean> enabledModel, boolean addLabel) {
//        TextField<String> tf = new TextField<String>(id) {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public boolean isEnabled() {
//                return enabledModel == null ? true : enabledModel.getObject();
//            }
//        };
//        if (addLabel) 
//            addInternalLabel(id);
//        addComponent(tf);
//        return tf;
//    }
//
//    public PatientNameField addPatientNameField(String id, IModel<String> model, boolean useFnGn, final IModel<Boolean> enabledModel, boolean addLabel) {
//        PatientNameField tf = new PatientNameField(id, model, useFnGn) {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public boolean isEnabled() {
//                return enabledModel == null ? true : enabledModel.getObject();
//            }
//        };
//        if (addLabel) 
//            addInternalLabel(id);
//        addComponent(tf);
//        return tf;
//    }
//
//    public SimpleDateTimeField addDateTimeField(String id, IModel<Date> model, final IModel<Boolean> enabledModel, final boolean max, boolean addLabel) {
//        SimpleDateTimeField dtf = getSimpleDateTimeField(id, model, enabledModel, max);
//        if (addLabel) 
//            addInternalLabel(id);
//        addComponent(dtf);
//        return dtf;
//    }
//
//    public SimpleDateTimeField getSimpleDateTimeField(String id, IModel<Date> model,
//            final IModel<Boolean> enabledModel, final boolean max) {
//        final SimpleDateTimeField dtf = new SimpleDateTimeField(id, model, max) {
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public boolean isEnabled() {
//                return enabledModel == null ? true : enabledModel.getObject();
//            }
//        };
//        dtf.getDateField().add(new AttributeModifier("title", 
//                new StringResourceModel(toResourceKey(id) + ".date.tooltip", this, 
//                        new AbstractReadOnlyModel<String>() {
//                            private static final long serialVersionUID = 1L;
//        
//                            @Override
//                            public String getObject() {
//                                return DateUtils.getDatePattern(dtf);
//                            }
//                        }
//                )
//        ));
//        dtf.getTimeField().add(new AttributeModifier("title", 
//                new StringResourceModel(toResourceKey(id) + ".time.tooltip", this, null)));
//        return dtf;
//    }
//    
//    public SimpleDateTimeField getDateTextField(String id, IModel<Date> model, String tooltipPrefix, final IModel<Boolean> enabledModel) {
//        SimpleDateTimeField dtf = new SimpleDateTimeField(id, model) {
//                private static final long serialVersionUID = 1L;
//
//            @Override
//            public boolean isEnabled() {
//                return enabledModel == null ? true : enabledModel.getObject();
//            }
//        };
//        dtf.setWithoutTime(true);
//        if (tooltipPrefix != null) {
//            dtf.getDateField().add(new AttributeModifier("title", 
//                    new StringResourceModel((resourceIdPrefix != null ? resourceIdPrefix : "") 
//                            + tooltipPrefix
//                            + id 
//                            + ".date.tooltip", this, new PropertyModel<Object>(dtf,"textFormat"))));
//        }
//        dtf.add(markInvalidBehaviour);
//        return dtf;
//    }
//    
//    public DropDownChoice<?> addLabeledDropDownChoice(String id, IModel<Object> model, List<String> values) {
//        DropDownChoice<?> ch = model == null ? new DropDownChoice<Object>(id, values) :
//                                            new DropDownChoice<Object>(id, model, values);
//        addInternalLabel(id);
//        addComponent(ch);
//        return ch;
//    }
//
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    public DropDownChoice addDropDownChoice(String id, final IModel<?> model, IModel<? extends List<? extends Object>> choices, final IModel<Boolean> enabledModel, boolean addLabel) {
//        DropDownChoice<?> ch = model == null ? new DropDownChoice<Object>(id, choices)  {
//
//                                                private static final long serialVersionUID = 1L;
//                                    
//                                                @Override
//                                                public boolean isEnabled() {
//                                                    return enabledModel == null ? true : enabledModel.getObject();
//                                                }
//                                            } : 
//                                            new DropDownChoice(id, model, choices) {
//
//                                                private static final long serialVersionUID = 1L;
//
//                                                @Override
//                                                public boolean isEnabled() {
//                                                    return enabledModel == null ? true : enabledModel.getObject();
//                                                }                                                
//                                            };
//        if (addLabel) 
//            addInternalLabel(id);
//        addComponent(ch);
//        return ch;
//    }
//
//    public CheckBox addLabeledCheckBox(String id, IModel<Boolean> model) {
//        CheckBox chk = model == null ? new CheckBox(id) :
//                                            new CheckBox(id, model);
//        addInternalLabel(id);
//        addComponent(chk);
//        return chk;
//    }
//    
//    public Label addInternalLabel(String id) {
//        String labelId = id + LABEL_ID_EXTENSION;
//        return addLabel(labelId);
//    }
//    
//    public Label addLabel(String id) {
//        Label l = new Label(id, new ResourceModel(toResourceKey(id)));
//        addComponent(l);
//        return l;
//    }
//
//    private String toResourceKey(String id) {
//        StringBuffer resourceKey = new StringBuffer(id);
//        if (parent != null) {
//            resourceKey.insert(0, ".");
//            resourceKey.insert(0, parent.getId());
//        }
//        if (resourceIdPrefix != null) resourceKey.insert(0, resourceIdPrefix);
//        return resourceKey.toString();
//    }
    
    public static void addInvalidComponentsToAjaxRequestTarget(
            final AjaxRequestTarget target, final Form<?> form) {
    	
        form.visitChildren(
        		new IVisitor<Component, Void>() {

		            public void component(Component component, IVisit<Void> visit) {
		                if (component instanceof FormComponent<?>) {
		                    FormComponent<?> formComponent = (FormComponent<?>) component;
		                    if (!formComponent.isValid()) 
		                        target.add(formComponent);
		                }
		            }
        		});
    }
    
    public static void addFormComponentsToAjaxRequestTarget(
            final AjaxRequestTarget target, final Form<?> form) {
    	
        form.visitChildren(
        		new IVisitor<Component, Void>() {

					public void component(Component component, IVisit<Void> visit) {
		                if (component.getOutputMarkupId()) 
		                    target.add(component);
					}
		        });
    }

    class FormVisitor implements IVisitor<Component, Void>, Serializable {
        
        private static final long serialVersionUID = 0L;

		public void component(Component component, IVisit<Void> visit) {
            if (componentHasNoTooltip(component)) 
            	component.add(new TooltipBehavior(resourceIdPrefix, component.getId()).setGenerateComponentTreePrefix());
            if (component instanceof FormComponent<?>) {
            	component.add(markInvalidBehaviour);
            	component.setOutputMarkupId(true);
            }
		}
    }

    public boolean componentHasNoTooltip(Component component) {
        for (Behavior behavior : component.getBehaviors()) 
            if (behavior instanceof TooltipBehavior)
                return false;
        return true;
    }
}

