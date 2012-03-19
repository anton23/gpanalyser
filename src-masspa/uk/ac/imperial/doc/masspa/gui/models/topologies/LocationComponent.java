package uk.ac.imperial.doc.masspa.gui.models.topologies;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.gui.util.Constants;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.model.Location;

/**
 * Represents a location in the topology
 * It stores information about the location
 * (e.g. active, inactive) and the agent
 * populations at that location. The class
 * is serializable.
 * 
 * @author Chris Guenther
 */
@Root
public class LocationComponent extends Location
{
	private boolean m_active = true;
	private double m_radius = Constants.s_LOCATION_VIEWER_LOC_RADIUS_DEFAULT;
	private Map<MASSPAAgentPopComponent, MASSPAAgentPopComponent> m_populations = new HashMap<MASSPAAgentPopComponent, MASSPAAgentPopComponent>();

	/** Needed by SimpleXML-Serializer*/
	protected LocationComponent()
	{
	}
	
	/**
	 * Create a new location at coordinates {@code _coords}
	 * @param _coords
	 */
	public LocationComponent(Integer... _coords)
	{
		this(Arrays.asList(_coords));
	}

	/**
	 * Create a new location at coordinates {@code _coords}
	 * @param _coords
	 */
	public LocationComponent(List<Integer> coords)
	{
		super(new LinkedList<Integer>(coords));
	}
	
	//**********************************************
	// Getters/Setters and SimpleXML-Serialization
	//**********************************************
	/**
	 * Set location coordinates to {@code _s} which must have form "1,5,2,..."
	 * @param _s
	 */
	@Attribute(name="coords") protected void setCoordsStr(String _s)
	{
		List<Integer> l = new LinkedList<Integer>();
		for (String s : _s.split(",")) {l.add(Integer.parseInt(s));}
		setCoords(l);
	}
	
	/**
	 * @return location coordinates as String of form "1,5,2,..."
	 */
	@Attribute(name="coords") public String getCoordsStr()
	{
		return getCoords().toString().replaceAll("\\[|\\]|\\s", "");
	}

	/**
	 * Set location active to {@code _a}
	 * @param _a
	 */
	@Attribute(name="active") public void setActive(boolean _a) {m_active = _a;}
	
	/**
	 * @return true if location is active
	 */
	@Attribute(name="active") public boolean getActive() {return m_active;}

	/**
	 * Set location radius to {@code _r}
	 * @param _r
	 */
	@Attribute(name="radius") public void setRadius(double _r) {m_radius=_r;}
	
	/**
	 * @return radius of location
	 */
	@Attribute(name="radius") public double getRadius() {return m_radius;}
	
	/**
	 * Add all populations in {@code _l} to populations map
	 * @param _l
	 */
	@ElementList(name="populations") protected void setPopulationsCopy(List<MASSPAAgentPopComponent> _l)
	{
		for (MASSPAAgentPopComponent p: _l)
		{
			m_populations.put(p,p);
		}
	}
	
	/**
	 * @return copied list of all populations in this location
	 */
	@ElementList(name="populations") public List<MASSPAAgentPopComponent> getPopulationsCopy()
	{
		return new LinkedList<MASSPAAgentPopComponent>(m_populations.values());
	}
	
	/**
	 * Add a new agent population
	 * @param _c agent population to be added
	 * @param _e the agent population expression
	 */
	public void addAgentPopulation(MASSPAComponent _c, AbstractExpression _e)
	{
		MASSPAAgentPopComponent p = new MASSPAAgentPopComponent(_c,this);
		p.setInitialPopulation(_e);
		m_populations.put(p,p);
	}
	
	/**
	 * Remove an agent population
	 * @param _c agent population to be deleted
	 */
	public void removeAgentPopulation(MASSPAAgentPopComponent _p)
	{
		m_populations.remove(_p);
	}
	
	/**
	 * Remove an agent population
	 * @param _c agent state for which population needs to be deleted
	 */
	public void removeAgentPopulation(MASSPAComponent _c)
	{
		MASSPAAgentPopComponent p = new MASSPAAgentPopComponent(_c,this);
		m_populations.remove(p);
	}
	
	/**
	 * Find a specific agent population
	 * @param _c agent population to be deleted
	 * @return agent population or null if it does not exist
	 */
	public MASSPAAgentPopComponent getPopulation(MASSPAComponent _c)
	{
		MASSPAAgentPopComponent p = new MASSPAAgentPopComponent(_c,this);
		return m_populations.get(p);
	}
	
	/**
	 * @return unmodifiable collection of all agent populations defined for this location
	 */
	public Collection<MASSPAAgentPopComponent> getPopulations()
	{
		return Collections.unmodifiableCollection(m_populations.values());
	}
}
