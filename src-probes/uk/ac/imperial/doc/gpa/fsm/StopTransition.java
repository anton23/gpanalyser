package uk.ac.imperial.doc.gpa.fsm;

public class StopTransition extends SignalTransition
{
    public static final String stop = "stop_signal";

    public StopTransition ()
    {
        super (stop);
    }

    @Override
    public String toPEPAString ()
    {
        return (stop);
    }
}
