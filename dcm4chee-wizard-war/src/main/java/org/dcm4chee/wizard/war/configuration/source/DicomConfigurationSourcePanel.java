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

package org.dcm4chee.wizard.war.configuration.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.OddEvenListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.wizard.war.configuration.model.source.DicomConfigurationSourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class DicomConfigurationSourcePanel extends Panel {

    private static final long serialVersionUID = 1L;

    private static final String MODULE_NAME = "source";

    private static Logger log = LoggerFactory.getLogger(DicomConfigurationSourcePanel.class);

    private PropertyListView<DicomConfigurationSourceModel> list;

    public DicomConfigurationSourcePanel(final String id) {
        super(id);

        // name
        // type
        //
        // LdapEnv env = new LdapEnv();

        // host
        // port
        // cn
        // dc
        // password

        // env.setUrl("ldap://localhost:1389");

        // env.setUserDN("cn=admin,dc=nodomain"); //slapd
        // env.setUserDN("cn=Directory Manager"); //OpenDJ
        // env.setPassword("#trebor33");
        // config = new LdapProxyConfiguration(env, "dc=nodomain");

        add(new Label("nameHdr.label", new ResourceModel("source.nameHdr.label")));
        add(new Label("typeHdr.label", new ResourceModel("source.typeHdr.label")));
        add(new Label("hostHdr.label", new ResourceModel("source.hostHdr.label")));
        add(new Label("portHdr.label", new ResourceModel("source.portHdr.label")));
        add(new Label("cnHdr.label", new ResourceModel("source.cnHdr.label")));
        add(new Label("dcHdr.label", new ResourceModel("source.dcHdr.label")));
        add(new Label("passwordHdr.label", new ResourceModel("source.passwordHdr.label")));
        add(new Label("descriptionHdr.label", new ResourceModel("source.descriptionHdr.label")));

        // final List<ConfigSource> configSourceList = ((UserAccess)
        // JNDIUtils.lookup(UserAccess.JNDI_NAME)).getAllAETGroups();

        add((list = new PropertyListView<DicomConfigurationSourceModel>("list",
                new Model<ArrayList<DicomConfigurationSourceModel>>()) {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unused")
			protected ListItem<DicomConfigurationSourceModel> newItem(final int index) {
                return new OddEvenListItem<DicomConfigurationSourceModel>(index, getListItemModel(getModel(), index));
            }

            @Override
            protected void populateItem(ListItem<DicomConfigurationSourceModel> item) {
                // StringBuffer tooltip = new StringBuffer();
                // String name = item.getModelObject().getTitle();
                // for (AETGroup aetGroup : aetGroups)
                // if (aetGroup.getAets().contains(name))
                // tooltip.append(aetGroup.getGroupname()).append(" ");
                item.add(new Label("name").add(new AttributeModifier("title", new Model<String>(item.getModelObject()
                        .getDescription()))));
                item.add(new Label("type"));
                item.add(new Label("host"));
                item.add(new Label("port"));
                item.add(new Label("cn"));
                item.add(new Label("dc"));
                item.add(new Label("description"));

                // int[] winSize =
                // WebCfgDelegate.getInstance().getWindowSize("aeEdit");
                // item.add(new ModalWindowLink("editAET", modalWindow,
                // winSize[0], winSize[1]) {
                // private static final long serialVersionUID = 1L;
                //
                // @Override
                // public void onClick(AjaxRequestTarget target) {
                // modalWindow
                // .setPageCreator(new ModalWindow.PageCreator() {
                //
                // private static final long serialVersionUID = 1L;
                //
                // @Override
                // public Page createPage() {
                // return new CreateOrEditAETPage(modalWindow,
                // item.getModelObject(), AEListPanel.this);
                // }
                // });
                // super.onClick(target);
                // }
                // }
                // .add(new Image("ae.editAET.image",
                // ImageManager.IMAGE_AE_EDIT)
                // .add(new ImageSizeBehaviour("vertical-align: middle;")))
                // .add(new TooltipBehaviour("ae."))
                // .add(new SecurityBehavior(getModuleName() + ":editAETLink"))
                // );
                //
                // AjaxLink<?> removeAET = new AjaxLink<Object>("removeAET") {
                //
                // private static final long serialVersionUID = 1L;
                //
                // @Override
                // public void onClick(AjaxRequestTarget target) {
                // confirm.confirm(target, new
                // StringResourceModel("ae.confirmDelete", AEListPanel.this,
                // null, new Object[]{item.getModelObject()}),
                // item.getModelObject());
                // }
                // };
                // removeAET.add(new Image("ae.removeAET.image",
                // ImageManager.IMAGE_COMMON_REMOVE)
                // .add(new ImageSizeBehaviour()));
                // removeAET.add(new TooltipBehaviour("ae."));
                // item.add(removeAET);
                // removeAET.add(new SecurityBehavior(getModuleName() +
                // ":removeAETLink"));
                //
                // item.add(new AjaxLink<Object>("echo") {
                //
                // private static final long serialVersionUID = 1L;
                //
                // @Override
                // public void onClick(AjaxRequestTarget target) {
                // dicomEchoWindow.show(target, item.getModelObject());
                // }
                // }
                // .add(new Image("ae.echoAET.image",
                // ImageManager.IMAGE_AE_ECHO)
                // .add(new ImageSizeBehaviour()))
                // .add(new TooltipBehaviour("ae."))
                // .add(new SecurityBehavior(getModuleName() +
                // ":dicomEchoLink"))
                // );
            }
        }));
        updateDicomConfigurationList();
    }

    @Override
    public void onBeforeRender() {
        super.onBeforeRender();
        updateDicomConfigurationList();
    }

    protected void updateDicomConfigurationList() {
        String line;
        BufferedReader reader = null;
        try {
            List<DicomConfigurationSourceModel> updatedList = new ArrayList<DicomConfigurationSourceModel>();
            String fn = System.getProperty("dcm4chee-web3.cfg.path", "conf/dcm4chee-web3/");
            if (fn == null)
                throw new FileNotFoundException(
                        "Web config path not found! Not specified with System property 'dcm4chee-web3.cfg.path'");
            File configFile = new File(fn + "dicom-configuration.json");
            if (!configFile.isAbsolute())
                configFile = new File(System.getProperty("jboss.server.home.dir"), configFile.getPath());

            reader = new BufferedReader(new FileReader(configFile));
            while ((line = reader.readLine()) != null)
                updatedList.add((DicomConfigurationSourceModel) JSONObject.toBean(JSONObject.fromObject(line),
                        DicomConfigurationSourceModel.class));
            list.setModelObject(updatedList);
        } catch (IOException ioe) {
            log.error("Error updating dicom configuration list", ioe);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public static String getModuleName() {
        return MODULE_NAME;
    }
}
