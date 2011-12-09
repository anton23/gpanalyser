package uk.ac.imperial.doc.masspa.gui.models.topologies;

/**
 * This class contains static functions that
 * allow the generation of common topology
 * patterns.
 * 
 * @author Chris Guenther
 */
public class TopologyGenerator
{
	// Some topology type definitions
	public final static String s_TYPE_REG_GRID = "REG_GRID";
	public final static String s_TYPE_REG_RADIAL = "REG_RADIAL";
	
	/**
	 * Create a two dimensional grid topology with dimensions
	 * {@code _x} by {@code _y}
	 * @param _x number of horizontal lines
	 * @param _y number of vertical lines
	 * @return grid topology
	 */
	public static Topology genRectangularGridTopology(int _x, int _y)
	{
		Topology topo = new Topology();
		topo.setType(s_TYPE_REG_GRID);

		for (int x=0; x<_x; x++)
		{
			for (int y=0; y<_y; y++)
			{
				topo.addLocation(new LocationComponent(x,y));
			}
		}

		return topo;
	}
	
	/**
	* Creates a radial grid topology where each outer rings have more cells
	 * @param _locsPerRing locations in each ring
	 * @return increasing radial grid topology
	 */
	public static Topology genRadialIncreasingTopology(int _radius)
	{
		Topology topo = new Topology();
		topo.setType(s_TYPE_REG_RADIAL);
		
		for (int x=-_radius; x<=_radius; x++)
		{
			for (int y=-_radius; y<=_radius; y++)
			{
				if (x*x+y*y <= _radius*_radius)
				{
					topo.addLocation(new LocationComponent(x+_radius,y+_radius));
				}
			}
		}
		
		return topo;
	}
}
