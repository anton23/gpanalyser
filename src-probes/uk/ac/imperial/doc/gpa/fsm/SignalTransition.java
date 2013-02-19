package uk.ac.imperial.doc.gpa.fsm;

public class SignalTransition extends Transition
{
	public SignalTransition(String name)
	{
		super (name);
	}

    @Override
    public String toPEPAString ()
    {
        return name + ".";
    }

    @Override
    public ITransition getCopy ()
    {
        return new SignalTransition (name);
    }

    @Override
    public ITransition getSimpleTransition ()
    {
        return new Transition (name);
    }
}
