package uk.ac.imperial.doc.masspa.representation.components;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.language.Messages;

/***
 * Class representing a prefix
 * 
 * @author Chris Guenther 
 */
public class Prefix
{
	private final String m_action;
	private final AbstractExpression m_rate;
	private final MASSPAComponent m_continuation;
	
	public Prefix(final String _action, final String _rate, final MASSPAComponent _continuation)
	{
		this(_action, new ConstantExpression(_rate), _continuation);
		if (_rate == null) {throw new AssertionError(Messages.s_PREFIX_NULL_RATE);}
	}

	public Prefix(final String _action, final AbstractExpression _rate, final MASSPAComponent _continuation)
	{
		m_action = _action;
		if (m_action == null) {throw new AssertionError(Messages.s_PREFIX_NULL_ACTION);}
		m_rate = _rate;
		if (m_rate == null) {throw new AssertionError(Messages.s_PREFIX_NULL_RATE);}
		m_continuation = _continuation;
		if (m_continuation == null) {throw new AssertionError(Messages.s_PREFIX_NULL_CONTINUATION);}
	}
	
	/**
	 * @return name of the action that triggers this transition
	 */
	public String getAction()
	{
		return m_action;
	}
	
	/**
	 * @return exponential transition rate parameter
	 */
	public AbstractExpression getRate()
	{
		return m_rate;
	}

	/**
	 * @return the successor component
	 */
	public MASSPAComponent getContinuation()
	{
		return m_continuation;
	}

	//***************************************
	// Object overwrites
	//***************************************
	@Override
	public String toString()
	{
		return "(" + ((!m_action.isEmpty()) ? m_action + "," : "") + m_rate + ")." + m_continuation.toString();
	}
	
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(final Object _o)
	{
		if (this == _o) {return true;}
		if (!(_o instanceof Prefix)) return false;
		Prefix asPrefix = (Prefix) _o;
		return m_action.equals(asPrefix.m_action) &&
			   m_rate.equals(asPrefix.m_rate) &&
			   m_continuation.equals(asPrefix.m_continuation);
	}	
}
