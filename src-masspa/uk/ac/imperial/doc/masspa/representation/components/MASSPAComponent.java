package uk.ac.imperial.doc.masspa.representation.components;

import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.doc.masspa.language.Messages;

/***
 * This abstract class describes MASSPAComponent definitions.
 * MASSPAComponents are the building blocks of MASSPA agents.
 * 
 * @author Chris Guenther
 */
public abstract class MASSPAComponent
{
	private final String m_name;

	public MASSPAComponent(final String _name)
	{
		m_name = _name;
		if (m_name == null) {throw new AssertionError(Messages.s_MASSPA_COMPONENT_NULL_NAME);}
	}
	
	public String getName()
	{
		return m_name;
	}
	
	/**
	 * Check if component matches @{code _pattern}. Pattern matching
	 * is useful when describing passage times boundaries.
	 * 
	 * @param _pattern to match
	 * @return true iff this component matches _pattern 
	 */
	public boolean matchPattern(final MASSPAComponent _pattern)
	{
		return false;
	}

	/**
	 * Returns the derivative states of this component. Given a set of known
	 * states to avoid infinite recursion.
	 * 
	 * @return set of already derivative states.
	 */
	public Set<MASSPAComponent> getDerivativeStates()
	{
		Set<MASSPAComponent> knownStates = new HashSet<MASSPAComponent>();
		getDerivativeStates(knownStates);
		return knownStates;
	}
	
	/**
	 * Adds any derivative state of states not already in {@code _known}
	 * to _known
	 * 
	 * @param _known set of states that have already been explored
	 */
	protected void getDerivativeStates(final Set<MASSPAComponent> _known)
	{
	}

	/**
	 * @return true iff component is defined
	 */
	public boolean hasDefinition()
	{
		return getDefinition() != null;
	}
	
	/***
	 * @return the definition of a compoment
	 */
	public MASSPAComponent getDefinition()
	{
		return null;
	}

	//***************************************
	// Object overwrites
	//***************************************
	@Override
	public String toString()
	{
		return m_name;
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
		if (!(_o instanceof MASSPAComponent)) {return false;}
		return m_name.equals(((MASSPAComponent)_o).m_name);
	}
}
