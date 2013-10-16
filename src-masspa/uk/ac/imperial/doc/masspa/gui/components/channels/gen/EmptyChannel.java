package uk.ac.imperial.doc.masspa.gui.components.channels.gen;

import java.awt.Dimension;

import javax.swing.JFormattedTextField;

import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.language.Labels;

/**
 * Empty Channel - No Options.
 * 
 * @author Chris Guenther
 */
public class EmptyChannel extends JChannelGeneratorOptions
{
	private static final long serialVersionUID = -922650785676007446L;

	public EmptyChannel(ObservableAgents _agents)
	{
		super(_agents);
		m_options.setPreferredSize(new Dimension(1000,60));
		JFormattedTextField vertSpacer = new JFormattedTextField(Labels.s_CHANNEL_EDITOR_NO_OPS);
		vertSpacer.setEnabled(false);
		m_options.removeAll();
		m_options.add(vertSpacer);
	}
}
