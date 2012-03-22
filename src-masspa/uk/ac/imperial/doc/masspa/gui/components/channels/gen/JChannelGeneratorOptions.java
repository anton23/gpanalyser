package uk.ac.imperial.doc.masspa.gui.components.channels.gen;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.gui.models.channels.Channels;
import uk.ac.imperial.doc.masspa.gui.models.topologies.Topology;
import uk.ac.imperial.doc.masspa.gui.util.Constants;

/**
 * This class is the base class for
 * all channel generator options.
 * Each option class provides input
 * fields for a specific channel
 * generators.
 * 
 * @author Chris Guenther
 */
public abstract class JChannelGeneratorOptions extends JPanel
{
	private static final long serialVersionUID = 3076123118277446725L;

	// Components
	protected final BasicChannel 	m_options;
	protected final JScrollPane		m_scroll;
	
	public JChannelGeneratorOptions(ObservableAgents _agents)
	{
		super(new FlowLayout(FlowLayout.LEFT, Constants.s_CHANNEL_EDITOR_FLOW_LAYOUT_GAP_H, Constants.s_CHANNEL_EDITOR_FLOW_LAYOUT_GAP_V));
		m_options = new BasicChannel(_agents);
		m_options.setLayout(new FlowLayout(FlowLayout.LEFT, Constants.s_CHANNEL_EDITOR_FLOW_LAYOUT_GAP_H, Constants.s_CHANNEL_EDITOR_FLOW_LAYOUT_GAP_V));
		m_scroll = new JScrollPane(m_options, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		m_scroll.setPreferredSize(new Dimension(Constants.s_MAIN_RIGHT-18,Constants.s_CHANNEL_EDITOR_GENERATOR_BAR_OPTIONS_HEIGHT));
		add(m_scroll);	
	}
	
	/**
	 * Check if generator accepts a certain topology
	 * @param _topo to be used for channel generation
	 * @return true if channels can be generated for this topology
	 */
	public boolean acceptsTopology(Topology _topo)
	{
		return true;
	}
	
	/**
	 * Let action listeners register to process
	 * generate/button events
	 * @param _l the listener to be registered
	 */
	public void addActionListener(ActionListener _l)
	{	
	}
	
	/**
	 * Generate channels for given {@code _topology}
	 * @param _topology
	 */
	public Channels genChannels(Topology _topology)
	{
		return null;
	}
}
