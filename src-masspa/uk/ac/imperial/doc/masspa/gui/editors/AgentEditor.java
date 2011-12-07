package uk.ac.imperial.doc.masspa.gui.editors;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;

import uk.ac.imperial.doc.masspa.gui.components.JAgentTree;
import uk.ac.imperial.doc.masspa.gui.components.JLineNumberCol;
import uk.ac.imperial.doc.masspa.gui.editors.controllers.AgentEditorController;
import uk.ac.imperial.doc.masspa.gui.interfaces.ITab;
import uk.ac.imperial.doc.masspa.gui.interfaces.ITabController;
import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.gui.models.ObservableDocument;
import uk.ac.imperial.doc.masspa.gui.util.CompoundUndoManager;
import uk.ac.imperial.doc.masspa.gui.util.Constants;
import uk.ac.imperial.doc.masspa.language.Labels;

import javax.swing.JLabel;

/**
 * This class defines the layout of the
 * MASSPA Agent Editor tab. The editor
 * let's users define Constants, Variables
 * and MASSPA Agents in a text editor.
 * 
 * @author Chris Guenther
 */
public class AgentEditor extends JPanel implements ITab
{
	private static final long serialVersionUID = -384715157193648430L;

	// Layout components
	protected final JPanel			m_agentDefPanel;
	protected final JPanel			m_agentTreePanel;
	protected final JSplitPane 		m_horiSplit;
	
	// Editor Components
	protected final JLabel 			m_agentDefLabel;
	protected final JLineNumberCol	m_agentDefLineNos;
	protected final JTextPane 		m_agentDefText;
	protected final JScrollPane		m_agentDefScroll;
	protected final JLabel 			m_agentTreeLabel;
	protected final JAgentTree		m_agentTree;
	protected final JScrollPane		m_agentTreeScroll;
	protected final JButton			m_compileBtn;
	
	// The models
	protected final ObservableAgents m_agents;
	
	// The controller
	protected final AgentEditorController m_ctrl;
	
	/**
	 * Create the tab
	 * @param _agentDefDoc
	 * @param _pctmc
	 */
	public AgentEditor(ObservableDocument _agentDefDoc, ObservableAgents _agents)
	{
		// Overall layout
		super(new BorderLayout());
		
		// Setup models
		m_agents = _agents;
		
		// Initialise editor components
		m_agentDefLabel = new JLabel(Labels.s_AGENT_EDITOR_DEF_LABEL);
		m_agentDefText = new JTextPane(_agentDefDoc);
		m_agentDefLineNos = new JLineNumberCol(_agentDefDoc);

		m_agentDefText.getDocument().addDocumentListener(m_agentDefLineNos.getDocListener());
		m_agentDefScroll = new JScrollPane(m_agentDefText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		m_agentDefScroll.setRowHeaderView(m_agentDefLineNos);
		
		m_agentTreeLabel = new JLabel(Labels.s_AGENT_EDITOR_TREE_LABEL);
		m_agentTree = new JAgentTree(m_agents);
		m_agentTreeScroll = new JScrollPane(m_agentTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		m_compileBtn = new JButton();
		m_compileBtn.setText(Labels.s_AGENT_EDITOR_COMPILE);
		
		// Initialise layout components
		m_agentDefPanel = new JPanel();
		m_agentDefPanel.setLayout(new BorderLayout());
		m_agentTreePanel = new JPanel();
		m_agentTreePanel.setLayout(new BorderLayout());
		
		// Create composition 
		m_agentDefPanel.add(m_agentDefLabel, BorderLayout.NORTH);
		m_agentDefPanel.add(m_agentDefScroll);
		
		m_agentTreePanel.add(m_agentTreeLabel, BorderLayout.NORTH);
		m_agentTreePanel.add(m_agentTreeScroll);
		m_agentTreePanel.add(m_compileBtn, BorderLayout.SOUTH);
		
		m_horiSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, m_agentDefPanel, m_agentTreePanel);
		m_horiSplit.setResizeWeight(Constants.s_AGENT_EDITOR_DEF_SIZE_HORI);
		add(m_horiSplit);
		
		// Setup controller
		m_ctrl = new AgentEditorController(m_compileBtn, m_agentDefText.getDocument(), new CompoundUndoManager(m_agentDefText), m_agents);
	}

	//**************************************************
	// Implement ITab interface
	//**************************************************
	@Override
	public ITabController getTabController()
	{
		return m_ctrl;
	}
}
