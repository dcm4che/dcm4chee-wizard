package org.dcm4chee.wizard.war.configuration.simple.tree;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultTableTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ISortableTreeProvider;
import org.dcm4che.conf.api.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigTableTree extends DefaultTableTree<ConfigTreeNode,String> {

	private static final long serialVersionUID = 1L;

	private static Logger log = LoggerFactory.getLogger(ConfigTableTree.class);
	
	public ConfigTableTree(String id, List<IColumn<ConfigTreeNode,String>> columns,
			ISortableTreeProvider<ConfigTreeNode,String> provider, int rowsPerPage) {
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
