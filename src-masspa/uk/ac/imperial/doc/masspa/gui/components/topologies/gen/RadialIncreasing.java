package uk.ac.imperial.doc.masspa.gui.components.topologies.gen;

import uk.ac.imperial.doc.masspa.gui.models.topologies.Topology;
import uk.ac.imperial.doc.masspa.gui.models.topologies.TopologyGenerator;

/**
 * Options for a radial grid topology where
 * each ring has a different number of cells
 * 
 * @author Chris Guenther
 */
public class RadialIncreasing extends Radial
{
	private static final long serialVersionUID = 2122260408594885748L;
		
	public RadialIncreasing()
	{
		super();
	}
	
	@Override
	public Topology genTopology()
	{
		int radius = Math.abs(Integer.parseInt(m_nofRings.getText()));
		return TopologyGenerator.genRadialIncreasingTopology(radius);
	}
}
