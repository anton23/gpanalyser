package uk.ac.imperial.doc.masspa.gui.components.topologies.gen;

import javax.swing.JFormattedTextField;

import uk.ac.imperial.doc.masspa.language.Labels;

/**
 * Empty topology - No options.
 * 
 * @author Chris Guenther
 */
public class EmptyTopology extends JTopologyGeneratorOptions
{
	private static final long serialVersionUID = 5638165204426784289L;

	public EmptyTopology()
	{
		super();
		JFormattedTextField vertSpacer = new JFormattedTextField(Labels.s_LOCATION_EDITOR_NO_OPS);
		vertSpacer.setEnabled(false);
		add(vertSpacer);
	}
}
