package uk.ac.imperial.doc.gpa.fsm;

import java.util.UUID;

public class EmptyTransition implements ITransition
{
    private int hashCode;
    
    public EmptyTransition ()
    {
        hashCode = UUID.randomUUID().hashCode();
    }
    
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

    @Override
    public boolean equals (Object o)
    {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;

        EmptyTransition that = (EmptyTransition) o;

        return (hashCode == that.hashCode);
    }

    @Override
    public int hashCode ()
    {
        return hashCode;
    }
}
