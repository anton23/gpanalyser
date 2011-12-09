package uk.ac.imperial.doc.masspa.gui.components;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.gui.models.ObservableTopology;
import uk.ac.imperial.doc.masspa.gui.models.topologies.LocationComponent;
import uk.ac.imperial.doc.masspa.gui.models.topologies.MASSPAAgentPopComponent;
import uk.ac.imperial.doc.masspa.gui.models.topologies.Topology;
import uk.ac.imperial.doc.masspa.gui.util.Actions;
import uk.ac.imperial.doc.masspa.gui.util.list.SortedListModel;
import uk.ac.imperial.doc.masspa.language.Labels;
import uk.ac.imperial.doc.masspa.representation.components.AllComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;

import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JList;

import com.google.common.collect.Multimap;
import javax.swing.JTextField;

public class JLocationsSetup extends JPanel
{
	private static final long serialVersionUID = -3966916062543995253L;
	
	// Components
	private final JTabbedPane		m_tabs;
	
	// Disabled locations options
	private final JPanel			m_disabledLocs;
	private final JLabel			m_disabledLocsLabel;
	private final JList 			m_disabledLocsList;	
	private final JScrollPane		m_disabledLocsListScroll;
	
	// Population options
	private final JPanel			m_populations;
	private final JLabel			m_stateLabel;
	private final JComboBox			m_stateCombo;
	private final JPanel			m_stateComboPanel;
	private final JTextField 		m_populationExpressionText;
	private final JButton			m_addPopulationBtn;
	private final JLabel			m_populationsLabel;
	private final JList 			m_populationsList;	
	private final JScrollPane		m_populationsListScroll; 
	
	// Models
	private DefaultListModel 		m_disabledLocsModel;
	private DefaultListModel		m_populationsModel;
	private Topology				m_topology=null;
	
	/**
	 * Create the Locations setup panel.
	 * @param _agents
	 * @param _topology
	 */
	public JLocationsSetup(ObservableAgents _agents, ObservableTopology _topology)
	{
		// Overall layout
		super(new BorderLayout());
		
		// Setup models
		m_disabledLocsModel = new SortedListModel();
		m_populationsModel = new SortedListModel();
		
		// Disabled locations options
		m_disabledLocs = new JPanel();
		m_disabledLocs.setLayout(new BoxLayout(m_disabledLocs,BoxLayout.Y_AXIS));
		m_disabledLocsLabel = new JLabel(Labels.s_LOCATION_EDITOR_DISABLED_LOCS);
		m_disabledLocsLabel.setAlignmentX(CENTER_ALIGNMENT);
		m_disabledLocsLabel.setToolTipText(Labels.s_LOCATION_EDITOR_ENABLE_DISABLE_TIP);
		m_disabledLocsList = new JList(m_disabledLocsModel);
		m_disabledLocsList.setName(Labels.s_LOCATION_EDITOR_DISABLED_LOCS);
		m_disabledLocsList.setToolTipText(Labels.s_LOCATION_EDITOR_ENABLE_LOCS_TIP);
		m_disabledLocsListScroll = new JScrollPane(m_disabledLocsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		m_disabledLocsListScroll.setAlignmentX(CENTER_ALIGNMENT);
		m_disabledLocs.add(m_disabledLocsLabel);
		m_disabledLocs.add(m_disabledLocsListScroll);
		
		// Population options
		m_populations = new JPanel();
		m_populations.setLayout(new BoxLayout(m_populations, BoxLayout.Y_AXIS));
		m_stateLabel = new JLabel(Labels.s_LOCATION_EDITOR_STATE);
		m_stateLabel.setAlignmentX(CENTER_ALIGNMENT);
		m_stateCombo = new JComboBox();
		m_stateComboPanel = new JPanel();
		m_stateComboPanel.add(m_stateCombo);
		m_stateComboPanel.setAlignmentX(CENTER_ALIGNMENT);
		m_populationExpressionText = new JTextField();
		m_populationExpressionText.setName(Labels.s_LOCATION_EDITOR_POPULATIONS_EXPR);
		m_populationExpressionText.setToolTipText(Labels.s_LOCATION_EDITOR_POPULATIONS_EXPR_TIP);
		m_addPopulationBtn = new JButton(Labels.s_LOCATION_EDITOR_ADD_POPULATIONS);
		m_addPopulationBtn.setToolTipText(Labels.s_LOCATION_EDITOR_ADD_POPULATIONS_TIP);
		m_addPopulationBtn.setActionCommand(Actions.s_ADD_POPULATION);
		m_addPopulationBtn.setAlignmentX(CENTER_ALIGNMENT);
		m_populationsLabel = new JLabel(Labels.s_LOCATION_EDITOR_POPULATIONS_LIST_LABEL);
		m_populationsLabel.setAlignmentX(CENTER_ALIGNMENT);
		m_populationsList = new JList(m_populationsModel);
		m_populationsList.setVisibleRowCount(40);
		m_populationsList.setName(Labels.s_LOCATION_EDITOR_POPULATIONS);
		m_populationsList.setToolTipText(Labels.s_LOCATION_EDITOR_REM_POPULATIONS_TIP);
		m_populationsListScroll = new JScrollPane(m_populationsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		m_populationsListScroll.setAlignmentX(CENTER_ALIGNMENT);
		m_populations.add(m_stateLabel);
		m_populations.add(m_stateComboPanel);
		m_populations.add(m_addPopulationBtn);
		m_populations.add(m_populationExpressionText);
		m_populations.add(m_populationsLabel);
		m_populations.add(m_populationsListScroll);
		
		// Create tabs
		m_tabs = new JTabbedPane(JTabbedPane.TOP);
		m_tabs.setBorder(new TitledBorder(null, Labels.s_LOCATION_EDITOR_OPTIONS, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		m_tabs.add(Labels.s_LOCATION_EDITOR_LOCATIONS, m_disabledLocs);
		m_tabs.add(Labels.s_LOCATION_EDITOR_POPULATIONS, m_populations);

		// Composition
		add(m_tabs);
		
		// Register as Listener
		_agents.addChangeListener(new AgentsChangeListener());
		_topology.addChangeListener(new TopologyChangeListener());
		m_stateCombo.addItemListener(new StateComboChangeListener());
		m_populationsList.addListSelectionListener(new PopulationListChangeListener());
		
		// Init state Combobox
		updateAgentStates(null);
	}
	
	/**
	 * Listener for button action events
	 * @param _l listener to be added
	 */
	public void addActionListener(ActionListener _l)
	{
		m_addPopulationBtn.addActionListener(_l);
	}
	
	/**
	 * Listener for list key events
	 * @param _l listener to be added
	 */
	public void addKeyListener(KeyListener _l)
	{
		m_disabledLocsList.addKeyListener(_l);
		m_populationsList.addKeyListener(_l);
	}

	/**
	 * Listener for list item selection events
	 * @param _l listener to be added
	 */
	public void addListSelectionListener(ListSelectionListener _l)
	{
		m_disabledLocsList.addListSelectionListener(_l);
		m_populationsList.addListSelectionListener(_l);
	}
	
	/**
	 * @return currently selected agent state in population options
	 */
	public MASSPAComponent getCurrentPopulationState()
	{
		return (MASSPAComponent)m_stateCombo.getSelectedItem();
	}
	
	/**
	 * @return population expression as string
	 */
	public String getPopulationExpression()
	{
		return m_populationExpressionText.getText();
	}
		
	/**
	 * Find all disabled states
	 * @param topology
	 */
	private void updateDisabledList(Topology _topology)
	{
		m_disabledLocsModel.clear();
		if (_topology == null) {return;}

		for (LocationComponent l : _topology.getLocations())
		{
			if (!l.getActive())
			{
				m_disabledLocsModel.addElement(l);
			}
		}
	}

	/**
	 * Add all states to the combo box
	 * @param agentTypes
	 */
	private void updateAgentStates(Multimap<String, MASSPAComponent> _agentTypes)
	{
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement(new AllComponent());
		
		if (_agentTypes != null)
		{
			for (Entry<String,Collection<MASSPAComponent>> map : _agentTypes.asMap().entrySet())
			{
				for (MASSPAComponent comp : map.getValue())
				{
					model.addElement(comp);
				}
			}
		}
		
		m_stateCombo.setModel(model);
		changePopulationList();
	}
	
	/**
	 * Show population list for current filter
	 */
	private void changePopulationList()
	{
		changePopulationList((MASSPAComponent) m_stateCombo.getSelectedItem());
	}
	
	/**
	 * Show population list for filter {@code _c}
	 * @param _c
	 */
	private void changePopulationList(MASSPAComponent _c)
	{
		m_populationsModel.clear();
		if (_c == null || m_topology == null) {return;}
		
		// Now list all population definitions
		for (LocationComponent l : m_topology.getLocations())
		{
			if (l.getActive())
			{
				// Show all
				if (_c instanceof AllComponent)
				{
					for (MASSPAAgentPopComponent p : l.getPopulations())
					{
						m_populationsModel.addElement(p);
					}
				}
				// Show those of type _c only
				else
				{
					MASSPAAgentPopComponent p = l.getPopulation(_c);
					if (p != null)
					{
						m_populationsModel.addElement(p);
					}
				}
			}
		}
	}
	
	private class TopologyChangeListener implements ChangeListener
	{
		//******************************************
		// Implement ChangeListener interface
		//******************************************
		@Override
		public void stateChanged(ChangeEvent _e)
		{
			m_topology = ((ObservableTopology)_e.getSource()).getTopology();
			updateDisabledList(m_topology);
			changePopulationList();
		}
	}
	
	private class AgentsChangeListener implements ChangeListener
	{
		//******************************************
		// Implement ChangeListener interface
		//******************************************
		@Override
		public void stateChanged(ChangeEvent _e)
		{
			ObservableAgents agents = (ObservableAgents) _e.getSource();
			if (agents.getMASSPAAgents() != null)
			{
				updateAgentStates(agents.getMASSPAAgents().getAgents());
				changePopulationList();
			}
		}
	}

	private class StateComboChangeListener implements ItemListener
	{
		//******************************************
		// Implement ItemListener interface
		//******************************************
		@Override
		public void itemStateChanged(ItemEvent _e)
		{
			// m_stateCombo change 
			changePopulationList();
		}
	}

	private class PopulationListChangeListener implements ListSelectionListener
	{
		//**************************************************
		// Implement the ListSelectionListener interface
		//**************************************************
		@Override
		public void valueChanged(ListSelectionEvent _e)
		{
			MASSPAAgentPopComponent pop = (MASSPAAgentPopComponent)((JList) _e.getSource()).getSelectedValue();
			m_populationExpressionText.setText((pop != null && pop.getInitialPopulation() != null) ? pop.getInitialPopulation().toString() : "");
		}
	}
}
