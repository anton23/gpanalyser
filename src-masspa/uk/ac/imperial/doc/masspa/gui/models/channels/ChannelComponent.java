package uk.ac.imperial.doc.masspa.gui.models.channels;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import uk.ac.imperial.doc.masspa.gui.models.topologies.LocationComponent;

/**
 * This class represents an abstract one-way
 * channel between a sender and a receiver
 * location. All concrete channels that have
 * the same direction are held in this class.
 * The class is also serializable.
 * 
 * @author Chris Guenther
 */
@Root
public class ChannelComponent
{
	private LocationComponent m_senderLoc;
	private LocationComponent m_receiverLoc;
	private final Map<MASSPAChannelComponent,MASSPAChannelComponent> m_dataChannels;
	
	/** Needed by SimpleXML-Serializer*/
	protected ChannelComponent()
	{
		m_dataChannels = new HashMap<MASSPAChannelComponent,MASSPAChannelComponent>();
	}
	
	/**
	 * Create a new channel component that will contain all channels for messages from
	 * {@code _senderLoc} to {@code _receiverLoc}
	 * @param _senderLoc
	 * @param _receiverLoc
	 */
	public ChannelComponent(LocationComponent _senderLoc, LocationComponent _receiverLoc)
	{
		m_senderLoc = _senderLoc;
		m_receiverLoc = _receiverLoc;
		m_dataChannels = new HashMap<MASSPAChannelComponent,MASSPAChannelComponent>();
	}

	//**********************************************
	// Getters/Setters and SimpleXML-Serialization
	//**********************************************
	/**
	 * set sender location to {@code _s}
	 * @_s
	 */
	@Element(name="senderLocation") protected void setSenderLoc(LocationComponent _s){m_senderLoc=_s;}
	
	/**
	 * @return sender location
	 */
	@Element(name="senderLocation") public LocationComponent getSenderLoc(){return m_senderLoc;}
	
	/**
	 * set receiving location to {@code _r}
	 * @_r
	 */
	@Element(name="receiverLocation") protected void setReceiverLoc(LocationComponent _r){m_receiverLoc=_r;}
	
	/**
	 * @return receiving location
	 */
	@Element(name="receiverLocation") public LocationComponent getReceiverLoc(){return m_receiverLoc;}
	
	/**
	 * copy datachannels in {@code _c} into this class' datachannels
	 * @param _c
	 */
	@ElementList(name="dataChannels") protected void setDataChannelsCopy(List<MASSPAChannelComponent> _l){for (MASSPAChannelComponent c : _l){addDataChannel(c);}}
	
	/**
	 * @return a copy of the list of data channels stored in this class.
	 */
	@ElementList(name="dataChannels") public List<MASSPAChannelComponent> getDataChannelsCopy(){return new LinkedList<MASSPAChannelComponent>(m_dataChannels.values());}
	
	/**
	 * @return unmodifiable collection of data channels.
	 */
	public Collection<MASSPAChannelComponent> getDataChannels(){return Collections.unmodifiableCollection(m_dataChannels.values());}
	
	/**
	 * Add data channel
	 * @param _c channel to be added
	 */
	public void addDataChannel(MASSPAChannelComponent _c)
	{
		m_dataChannels.put(_c,_c);
	}
	
	/**
	 * Remove data channel
	 * @param _c channel to be removed
	 */
	public void removeDataChannel(MASSPAChannelComponent _c)
	{
		m_dataChannels.remove(_c);
	}
		
	/**
	 * @return number of data channels
	 */
	public int getNofDataChannels()
	{
		return m_dataChannels.size();
	}
	
	/**
	 * @return true if there is at least one active datachannel
	 */
	public boolean isActive()
	{
		return (getNofDataChannels() > 0 && m_senderLoc.getActive() && m_receiverLoc.getActive());
	}
	
	//************************************
	// Object overwrites
	//************************************
	@Override
	public String toString()
	{
		return m_senderLoc.toString() + " => " + m_receiverLoc.toString();
	}
	
	@Override
	public int hashCode()
	{
		return m_senderLoc.hashCode() + m_receiverLoc.hashCode();
	}
	
	@Override
	public boolean equals(Object _o)
	{
		if (this == _o) {return true;}
		if (!(_o instanceof ChannelComponent)) {return false;}
		return ((ChannelComponent)_o).getSenderLoc().equals(getSenderLoc()) &&
			   ((ChannelComponent)_o).getReceiverLoc().equals(getReceiverLoc());
	}
}
