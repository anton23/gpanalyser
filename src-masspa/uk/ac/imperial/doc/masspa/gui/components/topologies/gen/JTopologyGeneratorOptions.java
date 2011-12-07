package uk.ac.imperial.doc.masspa.gui.components.topologies.gen;

import java.awt.FlowLayout;

import javax.swing.JPanel;

import uk.ac.imperial.doc.masspa.gui.models.topologies.Topology;
import uk.ac.imperial.doc.masspa.gui.util.Constants;

/**
 * This class is the base class for
 * all topology generator options.
 * Each option class provides input
 * fields for a specific topology
 * generators.
 * 
 * @author Chris Guenther
 */
public abstract class JTopologyGeneratorOptions extends JPanel
{
	private static final long serialVersionUID = -3615677499876388598L;

	public JTopologyGeneratorOptions()
	{
		super(new FlowLayout(FlowLayout.LEFT, Constants.s_LOCATION_EDITOR_FLOW_LAYOUT_GAP_H, Constants.s_LOCATION_EDITOR_FLOW_LAYOUT_GAP_V));
	}

	public Topology genTopology()
	{
		return null;
	}
}
