package uk.ac.imperial.doc.masspa.representation.model;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.language.Messages;

/***
 * This class defines a movement from one MASSPAAgent population to another.
 * There is a unique movement for each pair of agent populations.
 * The rate of the movement describes the rate at which an individual agent
 * moves from {@code m_from} to {@code m_to}
 * @author Chris Guenther
 */
public class MASSPAMovement implements Comparable<MASSPAMovement>
{
	private String m_leaveAction;
	private MASSPAAgentPop m_from;
	private String m_enterAction;
	private MASSPAAgentPop m_to;
	private AbstractExpression m_rate;
	
	/**
	 *
	 */
	protected MASSPAMovement()
	{	
	}
	
	/***
	 * Create a new movement between any two agent populations
	 * @param _leaveAction action name to record leaving agents
	 * @param _from state which the agent moves out of
	 * @param _enterAction action name to record entering agents
	 * @param _to state which the agent moves to
	 * @param _rate at which an individual agent in {@code _from} moves to {@code _to}
	 */
	public MASSPAMovement(final String _leaveAction, final MASSPAAgentPop _from, final String _enterAction, final MASSPAAgentPop _to, final AbstractExpression _rate)
	{	
		setLeaveAction(_leaveAction);
		if (getLeaveAction() == null) {throw new AssertionError(Messages.s_MOVEMENT_NULL_LEAVE_ACTION);}
		setFrom(_from);
		if (getFrom() == null) {throw new AssertionError(Messages.s_MOVEMENT_NULL_FROM);}
		setEnterAction(_enterAction);
		if (getEnterAction() == null) {throw new AssertionError(Messages.s_MOVEMENT_NULL_ENTER_ACTION);}
		setTo(_to);
		if (getTo() == null) {throw new AssertionError(Messages.s_MOVEMENT_NULL_TO);}
		setRate(_rate);
		if (getRate() == null) {throw new AssertionError(Messages.s_MOVEMENT_NULL_RATE);}
	}

	// Getters/Setters
	protected void setFrom(MASSPAAgentPop _f) {m_from=_f;}
	public MASSPAAgentPop getFrom() {return m_from;}
	protected void setLeaveAction(String _la) {m_leaveAction=_la;}
	public String getLeaveAction() {return m_leaveAction;}
	protected void setTo(MASSPAAgentPop _t) {m_to=_t;}
	public MASSPAAgentPop getTo() {return m_to;}
	protected void setEnterAction(String _ea) {m_enterAction=_ea;}
	public String getEnterAction() {return m_enterAction;}
	public void setRate(AbstractExpression _rate) {m_rate =_rate;}
	public AbstractExpression getRate() {return m_rate;}
	
	//*******************************************
	// Implement Comparable interface
	//*******************************************
	@Override
	public int compareTo(MASSPAMovement _move)
	{
		int diff=getFrom().compareTo(_move.getFrom());
		return (diff != 0) ? diff : getTo().compareTo(_move.getTo());
	}
		
	//*******************************************
	// Object overrides
	//*******************************************
	@Override
	public String toString()
	{
		return "Move(" + m_leaveAction + "," + m_from.getName() + "," + m_enterAction + "," + m_from.getName() + ") = " + m_rate;
	}
	
	@Override
	public int hashCode()
	{
		return m_from.hashCode()+m_to.hashCode();
	}	
	
	@Override
	public boolean equals(final Object _o)
	{
		if (this == _o) {return true;}
		if (!(_o instanceof MASSPAMovement)) {return false;}
		MASSPAMovement move = (MASSPAMovement) _o;
		return m_from.equals(move.m_from) && m_to.equals(move.m_to);
	}
}
