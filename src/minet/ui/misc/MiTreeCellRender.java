package minet.ui.misc;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import common.datastruct.User;

public class MiTreeCellRender extends DefaultTreeCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2314297348179778883L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		// TODO Auto-generated method stub
		
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		
		if (node.getUserObject() instanceof User) {
			User user = (User)node.getUserObject();
			setIcon(user.getSmallIcon());
			setText(user.getName());
		}
		return this;

	}
}
