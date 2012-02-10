package uk.ac.imperial.doc.gpa.fsm;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

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
            = NFADetectors.detectAllAcceptingStates(startingState);
        Set<NFAState> nonacceptingStates
            = NFADetectors.detectAllNonAcceptingStates(startingState);
        Collection<Set<NFAState>> sets
            = new HashSet<Set<NFAState>> ();
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

		Multimap<ITransition, NFAState> transitions;

		for (NFAState state : closure)
		{
			transitions = state.getTransitions ();
			for (ITransition transition : transitions.keySet ())
			{
				if (!(transition instanceof EmptyTransition))
				{
					Collection<NFAState> reachedStates
                        = transitions.get (transition);
                    for (NFAState reachedState : reachedStates)
                    {
                        transitionsToStates.add
                            (new TransitionStatePair
                                (transition, reachedState));
                        if (!closures.containsKey (reachedState))
                        {
                            findClosuresAndTransitions (reachedState,
                                closures, newTransitions);
                        }
                    }
				}
			}
		}
	}

	private static Set<NFAState> getEmptyClosure (NFAState startingState)
	{
		Set<NFAState> closure = new HashSet<NFAState> ();
		Multimap<ITransition, NFAState> transitions
			= startingState.getTransitions ();
		for (ITransition transition : transitions.keySet ())
		{
			if (transition instanceof EmptyTransition)
			{
                Collection<NFAState> reachedStates
                        = transitions.get (transition);
                for (NFAState reachedState : reachedStates)
                {
                    closure.add (reachedState);
	    			closure.addAll (getEmptyClosure (reachedState));
                }
			}
		}
		return closure;
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
        Map<NFAState, Multimap<ITransition, NFAState>> setToSet
            = new HashMap<NFAState, Multimap<ITransition, NFAState>> ();

        for (Set<NFAState> set : partition.getSets ())
        {
            for (NFAState state : set)
            {
                Multimap<ITransition, NFAState> setTransitions
                    = HashMultimap.create();
                setToSet.put (state, setTransitions);

                Multimap<ITransition, NFAState> transitions
                    = state.getTransitions ();
                for (ITransition transition : transitions.keySet ())
                {
                    for (NFAState s : transitions.get (transition))
                    {
                        setTransitions.putAll
                            (transition, partition.getSetOfState (s));
                    }
                }
            }
        }

        // find equal states
        Multimap<NFAState, NFAState> mappings = HashMultimap.create ();
        Collection<NFAState> visited = new HashSet<NFAState> ();
        for (NFAState state : partition.getStates ())
        {
            if (!(visited.contains (state)))
            {
                for (NFAState state2 : partition.getStates ())
                {
                    if (setToSet.get (state).equals (setToSet.get (state2)))
                    {
                        mappings.put (state, state2);
                        visited.add (state2);
                    }
                }
            }
        }

        // create new sets
        Collection<Set<NFAState>> newSets = new HashSet<Set<NFAState>> ();
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
                Multimap<ITransition, NFAState> transitions
                    = state.getTransitions ();
                for (ITransition transition : transitions.keySet ())
                {
                    for (NFAState s : transitions.get (transition))
                    {
                        newStates.get (set).addTransition (transition,
                            newStates.get (partition.getSetOfState (s)));
                    }
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
