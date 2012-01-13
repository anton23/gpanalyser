package uk.ac.imperial.doc.masspa.gui.models.channels;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import uk.ac.imperial.doc.masspa.gui.models.topologies.LocationComponent;

/**
 * This class is a container for all channels.
 * The class is serializable.
 * 
 * @author Chris Guenther
 */
@Root
public class Channels
{
	private final Map<ChannelComponent, ChannelComponent> m_channels;
	
	public Channels()
	{
		m_channels = new HashMap<ChannelComponent,ChannelComponent>();
	}
	
	//**********************************************
	// Getters/Setters and SimpleXML-Serialization
	//**********************************************
	/**
	 * Copy channels from {@code _l} into channel list
	 * @param _l
	 */
	@ElementList(name="channels") protected void setChannelsCopy(List<ChannelComponent> _l){for (ChannelComponent c : _l){m_channels.put(c,c);}};
	
	/**
	 * @return a copy of the list of all channels
	 */
	@ElementList(name="channels") public List<ChannelComponent> getChannelsCopy() {return new LinkedList<ChannelComponent>(m_channels.values());}
	
	/**
	 * @return unmodifiable collection of all channels
	 */
	public Collection<ChannelComponent> getChannels()
	{
		return Collections.unmodifiableCollection(m_channels.values());
	}
	
	/**
	 * Return channel if it exists and otherwise create a new channel
	 * @param _chan the channel
	 */
	public ChannelComponent getChannel(MASSPAChannelComponent _chan)
	{
		LocationComponent senderLoc = new LocationComponent(_chan.getSender().getLocation().getCoords());
		LocationComponent receiverLoc = new LocationComponent(_chan.getReceiver().getLocation().getCoords());
		return getChannel(senderLoc,receiverLoc);
	}
	
	/**
	 * Return channel if it exists and otherwise create a new channel
	 * @param _sender of the channel
	 * @param _receiver of the channel
	 */
	public ChannelComponent getChannel(LocationComponent _sender, LocationComponent _receiver)
	{
		ChannelComponent c = new ChannelComponent(_sender, _receiver);
		return getChannel(c);
	}

	/**
	 * Get or create channel {@code _c}
	 * @_c
	 */
	protected ChannelComponent getChannel(ChannelComponent _c)
	{
		if (!m_channels.containsKey(_c))
		{
			m_channels.put(_c, _c);
		}
		return m_channels.get(_c);
	}
	
	/**
	 * Remove channel if it exists
	 * @param _sender of the channel
	 * @param _receiver of the channel
	 */
	public void removeChannel(ChannelComponent _c)
	{
		m_channels.remove(_c);
	}
}
