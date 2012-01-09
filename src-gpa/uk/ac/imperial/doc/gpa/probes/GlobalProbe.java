package uk.ac.imperial.doc.gpa.probes;

import uk.ac.imperial.doc.gpa.fsm.ITransition;
import uk.ac.imperial.doc.gpa.fsm.NFAState;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GlobalProbe implements IProbe
{
	private NFAState startingState = null;
    private NFAState currentState = null;
	private String name;
	private boolean repeating;

	public String getName ()
	{
		return name;
	}

	public void setName (String name)
	{
		this.name = name;
	}

	public void setRepeating (boolean repeating)
	{
		this.repeating = repeating;
	}

	public void setStartingState (NFAState state)
	{
        if (startingState != null)
        {
            throw new Error ("Cannot set state " + state +
                " for probe " + this + ", it has already been set");
        }
		startingState = state;
        currentState = startingState;
	}

    public NFAState getStartingState ()
    {
        return startingState;
    }
    
    public Set<ITransition> getAvailableTransitions ()
    {
        return currentState.getTransitions ().keySet ();
    }

    public void advanceWithTransition (ITransition transition,
        List<AbstractExpression> statesCountExpressions,
        Map<String, AbstractExpression> mapping, double[] data)
    {
        currentState = currentState.advanceWithTransition
            (transition, statesCountExpressions, mapping, data);
        System.out.println ("Global probe advanced with " + transition);
        if (currentState.isAccepting ())
        {
            if (repeating)
            {
                currentState = startingState;
            }
            return;
        }

        // any available signal? (assuming always only one at once)
        Map<ITransition, NFAState> sigs = currentState.getSignalTransitions ();
        
        if (sigs.size () > 1)
        {
            throw new Error ("Global probe state " + currentState
                + " has more than one signal available.");
        }
        
        if (sigs.size () == 1)
        {
            Set<ITransition> signals = sigs.keySet ();
            for (ITransition signal : signals)
            {
                currentState = currentState.advanceWithTransition (signal);
                System.out.println ("sent " + signal + " signal");
            }
        }
    }
}
