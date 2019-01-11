package no.valg.eva.admin.frontend.util;

import java.util.ArrayList;
import java.util.List;

import no.evote.util.Treeable;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public final class TreeUtil {

	private TreeUtil() {
	}

	public static TreeNode pathToSubTree(final List<? extends Treeable> objects) {
		TreeNode root = new DefaultTreeNode("root", null);
		dotpathToTree(objects, new DefaultTreeNode(objects.get(0), root));
		return root;
	}

	// Populates a parent tree node based on a list of paths
	public static TreeNode pathToTree(final List<? extends Treeable> objects) {
		TreeNode root = new DefaultTreeNode("treeRoot", null);
		for (Treeable o : findRoots(objects)) {

			dotpathToTree(objects, new DefaultTreeNode(o, root));
		}

		return root;
	}

	// Populates a parent tree node based on a list of paths
	private static void dotpathToTree(final List<? extends Treeable> objects, final TreeNode parent) {
		for (Treeable o : objects) {
			String id = ((Treeable) parent.getData()).getPath();
			if (o.getPath().length() > id.length()
					&& (o.getPath().substring(0, id.length() + 1).equals(id + ".") && !o.getPath().substring(id.length() + 1, o.getPath().length())
							.contains("."))) {
				dotpathToTree(objects, new DefaultTreeNode(o, parent));
			}
		}
	}

	// Finds the node in a tree containing the data object
	public static TreeNode getNodeFromData(final TreeNode parent, final Object data) {
		if (parent.getData().equals(data)) {
			return parent;
		}

		for (TreeNode node : parent.getChildren()) {
			TreeNode node2 = getNodeFromData(node, data);
			if (node2 != null) {
				return node2;
			}
		}

		return null;
	}

	public static void setAllUnselected(final TreeNode parent) {
		((DefaultTreeNode) parent).setType("");
		for (TreeNode node : parent.getChildren()) {
			setAllUnselected(node);
		}
	}

	// Finds all roots in the list of paths
	public static List<Treeable> findRoots(final List<? extends Treeable> elements) {
		List<Treeable> roots = new ArrayList<>();
		for (Treeable t : elements) {

			Boolean hasParents = false;
			for (Treeable p : elements) {
				if ((p.getPath().length() < t.getPath().length()) && t.getPath().substring(0, p.getPath().length()).equals(p.getPath())) {
					hasParents = true;
				}
			}
			if (!hasParents) {
				roots.add(t);
			}
		}
		return roots;
	}

}
