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

package org.dcm4chee.wizard.tree;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultTableTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ISortableTreeProvider;
import org.dcm4che.conf.api.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigTableTree extends DefaultTableTree<ConfigTreeNode, String> {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(ConfigTableTree.class);

    public ConfigTableTree(String id, List<IColumn<ConfigTreeNode, String>> columns,
            ISortableTreeProvider<ConfigTreeNode, String> provider, int rowsPerPage) {
        super(id, columns, provider, rowsPerPage);
    }

    @Override
    public void expand(ConfigTreeNode node) {
        if (node.getNodeType().equals(ConfigTreeNode.TreeNodeType.DEVICE)) {
            try {
                ConfigTreeProvider.get().loadDevice(node);
            } catch (ConfigurationException e) {
                log.error("Error loading device " + node.getName() + " into tree", e);
                return;
            }
        }
        super.expand(node);
    }

    @Override
    public void collapse(ConfigTreeNode node) {
        if (node.getNodeType().equals(ConfigTreeNode.TreeNodeType.DEVICE))
            ConfigTreeProvider.get().unloadDevice(node);
        super.collapse(node);
    }
}
