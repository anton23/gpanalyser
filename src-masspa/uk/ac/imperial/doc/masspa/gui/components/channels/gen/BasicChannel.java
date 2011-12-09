package uk.ac.imperial.doc.masspa.gui.components.channels.gen;

import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.gui.util.Constants;
import uk.ac.imperial.doc.masspa.gui.util.ExpressionValidator;
import uk.ac.imperial.doc.masspa.language.Labels;
import uk.ac.imperial.doc.masspa.representation.components.AllComponent;
import uk.ac.imperial.doc.masspa.representation.components.AllMessage;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAMessage;

import com.google.common.collect.Multimap;

/**
 * This class contains the basic
 * options needed to describe a
 * channel
 * 
 * @author Chris Guenther
 */
public class BasicChannel extends JPanel
{
	private static final long serialVersionUID = 7496574974942023351L;

	// Components
	private final JLabel 			m_senderStateLabel;
	private final JComboBox			m_senderStatesCombo;
	private final JLabel 			m_receiverStateLabel;
	private final JComboBox			m_receiverStatesCombo;
	private final JLabel			m_messageType;
	private final JComboBox			m_messageTypesCombo;
	private final JLabel 			m_intensityLabel;
	private final JTextField 		m_intensity;
	private final JCheckBox 		m_popProportionalIntensity;
	
	public BasicChannel(ObservableAgents _agents)
	{
		m_senderStateLabel = new JLabel(Labels.s_CHANNEL_EDITOR_SENDER_STATE_LABEL);
		m_senderStateLabel.setAlignmentX(LEFT_ALIGNMENT);
		m_senderStatesCombo = new JComboBox();
		m_senderStatesCombo.setAlignmentX(LEFT_ALIGNMENT);
		m_receiverStateLabel = new JLabel(Labels.s_CHANNEL_EDITOR_RECEIVER_STATE_LABEL);
		m_receiverStateLabel.setAlignmentX(LEFT_ALIGNMENT);
		m_receiverStatesCombo = new JComboBox();
		m_receiverStatesCombo.setAlignmentX(LEFT_ALIGNMENT);
		m_messageType = new JLabel(Labels.s_CHANNEL_EDITOR_MESSAGE_TYPE_LABEL);
		m_messageType.setAlignmentX(LEFT_ALIGNMENT);
		m_messageTypesCombo = new JComboBox();
		m_messageTypesCombo.setAlignmentX(LEFT_ALIGNMENT);
		m_intensityLabel = new JLabel(Labels.s_CHANNEL_EDITOR_CHANNEL_INTENSITY);
		m_intensityLabel.setAlignmentX(LEFT_ALIGNMENT);
		m_intensity = new JTextField(Constants.s_CHANNEL_EDITOR_INTENSITY_DEFAULT);
		m_intensity.setColumns(Constants.s_CHANNEL_EDITOR_INTENSITY_COLS);
		m_intensity.setAlignmentX(LEFT_ALIGNMENT);
		m_popProportionalIntensity = new JCheckBox(Labels.s_CHANNEL_EDITOR_POPULATION_PROPORTIONAL_RATE);
		m_popProportionalIntensity.setSelected(Constants.s_CHANNEL_EDITOR_PROP_RATE);
		m_popProportionalIntensity.setAlignmentX(LEFT_ALIGNMENT);
		
		add(m_senderStateLabel);
		add(m_senderStatesCombo);
		add(m_receiverStateLabel);
		add(m_receiverStatesCombo);
		add(m_messageType);
		add(m_messageTypesCombo);
		add(m_intensityLabel);
		add(m_intensity);
		add(m_popProportionalIntensity);
		
		// Register as Listener
		if (_agents != null) {_agents.addChangeListener(new AgentsChangeListener());}
		
		// Init
		updateAgentStates(null);
		updateMessageTypes(null);
	}
	
	/**
	 * Add listener to combo boxes
	 * @param _l
	 */
	public void setItemListener(ItemListener _l)
	{
		m_senderStatesCombo.addItemListener(_l);
		m_receiverStatesCombo.addItemListener(_l);
		m_messageTypesCombo.addItemListener(_l);
	}
	
	/**
	 * @return all sender states that were selected
	 */
	public Set<MASSPAComponent> getSenderStates()
	{
		Set<MASSPAComponent> ret = new HashSet<MASSPAComponent>();
		ret.add((MASSPAComponent)m_senderStatesCombo.getSelectedItem());
		return ret;
	}
	
	/**
	 * @return all receiver states that were selected
	 */
	public Set<MASSPAComponent> getReceiverStates()
	{
		Set<MASSPAComponent> ret = new HashSet<MASSPAComponent>();
		ret.add((MASSPAComponent)m_receiverStatesCombo.getSelectedItem());
		return ret;
	}

	/**
	 * @return all message types that were selected
	 */
	public Set<MASSPAMessage> getMessageTypes()
	{
		Set<MASSPAMessage> ret = new HashSet<MASSPAMessage>();
		ret.add((MASSPAMessage)m_messageTypesCombo.getSelectedItem());
		return ret;
	}
	
	/**
	 * Add all states to the combo boxes
	 * @param agentTypes
	 */
	private void updateAgentStates(Multimap<String, MASSPAComponent> _agentTypes)
	{
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		DefaultComboBoxModel model2 = new DefaultComboBoxModel();
		model.addElement(new AllComponent());
		model2.addElement(new AllComponent());
		
		if (_agentTypes != null)
		{
			for (Entry<String,Collection<MASSPAComponent>> map : _agentTypes.asMap().entrySet())
			{
				for (MASSPAComponent comp : map.getValue())
				{
					model.addElement(comp);
					model2.addElement(comp);
				}
			}
		}
		m_senderStatesCombo.setModel(model);
		m_receiverStatesCombo.setModel(model2);
	}
	
	private void updateMessageTypes(Set<MASSPAMessage> _messageTypes)
	{
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement(new AllMessage());
		if (_messageTypes != null)
		{
			for (MASSPAMessage m : _messageTypes)
			{
				model.addElement(m);
			}
		}
		m_messageTypesCombo.setModel(model);
	}
	
	/**
	 * @return true iff rate is supposed to be proportional
	 */
	public boolean hasProportionalRate()
	{
		return m_popProportionalIntensity.isSelected();
	}
	
	/**
	 * @return intensity expression field text value
	 */
	public String getIntensityExprText()
	{
		 return m_intensity.getText();
	}
	
	/**
	 * @return compiled expression representing the intensity or null
	 * 		   there is a compilation error. 
	 */
	public AbstractExpression getIntensityExpr()
	{
		 return ExpressionValidator.validate(m_intensity.getText());
	}
	
	/**
	 * Set intensity to {@code _intensity}
	 * @param _intensity
	 */
	public void setIntensity(String _intensity)
	{
		m_intensity.setText(_intensity);
	}
	
	/**
	 * Enable/Disable gui component for intensity
	 * in edit/view mode
	 * @param _b iff true then user can modify channel intensity
	 */
	public void setEnableIntensityComponent(boolean _b)
	{
		m_intensity.setEnabled(_b);
		m_popProportionalIntensity.setEnabled(_b);
	}

	/**
	 * Set visibility of {@code m_popProportionalIntensity}
	 * @param _b iff true then {@code m_popProportionalIntensity} is visible
	 */
	public void setVisibleProportionalIntensity(boolean _b)
	{
		m_popProportionalIntensity.setVisible(_b);
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
				updateMessageTypes(agents.getMASSPAAgents().getMessages());
			}
		}
	}
}
