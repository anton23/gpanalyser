package uk.ac.imperial.doc.masspa.gui.models;

import javax.swing.event.ChangeEvent;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import uk.ac.imperial.doc.masspa.gui.models.channels.Channels;
import uk.ac.imperial.doc.masspa.gui.models.topologies.Topology;

/**
 * This is a container class for a Topology
 * which fires events whenever the Topology
 * changes. The class is serializable.
 * 
 * @author Chris Guenther
 */
@Root @Namespace(reference="http://uk.ac.imperial.doc.masspa/ObservableTopology/1")
public class ObservableTopology extends ObservableContainer
{
	private Topology m_topology = null;

	/** Needed by SimpleXML-Serializer */
	protected ObservableTopology()
	{
	}

	/**
	 * Create a new container for {@code _topology}
	 * @param _topology
	 */
	public ObservableTopology(Topology _topology)
	{
		m_topology = _topology;
	}

	//**********************************************
	// Getters/Setters and SimpleXML-Serialization
	//**********************************************
	/**
	 * @return the topology
	 */
	@Element(name="topology", required=false) public Topology getTopology()
	{
		return m_topology;
	}
	
	/**
	 * Set the topology to {@code _t} and fire a ChangeEvent
	 * @param _t
	 */
	@Element(name="topology", required=false) public void setTopology(Topology _t)
	{
		m_topology = _t;
		fireChangeEvent(new ChangeEvent(this));
	}
	
	/**
	 * Set the topology channels to {@code _c} and fire a ChangeEvent
	 * @param _c
	 */
	public void setChannels(Channels _c)
	{
		if (m_topology == null) {return;}
		m_topology.setChannels(_c);
		fireChangeEvent(new ChangeEvent(this));
	}
}
