package uk.ac.imperial.doc.gpa.fsm;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

public class NFAtoDFA
{
	public static NFAState convertToDFA (NFAState startingState, String naming)
	{
        Multimap<NFAState, NFAState> closures = HashMultimap.create ();
        findEmptyClosures (startingState, closures);

        BiMap<Collection<NFAState>, NFAState> mergers = HashBiMap.create ();
        Multimap<NFAState, TransitionStatesPair> newTransitions
            = HashMultimap.create ();
        for (NFAState state : closures.keySet ())
        {
            Multimap<ITransition, NFAState> transitions
                = state.getTransitions ();

            // we find the set of states this transition could lead to
            for (ITransition transition : transitions.keySet ())
            {
                if (!(transition.isEmptyTransition ()))
                {
                    Collection<NFAState> reachedStates
                        = transitions.get (transition);
                    Collection <NFAState> closure
                        = findClosure (reachedStates, closures);
                    newTransitions.put (state, new TransitionStatesPair
                            (transition, closure));
                }
            }
        }

        NFAState newStartingState
            = mergeStates (closures.get (startingState), naming);
        mergers.put (closures.get (startingState), newStartingState);

        addTransitions (newStartingState, mergers, newTransitions,
                naming, new HashSet<NFAState> ());

		return minimise (newStartingState, naming);
	}

	private static NFAState minimise (NFAState startingState, String naming)
	{
        Set<NFAState> acceptingStates
            = NFADetectors.detectAllAcceptingStates (startingState);
        Set<NFAState> nonacceptingStates
            = NFADetectors.detectAllNonAcceptingStates (startingState);
        Collection<Set<NFAState>> sets
            = new HashSet<Set<NFAState>> ();
        sets.add (acceptingStates);
        sets.add (nonacceptingStates);
        Partition partition = new Partition (sets, null);

        partition = sortPartitionSetsByTransitions (partition, startingState);
        startingState = createNewDFAFromPartition (partition, naming);

        return startingState;
	}

    private static NFAState getMerger
        (BiMap<Collection<NFAState>, NFAState> mergers,
         Collection<NFAState> closure, String naming)
    {
        NFAState merger = mergers.get (closure);
        if (merger == null)
        {
            merger = mergeStates (closure, naming);
            mergers.put (closure, merger);
        }

        return merger;
    }

    private static void addTransitions
        (NFAState state, BiMap<Collection<NFAState>, NFAState> mergers,
         Multimap<NFAState, TransitionStatesPair> newTransitions, String naming,
         Set<NFAState> visited)
    {
        if (visited.contains (state))
        {
            return;
        }
        visited.add (state);

        Multimap<ITransition, NFAState> newJointTransitions
            = HashMultimap.create ();
        Collection<NFAState> closure = mergers.inverse ().get (state);
        for (NFAState s : closure)
        {
            Collection<TransitionStatesPair> pairs = newTransitions.get (s);
            for (TransitionStatesPair pair : pairs)
            {
                newJointTransitions.putAll
                    (pair.getTransition (), pair.getStates ());
            }
        }

        for (ITransition transition : newJointTransitions.keySet ())
        {
            NFAState merged = getMerger (mergers,
                    newJointTransitions.get (transition), naming);
            state.addTransition (transition, merged);
            addTransitions (merged, mergers, newTransitions,
                    naming, visited);
        }
    }

    private static NFAState mergeStates
        (Collection<NFAState> states, String name)
    {
        boolean createAccepting = false;
        String predicate = "";
        for (NFAState s : states)
        {
            createAccepting = s.isAccepting () || createAccepting;
            NFAPredicate pred = s.getPredicate ();
            if (pred != null && !pred.getPredicateString ().equals (""))
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

    private static Collection<NFAState> findClosure
        (Collection<NFAState> states, Multimap<NFAState, NFAState> closures)
    {
        Collection<NFAState> closure = new HashSet<NFAState> ();
        closure.addAll (states);
        for (NFAState state : states)
        {
            closure.addAll (closures.get (state));
        }

        if (states.equals (closure))
        {
            return states;
        }
        else
        {
            return findClosure (closure, closures);
        }
    }

    private static void findEmptyClosures
        (NFAState startingState, Multimap<NFAState, NFAState> closures)
	{
        Set<NFAState> states = NFADetectors.detectAllStates (startingState);
        for (NFAState state : states)
        {
            Set<NFAState> closure = new HashSet<NFAState> ();
            getEmptyClosure (state, closure);
            closures.putAll(state, closure) ;
        }
	}

	private static void getEmptyClosure
        (NFAState state, Set<NFAState> closure)
	{
        closure.add (state);
		Multimap<ITransition, NFAState> transitions = state.getTransitions ();
		for (ITransition transition : transitions.keySet ())
		{
			if (transition.isEmptyTransition ())
			{
                Collection<NFAState> reachedStates
                    = transitions.get (transition);
                for (NFAState reachedState : reachedStates)
                {
                    if (!closure.contains(reachedState))
                    {
                        closure.add (reachedState);
                        getEmptyClosure (reachedState, closure);
                    }
                }
			}
		}
	}

    private static class TransitionStatesPair
	{
		private final ITransition transition;
		private final Collection<NFAState> states;

		public TransitionStatesPair
            (ITransition transition, Collection<NFAState> states)
		{
			this.transition = transition;
			this.states = states;
		}

		public ITransition getTransition ()
		{
			return transition;
		}

		public Collection<NFAState> getStates ()
		{
			return states;
		}
	}

    private static class Partition
    {
        private final Collection<Set<NFAState>> sets;
        private final Set<NFAState> startingSet;

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
            for (final Set<NFAState> set : sets)
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
                for (NFAState state2 : partition.getSetOfState (state))
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
