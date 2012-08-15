package uk.ac.imperial.doc.gpa.fsm;

public class StartTransition extends SignalTransition
{
    public static final String start = "start_signal";
    public StartTransition ()
    {
        super (start);
    }

    @Override
    public String toPEPAString ()
    {
        return (start);
    }
}
