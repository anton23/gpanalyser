package uk.ac.imperial.doc.masspa.util;

import java.util.LinkedList;

import uk.ac.imperial.doc.masspa.representation.model.AllLocation;
import uk.ac.imperial.doc.masspa.representation.model.Location;
import uk.ac.imperial.doc.masspa.representation.model.VarLocation;

public class LocationHelper
{
	/**
	 * Find location at the end of the string
	 * if it is a parameterised location it will
	 * be replaced by _loc
	 * @param _s
	 * @param _loc
	 * @return new localised location
	 */
	public static Location getLocalisedLocation(String _s, Location _loc)
	{
		if (_s.contains(VarLocation.getInstance().toString()))
		{
			return _loc;
		}
		else if (_s.contains(AllLocation.getInstance().toString()))
		{
			throw new AssertionError(_s + " is invalid. Constants, Variables and Functions can only have location @(x) or @(INT(,INT)+)");
		}

		String coordsStr = (_s.split("@"))[1];
		coordsStr = coordsStr.replace("(","").replace(")", "");
		String[] coords = coordsStr.split(",");
		LinkedList<Integer> coordsList = new LinkedList<Integer>();
		for (int i=0; i<coords.length; i++)
		{
			coordsList.add(Integer.parseInt(coords[i]));
		}
		return new Location(coordsList);
	}
}
