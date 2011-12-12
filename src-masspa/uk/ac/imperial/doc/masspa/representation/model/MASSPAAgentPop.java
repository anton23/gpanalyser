package uk.ac.imperial.doc.masspa.representation.model;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.language.Labels;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.pctmc.representation.State;

/***
 * This class defines a MASSPA Agent population. A MASSPAAgentPop has
 * a location, a type identifier (agent state) and an initial distribution.
 * 
 * @author Chris Guenther
 */
public class MASSPAAgentPop extends State implements Comparable<MASSPAAgentPop>
{
	private MASSPAComponent m_component;
	private Location m_loc;
	private AbstractExpression m_initPop=null;

	/**
	 * 
	 */
	protected MASSPAAgentPop()
	{
	}
	
	/**
	 * @param _component agent state name
	 * @param _loc location of agent
	 */
	public MASSPAAgentPop(final MASSPAComponent _component, final Location _loc)
	{
		setComponent(_component);
		if (getComponent() == null) {throw new AssertionError(Messages.s_COMPILER_AGENTPOP_NULL_COMPONENT);}
		setLocation(_loc);
		if (getLocation() == null) {throw new AssertionError(Messages.s_COMPILER_AGENTPOP_NULL_LOCATION);}
	}

	//**********************************************
	// Getters/Setters
	//**********************************************
	protected void setComponent(MASSPAComponent _component) {m_component=_component;}
	public MASSPAComponent getComponent() {return m_component;}
	protected void setLocation(Location _loc) {m_loc=_loc;}
	public Location getLocation() {return m_loc;}
	public boolean hasInitialPopulation() {return m_initPop != null;}
	public AbstractExpression getInitialPopulation() {return m_initPop;}
	
	/**
	 * Set {@code _initPop} size for this population. Population size can only
	 * be set once.
	 * 
	 * @param _initPop
	 * @return true iff the initial population size was set to {@code _initPop}
	 */
	public boolean setInitialPopulation(AbstractExpression _initPop)
	{
		if (hasInitialPopulation()) {return false;}
		m_initPop = _initPop;
		return true;
	}
	
	/**
	 * @return ConstComponent.getName() or null if ConstComponent is null
	 */
	public String getComponentName()
	{
		return m_component.getName();
	}
	
	/**
	 * @return ConstComponent.getName() @ location
	 */
	public String getName()
	{
		return m_component.getName() + m_loc.toString();
	}
	
	/**
	 * @return ConstComponent.getName() @ location = initPop
	 */
	public String getNameAndInitPop()
	{
		return  getName() + "=" + m_initPop;
	}
	
	//*******************************************
	// Implement Comparable interface
	//*******************************************
	@Override
	public int compareTo(MASSPAAgentPop _pop)
	{
		int diff=m_loc.compareTo(_pop.getLocation());
		return (diff != 0) ? diff : getComponentName().compareTo(_pop.getComponentName());
	}
	
	//*******************************************
	// Object overrides
	//*******************************************
	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public int hashCode()
	{
		return m_component.hashCode() + m_loc.hashCode();
	}
		
	@Override
	public boolean equals(final Object _o)
	{
		if (this == _o) {return true;}
		if (!(_o instanceof MASSPAAgentPop)) {return false;}
		MASSPAAgentPop agentPop = (MASSPAAgentPop) _o;
		return m_component.equals(agentPop.m_component) && m_loc.equals(agentPop.m_loc);
	}
		
	/**
	 * This class is provides a constant placeholder that
	 * symbolises the entire population of an agent (i.e.
	 * the agent population including all successor and
	 * predecessor states)
	 * @author Chris Guenther
	 */
	private static class ConstRecvAgentPop extends ConstantExpression
	{
		public ConstRecvAgentPop()
		{
			super(Labels.s_RECEIVING_AGENT_POP);
		}
	}
	public final static ConstRecvAgentPop s_constRecvAgentPop = new ConstRecvAgentPop();
}
