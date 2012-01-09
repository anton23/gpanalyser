package uk.ac.imperial.doc.gpa.fsm;

public class SignalTransition implements ITransition
{
	private String name = null;

	public SignalTransition(String name)
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
        return name + ".";
    }

	@Override
	public boolean equals (Object transition)
	{
		return (transition instanceof SignalTransition
			&& ((SignalTransition)transition).getName ().equals (name));
	}

	@Override
	public int hashCode ()
	{
		return name.hashCode () + 1;
	}

    public ITransition getCopy ()
    {
        return new SignalTransition (name);
    }
}
