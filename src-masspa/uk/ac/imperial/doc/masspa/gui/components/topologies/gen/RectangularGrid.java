package uk.ac.imperial.doc.masspa.gui.components.topologies.gen;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import uk.ac.imperial.doc.masspa.gui.models.topologies.Topology;
import uk.ac.imperial.doc.masspa.gui.models.topologies.TopologyGenerator;
import uk.ac.imperial.doc.masspa.gui.util.Constants;
import uk.ac.imperial.doc.masspa.language.Labels;

/**
 * Options for a grid topology with dimensions m_LocsX, m_LocsY
 * 
 * @author Chris Guenther
 */
public class RectangularGrid extends JTopologyGeneratorOptions
{
	private static final long serialVersionUID = -3465231455438335329L;

	// Components
	protected final JLabel 					m_LocsXLabel;
	protected final JLabel 					m_LocsYLabel;
	protected final JFormattedTextField		m_LocsX;
	protected final JFormattedTextField		m_LocsY;
	
	public RectangularGrid()
	{
		super();
		
		// Initialise components
		m_LocsXLabel = new JLabel(Labels.s_LOCATION_EDITOR_LOCS_X);
		m_LocsYLabel = new JLabel(Labels.s_LOCATION_EDITOR_LOCS_Y);
		m_LocsX = new JFormattedTextField(NumberFormat.getInstance());
		m_LocsX.setText(Constants.s_LOCATION_EDITOR_LOCS_X_DEFAULT);
		m_LocsX.setColumns(Constants.s_LOCATION_EDITOR_LOCS_WIDTH_DEFAULT);
		m_LocsY = new JFormattedTextField(NumberFormat.getInstance());
		m_LocsY.setText(Constants.s_LOCATION_EDITOR_LOCS_Y_DEFAULT);
		m_LocsY.setColumns(Constants.s_LOCATION_EDITOR_LOCS_WIDTH_DEFAULT);
		
		// Layout
		add(m_LocsXLabel);
		add(m_LocsX);
		add(m_LocsYLabel);
		add(m_LocsY);
	}
	
	/**
	 * Create a two dimensional grid topology.
	 */
	@Override
	public Topology genTopology()
	{
		int locsX = Math.abs(Integer.parseInt(m_LocsX.getText()));
		int locsY = Math.abs(Integer.parseInt(m_LocsY.getText()));
		return TopologyGenerator.genRectangularGridTopology(locsX, locsY);
	}
}
