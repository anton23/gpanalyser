package uk.ac.imperial.doc.masspa.representation.components;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;

/***
 * This class is used to create component objects from
 * the AST. It also determines all messages in the tree
 * and can be used to validate the component definitions.
 * Basically this class holds all the non-spatial agent
 * information of the MASSPA model.
 * 
 * @author Chris Guenther
 */
public class MASSPAAgents
{
	private final Map<String,String> m_actions;
	private final Map<MASSPAMessage,MASSPAMessage> m_msgs;
	private final Map<MASSPAComponent,String> m_scopes;
	private final Map<String,MASSPAComponent> m_components;
	private final HashMultimap<MASSPAComponent, MASSPAComponent> m_successorStates;
	private final HashMultimap<MASSPAComponent, MASSPAComponent> m_predecessorStates;
	
	public MASSPAAgents()
	{
		MASSPALogging.info(Messages.s_AGENT_DEFINITION_ANALYSIS);
		m_actions = new HashMap<String,String>();
		m_msgs = new HashMap<MASSPAMessage,MASSPAMessage>();
		m_scopes = new HashMap<MASSPAComponent,String>();
		m_components = new HashMap<String,MASSPAComponent>();
		
		// Relationship information
		m_successorStates = HashMultimap.create();
		m_predecessorStates = HashMultimap.create();
		
		// Create special components
		getAnyComponent();
		getStopComponent();
	}
	
	// *****************************************************************
	// Actions
	// *****************************************************************
	/***
	 * @return  unmodifiable set of known actions
	 */
	public final Set<String> getActions()
	{
		return Collections.unmodifiableSet(m_actions.keySet());
	}

	/**
	 * @param _name
	 * @return Action with name {@code _name} or null if it doesn't exist.
	 */
	public final String getAction(final String _name)
	{
		return m_actions.get(_name);
	}
	
	/**
	 * @param _name
	 * Register action with name {@code _name} if it doesn't exist yet.
	 */
	public void addAction(final String _name)
	{
		if (getAction(_name) != null) {return;}
		m_actions.put(_name,_name);
	}
	
	// *****************************************************************
	// Messages
	// *****************************************************************
	/***
	 * @return  unmodifiable set of known messages
	 */
	public final Set<MASSPAMessage> getMessages()
	{
		return Collections.unmodifiableSet(m_msgs.keySet());
	}
	
	/**
	 * @param _msg
	 * @return MASSPAMessage with name {@code _msg} or null if it doesn't exist.
	 */
	public final MASSPAMessage getMessage(final String _msg)
	{
		return getMessage(new MASSPAMessage(_msg));
	}
	
	/**
	 * @param _msg
	 * @return MASSPAMessage with name {@code _msg} or null if it doesn't exist.
	 */
	public final MASSPAMessage getMessage(final MASSPAMessage _msg)
	{
		return m_msgs.get(_msg);
	}
	
	/**
	 * @param _msg
	 * Register message {@code _msg} if it doesn't exist yet.
	 */
	public void addMessage(final MASSPAMessage _msg)
	{
		if (getMessage(_msg) != null) {return;}
		m_msgs.put(_msg, _msg);
	}
	
	// *****************************************************************
	// Components
	// *****************************************************************
	/**
	 * @return unmodifiable collection of components
	 */
	public final Collection<MASSPAComponent> getComponents()
	{
		return Collections.unmodifiableCollection(m_components.values());
	}
	
	/**
	 * @param _name
	 * @return Component with name {@code _name} or null if it doesn't exist.
	 */
	public final MASSPAComponent getComponent(final String _name)
	{
		return m_components.get(_name);
	}
		
	/***
	 * Get ConstComponent by {@code _name}. If ConstComponent doesn't exist
	 * as new ConstComponent({@code _name}) is created.
	 * @param _scope of the ConstComponent
	 * @param _name name of the ConstComponent
	 * @param _line where ConstComponent is referenced
	 * @throws AssertionError if {@code _name} is associated with a non-ConstComponent
	 * @return ConstComponent
	 */
	public final ConstComponent getConstComponent(final String _scope, final String _name, final int _line)
	{
		if (_name == null || _scope == null) {return null;}
		MASSPAComponent c = m_components.get(_name);
		if (c != null)
		{
			if (c instanceof ConstComponent)
			{
				ConstComponent cc = (ConstComponent)c;
				if (!m_scopes.get(cc).equals(_scope))
				{
					String err = String.format(Messages.s_AGENT_STATE_INVALID_SCOPE,_name,m_scopes.get(cc),_scope,_line);
					MASSPALogging.fatalError(err);
					throw new AssertionError(err);
				}
				return cc;
			}
			else
			{
				String err = String.format(Messages.s_AGENT_STATE_NAME_INVALID,_name,_line);
				MASSPALogging.fatalError(err);
				throw new AssertionError(err);
			}
		}
		
		// Insert new component
		c = new ConstComponent(_name);
		m_scopes.put(c, _scope);
		m_components.put(_name, c);
		return (ConstComponent)c;
	}

	/**
	 * @param _choices
	 * @return create ChoiceComponent from list {@code _choices}
	 */
	public final MASSPAComponent getChoiceComponent(final List<Prefix> _choices)
	{
		if (_choices == null) {return null;}
		return new ChoiceComponent(_choices);
	}
	
	/**
	 * @return global AnyComponent
	 */
	public final MASSPAComponent getAnyComponent()
	{
		AnyComponent any = new AnyComponent();
		if (m_components.containsKey(any.getName()))
		{
			return m_components.get(any.getName());
		}
		m_components.put(any.getName(), any);
		return any;
	}
	
	/**
	 * @return global StopComponent
	 */
	public final MASSPAComponent getStopComponent()
	{
		StopComponent stop = new StopComponent();
		if (m_components.containsKey(stop.getName()))
		{
			return m_components.get(stop.getName());
		}
		m_components.put(stop.getName(), stop);
		return stop;
	}

	// *****************************************************************
	// Component validation
	// *****************************************************************
	/***
	 * Validate all agent definitions
	 * @return true iff all definitions are legal
	 */
	public boolean validateComponentDefinitions()
	{
		// Generate relationships
		generateRelationships();
		
		// Keep track of sent/received message types
		Set<MASSPAMessage> msgNamesSend = null;
		Set<MASSPAMessage> msgNamesReceive = null;
		msgNamesSend = new HashSet<MASSPAMessage>();
		msgNamesReceive = new HashSet<MASSPAMessage>();
		
		for (MASSPAComponent m : m_components.values())
		{
			MASSPAComponent def = m.getDefinition();
			if (def == null && m instanceof ConstComponent)
			{
				String err = String.format(Messages.s_AGENT_STATE_UNDEFINED, m.getName());
				MASSPALogging.fatalError(err);
				throw new AssertionError(err);
			}
			else if (def != null)
			{
				if (def instanceof ConstComponent)
				{
					String err = String.format(Messages.s_AGENT_STATE_DEFINITION_NOT_ALLOWED, m.getName(), def.getName());
					MASSPALogging.fatalError(err);
					throw new AssertionError(err);
				}
				else if (def instanceof ChoiceComponent)
				{
					for (Prefix p : ((ChoiceComponent)def).getChoices())
					{
						if (p==null)
						{
							String err = String.format(String.format(Messages.s_AGENT_STATE_INVALID_DEFINITION, m.getName()));
							MASSPALogging.fatalError(err);
							throw new AssertionError(err);
						}
						else if (p instanceof MessagePrefix)
						{
							MessagePrefix mp = (MessagePrefix) p;
							if (p instanceof SendPrefix)
							{
								msgNamesSend.add(mp.getMsg());
							}
							else if (p instanceof ReceivePrefix)
							{
								msgNamesReceive.add(mp.getMsg());
							}
						}
					}
				}
				else
				{
					// Handle stop/any components
				}
			}
		}
		
		// Check if any message types have no receiver
		Set<MASSPAMessage> msgNoReceiver = new HashSet<MASSPAMessage>(msgNamesSend);
		msgNoReceiver.removeAll(msgNamesReceive);
		if (msgNoReceiver.size() != 0)
		{
			MASSPALogging.warn(String.format(Messages.s_MESSAGES_HAVE_NO_RECEIVER, MASSPAMessage.MsgNamesToString(msgNoReceiver)));
		}
		// Check if any message types have no sender
		Set<MASSPAMessage> msgNoSender = new HashSet<MASSPAMessage>(msgNamesReceive);
		msgNoSender.removeAll(msgNamesSend);
		if (msgNoSender.size() != 0)
		{
			MASSPALogging.warn(String.format(Messages.s_MESSAGES_HAVE_NO_SENDER, MASSPAMessage.MsgNamesToString(msgNoSender)));
		}
		
		return true;
	}
	
	// *****************************************************************
	// Component relationship information
	// *****************************************************************
	/**
	 * Generate relationship information for
	 * predecessor/successor pairs
	 */
	public void generateRelationships()
	{
		// Create a mapping that makes it easier to find all successor/predecessor
		// states for a given MASSPA component
		Multimap<String, MASSPAComponent> agents = getAgents();
		for (Collection<MASSPAComponent> agentStates : agents.asMap().values())
		{
			Set<MASSPAComponent> agentStatesSet = new HashSet<MASSPAComponent>(agentStates);
			// Each collection of agentState contains a list of all state
			// a particular agent can be in. We use this information for generate
			// predecessor/successor relationships
			for (MASSPAComponent mc : agentStatesSet)
			{
				for (MASSPAComponent mc2 : mc.getDerivativeStates())
				{
					m_successorStates.put(mc, mc2);
					m_predecessorStates.put(mc2, mc);
				}
			}
		}
	}
	
	/**
	 * @param _mc
	 * @return unmodifiable set of all successor states of {@code _mc}
	 */
	public final Set<MASSPAComponent> getSuccessorStates(final MASSPAComponent _mc)
	{
		return Collections.unmodifiableSet(m_successorStates.get(_mc));
	}
	
	/**
	 * @param _mc
	 * @return unmodifiable set of all predecessor states of {@code _mc}
	 */
	public final Set<MASSPAComponent> getPredecessorStates(final MASSPAComponent _mc)
	{
		return  Collections.unmodifiableSet(m_predecessorStates.get(_mc));
	}
	
	/**
	 * @param _mc
	 * @return set of all agent states of agent with state {@code _mc}
	 */
	public final Set<MASSPAComponent> getAgentStates(final MASSPAComponent _mc)
	{
		Set<MASSPAComponent> ret = new HashSet<MASSPAComponent>();
		ret.addAll(getPredecessorStates(_mc));
		ret.addAll(getSuccessorStates(_mc));
		return ret;
	}
	
	/**
	 * @return tree of agents and their states
	 */
	public final Multimap<String, MASSPAComponent> getAgents()
	{
		Multimap<String, MASSPAComponent> tree = HashMultimap.create();
		for (MASSPAComponent m : m_components.values())
		{
			if (!(m instanceof ConstComponent)) {continue;}

			String scopeName =  m_scopes.get(m);
			tree.put(scopeName, m);
		}
		return tree;
	}
}
