package uk.ac.imperial.doc.masspa.representation.model;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.pctmc.representation.State;

/**
 * A MASSPA class representing an action count.
 * This can be used for impulse reward computation.
 * 
 * @author Chris Guenther
 */
public class MASSPAActionCount extends State
{
	private String m_name;
	private Location m_loc;
	private AbstractExpression m_initVal;
	
	public MASSPAActionCount(final String _name, final Location _loc)
	{
		if (_name == null) {throw new AssertionError(Messages.s_ACTIONCOUNT_NULL_NAME);}
		m_name = "#"+_name.replace("#", "");
		m_loc = (_loc == null) ? AllLocation.getInstance() : _loc;
	}

	//**********************************************
	// Getters/Setters
	//**********************************************
	public final String getName()
	{
		return m_name;
	}
	
	public final Location getLocation()
	{
		return m_loc;
	}
	
	public final AbstractExpression getInitVal()
	{
		return m_initVal;
	}
	
	public boolean hasInitVal()
	{
		return (m_initVal != null);
	}

	public boolean setInitVal(AbstractExpression _e)
	{
		if (_e == null) {throw new AssertionError(String.format(Messages.s_ACTIONCOUNT_NULL_INITVAL,toString()));}
		m_initVal = _e;
		return hasInitVal();
	}
	
	//*******************************************
	// Object overrides
	//*******************************************
	@Override 
	public String toString() {return m_name+m_loc;}
	
	@Override
	public int hashCode() {return m_name.hashCode();}
	
	@Override
	public boolean equals(Object _o)
	{
		if (this == _o) {return true;}
		if (!(_o instanceof MASSPAActionCount)) {return false;}
		return m_name.equals(((MASSPAActionCount)_o).m_name) && m_loc.equals(((MASSPAActionCount)_o).m_loc);
	}
}