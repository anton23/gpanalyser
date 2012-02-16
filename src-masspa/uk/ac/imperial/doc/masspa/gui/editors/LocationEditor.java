package uk.ac.imperial.doc.masspa.gui.editors;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import uk.ac.imperial.doc.masspa.gui.components.JLocationsSetup;
import uk.ac.imperial.doc.masspa.gui.components.topologies.JTopologyViewer;
import uk.ac.imperial.doc.masspa.gui.components.topologies.gen.JTopologyGenerator;
import uk.ac.imperial.doc.masspa.gui.editors.controllers.LocationEditorController;
import uk.ac.imperial.doc.masspa.gui.interfaces.ITab;
import uk.ac.imperial.doc.masspa.gui.interfaces.ITabController;
import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.gui.models.ObservableTopology;
import uk.ac.imperial.doc.masspa.gui.util.Constants;

/**
 * This class defines the layout of the
 * MASSPA Location Editor tab. The editor
 * let's users define MASSPA locations and
 * agent populations.
 * 
 * @author Chris Guenther
 */
public class LocationEditor extends JPanel implements ITab
{
	private static final long serialVersionUID = 515749132154022897L;	
	
	// Layout components
	protected final JSplitPane				m_topVertSplit;
	protected final JSplitPane				m_horiSplit;
	protected final JTopologyGenerator		m_topologyGenBar;
	protected final JTopologyViewer			m_topologyViewer;
	protected final JLocationsSetup			m_locationsSetup;
	
	// The models
	protected final ObservableAgents		m_agents;
	protected final ObservableTopology		m_topology;

	// The controller
	protected final LocationEditorController m_ctrl;
	
	/**
	 * Create the tab
	 * @param _agents
	 * @param _topology
	 */
	public LocationEditor(ObservableAgents _agents, ObservableTopology _topology)
	{	
		// Overall layout
		super(new BorderLayout());
		
		// Setup model
		m_agents = _agents;
		m_topology = _topology;
		
		// Initialise layout components
		m_topologyGenBar = new JTopologyGenerator();
		m_topologyViewer = new JTopologyViewer(m_topology);
		m_locationsSetup = new JLocationsSetup(m_agents,m_topology);
		
		// Create composition
		m_horiSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, m_topologyViewer, m_locationsSetup);
		m_horiSplit.setResizeWeight(Constants.s_LOCATION_EDITOR_SIZE_HORI);
		m_horiSplit.setEnabled(false);
		m_topVertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, m_topologyGenBar, m_horiSplit);
		m_topVertSplit.setResizeWeight(Constants.s_LOCATION_EDITOR_SIZE_VERT_TOP);
		m_topVertSplit.setEnabled(false);
		add(m_topVertSplit);
		
		// Setup controller
		m_ctrl = new LocationEditorController(m_topology, m_topologyGenBar, m_topologyViewer, m_locationsSetup);
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
