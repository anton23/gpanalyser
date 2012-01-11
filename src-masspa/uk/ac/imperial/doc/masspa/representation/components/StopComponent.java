package uk.ac.imperial.doc.masspa.representation.components;

import java.util.Set;

import uk.ac.imperial.doc.masspa.language.Labels;

/***
 * This class represents the absorbing (nil) state.
 * 
 * @author Chris Guenther
 */
public class StopComponent extends MASSPAComponent
{
	public StopComponent()
	{
		super(Labels.s_STOP);
	}

	@Override
	public boolean matchPattern(final MASSPAComponent _pattern)
	{
		if(_pattern instanceof AnyComponent) {return true;}
		return equals(_pattern);
	}

	@Override
	protected void getDerivativeStates(final Set<MASSPAComponent> _known)
	{
		_known.add(this);
	}
	
	//***************************************
	// Object overwrites
	//***************************************	
	@Override
	public int hashCode()
	{
		return 0;
	}
	
	@Override
	public boolean equals(final Object _o)
	{
		return (_o instanceof StopComponent);
	}
}
