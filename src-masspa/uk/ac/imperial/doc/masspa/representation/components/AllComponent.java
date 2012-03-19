package uk.ac.imperial.doc.masspa.representation.components;

import uk.ac.imperial.doc.masspa.language.Labels;

/**
 * The component is mainly used by the GUI for
 * filtered selections of Component.<br>
 * E.g. select &lt;All&gt; populations in location X.
 * 
 * @author Chris Guenther
 */
public class AllComponent extends MASSPAComponent
{
	public AllComponent()
	{
		super(Labels.s_ALL);
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
		return (_o instanceof AllComponent);
	}
}
