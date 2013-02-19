package uk.ac.imperial.doc.masspa.representation.components;

import uk.ac.imperial.doc.masspa.language.Labels;

/***
 * This class represents a state that will
 * match any state.
 *
 * @author Chris Guenther
 */
public class AnyComponent extends MASSPAComponent
{	
	public AnyComponent()
	{
		super(Labels.s_ANY);
	}

	@Override
	public boolean matchPattern(final MASSPAComponent _pattern)
	{
		return true;
	}
	
	//***************************************
	// Object overwrites
	//***************************************
	@Override
	public boolean equals(final Object _o)
	{
		return (_o instanceof AnyComponent);
	}
}
