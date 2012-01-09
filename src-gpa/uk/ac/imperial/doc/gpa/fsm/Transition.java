package uk.ac.imperial.doc.gpa.fsm;

public class Transition implements ITransition
{
	private String name = null;

	public Transition (String name)
	{
		this.name = name;
	}

	public String getName ()
	{
		return name;
	}
	
	public String toString ()
	{
		return name;
	}

    public String toPEPAString ()
    {
        return "(" + name + ", T).";
    }

	@Override
	public boolean equals (Object transition)
	{
		return (transition instanceof Transition
			&& ((Transition) transition).getName ().equals (name));
	}

	@Override
	public int hashCode ()
	{
		return name.hashCode ();
	}

    public ITransition getCopy ()
    {
        return new Transition (name);
    }
}
