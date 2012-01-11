package uk.ac.imperial.doc.gpa.fsm;

public class EmptyTransition implements ITransition
{
	public String toString ()
	{
		return "empty_transition";
	}

    public String toPEPAString ()
    {
        throw new Error("EmptyTransition cannot be converted to PEPA String.");
    }

    public ITransition getCopy ()
    {
        return new EmptyTransition ();
    }

    public ITransition getSimpleTransition ()
    {
        throw new Error("EmptyTransition cannot create a simple transition.");
    }
}
