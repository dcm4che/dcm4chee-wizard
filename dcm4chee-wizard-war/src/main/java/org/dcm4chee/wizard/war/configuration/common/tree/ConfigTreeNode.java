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

package org.dcm4chee.wizard.war.configuration.common.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dcm4chee.wizard.war.configuration.common.tree.ConfigTreeProvider.ConfigurationType;
import org.dcm4chee.wizard.war.configuration.simple.model.ConfigNodeModel;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class ConfigTreeNode implements Serializable, Comparable<ConfigTreeNode> {
	
	private static final long serialVersionUID = 1L;

	public enum TreeNodeType {
		DEVICE, CONNECTION, APPLICATION_ENTITY, TRANSFER_CAPABILITY,
		FORWARD_RULE, FORWARD_SCHEDULE, RETRY, COERCION, // proxy values
		CONTAINER_CONNECTIONS, 
		CONTAINER_APPLICATION_ENTITIES, 
		CONTAINER_TRANSFER_CAPABILITIES, 
		CONTAINER_TRANSFER_CAPABILITY_TYPE, 
		CONTAINER_FORWARD_RULES, 
		CONTAINER_FORWARD_SCHEDULES, 
		CONTAINER_RETRIES, 
		CONTAINER_COERCIONS
	};

	// device containers
	public static final int CONTAINER_CONNECTIONS = 0;
	public static final int CONTAINER_APPLICATION_ENTITIES = 1;
	
	// ae containers
	public static final int CONTAINER_TRANSFER_CAPABILITIES = 0;
	// proxy ae containers
	public static final int CONTAINER_FORWARD_RULES = 1;
	public static final int CONTAINER_FORWARD_SCHEDULES = 2;
	public static final int CONTAINER_RETRIES = 3;
	public static final int CONTAINER_COERCION = 4;
	
	private String name;
	private TreeNodeType type;
	private ConfigurationType configurationType;
	private ConfigTreeNode parent;
	private List<ConfigTreeNode> children = new ArrayList<ConfigTreeNode>();
	private ConfigNodeModel model;

	public ConfigTreeNode(ConfigTreeNode parent, String name, TreeNodeType type, ConfigNodeModel model)	{
		this.name = name;
		this.type = type;
		if (parent != null)
			parent.add(this);
		this.model = model;
	}

	public ConfigTreeNode(ConfigTreeNode parent, String name, TreeNodeType type, 
			ConfigurationType configurationType, ConfigNodeModel model)	{
		this(parent, name, type, model);
		this.configurationType = configurationType;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public TreeNodeType getNodeType() {
		return type;
	}
	
	public ConfigurationType getConfigurationType() {
		return configurationType;
	}

	public void setConfigurationType(ConfigurationType configurationType) {
		this.configurationType =configurationType;
	}

	public ConfigNodeModel getModel() {
		return model;
	}

	public void setModel(ConfigNodeModel model) {
		this.model = model;
	}

	public ConfigTreeNode getParent() {
		return parent;
	}

	public ConfigTreeNode getAncestor(int hops) {
		return (hops < 0) ? null : (hops == 0) ? this : parent.getAncestor(hops - 1);
	}

	public ConfigTreeNode getRoot() {
		return parent == null ? this : parent.getRoot();
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public List<ConfigTreeNode> getChildren() {
		return children;
	}

	public void removeChildren() {
		children.clear();
	}

	public ConfigTreeNode getContainer(int position) {
		return this.getChildren().get(position);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return getPath().hashCode();
	}
	
	public String getPath() {
		return parent != null ? parent.getPath() + name : name;
	}

	public void remove() {
		if (parent != null) {
			parent.children.remove(this);
			parent = null;
		}
	}

	public void add(ConfigTreeNode dtn) {
		add(dtn, children.size());
	}
	
	public void add(ConfigTreeNode node, int index)	{
		node.remove();
		node.parent = this;
		children.add(index, node);
	}

	public int compareTo(ConfigTreeNode node) {
		return this.getName().compareTo(node.getName());
	}
	
	@Override
	public boolean equals(Object node) {
		return (node instanceof ConfigTreeNode) ? 
				compareTo((ConfigTreeNode) node) == 0 : false;
	}
}