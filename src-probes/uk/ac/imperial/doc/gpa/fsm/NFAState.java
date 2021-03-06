package uk.ac.imperial.doc.gpa.fsm;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

// to avoid loops in a graph, the equality is based on identity. Therefore
// this class MUST NOT be used, whenever two different instances are compared.
public class NFAState
{
	private Multimap<ITransition, NFAState> outgoings = HashMultimap.create ();
	private boolean accepting = false;
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

    public void replaceTransition (ITransition transition, NFAState state)
    {
        outgoings.removeAll (transition);
        addTransition (transition, state);
    }

    public NFAState advanceWithTransition (ITransition transition)
    {
        Collection<NFAState> next = outgoings.get (transition);
        for (NFAState s : next)
        {
            return s;
        }

        // self-loop
        return this;
    }

    public Multimap<ITransition, NFAState> getSignalTransitions ()
    {
        Multimap<ITransition, NFAState> signalTransitions
            = HashMultimap.create ();
        for (ITransition transition : outgoings.keySet ())
        {
            if (transition instanceof SignalTransition)
            {
                signalTransitions.putAll
                        (transition, outgoings.get (transition));
            }
        }
        return Multimaps.unmodifiableMultimap (signalTransitions);
    }

	public Multimap<ITransition, NFAState> getTransitions ()
	{
		return Multimaps.unmodifiableMultimap (outgoings);
	}

    public Multimap<ITransition, NFAState> getRawTransitions ()
    {
        return outgoings;
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

        // dirty hack for deep clone library
        // - otherwise outgoings would be null during deep copying
        if (outgoings == null)
        {
            outgoings = HashMultimap.create ();
        }

        for (ITransition transition : outgoings.keySet ())
        {
            Collection<NFAState> to = outgoings.get (transition);
            for (NFAState s : to)
            {
                if (!visited.contains (s))
                {
                    if (transition instanceof EmptyTransition)
                    {
                        ++empty;
                        result += s.hashCode (visited);
                    }
                    else
                    {
                        result += transition.hashCode ()
                                * s.hashCode (visited);
                    }
                }
            }
        }
        result = 31 * result + (accepting ? 1 : 0);
        return result * empty;
    }
}
