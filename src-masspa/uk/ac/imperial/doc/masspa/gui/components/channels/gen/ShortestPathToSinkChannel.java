package uk.ac.imperial.doc.masspa.gui.components.channels.gen;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.gui.models.channels.ChannelGenerator;
import uk.ac.imperial.doc.masspa.gui.models.channels.Channels;
import uk.ac.imperial.doc.masspa.gui.models.topologies.LocationComponent;
import uk.ac.imperial.doc.masspa.gui.models.topologies.Topology;
import uk.ac.imperial.doc.masspa.gui.util.Actions;
import uk.ac.imperial.doc.masspa.gui.util.Constants;
import uk.ac.imperial.doc.masspa.gui.util.MathExtra;
import uk.ac.imperial.doc.masspa.language.Labels;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;

public class ShortestPathToSinkChannel extends JChannelGeneratorOptions implements ISinkSetter
{
	private static final long serialVersionUID = -8890167443677910706L;

	// Components
	private final JLabel m_sinkLabel;
	private final JButton m_setSinkBtn;
	private final JComboBox m_sinksCombo;
	private final JLabel m_maxHopLenLabel;
	private final JTextField m_maxHopLen;
	
	public ShortestPathToSinkChannel(ObservableAgents _agents)
	{
		super(_agents);
		m_options.setPreferredSize(new Dimension(1500,60));
		
		m_sinkLabel = new JLabel(Labels.s_CHANNEL_EDITOR_SINKS_LABEL);
		m_setSinkBtn = new JButton(Labels.s_CHANNEL_EDITOR_SET_SINK);
		m_setSinkBtn.setToolTipText(Labels.s_CHANNEL_EDITOR_SET_SINK_TIP);
		m_setSinkBtn.setActionCommand(Actions.s_SET_SINK);
		m_sinksCombo = new JComboBox();
		m_sinksCombo.setToolTipText(Labels.s_CHANNEL_EDITOR_SINKS_TIP);
		m_maxHopLenLabel = new JLabel(Labels.s_CHANNEL_EDITOR_MAX_HOP_LEN);
		m_maxHopLen = new JTextField(Double.toString(MathExtra.round(Math.sqrt(Constants.s_CHANNEL_EDITOR_MAX_HOP_LEN_DEFAULT),5)+0.00001));
		m_maxHopLen.setToolTipText(Labels.s_CHANNEL_EDITOR_MAX_HOP_LEN_TIP);
		m_maxHopLen.setColumns(Constants.s_CHANNEL_EDITOR_MAX_HOP_LEN_COLS);

		m_options.add(m_sinkLabel);
		m_options.add(m_setSinkBtn);
		m_options.add(m_sinksCombo);
		m_options.add(m_maxHopLenLabel);
		m_options.add(m_maxHopLen);
	}
	
	/**
	 * Generate channels for given {@code _topology}
	 * @param _topology
	 */
	@Override
	public Channels genChannels(Topology _topology)
	{
		MASSPALogging.clearConsoles();
		if (_topology == null)
		{
			MASSPALogging.error(Messages.s_CHANNEL_EDITOR_NO_TOPOLOGY);
			return null;
		}
		LocationComponent sink = (LocationComponent) m_sinksCombo.getSelectedItem();
		if (sink == null)
		{
			MASSPALogging.error(Messages.s_CHANNEL_EDITOR_NO_SINK);
			return null;
		}
		double maxHopLength = Double.parseDouble(m_maxHopLen.getText());
		boolean propInt = m_options.hasProportionalRate();
		AbstractExpression intensity = m_options.getIntensityExpr();
		if (intensity == null)
		{
			MASSPALogging.error(String.format(Messages.s_CHANNEL_EDITOR_INVALID_INTENSITY_EXPRESSION, m_options.getIntensityExprText()));
			return null;
		}

		return ChannelGenerator.genShortestPathToSinkChannels(_topology,m_options.getSenderStates(),m_options.getReceiverStates(),m_options.getMessageTypes(),sink,maxHopLength,intensity,propInt);
	}

	@Override
	public void addActionListener(ActionListener _l)
	{
		super.addActionListener(_l);
		m_setSinkBtn.addActionListener(_l);
	}

	//******************************************
	// Implement ISinkSetter interface
	//******************************************
	public void setSink(Collection<LocationComponent> _selectedLocations)
	{
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		for (LocationComponent l : _selectedLocations)
		{
			model.addElement(l);
		}
		m_sinksCombo.setModel(model);
	}
}
