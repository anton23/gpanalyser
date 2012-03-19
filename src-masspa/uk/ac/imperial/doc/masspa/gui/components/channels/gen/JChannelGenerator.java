package uk.ac.imperial.doc.masspa.gui.components.channels.gen;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.gui.models.ObservableTopology;
import uk.ac.imperial.doc.masspa.gui.util.Actions;
import uk.ac.imperial.doc.masspa.gui.util.Constants;
import uk.ac.imperial.doc.masspa.language.Labels;

/**
 * The Channel generator utility panel.
 * 
 * @author Chris Guenther
 */
public class JChannelGenerator extends JPanel
{
	private static final long serialVersionUID = -2492862151102665192L;

	// Components
	private final JButton						m_genBtn;
	private final JLabel						m_channelGeneratorLabel;
	private final JComboBox						m_channelGeneratorCombo;
	private JChannelGeneratorOptions			m_channelGen;

	// Model
	private ObservableAgents						m_agents;
	private ObservableTopology						m_topology;
	
	public JChannelGenerator(ObservableAgents _agents, ObservableTopology _topology)
	{
		super(new FlowLayout(FlowLayout.LEFT, Constants.s_CHANNEL_EDITOR_FLOW_LAYOUT_GAP_H, Constants.s_CHANNEL_EDITOR_FLOW_LAYOUT_GAP_V));
		
		// Setup models
		m_agents = _agents;
		m_topology = _topology;
		
		// Setup components
		m_genBtn = new JButton(Labels.s_CHANNEL_EDITOR_GEN_CHANNELS);
		m_genBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		m_genBtn.setActionCommand(Actions.s_GEN_CHANNELS);
		m_channelGeneratorLabel = new JLabel(Labels.s_CHANNEL_EDITOR_CHOOSE_CHANNEL_GENERATOR);
		m_channelGeneratorCombo = new JComboBox();
		fillGeneratorCombo();
				
		// Create Layout
		genLayout();
		
		// Register as listener
		m_channelGeneratorCombo.addItemListener(new ChannelGeneratorChoiceListener());
		m_topology.addChangeListener(new TopologyChangeListener());
	}

	/**
	 * Population channel generator combo box with generators suitable
	 * for the current topology.
	 */
	private void fillGeneratorCombo()
	{
		String[] availableGenerators = JChannelGeneratorOptionsFactory.getChannelGenerators(m_topology.getTopology());
		DefaultComboBoxModel m = new DefaultComboBoxModel(availableGenerators);
		if (availableGenerators.length == 0) {m.addElement("EmptyChannel");}
		m_channelGeneratorCombo.setModel(m);
		setChannelGenOptions((String)m.getSelectedItem());
	}

	/**
	 * Create a new channel generator options bar for {@code _gen}
	 * @param _gen name of the generator
	 */
	private void setChannelGenOptions(String _gen)
	{
		m_channelGen = JChannelGeneratorOptionsFactory.getChannelGen(_gen, m_agents);
		// The bar options might need to communicate with the controller
		for (ActionListener l : m_genBtn.getListeners(ActionListener.class)){m_channelGen.addActionListener(l);}
	}
	
	/**
	 * Layout the components in the panel
	 */
	private void genLayout()
	{
		// Create Layout
		add(m_genBtn);
		add(m_channelGeneratorLabel);
		add(m_channelGeneratorCombo);
		add(m_channelGen);
	}
	
	/**
	 * @param _l listener to be added
	 */
	public void addActionListener(ActionListener _l)
	{
		m_genBtn.addActionListener(_l);
		m_channelGen.addActionListener(_l);
	}
	
	/**
	 * @return options for the channel generator
	 */
	public JChannelGeneratorOptions getChannelGenerator()
	{
		return m_channelGen;
	}
	
	/**
	 * Redraw channel generator bar
	 */
	private void redraw()
	{
		// Avoid deadlocks
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{	
					// Change channel generator and repaint
					removeAll();
					genLayout();
					revalidate();
					repaint();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	private class ChannelGeneratorChoiceListener implements ItemListener
	{
		//**************************************************
		// Implement the ItemListener interface
		//**************************************************
		/**
		 * Handle changes to the channel generator choice comboBox.
		 */
		@Override
		public void itemStateChanged(ItemEvent _e)
		{
			// m_channelGeneratorCombo change 
			if (_e.getStateChange() == ItemEvent.SELECTED)
			{	
				setChannelGenOptions((String)_e.getItem());
				redraw();
			}	
		}
	}

	private class TopologyChangeListener implements ChangeListener
	{
		//**************************************************
		// Implement the ChangeListener interface
		//**************************************************
		/**
		 * Handle changes to the topology.
		 */
		@Override
		public void stateChanged(ChangeEvent _e)
		{
			// Topology has changed
			fillGeneratorCombo();
			redraw();
		}
	}
}
