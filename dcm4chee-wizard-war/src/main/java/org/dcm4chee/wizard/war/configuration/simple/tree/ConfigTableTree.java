package org.dcm4chee.wizard.war.configuration.simple.tree;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.dcm4che.conf.api.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wickettree.DefaultTableTree;
import wickettree.ISortableTreeProvider;

public class ConfigTableTree extends DefaultTableTree<ConfigTreeNode> {

	private static final long serialVersionUID = 1L;

	private static Logger log = LoggerFactory.getLogger(ConfigTableTree.class);
	
	public ConfigTableTree(String id, List<IColumn<ConfigTreeNode>> columns,
			ISortableTreeProvider<ConfigTreeNode> provider, int rowsPerPage) {
		super(id, columns, provider, rowsPerPage);
	}

	@Override
	public void expand(ConfigTreeNode node) {
		if (node.getNodeType().equals(ConfigTreeNode.TreeNodeType.DEVICE)) {
			try {
				ConfigTreeProvider.get().loadDevice(node);
			} catch (ConfigurationException e) {
				log.error("Error loading device " + node.getName() + " into tree", e);
				e.printStackTrace();
			}
		}		
		super.expand(node);
	}
}
