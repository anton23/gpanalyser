package uk.ac.imperial.doc.gpa.fsm;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

import java.util.*;

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
		System.out.println ("created state " + name);
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
		System.out.println (this + " was set " + (accepting ? "" : "not") + "accepting");
	}

	public void addTransition (ITransition transition, NFAState state)
	{
		outgoings.put (transition.getCopy (), state);
		System.out.println ("debug: added transition " + transition + " from " + this + " to " + state + ", transition hashCode " + transition.hashCode ());
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
        NFAState next = outgoings.get(transition);
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
        System.out.println ("set predicate: " + predicate.getPredicateString());
        this.predicate = predicate;
    }

    @Override
    public boolean equals (Object o)
    {
        List<NFAState> visited = new ArrayList<NFAState> ();
        return equals (o, visited);
    }

    public boolean equals (Object o, List<NFAState> visited)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass () != o.getClass ())
        {
            return false;
        }

        NFAState nfaState = (NFAState) o;

        if (accepting != nfaState.accepting)
        {
            return false;
        }
        for (ITransition transition : outgoings.keySet ())
        {
            if (!visited.contains (outgoings.get (transition)))
            {
                boolean equal = outgoings.get (transition).equals (this);
                if (!equal)
                {
                    return false;
                }
            }
        }
        if (predicate != null ? !predicate.equals (nfaState.predicate)
            : nfaState.predicate != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode ()
    {
        List<NFAState> visited = new ArrayList<NFAState> ();
        return hashCode (visited);
    }

    public int hashCode (List<NFAState> visited)
    {
        int result = 0;
        visited.add (this);
        for (ITransition transition : outgoings.keySet ())
        {
            if (!visited.contains (outgoings.get (transition)))
            {
                result += transition.hashCode ()
                        * outgoings.get (transition).hashCode (visited);
            }
        }
        result = 31 * result + (accepting ? 1 : 0);
        result = 31 * result + (predicate != null ? predicate.hashCode () : 0);
        return result;
    }
}
