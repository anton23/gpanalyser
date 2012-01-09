package uk.ac.imperial.doc.gpa.fsm;

public class AnyTransition implements ITransition
{	
	public String toString ()
	{
		return "any_transition";
	}
    
    public String toPEPAString ()
    {
        throw new Error("AnyTransition cannot be converted to PEPA String.");
    }

	@Override
	public boolean equals (Object transition)
	{
		return (transition instanceof AnyTransition);
	}

	@Override
	public int hashCode ()
	{
		return toString ().hashCode ();
	}

    public ITransition getCopy ()
    {
        return new AnyTransition ();
    }
}
