package uk.ac.imperial.doc.masspa.gui.models.channels;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.masspa.gui.models.topologies.LocationComponent;
import uk.ac.imperial.doc.masspa.gui.models.topologies.MASSPAAgentPopComponent;
import uk.ac.imperial.doc.masspa.gui.models.topologies.Topology;
import uk.ac.imperial.doc.masspa.gui.util.MathExtra;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAMessage;

/**
 * This class contains static functions that
 * allow the generation of common channel
 * communication patterns.
 * 
 * @author Chris Guenther
 */
public class ChannelGenerator
{

	/**
	 * This communication pattern will create:
	 * The shortest possible route to the sink for all components
	 * 
	 * @param _topology
	 * @param _senderStates
	 * @param _receiverStates
	 * @param _messageTypes message types to be sent
	 * @param _sink the sink location
	 * @param _expr channel intensity scaling 
	 * @param _bPopProportional iff true _expr rate is divided by receiving and type population size
	 * @return
	 */
	public static Channels genShortestPathToSinkChannels(Topology _topology, Set<MASSPAComponent> _senderStates,
												   Set<MASSPAComponent> _receiverStates, Set<MASSPAMessage> _messageTypes,
												   LocationComponent _sink, double _maxHopLen, AbstractExpression _expr, boolean _bPopProptional)
	{
		Channels chans = new Channels();
		Set<LocationComponent> unexploredLocs = new HashSet<LocationComponent>(_topology.getLocations());
		Map<LocationComponent,Double> currentPaths = new HashMap<LocationComponent,Double>();
		currentPaths.put(_sink,0.0);
		unexploredLocs.removeAll(currentPaths.keySet());		

		while (unexploredLocs.size() > 0)
		{
			Map<LocationComponent,Double> newPaths = new HashMap<LocationComponent,Double>();
			for (LocationComponent loc : unexploredLocs)
			{
				double minDistToThinkLocal = 100000;
				for (LocationComponent pathLoc : currentPaths.keySet())
				{
					double hopLen = loc.getDistanceTo(pathLoc);
					if (hopLen <= _maxHopLen)
					{
						double distToSink = hopLen + currentPaths.get(pathLoc);
						minDistToThinkLocal = (minDistToThinkLocal > distToSink) ? distToSink : minDistToThinkLocal;
					}
				}
				
				for (LocationComponent pathLoc : currentPaths.keySet())
				{
					double hopLen = loc.getDistanceTo(pathLoc);
					if (hopLen <= _maxHopLen && MathExtra.round( hopLen + currentPaths.get(pathLoc), 5) == MathExtra.round(minDistToThinkLocal, 5))
					{
						ChannelComponent c = chans.getChannel(new ChannelComponent(loc,pathLoc));
						addMASSPAChannels(c,_senderStates,_receiverStates,_messageTypes,_expr,_bPopProptional);
						newPaths.put(loc,loc.getDistanceTo(pathLoc) + currentPaths.get(pathLoc));
					}
				}
			}
			currentPaths.putAll(newPaths);
			unexploredLocs.removeAll(newPaths.keySet());
		}
		
		return chans;
	}

	public static void addMASSPAChannels(ChannelComponent _c,
			Set<MASSPAComponent> _senderStates,
			Set<MASSPAComponent> _receiverStates,
			Set<MASSPAMessage> _messageTypes,
			AbstractExpression _expr,
			boolean _bPopProptional)
	{
		// Divide by receiving agent population rate
		if (_bPopProptional)
		{
			_expr = DivExpression.create(_expr, MASSPAAgentPopComponent.s_constRecvAgentPop);
		}
		for (MASSPAComponent senderState : _senderStates)
		{
			MASSPAAgentPopComponent senderPop = new MASSPAAgentPopComponent(senderState, _c.getSenderLoc());
			for (MASSPAComponent receiverState : _receiverStates)
			{
				MASSPAAgentPopComponent receiverPop = new MASSPAAgentPopComponent(receiverState, _c.getReceiverLoc());
				for (MASSPAMessage msg : _messageTypes)
				{
					_c.addDataChannel(new MASSPAChannelComponent(senderPop, receiverPop, msg, _expr));
				}
			}	
		}
	}

}
