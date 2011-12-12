package uk.ac.imperial.doc.masspa.representation.model;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.language.Messages;

/***
 * This class defines a birth process for a MASSPAAgent population
 * 
 * @author Chris Guenther
 */
public class MASSPABirth implements Comparable<MASSPABirth>
{
	private String m_action;
	private MASSPAAgentPop m_pop;
	private AbstractExpression m_rate;
	
	/**
	 *
	 */
	protected MASSPABirth()
	{	
	}
	
	/**
	 * Create a birth process
	 * @param _action Action is triggered when a new individual is born
	 * @param _pop Population the individual is born into
	 * @param _rate Rate at which individuals are born
	 */
	public MASSPABirth(final String _action, final MASSPAAgentPop _pop, final AbstractExpression _rate)
	{	
		setAction(_action);
		if (getAction() == null) {throw new AssertionError(Messages.s_COMPILER_BIRTH_NULL_ACTION);}
		setPop(_pop);
		if (getPop() == null) {throw new AssertionError(Messages.s_COMPILER_BIRTH_NULL_POP);}
		setRate(_rate);
		if (getRate() == null) {throw new AssertionError(Messages.s_COMPILER_BIRTH_NULL_RATE);}
	}

	// Getters/Setters
	protected void setAction(String _a) {m_action=_a;}
	public String getAction() {return m_action;}
	protected void setPop(MASSPAAgentPop _p) {m_pop=_p;}
	public MASSPAAgentPop getPop() {return m_pop;}
	public void setRate(AbstractExpression _rate) {m_rate =_rate;}
	public AbstractExpression getRate() {return m_rate;}
	
	//*******************************************
	// Implement Comparable interface
	//*******************************************
	@Override
	public int compareTo(MASSPABirth _move)
	{
		return getPop().compareTo(_move.getPop());
	}
		
	//*******************************************
	// Object overrides
	//*******************************************
	@Override
	public String toString()
	{
		return "Birth(" + m_action + "," + m_pop.getName() + ") = " + m_rate;
	}
	
	@Override
	public int hashCode()
	{
		return m_pop.hashCode();
	}	
	
	@Override
	public boolean equals(final Object _o)
	{
		if (this == _o) {return true;}
		if (!(_o instanceof MASSPABirth)) {return false;}
		MASSPABirth asBirth = (MASSPABirth) _o;
		return m_pop.equals(asBirth.m_pop);
	}
}
