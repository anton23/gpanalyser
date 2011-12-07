package uk.ac.imperial.doc.masspa.gui.components.topologies.gen;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JFormattedTextField;

import uk.ac.imperial.doc.masspa.gui.util.Constants;
import uk.ac.imperial.doc.masspa.language.Labels;

/**
 * Super class for all radial
 * topology generator options.
 * 
 * @author Chris Guenther
 */
public abstract class Radial extends JTopologyGeneratorOptions
{
	private static final long serialVersionUID = 8964631708504947441L;

	// Components
	protected final JLabel 					m_nofRingsLabel;
	protected final JFormattedTextField		m_nofRings;
		
	public Radial()
	{
		super();
		
		// Initialise components
		m_nofRingsLabel = new JLabel(Labels.s_LOCATION_EDITOR_NOF_RINGS);
		m_nofRings = new JFormattedTextField(NumberFormat.getInstance());
		m_nofRings.setText(Constants.s_LOCATION_EDITOR_NOF_RINGS_DEFAULT);
		m_nofRings.setColumns(Constants.s_LOCATION_EDITOR_NOF_RING_WIDTH_DEFAULT);
		
		// Layout
		add(m_nofRingsLabel);
		add(m_nofRings);
	}
}
