package uk.ac.imperial.doc.masspa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.language.Messages;

/***
 * Class representing a send prefix.
 * 
 * @author Chris Guenther
 */
public class SendPrefix extends MessagePrefix
{
	private final AbstractExpression m_nofMsgsSent;
	
	public SendPrefix(final String _action, final AbstractExpression _rate, final MASSPAMessage msg, final AbstractExpression _nofMsgsSent, final MASSPAComponent _continuation)
	{
		super(_action,_rate,msg,_continuation);
		m_nofMsgsSent = _nofMsgsSent;
		if (m_nofMsgsSent == null) {throw new AssertionError(Messages.s_COMPILER_SEND_PREFIX_NULL_NOF_MSG_SENT);}
	}

	/**
	 * @return nof messages that will be sent as part of the transition
	 */
	public AbstractExpression getNofMsgsSent()
	{
		return m_nofMsgsSent;
	}
		
	//***************************************
	// Object overwrites
	//***************************************
	@Override
	public String toString()
	{
		return super.toString().replace(")", "," + m_nofMsgsSent + ")").replace("[?!](", "!(");
	}

	@Override
	public boolean equals(final Object _o)
	{
		if (super.equals(_o) == false) {return false;}
		if (!(_o instanceof SendPrefix)) return false;
		SendPrefix asPrefix = (SendPrefix) _o;
		return m_nofMsgsSent.equals(asPrefix.m_nofMsgsSent);
	}
}
