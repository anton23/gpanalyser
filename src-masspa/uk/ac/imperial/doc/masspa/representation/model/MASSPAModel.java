package uk.ac.imperial.doc.masspa.representation.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAAgents;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAMessage;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/***
 * This class represents the Markovian Agent Model.
 * In particular it contains:
 * a) All agent types
 * b) A list of all locations
 * c) The agent distribution in each location
 * d) All message channels between any two agents
 * 
 * @author Chris Guenther
 */
public class MASSPAModel
{
	// *************************************************
	// Member variable declarations
	// *************************************************
	private final MASSPAAgents m_agents;
	private final Map<Location,Location> m_locations;
	private final Map<MASSPAAgentPop,MASSPAAgentPop> m_agentPops;
	private final Map<MASSPAActionCount,MASSPAActionCount> m_actionCounts;
	private final HashMultimap<MASSPAAgentPop,MASSPAChannel> m_channels;
	private final HashMultimap<MASSPAAgentPop,MASSPAMovement> m_movements;
	private final HashMultimap<MASSPAAgentPop,MASSPAMovement> m_movementsReverse;
	private final HashMap<MASSPAAgentPop,MASSPABirth> m_births;
	private final HashMultimap<MASSPAAgentPop, MASSPAAgentPop> m_neighbours;
	
	// *************************************************
	// Constructor
	// *************************************************	
	public MASSPAModel(final MASSPAAgents _compFact)
	{
		MASSPALogging.info(Messages.s_COMPILER_MODEL_DEFINITION_ANALYSIS);
		
		// Local definitions
		m_agents = _compFact;
		if (m_agents == null) {throw new AssertionError(Messages.s_COMPILER_MODEL_NULL_COMPONENTS);}

		// Spatial definitions
		m_locations = new HashMap<Location,Location>();
		m_locations.put(VarLocation.getInstance(),VarLocation.getInstance());
		
		m_agentPops = new HashMap<MASSPAAgentPop,MASSPAAgentPop>();
		m_actionCounts = new HashMap<MASSPAActionCount,MASSPAActionCount>();
		m_channels = HashMultimap.create();
		m_movements = HashMultimap.create();
		m_movementsReverse = HashMultimap.create();
		m_births = new HashMap<MASSPAAgentPop,MASSPABirth>();
		
		// Relationship information
		m_neighbours = HashMultimap.create();
	}

	// *****************************************************************
	// Getters
	// *****************************************************************
	/**
	 * @return the object which holds the information about the
	 * 		   non-spatial aspects of the MASSPA model
	 */
	public MASSPAAgents getMASSPAAgents()
	{
		return m_agents;
	}
	
	/**
	 * @param _name agent state name
	 * @param _loc location of agent
	 * @param _line token line in model file
	 * @throws AssertionError if {@code _name} or {@code _loc} are invalid/undefined
	 * @return MASSPAAgentPop {@code _name}&#64;{@code _loc}. If it doesn't exist, it is created and returned.
	 */
	public MASSPAAgentPop getAgentPop(final String _name, final Location _loc, final int _line)
	{
		MASSPAComponent component = m_agents.getComponent(_name);
		Location loc = m_locations.get(_loc);
		if (component == null || loc == null)
		{
			String err = String.format(Messages.s_COMPILER_AGENTPOP_INVALID_DEFINITION,_name,_loc,_line);
			if (component == null && loc != null)
			{
				err = String.format(Messages.s_COMPILER_AGENTPOP_MISSING_COMPONENT_DEFINITION,_name,_name,_loc,_line);
			}
			else if (component != null && loc == null)
			{
				err = String.format(Messages.s_COMPILER_AGENTPOP_MISSING_LOCATION_DEFINITION,_loc,_name,_loc,_line);
			}
			MASSPALogging.fatalError(err);
			throw new AssertionError(err);
		}
		MASSPAAgentPop agentPop = new MASSPAAgentPop(component,loc);
		if (!m_agentPops.containsKey(agentPop)) {m_agentPops.put(agentPop, agentPop);}
		return getAgentPop(agentPop);
	}
	
	/**
	 * @param _agentPop
	 * @return {@code _agentPop} if it exists. Otherwise return null
	 */
	public MASSPAAgentPop getAgentPop(MASSPAAgentPop _agentPop)
	{
		return m_agentPops.get(_agentPop);
	}
	
	/**
	 * @param _name action count name
	 * @param _loc location of action count
	 * @param _line token line in model file
	 * @throws AssertionError if {@code _name} or {@code _loc} are invalid/undefined
	 * @return MASSPAActionCount {@code _name}&#64;{@code _loc}. If it doesn't exist, it is created and returned.
	 */
	public MASSPAActionCount getActionCount(final String _name, final Location _loc, final int _line)
	{
		String action = m_agents.getAction(_name);
		Location loc = (_loc != null && _loc.equals(AllLocation.getInstance())) ? _loc : m_locations.get(_loc);
		if (action == null || loc == null)
		{
			String err = String.format(Messages.s_COMPILER_ACTIONCOUNT_INVALID_DEFINITION,_name,_loc,_line);
			if (action == null)
			{
				err = String.format(Messages.s_COMPILER_ACTIONCOUNT_MISSING_ACTION,_name,_name,_loc,_line);
			}
			else if (loc == null)
			{
				err = String.format(Messages.s_COMPILER_ACTIONCOUNT_MISSING_LOCATION_DEFINITION,_loc,_name,_loc,_line);
			}
			MASSPALogging.fatalError(err);
			throw new AssertionError(err);
		}
		MASSPAActionCount actionCount = new MASSPAActionCount(action,loc);
		if (!m_actionCounts.containsKey(actionCount)) {m_actionCounts.put(actionCount, actionCount);}
		return getActionCount(actionCount);
	}
	
	/**
	 * @param _actionCount
	 * @return {@code _actionCount} if it exists. Otherwise return null
	 */
	public MASSPAActionCount getActionCount(MASSPAActionCount _actionCount)
	{
		return m_actionCounts.get(_actionCount);
	}
	
	// *****************************************************************
	// "All" Getters
	// *****************************************************************
	/**
	 * Find all agents with all their derivatives
	 * @return tree of agents and their derivative states
	 */
	public Multimap<String, MASSPAComponent> getAllAgents()
	{
		return m_agents.getAgents();
	}
	

	public Set<String> getAllActions()
	{
		return m_agents.getActions();
	}
	
	/**
	 * @return unmodifiable set of all messages sent/received used in the model
	 */
	public Set<MASSPAMessage> getAllMessages()
	{
		return m_agents.getMessages();
	}
	
	/**
	 * @return unmodifiable list containing all locations
	 */
	public Set<Location> getAllLocations()
	{
		return Collections.unmodifiableSet(m_locations.keySet());
	}
		
	/**
	 * @return unmodifiable set of agent populations for all possible (MASSPAComponent, Location)
	 * pairs. Should there be no MASSPAAgentPop for a particular pair a new MASSPAAgentPop
	 * of with initial population size of 0 is created.
	 */
	public Set<MASSPAAgentPop> getAllAgentPopulations()
	{
		for (MASSPAComponent m : m_agents.getComponents())
		{
			for (Location loc : m_locations.values())
			{
				MASSPAAgentPop pop = getAgentPop(m.getName(),loc,-1);
				// Set population size to 0 if it isn't defined
				if (!pop.hasInitialPopulation())
				{
					pop.setInitialPopulation(new IntegerExpression(0));
				}
			}
		}
		return Collections.unmodifiableSet(m_agentPops.keySet());
	}
	
	/**
	 * @return unmodifiable set of actions counts for all possible (Action, Location)
	 * pairs. Should there be no MASSPAActionCount for a particular pair a new MASSPAActionCount
	 * of with initial population size of 0 is created.
	 */
	public Set<MASSPAActionCount> getAllActionCounts()
	{
		for (String action : m_agents.getActions())
		{			
			MASSPAActionCount globalCount = getActionCount(action,AllLocation.getInstance(),-1);
			globalCount.setInitVal(new IntegerExpression(0));
			for (Location loc : m_locations.values())
			{
				MASSPAActionCount count = getActionCount(action,loc,-1);
				
				// Set population size to 0 if it isn't defined
				if (!count.hasInitVal())
				{
					count.setInitVal(new IntegerExpression(0));
				}
				// Add to global action count population
				else
				{
					globalCount.setInitVal(SumExpression.create(globalCount.getInitVal(),count.getInitVal()));
				}
			}
		}
		return Collections.unmodifiableSet(m_actionCounts.keySet());
	}
	
	/**
	 * @param _receiver
	 * @param _msg 
	 * @return all channels for {@code _msg} that have receiving agent population {@code _receiver}
	 */
	public Set<MASSPAChannel> getAllChannels(final MASSPAAgentPop _receiver, final MASSPAMessage _msg)
	{
		Set<MASSPAChannel> l = new HashSet<MASSPAChannel>();
		for (MASSPAChannel chan : m_channels.get(_receiver))
		{
			if (chan.getMsg().equals(_msg))
			{
				l.add(chan);
			}
		}
		return l;
	}

	/**
	 * @param _from population we want to find the outgoing movements of
	 * @return unmodifiable set all movements out of {@code _from}
	 */
	public Set<MASSPAMovement> getAllMovements(final MASSPAAgentPop _from)
	{
		return Collections.unmodifiableSet(m_movements.get(_from));
	}
	
	/**
	 * Find birth process associated with {@code _pop}
	 * @param _pop
	 */
	public MASSPABirth getBirth(MASSPAAgentPop _pop)
	{
		return m_births.get(_pop);
	}
	
	// *****************************************************************
	// Fabric - Generate Locations/InitialPopulations/Channels
	// *****************************************************************
	/**
	 * Add a location to set of locations
	 * @param l location to be added
	 * @param _line token line in model file
	 */
	public void addLocation(final Location _l, final int _line)
	{
		if (_l == null)
		{
			MASSPALogging.error(String.format(Messages.s_COMPILER_LOCATION_NULL_DEFINITION, _line));
			return;
		}
		
		Location loc = m_locations.get(_l);
		if (loc != null)
		{
			MASSPALogging.warn(String.format(Messages.s_COMPILER_LOCATION_DUPLICATE_DEFINITION, loc, _line));
		}
		else
		{
			m_locations.put(_l, new Location(_l));
		}
	}

	/**
	 * Set the initial number of agents for {@code _agentPop}
	 * @param _agentPop specifies population
	 * @param _initPop initial agent population size
	 * @param _line token line in model file
	 */
	public void addInitialAgentPopulation(final MASSPAAgentPop _agentPop, final AbstractExpression _initPop, final int _line)
	{
		if (_agentPop.getLocation().equals(VarLocation.getInstance()))
		{
			MASSPALogging.error(String.format(Messages.s_COMPILER_AGENTPOP_VARLOCATION_INIT_DISTRIBUTION,_agentPop,_line));
			return;
		}
		MASSPAAgentPop agentPop = getAgentPop(_agentPop.getComponentName(), _agentPop.getLocation(), _line);
		if (!agentPop.setInitialPopulation(_initPop))
		{
			MASSPALogging.warn(String.format(Messages.s_COMPILER_AGENTPOP_DUPLICATE_DISTRIBUTION_DEFINITION,_agentPop,_line));
		}
	}
	
	/**
	 * Set the initial value for action count {@code _actionCount}
	 * @param _actionCount the action count
	 * @param _initVal initial action count value
	 * @param _line token line in model file
	 */
	public void addInitialActionCount(final MASSPAActionCount _actionCount, final AbstractExpression _initVal, final int _line)
	{
		if (_actionCount.getLocation().equals(VarLocation.getInstance()) || _actionCount.getLocation().equals(AllLocation.getInstance()))
		{
			MASSPALogging.error(String.format(Messages.s_COMPILER_ACTIONCOUNT_VARALLLOCATION_INITVAL,_actionCount,_line));
			return;
		}
		MASSPAActionCount actionCount = getActionCount(_actionCount.getName(), _actionCount.getLocation(), _line);
		if (!actionCount.setInitVal(_initVal))
		{
			MASSPALogging.warn(String.format(Messages.s_COMPILER_ACTIONCOUNT_INITVAL_DEFINITION_FAILED,_actionCount,_line));
		}
	}
	
	/**
	 * Create a channel between any two agent populations
	 * @param _sender sending agent population
	 * @param _receiver receiving agent population
	 * @param _msg message that is sent
	 * @param _intensity regulate channel quality (0<{@code _intensity}<1 => message loss, 1<={@code _intensity} message multiplication)
	 * @param _line where channel was defined
	 */
	public void addChannel(final MASSPAAgentPop _sender, final MASSPAAgentPop _receiver, final MASSPAMessage _msg, final AbstractExpression _intensity, final int _line)
	{
		// Check if parameters are ok
		new MASSPAChannel(_sender, _receiver, _msg, _intensity);
		
		// Create actual channel
		MASSPAAgentPop sender = getAgentPop(_sender.getComponentName(), _sender.getLocation(), _line);
		MASSPAAgentPop receiver = getAgentPop(_receiver.getComponentName(), _receiver.getLocation(), _line);
		MASSPAMessage msg = m_agents.getMessage(_msg);
		if (msg == null)
		{
			MASSPALogging.warn(String.format(Messages.s_COMPILER_CHANNEL_MESSAGE_UNKNOWN, _msg, _sender, _receiver, _line));
			return;
		}
		MASSPAChannel chan = new MASSPAChannel(sender, receiver, msg, _intensity);
		if (m_channels.get(receiver).contains(chan))
		{
			MASSPALogging.warn(String.format(Messages.s_COMPILER_CHANNEL_DUPLICATE_DEFINTION, chan, _line));
		}
		else
		{
			m_channels.put(receiver,chan);
		}
	}

	/**
	 * Create a new movement
	 * @param _leaveAction action name to record leaving agents
	 * @param _from state which the agent moves out of
	 * @param _enterAction action name to record entering agents
	 * @param _to state which the agent moves to
	 * @param _rate at which an individual agent in {@code _from} moves to {@code _to}
	 * @param _line where movement was defined
	 */
	public void addMovement(final String _leaveAction, final MASSPAAgentPop _from, final String _enterAction, final MASSPAAgentPop _to, final AbstractExpression _rate, final int _line)
	{
		// Check if parameters are ok
		new MASSPAMovement(_leaveAction, _from, _enterAction, _to, _rate);
		
		// Create actual movement
		MASSPAAgentPop from = getAgentPop(_from.getComponentName(), _from.getLocation(), _line);
		MASSPAAgentPop to = getAgentPop(_to.getComponentName(), _to.getLocation(), _line);
		MASSPAMovement move = new MASSPAMovement(_leaveAction, from, _enterAction, to, _rate);
		if (m_movements.get(from).contains(move))
		{
			MASSPALogging.warn(String.format(Messages.s_COMPILER_MOVEMENT_DUPLICATE_DEFINTION, move, _line));
		}
		else
		{
			m_movements.put(from, move);
			m_movementsReverse.put(to, move);
		}
	}

	/**
	 * Create a birth process
	 * @param _action Action is triggered when a new individual is born
	 * @param _pop Population the individual is born into
	 * @param _rate Rate at which individuals are born
	 * @param _line where birth was defined
	 */
	public void addBirth(String _action, MASSPAAgentPop _pop, AbstractExpression _rate, int _line)
	{
		// Check if parameters are ok
		new MASSPABirth(_action, _pop, _rate);
		
		// Create actual birth process
		MASSPAAgentPop pop = getAgentPop(_pop.getComponentName(), _pop.getLocation(), _line);
		MASSPABirth birth = new MASSPABirth(_action, pop, _rate);
		if (m_births.containsKey(pop))
		{
			MASSPALogging.warn(String.format(Messages.s_COMPILER_BIRTH_DUPLICATE_DEFINTION, birth, _line));
		}
		else
		{
			m_births.put(pop, birth);
		}
	}
	
	// *****************************************************************
	// Model relationship information
	// *****************************************************************
	/**
	 * Generate relationship information for neighbouring pairs
	 */
	public void generateNeighbours()
	{
		m_neighbours.clear();
		
		// Create all populations
		getAllAgentPopulations();
		
		// Find neighbours, i.e. other agents this agent can exchange messages with
		for (MASSPAChannel ch : m_channels.values())
		{
			m_neighbours.put(ch.getReceiver(), ch.getSender());
			m_neighbours.put(ch.getSender(), ch.getReceiver());
		}
	}
	
	/**
	 * Find all agent populations that communicate with {@code _pop} via message exchange
	 * @param _pop population we want to find the neighbours of
	 * @return unmodifiable set of all neighbours of {@code _pop}
	 */
	public Set<? extends State> getNeighbours(final MASSPAAgentPop _pop)
	{
		MASSPAAgentPop pop = m_agentPops.get(_pop);
		return Collections.unmodifiableSet(m_neighbours.get(pop));
	}

	/**
	 * Find all successor populations of {@code _pop}
	 * @param _pop population whose successor populations we wish to find
	 * @return set of all reachable successor populations of {@code _pop}
	 */
	public Set<? extends State> getSuccessorPopulations(final MASSPAAgentPop _pop)
	{
		return getSuccessorPopulations(_pop, new HashSet<MASSPAAgentPop>());
	}
	
	private Set<? extends State> getSuccessorPopulations(final MASSPAAgentPop _pop, final Set<MASSPAAgentPop> _pops)
	{
		// Local successor populations
		for (MASSPAComponent comp : m_agents.getSuccessorStates(m_agentPops.get(_pop).getComponent()))
		{
			MASSPAAgentPop pop = m_agentPops.get(new MASSPAAgentPop(comp,_pop.getLocation()));
			if (_pops.add(pop))
			{
				getSuccessorPopulations(pop, _pops);
			}
		}
		
		// Successor through movement
		for (MASSPAMovement move : m_movements.get(_pop))
		{
			if (_pops.add(move.getTo()))
			{
				getSuccessorPopulations(move.getTo(), _pops);
			}
		}
		
		return _pops;
	}

	/**
	 * Find all predecessor populations of {@code _pop}
	 * @param _pop population whose predecessor populations we wish to find
	 * @return set of all reachable predecessor populations of {@code _pop}
	 */
	public Set<? extends State> getPredecessorPopulations(final MASSPAAgentPop _pop)
	{
		return getPredecessorPopulations(_pop, new HashSet<MASSPAAgentPop>());
	}
	
	private Set<? extends State> getPredecessorPopulations(final MASSPAAgentPop _pop, final Set<MASSPAAgentPop> _pops)
	{
		// Local predecessor populations
		for (MASSPAComponent comp : m_agents.getPredecessorStates(m_agentPops.get(_pop).getComponent()))
		{
			MASSPAAgentPop pop = m_agentPops.get(new MASSPAAgentPop(comp,_pop.getLocation()));
			if (_pops.add(pop))
			{
				getPredecessorPopulations(pop, _pops);
			}
		}
		
		// Predecessor through movement
		for (MASSPAMovement move : m_movementsReverse.get(_pop))
		{
			if (_pops.add(move.getFrom()))
			{
				getPredecessorPopulations(move.getFrom(), _pops);
			}
		}
		
		return _pops;
	}
	
	/**
	 * Find all populations belonging to scope of agent with state {@code _pop} in location {@code _pop.getLocation}
	 * @param _pop population whose local predecessor and successor populations we wish to find
	 * @return set of all reachable local predecessor and successor populations of {@code _pop}
	 */
	public Set<? extends State> getAgentStatePopulations(MASSPAAgentPop _pop)
	{
		Set<MASSPAAgentPop> pops = new HashSet<MASSPAAgentPop>();
		for (MASSPAComponent pop : m_agents.getAgentStates(m_agentPops.get(_pop).getComponent()))
		{
			pops.add(m_agentPops.get(new MASSPAAgentPop(pop,_pop.getLocation())));
		}
		return pops;
	}
}
