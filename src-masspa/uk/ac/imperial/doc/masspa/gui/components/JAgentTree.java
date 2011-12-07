package uk.ac.imperial.doc.masspa.gui.components;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map.Entry;

import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.google.common.collect.Multimap;

import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.language.Labels;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;

/**
 * A component that displays MASSPA agent types and their derivatives as trees
 * 
 * @author Chris Guenther
 */
public class JAgentTree extends JTree
{
	private static final long serialVersionUID = -6806007606035102581L;

	public JAgentTree(ObservableAgents _agents)
	{
		// Empty the tree
		setTreeModel(null);
		
		// Register as listener
		_agents.addChangeListener(new AgentsChangeListener());
	}
	
	/**
	 * Convert the {@code _tree} into a JTreeModel and expand all nodes
	 * @param _tree agent tree
	 */
	public void setTreeModel(Multimap<String,MASSPAComponent> _tree)
	{
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(Labels.s_AGENT_EDITOR_AGENTS);
		DefaultTreeModel tree = new DefaultTreeModel(top);
		setModel(tree);
		
		// Build tree
		if (_tree != null)
		{
			for (Entry<String,Collection<MASSPAComponent>> node : _tree.asMap().entrySet())
			{
				DefaultMutableTreeNode agentType = new DefaultMutableTreeNode(node.getKey());
				for (MASSPAComponent comp : node.getValue())
				{
					DefaultMutableTreeNode agentState = new DefaultMutableTreeNode(comp);
					agentType.add(agentState);
				}
				top.add(agentType);
			}
		}
		
		// Expand tree automatically
		expandAll();
	}

	/**
	 * Uses {@link #expandAllRec(TreePath)} to expand the entire tree
	 */
	public void expandAll()
	{
		TreeNode root = (TreeNode) getModel().getRoot();
		expandAllRec(new TreePath(root));
	}

	/**
	 * Expand subtree of {@code _parent} 
	 * @param _parent a valid subtree of the agent tree;
	 */
	protected void expandAllRec(TreePath _parent)
	{
		TreeNode node = (TreeNode) _parent.getLastPathComponent();
		if (node.getChildCount() >= 0)
		{
			for (@SuppressWarnings({"unchecked"})
			Enumeration<TreeNode> e = (Enumeration<TreeNode>)node.children(); e.hasMoreElements();)
			{
				TreeNode n = e.nextElement();
				TreePath path = _parent.pathByAddingChild(n);
				expandAllRec(path);
			}
		}
		expandPath(_parent);
	}

	private class AgentsChangeListener implements ChangeListener
	{
		//****************************************************
		// Implement the ChangeListener interface 
		//****************************************************
		@Override
		public void stateChanged(ChangeEvent _e)
		{
			ObservableAgents agents = (ObservableAgents) _e.getSource();
			setTreeModel((agents.getMASSPAAgents() != null) ? agents.getMASSPAAgents().getAgents() : null);
		}
	}
}
