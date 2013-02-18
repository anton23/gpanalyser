package uk.ac.imperial.doc.gpa.probes;

import uk.ac.imperial.doc.gpa.fsm.NFAState;
import uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions.AbstractUExpression;

public class GlobalProbe implements IProbe
{
	private NFAState startingState = null;
	private String name;
    private AbstractUExpression u;

    public void setU (AbstractUExpression u)
    {
        this.u = u;
    }

    public AbstractUExpression getU ()
    {
        return u;
    }

    public String getName ()
	{
		return name;
	}

	public void setName (String name)
	{
		this.name = name;
	}

	public void setStartingState (NFAState state)
	{
        if (startingState != null)
        {
            throw new Error ("Cannot set state " + state +
                " for probe " + this + ", it has already been set");
        }
		startingState = state;
	}

    public NFAState getStartingState ()
    {
        return startingState;
    }
}
