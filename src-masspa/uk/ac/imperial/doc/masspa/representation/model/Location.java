package uk.ac.imperial.doc.masspa.representation.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.masspa.language.Messages;

public class Location implements Comparable<Location>
{
	private List<Integer> m_coords;

	protected Location() {setCoords(new LinkedList<Integer>());}
	
	public Location(List<Integer> _coords)
	{
		if (_coords == null)
		{
			throw new AssertionError(Messages.s_COMPILER_LOCATION_NULL_COORDS);
		}
		setCoords(_coords);
	}
	
	public Location(Location l)
	{
		this(l.m_coords);
	}

	//**********************************************
	// Getters/Setters
	//**********************************************
	protected void setCoords(List<Integer> _coords) {m_coords = Collections.unmodifiableList(_coords);}
	/**
	 * @return unmodifiable coordinate list
	 */
	public List<Integer> getCoords() {return m_coords;}

	//**********************************************
	// Auxillary
	//**********************************************
	public double getDistanceTo(Location _l)
	{
		if (m_coords.size() != _l.m_coords.size()) {throw new AssertionError(Messages.s_COMPILER_LOCATION_DIM_MISMATCH);}
		double lenSqd = 0;
		for (int i=0; i<m_coords.size();i++)
		{
			lenSqd += Math.pow(m_coords.get(i)-_l.m_coords.get(i), 2);
		}
		return Math.sqrt(lenSqd);
	}

	//**********************************************
	// Object overwrites
	//**********************************************
	@Override
	public String toString()
	{
		String s="";
		for (int i:m_coords) {if(s != ""){s+=",";} s+= i;}
		return "@("+s+")";
	}
	
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object _obj)
	{
		if (this == _obj) {return true;}
		if (!(_obj instanceof Location)) {return false;}
		Location _loc = (Location)_obj;
		if (m_coords.size() - _loc.m_coords.size() != 0) {return false;}
		int i;
		for (i=0; i<m_coords.size(); i++)
		{
			if (m_coords.get(i)-_loc.m_coords.get(i) != 0)
			{
				return false;
			}
		}
		return _loc.getClass() == this.getClass();
	}

	//**********************************************
	// Comparable
	//**********************************************
	@Override
	public int compareTo(Location _l)
	{
		if (this == _l) {return 0;}
		if (m_coords.size() - _l.m_coords.size() != 0) {return  (int)Math.signum(m_coords.size() - _l.m_coords.size());}
		for (int i=0; i<m_coords.size(); i++)
		{
			if (m_coords.get(i)-_l.m_coords.get(i) != 0)
			{
				return (int)Math.signum(m_coords.get(i)-_l.m_coords.get(i));
			}
		}
		return 0;
	}
}
