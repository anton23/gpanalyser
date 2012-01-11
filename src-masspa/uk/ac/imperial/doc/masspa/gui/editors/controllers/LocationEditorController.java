package uk.ac.imperial.doc.masspa.gui.editors.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.gui.components.JLocationsSetup;
import uk.ac.imperial.doc.masspa.gui.components.topologies.JTopologyViewer;
import uk.ac.imperial.doc.masspa.gui.components.topologies.gen.JTopologyGenerator;
import uk.ac.imperial.doc.masspa.gui.interfaces.ITabController;
import uk.ac.imperial.doc.masspa.gui.models.ObservableTopology;
import uk.ac.imperial.doc.masspa.gui.models.topologies.LocationComponent;
import uk.ac.imperial.doc.masspa.gui.models.topologies.MASSPAAgentPopComponent;
import uk.ac.imperial.doc.masspa.gui.util.Actions;
import uk.ac.imperial.doc.masspa.gui.util.ExpressionValidator;
import uk.ac.imperial.doc.masspa.gui.util.Shortcuts;
import uk.ac.imperial.doc.masspa.language.Labels;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.representation.components.AllComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;

/**
 * This class controls the LocationEditor tab.
 * 
 * @author Chris Guenther
 */
public class LocationEditorController implements ITabController
{
	private ObservableTopology m_topology;
	private JTopologyGenerator m_topologyGenBar;
	private JTopologyViewer m_topologyViewer;
	private JLocationsSetup m_locationsSetup;
		
	public LocationEditorController(ObservableTopology _topology, JTopologyGenerator _topologyGenBar,
									JTopologyViewer _topologyViewer, JLocationsSetup _locationsSetup)
	{
		m_topology = _topology;
		m_topologyGenBar = _topologyGenBar;
		m_topologyViewer = _topologyViewer;
		m_locationsSetup = _locationsSetup;
		
		// Register as Listener
		m_topologyGenBar.addActionListener(new LocationGeneratorActionListener());
		m_topologyViewer.addKeyListener(new TopologyViewerKeyListener());
		m_locationsSetup.addActionListener(new LocationSetupActionListener());
		m_locationsSetup.addKeyListener(new LocationSetupKeyListener());
		m_locationsSetup.addListSelectionListener(new LocationSetupListSelectionListener());
	}

	/**
	 * Disable currently selected locations
	 */
	private void disableSelectedLocations()
	{
		Collection<LocationComponent> locs = m_topologyViewer.getSelectedLocations();
		for (LocationComponent l : locs)
		{
			l.setActive(false);
		}
		// Notify listeners of the change
		if (locs.size() > 0)
		{
			m_topology.setTopology(m_topology.getTopology());
		}
	}
	
	/**
	 * Enable currently selected locations
	 */
	private void enableSelectedLocations()
	{
		Collection<LocationComponent> locs = m_topologyViewer.getSelectedLocations();
		for (LocationComponent l : locs)
		{
			l.setActive(true);
		}
		// Notify listeners of the change
		if (locs.size() > 0)
		{
			m_topology.setTopology(m_topology.getTopology());
		}
	}

	/**
	 * Enable locations
	 * @param _locs list of locations to be reenabled
	 */
	private void enableSelectedLocations(Object[] _locs)
	{
		for (Object l : _locs)
		{
			((LocationComponent)l).setActive(true);
		}
		// Notify listeners of the change
		if (_locs.length > 0)
		{
			m_topology.setTopology(m_topology.getTopology());
		}
	}
	
	/**
	 * Add new population expressions for selected locations
	 */
	private void addPopulationsForSelectedLocations()
	{	
		MASSPALogging.clearConsoles();

		// Try to parse the expression
		MASSPAComponent c = m_locationsSetup.getCurrentPopulationState();
		if (c == null)
		{
			MASSPALogging.error(Messages.s_LOCATION_EDITOR_NULL_STATE_POPULATION);
			return;
		}
		if (c instanceof AllComponent)
		{
			MASSPALogging.error(Messages.s_LOCATION_EDITOR_ALL_STATE_POPULATION);
			return;
		}
		
		String expr = m_locationsSetup.getPopulationExpression();
		AbstractExpression e = ExpressionValidator.validate(expr);
		if (e == null)
		{
			MASSPALogging.error(String.format(Messages.s_LOCATION_EDITOR_INVALID_POPULATION_EXPRESSION, expr));
			return;
		}		

		// Add populations to selected locations
		Collection<LocationComponent> locs = m_topologyViewer.getSelectedLocations();
		for (LocationComponent l : locs)
		{
			l.addAgentPopulation(c, e);
		}

		// Notify listeners of the change
		if (locs.size() > 0)
		{
			m_topology.setTopology(m_topology.getTopology());
		}
	}
		
	/**
	 * Remove population expressions
	 * @param _pops list of population expressions to be removed
	 */
	private void removeSelectedPopulations(Object[] _pops)
	{
		for (Object p : _pops)
		{
			LocationComponent l = ((MASSPAAgentPopComponent)p).getLocation();
			l.removeAgentPopulation((MASSPAAgentPopComponent)p);
		}
		// Notify listeners of the change
		if (_pops.length > 0)
		{
			m_topology.setTopology(m_topology.getTopology());
		}
	}

	private void highlightSelectedLocations(Object[] _locs)
	{
		Set<LocationComponent> locs = new HashSet<LocationComponent>();
		for (Object l : _locs)
		{
			locs.add((LocationComponent)l);	
		}
		
		// Deselect all locations
		m_topologyViewer.highlightLocations(locs);
	}
	
	private class LocationGeneratorActionListener implements ActionListener
	{
		//**************************************************
		// Implement ActionListener interface
		//**************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			if (_e.getActionCommand() == Actions.s_GEN_TOPOLOGY)
			{
				m_topology.setTopology(m_topologyGenBar.getTopologyGenerator().genTopology());
			}
		}
	}
	
	private class LocationSetupActionListener implements ActionListener
	{
		//**************************************************
		// Implement ActionListener interface
		//**************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			if (_e.getActionCommand() == Actions.s_ADD_POPULATION)
			{
				addPopulationsForSelectedLocations();
			}
		}
	}
	
	
	private class TopologyViewerKeyListener extends KeyAdapter
	{
		//*****************************************************
		// Override dummy KeyListener interface implementation
		//*****************************************************
		@Override
		public void keyPressed(KeyEvent _e) 
		{
			if (_e.getKeyCode() == Shortcuts.s_LOCATION_DISABLE)
			{
				disableSelectedLocations();
				MASSPALogging.ok(Messages.s_LOCATION_EDITOR_DISABLED_LOCATIONS);
			}
			else if (_e.getKeyCode() == Shortcuts.s_LOCATION_ENABLE)
			{
				enableSelectedLocations();
				MASSPALogging.ok(Messages.s_LOCATION_EDITOR_DISABLED_LOCATIONS);
			}
		}
	}

	private class LocationSetupKeyListener extends KeyAdapter
	{
		//*****************************************************
		// Override dummy KeyListener interface implementation
		//*****************************************************
		@Override
		public void keyPressed(KeyEvent _e) 
		{
			JList list = (JList) _e.getSource();
			if (list.getName() == Labels.s_LOCATION_EDITOR_DISABLED_LOCS)
			{
				if (_e.getKeyCode() == Shortcuts.s_DELETE)
				{
					Object[] locs = list.getSelectedValues();
					enableSelectedLocations(locs);
				}
			}
			else if (list.getName() == Labels.s_LOCATION_EDITOR_POPULATIONS)
			{
				if (_e.getKeyCode() == Shortcuts.s_DELETE)
				{
					Object[] pops = list.getSelectedValues();
					removeSelectedPopulations(pops);
				}
			}
		}
	}
	
	private class LocationSetupListSelectionListener implements ListSelectionListener
	{
		//******************************************
		// Implement ListSelectionListener interface
		//******************************************
		@Override
		public void valueChanged(ListSelectionEvent _e)
		{
			JList list = (JList) _e.getSource();;
			if (list.getName() == Labels.s_LOCATION_EDITOR_DISABLED_LOCS)
			{
				Object[] locs = list.getSelectedValues();
				highlightSelectedLocations(locs);
			}
			else if (list.getName() == Labels.s_LOCATION_EDITOR_POPULATIONS)
			{
				Object[] pops = list.getSelectedValues();
				Set<LocationComponent> locs = new HashSet<LocationComponent>();
				for (Object p : pops)
				{
					locs.add(((MASSPAAgentPopComponent)p).getLocation());
				}
				m_topologyViewer.setSelectedLocations(locs);
			}
		}
	}
}
