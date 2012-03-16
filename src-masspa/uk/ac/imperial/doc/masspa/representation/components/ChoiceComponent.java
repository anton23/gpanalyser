package uk.ac.imperial.doc.masspa.representation.components;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import uk.ac.imperial.doc.masspa.language.Messages;

/***
 * Representation of choice. Differs from the formal definition
 * in that it can only contain prefixes and not constants, i.e.
 * (a,r).P1 + P2 is not allowed.
 * 
 * @author Chris Guenther
 */
public class ChoiceComponent extends MASSPAComponent
{
	private final List<Prefix> m_choices;
	
	public ChoiceComponent(final List<Prefix> _choices)
	{
		super("");
		if (_choices == null) {throw new AssertionError(Messages.s_COMPILER_CHOICE_COMPONENT_NULL_CHOICES);}
		m_choices = Collections.unmodifiableList(_choices);
	}

	/**
	 * @return unmodifiable list of choices
	 */
	public List<Prefix> getChoices()
	{
		return m_choices;
	}
	
	@Override
	public boolean matchPattern(final MASSPAComponent _pattern)
	{
		if (_pattern instanceof AnyComponent) {return true;}
		return equals(_pattern);
	}

	@Override
	protected void getDerivativeStates(final Set<MASSPAComponent> _known)
	{
		for (Prefix p : m_choices)
		{
			MASSPAComponent c = p.getContinuation();
			if (!_known.contains(c))
			{
				_known.add(c);
				c.getDerivativeStates(_known);
			}
		}
	}
	
	//***************************************
	// Object overwrites
	//***************************************
	@Override
	public String toString()
	{
		String ret="";
		for (Prefix p : m_choices) {ret += (ret.isEmpty()) ? p.toString() : " + " + p.toString();}
		return ret;
	}
	
	@Override
	public int hashCode()
	{
		return m_choices.hashCode();
	}

	@Override
	public boolean equals(final Object _o)
	{
		if (this == _o) {return true;}
		if (!(_o instanceof ChoiceComponent)) {return false;}
		ChoiceComponent cp = (ChoiceComponent) _o;
		if (m_choices.size() != cp.m_choices.size()) {return false;}
		for (int i=0; i < m_choices.size(); i++)
		{
			if (!m_choices.get(i).equals(cp.m_choices.get(i))) {return false;}
		}
		return true;
	}
}
