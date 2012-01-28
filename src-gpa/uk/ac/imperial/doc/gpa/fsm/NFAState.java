package uk.ac.imperial.doc.gpa.fsm;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

import java.util.*;

// to avoid loops in a graph, the equality is based on identity. Therefore
// this class MUST NOT be used, whenever two different instances are compared.
public class NFAState
{
	private Map<ITransition, NFAState> outgoings
		= new HashMap<ITransition, NFAState> ();
	private boolean accepting = false;
    private NFAPredicate predicate = null;
	private String name;

	public NFAState (String name)
	{
		this.name = name;
	}

	public String toString ()
	{
		return getName ();
	}

	public String getName ()
	{
		return name;
	}

	public void setName (String name)
	{
		this.name = name;
	}

	public boolean isAccepting ()
	{
		return accepting;
	}

	public void setAccepting (boolean accepting)
	{
		this.accepting = accepting;
	}

	public void addTransition (ITransition transition, NFAState state)
	{
		outgoings.put (transition.getCopy (), state);
	}

    public void addTransitionIfNotExisting
        (ITransition transition, NFAState state)
    {
        if (!(outgoings.containsKey (transition)))
        {
            addTransition (transition, state);
        }
    }

    public NFAState advanceWithTransition (ITransition transition)
    {
        NFAState next = outgoings.get (transition);
        // self-loop
        if (next == null)
        {
            next = this;
        }
        return next;
    }

	public NFAState advanceWithTransition (ITransition transition,
        List<AbstractExpression> statesCountExpressions,
        Map<String, AbstractExpression> mapping, double[] data)
	{
        if (predicate == null || predicate.eval
            (statesCountExpressions, mapping, data))
        {
            return advanceWithTransition (transition);
        }
        return this;
	}

    public Map<ITransition, NFAState> getSignalTransitions ()
    {
        Map<ITransition, NFAState> signalTransitions
            = new HashMap<ITransition, NFAState> ();
        for (ITransition transition : outgoings.keySet())
        {
            if (transition instanceof SignalTransition)
            {
                signalTransitions.put (transition, outgoings.get (transition));
            }
        }
        return Collections.unmodifiableMap (signalTransitions);
    }

    public Set<ITransition> getAvailableNonSignalTransitions ()
    {
        Map<ITransition, NFAState> transitions
            = new HashMap<ITransition, NFAState> ();
        transitions.putAll (outgoings);
        Map<ITransition, NFAState> signals = getSignalTransitions ();
        for (ITransition signal : signals.keySet ())
        {
            transitions.remove (signal);
        }
        return Collections.unmodifiableMap (transitions).keySet ();
    }
    
	public Map<ITransition, NFAState> getTransitions ()
	{
		return Collections.unmodifiableMap (outgoings);
	}

    public Map<ITransition, NFAState> getRawTransitions ()
    {
        return outgoings;
    }

    public NFAPredicate getPredicate ()
    {
        return predicate;
    }

    public void setPredicate (NFAPredicate predicate)
    {
        this.predicate = predicate;
    }

    @Override
    public boolean equals (Object o)
    {
        return (this == o);
    }

    @Override
    public int hashCode ()
    {
        return hashCode (new ArrayList<NFAState> ());
    }

    private int hashCode (Collection<NFAState> visited)
    {
        visited.add (this);
        int result = 0;
        int empty = 0;
        for (ITransition transition : outgoings.keySet ())
        {
            NFAState to = outgoings.get (transition);
            if (!visited.contains (to))
            {
                if (transition instanceof EmptyTransition)
                {
                    ++empty;
                    result += to.hashCode (visited);
                }
                else
                {
                    result += transition.hashCode () * to.hashCode (visited);
                }
            }
        }
        result = 31 * result + (accepting ? 1 : 0);
        result = 31 * result
            + ((predicate != null && !(predicate instanceof NFADummyPredicate))
                ? predicate.hashCode () : 0);
        return result * empty;
    }
}
