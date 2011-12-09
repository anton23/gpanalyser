package uk.ac.imperial.doc.masspa.representation.components;

import java.util.Set;

import uk.ac.imperial.doc.masspa.language.Messages;

/***
 * This class represents a type of MASSPA message that
 * can be exchanged between two MASSPA agent populations.
 * 
 * @author Chris Guenther
 */
public class MASSPAMessage implements Comparable<MASSPAMessage>
{
	public static String MsgNamesToString(final Set<MASSPAMessage> _msgs)
	{
		String s="";
		for (MASSPAMessage i:_msgs) {if(s != ""){s+=",";} s+= i;}
		return "["+s+"]";
	}
	
	// Fields
	private final String m_msg;
	
	// Constructor
	public MASSPAMessage(final String _msg)
	{
		m_msg = _msg;
		if (m_msg == null) {throw new AssertionError(Messages.s_MASSPA_MESSAGE_NULL_MSG_NAME);}
	}
	
	// Getter
	public String getMsg()
	{
		return m_msg;
	}
	
	//***********************************************
	// Implementation of Comparable<MASSPAMessage>
	//***********************************************
	@Override
	public int compareTo(final MASSPAMessage _msg)
	{
		if (this == _msg) {return 0;}
		return m_msg.compareTo(_msg.m_msg);
	}
	
	//*******************************************
	// Object overrides
	//*******************************************
	@Override
	public String toString()
	{
		return m_msg;
	}
	
	@Override
	public int hashCode()
	{
		return m_msg.hashCode();
	}
	
	@Override
	public boolean equals(final Object _o)
	{
		if (this == _o) {return true;}
		return (_o instanceof MASSPAMessage && m_msg.equals(((MASSPAMessage)_o).m_msg));
	}		
}
