package uk.ac.imperial.doc.gpa.probes;

import uk.ac.imperial.doc.gpa.fsm.NFAState;

public interface IProbe
{
	public String getName ();

	public void setName (String name);

	public void setStartingState (NFAState state);

    public NFAState getStartingState ();
}
