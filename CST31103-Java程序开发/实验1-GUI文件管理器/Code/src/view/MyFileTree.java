package view;

import java.io.File;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import view.MyFileTree.DefaultFileNodes;

public class MyFileTree extends JTree {
	class DefaultFileNodes extends DefaultMutableTreeNode {
		public DefaultFileNodes() {
			for (File file : File.listRoots()) {
				add(new DefaultMutableTreeNode(file));
			}
		}
	}

	public MyFileTree() {
		setModel(new DefaultTreeModel(new DefaultFileNodes()));
	}

	public File getCurrentFile() {
		DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) getLastSelectedPathComponent();
		File file = (File) defaultMutableTreeNode.getUserObject();
		return file;
	}

	public void loadingTree(DefaultMutableTreeNode node, File[] tmp) {
		File file = (File) node.getUserObject();
		if (file.isDirectory()) {
			if (tmp == null)
				return;
			for (File f : tmp) {
				if (f.isDirectory()) {
					node.add(new DefaultMutableTreeNode(f));
				}
			}
		} else {
			return;
		}
	}
}
