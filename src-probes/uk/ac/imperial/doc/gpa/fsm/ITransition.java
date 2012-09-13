package uk.ac.imperial.doc.gpa.fsm;

public interface ITransition
{
    public String toPEPAString ();

    public ITransition getCopy ();

    public ITransition getSimpleTransition ();

    public boolean isEmptyTransition ();
}
