package uk.ac.imperial.doc.masspa.gui.components.topologies.gen;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import uk.ac.imperial.doc.masspa.gui.util.Actions;
import uk.ac.imperial.doc.masspa.gui.util.Constants;
import uk.ac.imperial.doc.masspa.language.Labels;

/**
 * The Topology generator utility panel.
 * 
 * @author Chris Guenther
 */
public class JTopologyGenerator extends JPanel
{
	private static final long serialVersionUID = 4632900105184568970L;

	// Components
	protected final JButton					m_genBtn;
	protected final JLabel					m_topologyGeneratorLabel;
	protected final JComboBox				m_topologyGeneratorCombo;
	protected JTopologyGeneratorOptions	m_topoGen;
		
	public JTopologyGenerator()
	{
		super(new FlowLayout(FlowLayout.LEFT, Constants.s_LOCATION_EDITOR_FLOW_LAYOUT_GAP_H, Constants.s_LOCATION_EDITOR_FLOW_LAYOUT_GAP_V));
		
		// Setup components
		m_genBtn = new JButton(Labels.s_LOCATION_EDITOR_GEN_TOPOLOGY);
		m_genBtn.setActionCommand(Actions.s_GEN_TOPOLOGY);
		m_genBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		m_topologyGeneratorLabel = new JLabel(Labels.s_LOCATION_EDITOR_CHOOSE_TOPOLOGY_GENERATOR);
		String[] availableGenerators = JTopologyGeneratorOptionsFactory.getTopologyGenerators();
		m_topologyGeneratorCombo = new JComboBox(availableGenerators);
		m_topoGen = JTopologyGeneratorOptionsFactory.getTopologyGen(availableGenerators[0]);
		
		// Create Layout
		genLayout();
		
		// Create listeners
		m_topologyGeneratorCombo.addItemListener(new TopologyGenChoiceListener());
	}

	protected void genLayout()
	{
		// Create Layout
		add(m_genBtn);
		add(m_topologyGeneratorLabel);
		add(m_topologyGeneratorCombo);
		add(m_topoGen);
	}
	
	/**
	 * @param _l listener to be added
	 */
	public void addActionListener(ActionListener _l)
	{
		m_genBtn.addActionListener(_l);
	}
	
	/**
	 * @return options for the topology generator
	 */
	public JTopologyGeneratorOptions getTopologyGenerator()
	{
		return m_topoGen;
	}
	
	/**
	 * This class handles changes to the topology generator
	 * choice comboBox.
	 */
	class TopologyGenChoiceListener implements ItemListener
	{
		//**************************************************
		// Implement the ItemListener interface
		//**************************************************
		@Override
		public void itemStateChanged(ItemEvent _e)
		{
			m_topoGen = JTopologyGeneratorOptionsFactory.getTopologyGen((String)_e.getItem());
			// m_topologyGeneratorCombo change 
			if (_e.getStateChange() == ItemEvent.SELECTED)
			{	
				// Avoid deadlocks
				EventQueue.invokeLater(new Runnable()
				{
					public void run()
					{
						try
						{	
							// Change topology generator and repaint
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
		}	
	}
}
