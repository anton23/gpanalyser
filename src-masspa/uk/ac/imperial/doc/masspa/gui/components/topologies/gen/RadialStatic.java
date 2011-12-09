package uk.ac.imperial.doc.masspa.gui.components.topologies.gen;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import uk.ac.imperial.doc.masspa.gui.models.topologies.Topology;
import uk.ac.imperial.doc.masspa.gui.util.Constants;
import uk.ac.imperial.doc.masspa.language.Labels;

/**
 * Options for a radial grid topology where
 * each ring has the same number of cells
 * 
 * @author Chris Guenther
 */
public abstract class RadialStatic extends Radial
{
	private static final long serialVersionUID = 6350630029096057724L;

	// Components
	protected final JLabel 					m_nofLocsPerRingLabel;
	protected final JFormattedTextField		m_nofLocsPerRing;
		
	public RadialStatic()
	{
		super();
		
		// Initialise components
		m_nofLocsPerRingLabel = new JLabel(Labels.s_LOCATION_EDITOR_NOF_LOCS_PER_RING);
		m_nofLocsPerRing = new JFormattedTextField(NumberFormat.getInstance());
		m_nofLocsPerRing.setText(Constants.s_LOCATION_EDITOR_NOF_LOCS_PER_RING_DEFAULT);
		m_nofLocsPerRing.setColumns(Constants.s_LOCATION_EDITOR_NOF_LOCS_PER_RING_WIDTH_DEFAULT);
		
		// Layout
		add(m_nofLocsPerRingLabel);
		add(m_nofLocsPerRing);
	}
	
	@Override
	public Topology genTopology()
	{
		//int locsPerRing = Math.abs(Integer.parseInt(m_nofLocsPerRing.getText()));
		//int radius = Math.abs(Integer.parseInt(m_nofRings.getText()));
		return null;
	}
}