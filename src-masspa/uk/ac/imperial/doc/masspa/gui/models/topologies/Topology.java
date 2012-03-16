package uk.ac.imperial.doc.masspa.gui.models.topologies;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import uk.ac.imperial.doc.masspa.gui.models.channels.Channels;

/**
 * Represents the topology defined in the
 * MASSPA-Modeller GUI.
 * 
 * @author Chris Guenther
 */
@Root
public class Topology
{
	private Map<LocationComponent,LocationComponent> m_locations;
	private String m_type;
	private Channels m_channels;
	
	public Topology()
	{
		m_locations = new HashMap<LocationComponent, LocationComponent>();
		m_type = "";
		m_channels = new Channels();
	}

	//**********************************************
	// Getters/Setters and SimpleXML-Serialization
	//**********************************************
	/**
	 * Copy locations {@code _l} into topology
	 * @param _l
	 */
	@ElementList(name="locations") protected void setLocationsCopy(List<LocationComponent>  _l) {for(LocationComponent l : _l){addLocation(l);}}
	
	/**
	 * @return copied list of all locations found in this topology
	 */
	@ElementList(name="locations") protected List<LocationComponent> getLocationsCopy() {return new LinkedList<LocationComponent>(m_locations.values());}	
	
	/**
	 * Set topology type to _s
	 * @param _s
	 */
	@Attribute(name="type", required=false) public void setType(String _s){m_type=_s;}
	
	/**
	 * The type field can by used to tag topologies, for instance
	 * some topology generators may want to tag the topology to
	 * inform channel/movement generators about the layout. This
	 * information can then be used to generate topology specific
	 * channels/movement descriptions.
	 * 
	 * @return topology type
	 */
	@Attribute(name="type", required=false) public String getType(){return m_type;}
	
	/**
	 * @return get all channels defined for the topology.
	 */
	@Element(name="channels", required=false) public Channels getChannels() {return m_channels;}
	
	/**
	 * set topology channels to {@code _c}
	 * @param _c new channels for topology.
	 */
	@Element(name="channels", required=false) public void setChannels(Channels _c) {m_channels = _c;}

	/**
	 * @return unmodifiable collection of locations in topology
	 */
	public Collection<LocationComponent> getLocations() {return Collections.unmodifiableCollection(m_locations.values());}
	
	/**
	 * Add new location at position {@code _l} (if it doesn't exist yet)
	 * @param _l position of location
	 */
	public void addLocation(LocationComponent _l)
	{
		if (m_locations.containsKey(_l)) {return;}
		m_locations.put(_l,_l);
	}

	/**
	 * Find location component at {@code _l}. If no such location
	 * component exists yet, add it.
	 * @param _l
	 * @return location object at position {@code _l} 
	 */
	public LocationComponent getLocation(LocationComponent _l)
	{
		addLocation(_l);
		return m_locations.get(_l);
	}
}
