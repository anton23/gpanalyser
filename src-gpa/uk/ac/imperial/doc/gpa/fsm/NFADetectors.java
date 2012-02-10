package uk.ac.imperial.doc.gpa.fsm;

import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NFADetectors
{
    public static Set<NFAState> detectAllStates(NFAState startingState)
    {
        Set<NFAState> states = new HashSet<NFAState>();
        detectStates(startingState, states,
                new HashSet<NFAState> (), new StateFilter ()
                    {
                        public boolean accept (NFAState state)
                        {
                            return true;
                        }
                    }
        );
        return states;
    }

    public static Set<NFAState> detectAllAcceptingStates
            (NFAState startingState)
	{
		Set<NFAState> accepting = new HashSet<NFAState> ();
        detectStates(startingState, accepting,
                new HashSet<NFAState> (), new StateFilter ()
                    {
                        public boolean accept (NFAState state)
                        {
                            return state.isAccepting ();
                        }
                    }
        );
		return accepting;
	}

    public static Set<NFAState> detectAllNonAcceptingStates
            (NFAState startingState)
    {
        Set<NFAState> nonaccepting = new HashSet<NFAState> ();
        detectStates(startingState, nonaccepting,
                new HashSet<NFAState> (), new StateFilter ()
                    {
                        public boolean accept (NFAState state)
                        {
                            return !state.isAccepting ();
                        }
                    }
        );
        return nonaccepting;
    }

    public static void detectStates(NFAState startingState,
                                    Set<NFAState> detected, Set<NFAState> visited, StateFilter filter)
	{
		if (filter.accept (startingState))
		{
			detected.add (startingState);
		}

		Multimap<ITransition, NFAState> transitions
			= startingState.getTransitions ();
		for (ITransition transition : transitions.keySet ())
		{
			Collection<NFAState> nextStates = transitions.get (transition);
            for (NFAState nextState : nextStates)
            {
                if (!visited.contains (nextState))
                {
                    visited.add (nextState);
                    detectStates(nextState, detected, visited, filter);
                }
            }
		}
	}

    public static NFAState detectSingleAcceptingState
            (NFAState startingState)
    {
        return detectSingleAcceptingStateI (startingState,
            new HashSet<NFAState> ());
    }

    public static NFAState detectSingleAcceptingStateI
            (NFAState startingState, Set<NFAState> visited)
	{
		if (startingState.isAccepting ())
		{
			return startingState;
		}

		NFAState result = null;
		Multimap<ITransition, NFAState> transitions
			= startingState.getTransitions ();
		for (ITransition transition : transitions.keySet ())
		{
            Collection<NFAState> nextStates = transitions.get (transition);
            for (NFAState nextState : nextStates)
            {
                if (!visited.contains (nextState))
                {
                    visited.add (nextState);
                    result = detectSingleAcceptingStateI (nextState, visited);
                }
                if (result != null)
                {
                    return result;
                }
            }
		}
		return result;
	}

    public static Set<ITransition> detectAlphabet
        (NFAState startingState, boolean includeSignals,
         Collection<ITransition> excluded)
    {
        Set<ITransition> alphabet = new HashSet<ITransition> ();
        detectAlphabetI (startingState, includeSignals,
            alphabet, new HashSet<NFAState> ());
        alphabet.removeAll(excluded);
        return alphabet;
    }

    public static void detectAlphabetI
            (NFAState startingState, boolean includeSignals,
             Set<ITransition> alphabet, Set<NFAState> visited)
	{
		Multimap<ITransition, NFAState> transitions
			= startingState.getTransitions ();
		Set<ITransition> transitionsSet = transitions.keySet ();
		for (ITransition transition : transitionsSet)
		{
			if (includeSignals || !(transition instanceof SignalTransition))
			{
				alphabet.add (transition);
			}
            Collection<NFAState> nextStates = transitions.get (transition);
            for (NFAState nextState : nextStates)
            {
                if (!visited.contains (nextState))
                {
                    visited.add (nextState);
                    detectAlphabetI (nextState, includeSignals, alphabet, visited);
                }
            }
		}
	}

    public static interface StateFilter
    {
        public boolean accept(NFAState state);
    }
}
