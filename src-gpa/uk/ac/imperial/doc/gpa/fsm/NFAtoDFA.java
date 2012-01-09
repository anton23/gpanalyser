package uk.ac.imperial.doc.gpa.fsm;

import java.util.*;

public class NFAtoDFA
{
	public static NFAState convertToDFA (NFAState startingState, String naming)
	{
		Map<NFAState, Set<NFAState>> closures
			= new HashMap<NFAState, Set<NFAState>>();
		Map<NFAState, List<TransitionStatePair>> transitions
			= new HashMap<NFAState, List<TransitionStatePair>> ();
		findClosuresAndTransitions (startingState, closures, transitions);

		int counter = 0;
		Map<NFAState, NFAState> newStates = new HashMap<NFAState, NFAState> ();
		for (NFAState state : closures.keySet ())
		{
			newStates.put
                (state, mergeStates (closures.get (state), naming + ++counter));
		}

		for (NFAState state : newStates.keySet ())
		{
			NFAState newState = newStates.get (state);
			List<TransitionStatePair> transitionPairs
				= transitions.get (state);
			for (TransitionStatePair pair : transitionPairs)
			{
				newState.addTransition (pair.getTransition (),
					newStates.get (pair.getState ()));
			}
		}

		return minimise (newStates.get (startingState),	naming);
	}

	private static NFAState minimise (NFAState startingState, String naming)
	{
        Set<NFAState> acceptingStates
            = detectAllAcceptingStates (startingState);
        Set<NFAState> nonacceptingStates
            = detectAllNonAcceptingStates (startingState);
        Collection<Set<NFAState>> sets
            = new LinkedList<Set<NFAState>> ();
        sets.add (acceptingStates);
        sets.add (nonacceptingStates);
        Partition partition = new Partition (sets, null);

        partition = sortPartitionSetsByTransitions (partition, startingState);
        startingState = createNewDFAFromPartition (partition, naming);

		return startingState;
	}

    private static NFAState mergeStates
        (Collection<NFAState> states, String name)
    {
        boolean createAccepting = false;
        String predicate = "";
        for (NFAState s : states)
        {
            createAccepting = createAccepting || s.isAccepting ();
            NFAPredicate pred = s.getPredicate ();
            if (pred != null && !pred.getPredicateString().equals(""))
            {
                if (!predicate.equals (""))
                {
                    predicate += " && ";
                }
                predicate += pred.getPredicateString ();
            }
        }

        NFAState newState = new NFAState (name);
        newState.setAccepting (createAccepting);
        newState.setPredicate (NFAPredicate.create (predicate));
        return newState;
    }

    public static Set<NFAState> detectAllStates (NFAState startingState)
    {
        Set<NFAState> states = new HashSet<NFAState> ();
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

	private static void detectStates (NFAState startingState,
        Set<NFAState> detected, Set<NFAState> visited, StateFilter filter)
	{
		if (filter.accept (startingState))
		{
			detected.add (startingState);
		}

		Map<ITransition, NFAState> transitions
			= startingState.getTransitions ();
		for (ITransition transition : transitions.keySet ())
		{
			NFAState nextState = transitions.get (transition);
			if (!visited.contains (nextState))
			{
				visited.add (nextState);
				detectStates(nextState, detected, visited, filter);
			}
		}
	}

	public static NFAState detectSingleAcceptingState
		(NFAState startingState)
	{
		return detectSingleAcceptingStateI (startingState,
			new HashSet<NFAState> ());
	}

	private static NFAState detectSingleAcceptingStateI
		(NFAState startingState, Set<NFAState> visited)
	{
		if (startingState.isAccepting ())
		{
			return startingState;
		}

		NFAState result = null;
		Map<ITransition, NFAState> transitions
			= startingState.getTransitions ();
		for (ITransition transition : transitions.keySet ())
		{
			NFAState nextState = transitions.get (transition);
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

	private static void detectAlphabetI
        (NFAState startingState, boolean includeSignals,
        Set<ITransition> alphabet, Set<NFAState> visited)
	{
		Map<ITransition, NFAState> transitions
			= startingState.getTransitions ();
		Set<ITransition> transitionsSet = transitions.keySet ();
		for (ITransition transition : transitionsSet)
		{
			if (includeSignals || !(transition instanceof SignalTransition))
			{
				alphabet.add (transition);
			}
			NFAState nextState = transitions.get (transition);
			if (!visited.contains (nextState))
			{
				visited.add (nextState);
				detectAlphabetI (nextState, includeSignals, alphabet, visited);
			}
		}
	}

	private static void findClosuresAndTransitions (NFAState startingState,
		Map<NFAState, Set<NFAState>> closures,
		Map<NFAState, List<TransitionStatePair>> newTransitions)
	{
		List<TransitionStatePair> transitionsToStates
			= new ArrayList<TransitionStatePair> ();
		newTransitions.put (startingState, transitionsToStates);

		Set<NFAState> closure = getEmptyClosure (startingState);
		closure.add (startingState);
		closures.put (startingState, closure);

		Map<ITransition, NFAState> transitions = null;

		for (NFAState state : closure)
		{
			transitions = state.getTransitions ();
			for (ITransition transition : transitions.keySet ())
			{
				if (!(transition instanceof EmptyTransition))
				{
					NFAState reachedState = transitions.get (transition);
					transitionsToStates.add
						(new TransitionStatePair (transition, reachedState));
					if (!closures.containsKey (reachedState))
					{
						findClosuresAndTransitions (reachedState,
							closures, newTransitions);
					}
				}
			}
		}
	}

	private static Set<NFAState> getEmptyClosure (NFAState startingState)
	{
		Set<NFAState> closure = new HashSet<NFAState> ();
		Map<ITransition, NFAState> transitions
			= startingState.getTransitions ();
		for (ITransition transition : transitions.keySet ())
		{
			if (transition instanceof EmptyTransition)
			{
				NFAState reachedState = transitions.get (transition);
				closure.add (reachedState);
				closure.addAll (getEmptyClosure (reachedState));
			}
		}
		return closure;
	}

    private static interface StateFilter
    {
        public boolean accept (NFAState state);
    }

	private static class TransitionStatePair
	{
		private ITransition transition;
		private NFAState state;

		public TransitionStatePair (ITransition transition, NFAState state)
		{
			this.transition = transition;
			this.state = state;
		}

		public ITransition getTransition ()
		{
			return transition;
		}

		public NFAState getState ()
		{
			return state;
		}
	}

    private static class Partition
    {
        Collection<Set<NFAState>> sets;
        Set<NFAState> startingSet;

        public Collection<Set<NFAState>> getSets ()
        {
            return sets;
        }

        public Set<NFAState> getStartingSet ()
        {
            return startingSet;
        }

        public Collection<NFAState> getStates ()
        {
            Collection<NFAState> coll = new HashSet<NFAState> ();

            for (Set<NFAState> set : sets)
            {
                coll.addAll (set);
            }

            return coll;
        }

        public Set<NFAState> getSetOfState (NFAState state)
        {
            for (Set<NFAState> set : sets)
            {
                if (set.contains (state))
                {
                    return set;
                }
            }

            throw new Error ("State not found in this partition.");
        }

        public Partition
            (Collection<Set<NFAState>> set, Set<NFAState> startingSet)
        {
            this.sets = set;
            this.startingSet = startingSet;
        }

        @Override
        public boolean equals (Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass ()) return false;

            Partition partition = (Partition) o;

            if (sets != null ? !sets.equals (partition.sets)
                : partition.sets != null) return false;
            if (startingSet != null ?
                !startingSet.equals (partition.startingSet)
                : partition.startingSet != null) return false;

            return true;
        }

        @Override
        public int hashCode ()
        {
            int result = sets != null ? sets.hashCode () : 0;
            result = 31 * result
                + (startingSet != null ? startingSet.hashCode () : 0);
            return result;
        }
    }

    private static Partition sortPartitionSetsByTransitions
        (Partition partition, NFAState startingState)
    {
        // find all transitions for all sets
        Map<NFAState, Map<ITransition, Set<NFAState>>> setToSet
            = new HashMap<NFAState, Map<ITransition, Set<NFAState>>> ();

        for (Set<NFAState> set : partition.getSets ())
        {
            for (NFAState state : set)
            {
                Map<ITransition, Set<NFAState>> setTransitions
                        = new HashMap<ITransition, Set<NFAState>> ();
                setToSet.put (state, setTransitions);

                Map<ITransition, NFAState>
                    transitions = state.getTransitions ();
                for (ITransition transition : transitions.keySet ())
                {
                    setTransitions.put (transition,
                        partition.getSetOfState (transitions.get (transition)));
                }
            }
        }

        // find equal states
        Map<NFAState, List<NFAState>> mappings
            = new HashMap<NFAState, List<NFAState>> ();
        Collection<NFAState> visited = new HashSet<NFAState> ();
        for (NFAState state : partition.getStates ())
        {
            if (!(visited.contains (state)))
            {
                List<NFAState> list = new ArrayList<NFAState> ();
                mappings.put (state, list);
                for (NFAState state2 : partition.getStates ())
                {
                    if (setToSet.get (state).equals (setToSet.get (state2)))
                    {
                        list.add (state2);
                        visited.add (state2);
                    }
                }
            }
        }

        // create new sets
        Collection<Set<NFAState>> newSets = new LinkedList<Set<NFAState>> ();
        Set<NFAState> startingSet = null;
        for (NFAState state : mappings.keySet ())
        {
            Set<NFAState> set = new HashSet<NFAState> ();
            set.add (state);
            set.addAll (mappings.get (state));
            newSets.add (set);
            if (set.contains (startingState))
            {
                startingSet = set;
            }
        }

        Partition newPartition = new Partition (newSets, startingSet);

        if (partition.equals (newPartition))
        {
            return partition;
        }

        return sortPartitionSetsByTransitions (newPartition, startingState);
    }

    private static NFAState createNewDFAFromPartition
        (Partition partition, String naming)
    {
        int counter = 0;
        NFAState startingState = null;
        Map<Set<NFAState>, NFAState> newStates
            = new HashMap<Set<NFAState>, NFAState> ();
        for (Set<NFAState> set : partition.getSets ())
        {
            NFAState newState = mergeStates(set, naming + ++counter);
            newStates.put (set, newState);
            if (set.equals (partition.startingSet))
            {
                startingState = newState;
            }
        }

        for (Set<NFAState> set : partition.getSets ())
        {
            for (NFAState state : set)
            {
                Map<ITransition, NFAState> transitions
                    = state.getTransitions ();
                for (ITransition transition : transitions.keySet ())
                {
                    newStates.get (set).addTransition (transition,
                        newStates.get (partition.getSetOfState
                            (transitions.get (transition))));
                }
            }
        }

        if (startingState == null)
        {
            throw new Error ("Minimisation failed - no starting state!");
        }
        return startingState;
    }
}
