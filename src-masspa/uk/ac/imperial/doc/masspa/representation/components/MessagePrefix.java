package uk.ac.imperial.doc.masspa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.language.Messages;

/***
 * Abstract class representing a message prefix.
 * Base class for send/receive prefixes.
 * 
 * @author Chris Guenther
 */
public abstract class MessagePrefix extends Prefix
{
	private final MASSPAMessage m_msg;

	public MessagePrefix(final String _action, final AbstractExpression _rate, final MASSPAMessage msg, final MASSPAComponent _continuation)
	{
		super(_action,_rate,_continuation);
		m_msg = msg;
		if (m_msg == null) {throw new AssertionError(Messages.s_MESSAGE_PREFIX_NULL_MSG);}
	}
	
	/**
	 * @return name of the message we receive or send.
	 */
	public MASSPAMessage getMsg()
	{
		return m_msg;
	}

	//***************************************
	// Object overwrites
	//***************************************
	@Override
	public String toString()
	{
		return super.toString().replace(")", "," + m_msg + ")").replace("(", "[?!](");
	}

	@Override
	public boolean equals(final Object _o)
	{
		if (super.equals(_o) == false) {return false;}
		if (!(_o instanceof MessagePrefix)) return false;
		MessagePrefix asPrefix = (MessagePrefix) _o;
		return m_msg.equals(asPrefix.m_msg);
	}
}
