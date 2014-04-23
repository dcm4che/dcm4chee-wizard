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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4chee.wizard.WizardApplication;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.ModalWindowRuntimeException;
import org.dcm4chee.wizard.common.component.secure.SecureSessionCheckPage;
import org.dcm4chee.wizard.model.ApplicationEntityModel;
import org.dcm4chee.wizard.model.TransferCapabilityModel;
import org.dcm4chee.wizard.tcxml.Group;
import org.dcm4chee.wizard.tcxml.Profile;
import org.dcm4chee.wizard.tcxml.Root;
import org.dcm4chee.wizard.tree.ConfigTreeNode;
import org.dcm4chee.wizard.tree.ConfigTreeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class ApplyTransferCapabilityProfilePage extends SecureSessionCheckPage {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(ApplyTransferCapabilityProfilePage.class);

    // mandatory
    private Model<Boolean> scuModel;
    private Model<Boolean> scpModel;
    private IModel<Group> groupModel;
    private IModel<Profile> profileModel;

    private Root root;

    public ApplyTransferCapabilityProfilePage(final ModalWindow window,
            final TransferCapabilityModel transferCapabilityModel, final ConfigTreeNode aeNode) {
        super();

        scuModel = Model.of(true);
        scpModel = Model.of(true);
        groupModel = new Model<Group>();
        profileModel = new Model<Profile>();

        try {
            root = ((WizardApplication) getApplication()).getTransferCapabilityProfiles();

            if (root.getTransferCapabilityGroups().size() > 0)
                groupModel.setObject(root.getTransferCapabilityGroups().get(0));

            final ApplicationEntity applicationEntity = ((ApplicationEntityModel) aeNode.getModel())
                    .getApplicationEntity();

            add(new WebMarkupContainer("apply-transferCapability-profile-title"));

            setOutputMarkupId(true);
            final ExtendedForm form = new ExtendedForm("form");
            form.setResourceIdPrefix("dicom.edit.transferCapability.profile.");
            add(form);

            form.add(new Label("scu.label", new ResourceModel("dicom.edit.transferCapability.profile.scu.label"))).add(
                    new CheckBox("scu", scuModel));

            form.add(new Label("scp.label", new ResourceModel("dicom.edit.transferCapability.profile.scp.label"))).add(
                    new CheckBox("scp", scpModel));

            form.add(new Label("group.label", new ResourceModel("dicom.edit.transferCapability.profile.group.label")));

            List<Group> groups = root.getTransferCapabilityGroups();
            Collections.sort(groups, new Comparator<Group>() {
                public int compare(Group group1, Group group2) {
                    return group1.getName().compareToIgnoreCase(group2.getName());
                }
            });

            final DropDownChoice<Group> groupDropDown = new DropDownChoice<Group>("group", groupModel, groups,
                    new IChoiceRenderer<Group>() {

                        private static final long serialVersionUID = 1L;

                        public Object getDisplayValue(Group group) {
                            return group.getName();
                        }

                        public String getIdValue(Group group, int index) {
                            return group.getName();
                        }
                    });
            form.add(groupDropDown.setNullValid(false));

            form.add(new Label("profile.label",
                    new ResourceModel("dicom.edit.transferCapability.profile.profile.label")));
            final DropDownChoice<Profile> profileDropDown = new DropDownChoice<Profile>("profile", profileModel, root
                    .getTransferCapabilityGroups().size() <= 0 ? new ArrayList<Profile>() : orderedProfiles(root
                    .getTransferCapabilityGroups().get(0)), new IChoiceRenderer<Profile>() {

                private static final long serialVersionUID = 1L;

                public Object getDisplayValue(Profile profile) {
                    return profile.name;
                }

                public String getIdValue(Profile profile, int index) {
                    return profile.name;
                }
            });
            form.add(profileDropDown.setNullValid(false).setEnabled(profileDropDown.getChoices().size() > 0));
            if (profileDropDown.getChoices().size() > 0)
                profileModel.setObject(profileDropDown.getChoices().get(0));

            groupDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    profileDropDown.setEnabled(false);
                    profileDropDown.setChoices(new ArrayList<Profile>());
                    String groupName = groupDropDown.getValue();
                    for (Group group : root.getTransferCapabilityGroups())
                        if (groupName.equals(group.getName()) && group.getTransferCapabilityProfiles().size() > 0) {
                            profileDropDown.setChoices(orderedProfiles(group));
                            if (profileDropDown.getChoices().size() > 0)
                                profileModel.setObject(profileDropDown.getChoices().get(0));
                            profileDropDown.setEnabled(true);
                        }
                    target.add(form);
                }
            });

            form.add(new IndicatingAjaxButton("submit", new ResourceModel("saveBtn"), form) {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        Profile profile = profileModel.getObject();
                        if (scuModel.getObject()) {
                            TransferCapability transferCapability = new TransferCapability(Role.SCU + " "
                                    + profile.name, profile.SOPClass, Role.SCU, profile.dicomTransferSyntaxes
                                    .toArray(new String[0]));
                            applicationEntity.addTransferCapability(transferCapability);
                        }

                        if (scuModel.getObject() || scpModel.getObject())
                            ConfigTreeProvider.get().mergeDevice(applicationEntity.getDevice());

                        if (scpModel.getObject()) {
                            TransferCapability transferCapability = new TransferCapability(Role.SCP + " "
                                    + profile.name, profile.SOPClass, Role.SCP, profile.dicomTransferSyntaxes
                                    .toArray(new String[0]));
                            applicationEntity.addTransferCapability(transferCapability);
                        }

                        if (scuModel.getObject() || scpModel.getObject())
                            ConfigTreeProvider.get().mergeDevice(applicationEntity.getDevice());

                        window.close(target);
                    } catch (Exception e) {
                        log.error(this.getClass().toString() + ": " + "Error modifying transfer capability: "
                                + e.getMessage());
                        log.debug("Exception", e);
                        throw new ModalWindowRuntimeException(e.getLocalizedMessage());
                    }
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    if (target != null)
                        target.add(form);
                }
            });
            form.add(new AjaxFallbackButton("cancel", new ResourceModel("cancelBtn"), form) {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    window.close(target);
                }

                @Override
                protected void onError(AjaxRequestTarget arg0, Form<?> arg1) {
                }
            }.setDefaultFormProcessing(false));
        } catch (ConfigurationException e) {
            log.error("Error creating TransferCapabilityValidator for sopClass TextField", e);
        }
    }

    private List<Profile> orderedProfiles(Group group) {
        List<Profile> profiles = group.getTransferCapabilityProfiles();
        Collections.sort(profiles, new Comparator<Profile>() {
            public int compare(Profile profile1, Profile profile2) {
                return profile1.name.compareToIgnoreCase(profile2.name);
            }
        });
        return profiles;
    }
}
