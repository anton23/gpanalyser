package uk.ac.imperial.doc.masspa.gui.components;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.gui.components.channels.gen.BasicChannel;
import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.gui.models.ObservableTopology;
import uk.ac.imperial.doc.masspa.gui.models.channels.ChannelComponent;
import uk.ac.imperial.doc.masspa.gui.models.channels.Channels;
import uk.ac.imperial.doc.masspa.gui.models.channels.MASSPAChannelComponent;
import uk.ac.imperial.doc.masspa.gui.util.Actions;
import uk.ac.imperial.doc.masspa.gui.util.list.SortedListModel;
import uk.ac.imperial.doc.masspa.language.Labels;
import uk.ac.imperial.doc.masspa.representation.components.AllComponent;
import uk.ac.imperial.doc.masspa.representation.components.AllMessage;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAMessage;

public class JChannelSetup extends JPanel
{
	private static final long serialVersionUID = -4480082337420274205L;

	// Components
	private final JTabbedPane	m_tabs;
	
	// Add Channel options
	private final BasicChannel	m_addChannels;
	private final JToggleButton	m_enableChannelAdd;
	private final JButton		m_deleteChannels;
	
	// Edit Channel options
	private final BasicChannel	m_editChannels;
	private final JLabel		m_channelLabel;
	private final JList 		m_channelList;	
	private final JScrollPane	m_channelListScroll;
	private final JButton		m_channelChange;
	
	// Models
	private DefaultListModel 	m_channelListModel;
	private Channels 			m_channels;
	
	public JChannelSetup(ObservableAgents _agents, ObservableTopology _topology)
	{
		// Overall layout
		super(new BorderLayout());
		
		// Setup models
		m_channelListModel = new DefaultListModel();
		m_channels = null;
		
		// Add channel options
		m_addChannels = new BasicChannel(_agents);
		m_addChannels.setLayout(new BoxLayout(m_addChannels,BoxLayout.Y_AXIS));
		m_enableChannelAdd = new JToggleButton(Labels.s_CHANNEL_EDITOR_TOGGLE_ADD_CHANNELS);
		m_enableChannelAdd.setToolTipText(Labels.s_CHANNEL_EDITOR_TOGGLE_ADD_CHANNELS_TIP);
		m_enableChannelAdd.setAlignmentX(LEFT_ALIGNMENT);
		m_enableChannelAdd.setSelected(true);
		m_deleteChannels = new JButton(Labels.s_CHANNEL_EDITOR_DELETE_CHANNELS);
		m_deleteChannels.setToolTipText(Labels.s_CHANNEL_EDITOR_DELETE_CHANNELS_TIP);
		m_deleteChannels.setActionCommand(Actions.s_DEL_CHANNELS);
		m_addChannels.add(m_enableChannelAdd);
		m_addChannels.add(m_deleteChannels);
		
		// Edit channel options
		m_editChannels = new BasicChannel(_agents);
		m_editChannels.setLayout(new BoxLayout(m_editChannels,BoxLayout.Y_AXIS));
		m_editChannels.setVisibleProportionalIntensity(false);
		m_channelLabel = new JLabel(Labels.s_CHANNEL_EDITOR_CHANNELS);
		m_channelLabel.setAlignmentX(LEFT_ALIGNMENT);
		m_channelList = new JList(m_channelListModel);
		m_channelList.setName(Labels.s_CHANNEL_EDITOR_CHANNELS);
		m_channelList.setToolTipText(Labels.s_CHANNEL_EDITOR_EDIT_LOCS_TIP);
		m_channelList.setVisibleRowCount(40);
		m_channelListScroll = new JScrollPane(m_channelList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		m_channelListScroll.setAlignmentX(LEFT_ALIGNMENT);
		m_channelChange = new JButton(Labels.s_CHANNEL_EDITOR_CHANGE_CHANNEL);
		m_channelChange.setToolTipText(Labels.s_CHANNEL_EDITOR_CHANGE_CHANNEL_TIP);
		m_channelChange.setActionCommand(Actions.s_CHANGE_CHANNELS);
		m_editChannels.add(new JSeparator());
		m_editChannels.add(m_channelLabel);
		m_editChannels.add(m_channelListScroll);
		m_editChannels.add(m_channelChange);
		
		// Create tabs
		m_tabs = new JTabbedPane(JTabbedPane.TOP);
		m_tabs.setBorder(new TitledBorder(null, Labels.s_CHANNEL_EDITOR_OPTIONS, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		m_tabs.add(Labels.s_CHANNEL_EDITOR_ADD_CHANNELS, m_addChannels);
		m_tabs.add(Labels.s_CHANNEL_EDITOR_EDIT_CHANNELS, m_editChannels);
		
		// Compositions
		add(m_tabs);
		
		// Create initial channel list
		updateChannelList();
		
		// Register as Listener
		_topology.addChangeListener(new TopologyChangeListener());
		m_editChannels.setItemListener(new EditChannelsListener());
		addListSelectionListener(new ChannelListSelectionListener());
	}
	
	/**
	 * Listener for list action events
	 * @param _l listener to be added
	 */
	public void addActionListener(ActionListener _l)
	{
		m_channelChange.addActionListener(_l);
		m_deleteChannels.addActionListener(_l);
	}
	
	/**
	 * Listener for list key events
	 * @param _l listener to be added
	 */
	public void addKeyListener(KeyListener _l)
	{
		m_channelList.addKeyListener(_l);
	}
	
	/**
	 * Listener for list item selection events
	 * @param _l listener to be added
	 */
	public void addListSelectionListener(ListSelectionListener _l)
	{
		m_channelList.addListSelectionListener(_l);
	}
	
	/**
	 * @return true iff {@code m_addChannels} is the active tab
	 * 		   and {@code m_enableChannelAdd} is toggled
	 */
	public boolean isChannelAddActive()
	{
		return m_tabs.getSelectedComponent().equals(m_addChannels) && m_enableChannelAdd.isSelected();
	}

	/**
	 * @return add tab selected sender states
	 */
	public Set<MASSPAComponent> getAddSenderStates()
	{
		return m_addChannels.getSenderStates();
	}
	
	/**
	 * @return add tab selected receiver states
	 */
	public Set<MASSPAComponent> getAddReceiverStates()
	{
		return m_addChannels.getReceiverStates();
	}
	
	/**
	 * @return add tab selected message types
	 */
	public Set<MASSPAMessage> getAddMessageTypes()
	{
		return m_addChannels.getMessageTypes();
	}
	
	/**
	 * @return add tab intensity expression as abstractExpression
	 */
	public AbstractExpression getAddIntensityExpression()
	{
		return m_addChannels.getIntensityExpr();
	}
	
	/**
	 * @return add tab intensity expression as string
	 */
	public String getAddIntensityExpressionText()
	{
		return m_addChannels.getIntensityExprText();
	}
	
	/**
	 * @return true iff newly added channels should have receiver
	 * 		   population proportional rates.
	 */
	public boolean hasAddProportionalRate()
	{
		return m_addChannels.hasProportionalRate();
	}
	
	/**
	 * Get channels that have been selected for editing
	 */
	public Set<MASSPAChannelComponent> getSelectedChannels()
	{
		Set<MASSPAChannelComponent> chans = new HashSet<MASSPAChannelComponent>();
		for (Object c : m_channelList.getSelectedValues())
		{
			chans.add((MASSPAChannelComponent)c);
		}				
		return chans;
	}
	
	/**
	 * @return edit tab intensity expression as string
	 */
	public String getEditIntensityExpressionText()
	{
		return m_editChannels.getIntensityExprText();
	}
	
	/**
	 * @return edit tab intensity expression as abstractExpression
	 */
	public AbstractExpression getEditIntensityExpression()
	{
		return m_editChannels.getIntensityExpr();
	}
	
	/**
	 * Enables/Disables gui components for intensity
	 * in edit/view mode
	 */
	private void toggleIntensitySetting()
	{
		m_editChannels.setEnableIntensityComponent((m_channelList.getSelectedValues().length > 0));
	}
	
	/**
	 * Show all channels for set filter
	 */
	private void updateChannelList()
	{
		m_channelListModel = new SortedListModel();
		if (m_channels != null)
		{
			MASSPAComponent sender = m_editChannels.getSenderStates().iterator().next();
			MASSPAComponent receiver = m_editChannels.getReceiverStates().iterator().next();
			MASSPAMessage msg = m_editChannels.getMessageTypes().iterator().next();
			if (sender == null || receiver == null || msg == null) {return;}
			
			for (ChannelComponent chanComp : m_channels.getChannels())
			{
				if (!chanComp.isActive()) {continue;}
				for (MASSPAChannelComponent chan : chanComp.getDataChannels())
				{
					if (!(sender instanceof AllComponent) &&
						!chan.getSender().getComponent().equals(sender))
					{
						continue;
					}
					if (!(receiver instanceof AllComponent) &&
						!chan.getReceiver().getComponent().equals(receiver))
					{
						continue;
					}
					if (!(msg instanceof AllMessage) &&
						!chan.getMsg().equals(msg))
					{
						continue;
					}
					m_channelListModel.addElement(chan);
				}
			}
		}
		m_channelList.setModel(m_channelListModel);
		toggleIntensitySetting();
	}
	
	private class TopologyChangeListener implements ChangeListener
	{
		//**************************************************
		// Implement the ChangeListener interface
		//**************************************************
		@Override
		public void stateChanged(ChangeEvent _e)
		{
			if (((ObservableTopology)_e.getSource()).getTopology() == null)
			{
				m_channels = null;
			}
			else
			{			
				m_channels = ((ObservableTopology)_e.getSource()).getTopology().getChannels();
			}
			updateChannelList();
		}
	}

	private class EditChannelsListener implements ItemListener
	{
		//**************************************************
		// Implement the ItemListener interface
		//**************************************************
		@Override
		public void itemStateChanged(ItemEvent _e)
		{
			// Combo box filter change
			updateChannelList();
		}
	}
	
	private class ChannelListSelectionListener implements ListSelectionListener
	{
		//**************************************************
		// Implement the ListSelectionListener interface
		//**************************************************
		@Override
		public void valueChanged(ListSelectionEvent _e)
		{
			JList list = (JList) _e.getSource();
			MASSPAChannelComponent c = (MASSPAChannelComponent)list.getSelectedValue();
			m_editChannels.setIntensity((c != null) ? c.getIntensity().toString() : "");
			toggleIntensitySetting();
		}
	}
}
