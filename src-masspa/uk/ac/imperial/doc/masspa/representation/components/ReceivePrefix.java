package uk.ac.imperial.doc.masspa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.masspa.language.Messages;

/***
 * Class representing a receive prefix.
 * 
 * @author Chris Guenther
 */
public class ReceivePrefix extends MessagePrefix
{
	private final AbstractExpression m_acceptanceProb;

	public ReceivePrefix(final String _action, final MASSPAMessage msg, final AbstractExpression _acceptanceProb, final MASSPAComponent _continuation)
	{
		super(_action,new IntegerExpression(1),msg,_continuation);
		m_acceptanceProb = _acceptanceProb;
		if (m_acceptanceProb == null) {throw new AssertionError(Messages.s_RECEIVE_PREFIX_NULL_ACC_PROB);}
	}

	/**
	 * @return probability of accepting a message
	 */
	public AbstractExpression getAcceptanceProbability()
	{
		return m_acceptanceProb;
	}

	//***************************************
	// Object overwrites
	//***************************************
	@Override
	public String toString()
	{
		return super.toString().replace(")", "," + m_acceptanceProb + ")").replace("[?!](", "?(");
	}

	@Override
	public boolean equals(final Object _o)
	{
		if (super.equals(_o) == false) {return false;}
		if (!(_o instanceof ReceivePrefix)) return false;
		ReceivePrefix asPrefix = (ReceivePrefix) _o;
		return m_acceptanceProb.equals(asPrefix.m_acceptanceProb);
	}
}
