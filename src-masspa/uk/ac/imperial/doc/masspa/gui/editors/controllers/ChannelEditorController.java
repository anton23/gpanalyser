package uk.ac.imperial.doc.masspa.gui.editors.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.gui.components.JChannelSetup;
import uk.ac.imperial.doc.masspa.gui.components.channels.gen.ISinkSetter;
import uk.ac.imperial.doc.masspa.gui.components.channels.gen.JChannelGenerator;
import uk.ac.imperial.doc.masspa.gui.components.topologies.JTopologyViewer;
import uk.ac.imperial.doc.masspa.gui.components.topologies.canvas.JLocationComponentVisualiser;
import uk.ac.imperial.doc.masspa.gui.components.topologies.canvas.JTopologyComponentVisualiser;
import uk.ac.imperial.doc.masspa.gui.interfaces.ITabController;
import uk.ac.imperial.doc.masspa.gui.models.ObservableTopology;
import uk.ac.imperial.doc.masspa.gui.models.channels.ChannelComponent;
import uk.ac.imperial.doc.masspa.gui.models.channels.ChannelGenerator;
import uk.ac.imperial.doc.masspa.gui.models.channels.Channels;
import uk.ac.imperial.doc.masspa.gui.models.channels.MASSPAChannelComponent;
import uk.ac.imperial.doc.masspa.gui.models.topologies.LocationComponent;
import uk.ac.imperial.doc.masspa.gui.util.Actions;
import uk.ac.imperial.doc.masspa.gui.util.Shortcuts;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;

/**
 * This class controls the ChannelEditor tab.
 * 
 * @author Chris Guenther
 */
public class ChannelEditorController implements ITabController
{
	private ObservableTopology m_topology;
	private JChannelGenerator m_channelGenBar;
	private JTopologyViewer m_topologyViewer;
	private JChannelSetup m_channelSetup;
		
	public ChannelEditorController(ObservableTopology _topology, JChannelGenerator _channelGenBar,
									JTopologyViewer _topologyViewer, JChannelSetup _channelSetup)
	{
		m_topology = _topology;
		m_channelGenBar = _channelGenBar;
		m_topologyViewer = _topologyViewer;
		m_channelSetup = _channelSetup;
		
		// Register as Listener
		m_channelGenBar.addActionListener(new ChannelGeneratorActionListener());
		m_channelSetup.addActionListener(new ChannelSetupActionListener());
		m_channelSetup.addKeyListener(new ChannelSetupKeyListener());
		m_channelSetup.addListSelectionListener(new ChannelSetupListSelectionListener());
		m_topologyViewer.addKeyListener(new TopologyCanvasKeyListener());
		m_topologyViewer.addListSelectionListener(new TopologyCanvasSelectionListener());
	}
	
	/**
	 * Remove all {@code _channels}
	 * @param _channels the channels that are to be removed
	 */
	private void removeSelectedChannels(Collection<MASSPAChannelComponent> _channels)
	{
		if (m_topology.getTopology() == null) {return;}
		Channels chans = m_topology.getTopology().getChannels();
		if (chans == null) {return;}
		
		for (MASSPAChannelComponent chan : _channels)
		{
			ChannelComponent c = chans.getChannel(chan);
			c.removeDataChannel(chan);
		}
		
		// Notify listeners of the change
		if (_channels.size() > 0)
		{
			m_topology.setTopology(m_topology.getTopology());
		}
	}
	
	/**
	 * Remove all channel containers and the MASSPAChannelComponent they contain
	 */
	private void removeSelectedChannels()
	{
		Channels chans = m_topology.getTopology().getChannels();
		if (chans == null) {return;}
		Collection<ChannelComponent> channels = m_topologyViewer.getSelectedChannels();
		for (ChannelComponent chan : channels)
		{
			chans.removeChannel(chan);
		}
		
		// Notify listeners of the change
		if (channels.size() > 0)
		{
			m_topology.setTopology(m_topology.getTopology());
		}
	}

	private class ChannelGeneratorActionListener implements ActionListener
	{
		//**************************************************
		// Implement ActionListener interface
		//**************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			if (_e.getActionCommand() == Actions.s_GEN_CHANNELS)
			{
				m_topology.setChannels(m_channelGenBar.getChannelGenerator().genChannels(m_topology.getTopology()));
			}
			if (_e.getActionCommand() == Actions.s_SET_SINK)
			{
				ISinkSetter s = (ISinkSetter) m_channelGenBar.getChannelGenerator(); 
				s.setSink(m_topologyViewer.getSelectedLocations());
			}
		}
	}
		
	private class ChannelSetupActionListener implements ActionListener
	{
		//**************************************************
		// Implement ActionListener interface
		//**************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			if (_e.getActionCommand() == Actions.s_CHANGE_CHANNELS)
			{
				AbstractExpression expr = m_channelSetup.getEditIntensityExpression();
				if (expr == null)
				{
					MASSPALogging.error(String.format(Messages.s_CHANNEL_EDITOR_INVALID_POPULATION_EXPRESSION, expr));
					return;
				}
				Set<MASSPAChannelComponent> chans = m_channelSetup.getSelectedChannels();
				for (MASSPAChannelComponent chan : chans)
				{
					chan.setIntensity(expr);
				}
				
				// Notify listeners of the change
				if (chans.size() > 0)
				{
					m_topology.setTopology(m_topology.getTopology());
				}
			}
			if (_e.getActionCommand() == Actions.s_DEL_CHANNELS)
			{
				removeSelectedChannels();
			}
		}
	}
	
	private class ChannelSetupKeyListener extends KeyAdapter
	{
		//*****************************************************
		// Override dummy KeyListener interface implementation
		//*****************************************************
		@Override
		public void keyPressed(KeyEvent _e) 
		{
			JList list = (JList) _e.getSource();
			if (_e.getKeyCode() == Shortcuts.s_DELETE)
			{
				Object[] channels = list.getSelectedValues();
				Set<MASSPAChannelComponent> chans = new HashSet<MASSPAChannelComponent>();
				for (Object c : channels)
				{
					chans.add((MASSPAChannelComponent)c);
				}
				removeSelectedChannels(chans);
			}
		}
	}

	private class ChannelSetupListSelectionListener implements ListSelectionListener
	{
		//******************************************
		// Implement ListSelectionListener interface
		//******************************************
		@Override
		public void valueChanged(ListSelectionEvent _e)
		{
			JList list = (JList) _e.getSource();
			Object[] channels = list.getSelectedValues();
			Set<MASSPAChannelComponent> chans = new HashSet<MASSPAChannelComponent>();
			for (Object c : channels)
			{
				chans.add((MASSPAChannelComponent)c);
			}
			m_topologyViewer.setSelectedChannels(chans);
		}
	}
	
	private class TopologyCanvasKeyListener extends KeyAdapter
	{
		//*****************************************************
		// Override dummy KeyListener interface implementation
		//*****************************************************
		@Override
		public void keyPressed(KeyEvent _e) 
		{
			if (_e.getKeyCode() == Shortcuts.s_DELETE)
			{
				removeSelectedChannels();
			}
		}
	}
	
	private class TopologyCanvasSelectionListener implements ListSelectionListener
	{
		//******************************************
		// Implement ListSelectionListener interface
		//******************************************
		@Override
		@SuppressWarnings("unchecked")
		public void valueChanged(ListSelectionEvent _e)
		{
			if (m_channelSetup.isChannelAddActive())
			{
				// Check that a pair of locations was selected
				List<JTopologyComponentVisualiser<?>> list = (List<JTopologyComponentVisualiser<?>>) _e.getSource();
				if (list.size() != 2 || !(list.get(0) instanceof JLocationComponentVisualiser)) {return;}
				
				// Parse channel intensity expression
				AbstractExpression e = m_channelSetup.getAddIntensityExpression();
				if (e == null)
				{
					MASSPALogging.error(String.format(Messages.s_CHANNEL_EDITOR_INVALID_INTENSITY_EXPRESSION, m_channelSetup.getAddIntensityExpressionText()));
					return;
				}

				// Create channel
				Channels chans = m_topology.getTopology().getChannels();
				LocationComponent sender = ((JLocationComponentVisualiser)list.get(0)).getLocationComponent();
				LocationComponent receiver = ((JLocationComponentVisualiser)list.get(1)).getLocationComponent();
				ChannelComponent cc = chans.getChannel(sender, receiver);
				ChannelGenerator.addMASSPAChannels(cc,m_channelSetup.getAddSenderStates(),
												   m_channelSetup.getAddReceiverStates(),
												   m_channelSetup.getAddMessageTypes(),
												   e,
												   m_channelSetup.hasAddProportionalRate());
				
				// Clear selection and notify topology/channel listeners
				m_topology.setTopology(m_topology.getTopology());
			}
		}
	}
}
