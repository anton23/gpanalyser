package uk.ac.imperial.doc.masspa.gui.editors;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import uk.ac.imperial.doc.masspa.gui.components.JChannelSetup;
import uk.ac.imperial.doc.masspa.gui.components.channels.gen.JChannelGenerator;
import uk.ac.imperial.doc.masspa.gui.components.topologies.JTopologyViewer;
import uk.ac.imperial.doc.masspa.gui.editors.controllers.ChannelEditorController;
import uk.ac.imperial.doc.masspa.gui.interfaces.ITab;
import uk.ac.imperial.doc.masspa.gui.interfaces.ITabController;
import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.gui.models.ObservableTopology;
import uk.ac.imperial.doc.masspa.gui.util.Constants;

/**
 * This class defines the layout of the
 * MASSPA Channel Editor tab. The editor
 * let's users define MASSPA channels.
 * 
 * @author Chris Guenther
 */
public class ChannelEditor extends JPanel implements ITab
{
	private static final long serialVersionUID = 3689206284233234025L;

	// Layout components
	private final JSplitPane			m_topVertSplit;
	private final JSplitPane			m_horiSplit;
	private final JChannelGenerator		m_channelGenBar;
	private final JTopologyViewer		m_topologyViewer;
	private final JChannelSetup			m_channelSetup;
	
	// The models
	private final ObservableAgents		m_agents;
	private final ObservableTopology	m_topology;
	
	// The controller
	private final ChannelEditorController m_ctrl;
	
	/**
	 * Create the tab
	 * @param _agents
	 * @param _topology 
	 */
	public ChannelEditor(ObservableAgents _agents, ObservableTopology _topology)
	{	
		// Overall layout
		super(new BorderLayout());
		
		// Setup model
		m_agents = _agents;
		m_topology = _topology;
		
		// Initialise layout components
		m_channelGenBar = new JChannelGenerator(m_agents, m_topology);
		m_topologyViewer = new JTopologyViewer(m_topology);
		m_topologyViewer.showChannels(true);
		m_channelSetup = new JChannelSetup(m_agents,m_topology);
		
		// Create composition
		m_horiSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, m_topologyViewer, m_channelSetup);
		m_horiSplit.setResizeWeight(Constants.s_CHANNEL_EDITOR_SIZE_HORI);
		m_horiSplit.setEnabled(false);
		m_topVertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, m_channelGenBar, m_horiSplit);
		m_topVertSplit.setResizeWeight(Constants.s_CHANNEL_EDITOR_SIZE_VERT_TOP);
		m_topVertSplit.setEnabled(false);
		add(m_topVertSplit);
		
		// Setup controller
		m_ctrl = new ChannelEditorController(m_topology, m_channelGenBar, m_topologyViewer, m_channelSetup);
	}

	//**************************************************
	// Implement the ITab interface
	//**************************************************
	@Override
	public ITabController getTabController()
	{
		return m_ctrl;
	}	
}
