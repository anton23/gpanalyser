package uk.ac.imperial.doc.gpa.fsm;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Set;

public class NFAUtils
{
    public static NFAState getBothCombination
        (CartesianUtils.CartesianState startingCartesian,
         List<CartesianUtils.CartesianState> cartesianStates, String naming)
    {
        for (CartesianUtils.CartesianState cstate : cartesianStates)
        {
            NFAState state1 = cstate.getState1 ();
            NFAState state2 = cstate.getState2 ();
            Multimap<ITransition, NFAState> transitions
                = HashMultimap.create ();
            transitions.putAll (state1.getTransitions ());
            transitions.putAll (state2.getTransitions ());

            for (ITransition transition : transitions.keySet ())
            {
                NFAState new_state1 = state1.advanceWithTransition (transition);
                NFAState new_state2 = state2.advanceWithTransition (transition);

                cstate.addTransition (transition,
                        CartesianUtils.getCartesianState
                            (new_state1, new_state2, cartesianStates));
            }
        }

        return CartesianUtils.convertCartesianToDFA
                (startingCartesian, cartesianStates, naming,
                        new CartesianUtils.AcceptingCartesianStateFilter ()
                        {
                            public boolean isAccepting
                                    (CartesianUtils.CartesianState cstate)
                            {
                                return (cstate.getState1 ().isAccepting ()
                                        && cstate.getState2 ().isAccepting ());
                            }
                        });
    }

    public static NFAState getResetCombination
        (CartesianUtils.CartesianState startingCartesian,
         List<CartesianUtils.CartesianState> cartesianStates, String naming)
    {
        for (CartesianUtils.CartesianState cstate : cartesianStates)
        {
            NFAState state1 = cstate.getState1 ();
            NFAState state2 = cstate.getState2 ();

            // these are handled when they are actually reached
            if (state2.isAccepting ())
            {
                continue;
            }

            Multimap<ITransition, NFAState> transitions
                    = HashMultimap.create ();
            transitions.putAll (state1.getTransitions ());
            transitions.putAll (state2.getTransitions ());

            for (ITransition transition : transitions.keySet ())
            {
                NFAState new_state1 = state1.advanceWithTransition (transition);
                NFAState new_state2 = state2.advanceWithTransition (transition);

                if (new_state2.isAccepting())
                {
                    if (!new_state1.isAccepting())
                    {
                        cstate.addTransition (transition, startingCartesian);
                    }
                }
                else
                {
                    cstate.addTransition (transition,
                            CartesianUtils.getCartesianState
                                (new_state1, new_state2, cartesianStates));
                }
            }
        }

        return CartesianUtils.convertCartesianToDFA
            (startingCartesian, cartesianStates, naming,
                    new CartesianUtils.AcceptingCartesianStateFilter ()
                    {
                        public boolean isAccepting
                                (CartesianUtils.CartesianState cstate)
                        {
                        return (cstate.getState1 ().isAccepting ()
                                && !cstate.getState2 ().isAccepting ());
                        }
                    });
    }

    public static NFAState getFailCombination
        (CartesianUtils.CartesianState startingCartesian,
         List<CartesianUtils.CartesianState> cartesianStates,
         String naming, Set<ITransition> alphabet)
    {
        for (CartesianUtils.CartesianState cstate : cartesianStates)
        {
            if (cstate.getState2 ().isAccepting ())
            {
                for (ITransition action : alphabet)
                {
                    cstate.addTransitionIfNotExisting
                        (action.getSimpleTransition (), cstate);
                }
                continue;
            }

            NFAState state1 = cstate.getState1 ();
            NFAState state2 = cstate.getState2 ();

            Multimap<ITransition, NFAState> transitions
                    = HashMultimap.create ();
            transitions.putAll (state1.getTransitions ());
            transitions.putAll (state2.getTransitions ());

            for (ITransition transition : transitions.keySet ())
            {
                NFAState new_state1 = state1.advanceWithTransition (transition);
                NFAState new_state2 = state2.advanceWithTransition (transition);

                cstate.addTransition (transition,
                        CartesianUtils.getCartesianState
                            (new_state1, new_state2, cartesianStates));
            }
        }

        return CartesianUtils.convertCartesianToDFA
            (startingCartesian, cartesianStates, naming,
                    new CartesianUtils.AcceptingCartesianStateFilter ()
                    {
                        public boolean isAccepting
                                (CartesianUtils.CartesianState cstate)
                        {
                            return (cstate.getState1 ().isAccepting ()
                                    && !cstate.getState2 ().isAccepting ());
                        }
                    });
    }

    public static void unifyAcceptingStates
        (NFAState starting_state, NFAState new_reached_state)
    {
        Set<NFAState> accepting_states
            = NFADetectors.detectAllAcceptingStates (starting_state);
        for (NFAState state : accepting_states)
        {
            state.setAccepting (false);
            state.addTransition
                (new EmptyTransition (), new_reached_state);
        }
        new_reached_state.setAccepting (true);
    }

    public static void invertAcceptingStates (NFAState starting_state)
    {
        Set<NFAState> states = NFADetectors.detectAllStates (starting_state);
        for (NFAState state : states)
        {
            state.setAccepting (!state.isAccepting ());
        }
    }

    /*
    public static void removeAnyTransitions
        (Set<ITransition> alphabet, NFAState startingState)
    {
        Set<NFAState> states = NFADetectors.detectAllStates (startingState);
        Set<NFAState> statesWithAny = new HashSet<NFAState>();
        for (NFAState state : states)
        {
            Multimap<ITransition, NFAState> transitions
                = state.getRawTransitions ();
            for (ITransition transition : transitions.keySet ())
            {
                if (transition instanceof AnyTransition)
                {
                    statesWithAny.add(state);
                }
            }
        }

        ITransition any = new AnyTransition ();
        for (NFAState state : statesWithAny)
        {
            NFAState other = state.advanceWithTransition (any);
            for (ITransition newTransition : alphabet)
            {
                state.addTransitionIfNotExisting
                    (newTransition.getSimpleTransition (), other);
            }
            state.getRawTransitions().removeAll (any);
        }
    }

    public static void removeSurplusSelfLoops (NFAState startingState)
    {
        Set<NFAState> states = NFADetectors.detectAllStates (startingState);
        Map<ITransition, Boolean> usedTransitions
            = new HashMap<ITransition, Boolean> ();
        for (NFAState state : states)
        {
            Multimap<ITransition, NFAState> transitions
                = state.getRawTransitions ();
            for (ITransition transition : transitions.keySet ())
            {
                Boolean used = usedTransitions.get (transition);
                Collection<NFAState> reached = transitions.get (transition);
                if (!(used != null && used)
                    && ((reached.size() == 1) && reached.contains (state)))
                {
                    usedTransitions.put (transition, false);
                }
                else
                {
                    usedTransitions.put (transition, true);
                }
            }
        }

        for (ITransition transition : usedTransitions.keySet())
        {
            if (!usedTransitions.get (transition))
            {
                for (NFAState state : states)
                {
                    state.getRawTransitions ().removeAll (transition);
                }
            }
        }

    }

    public static void extendStatesWithSelfLoops
        (Set<ITransition> alphabet, NFAState startingState)
    {
        Set<NFAState> states = NFADetectors.detectAllStates (startingState);
        for (NFAState state : states)
        {
            for (ITransition transition : alphabet)
            {
                state.addTransitionIfNotExisting
                    (transition.getSimpleTransition (), state);
            }
        }
    }
    */

    public static NFAState getNewFailureState (Set<ITransition> allActions)
    {
        NFAState failure = new NFAState ("failure");
        for (ITransition transition : allActions)
        {
            failure.addTransition (transition.getSimpleTransition (), failure);
        }
        failure.setAccepting (false);
        return failure;
    }

    public static class Signaller
    {
        private NFAState startingState;
        private SignalTransition signal;

        public Signaller (NFAState startingState, SignalTransition signal)
        {
            this.startingState = startingState;
            this.signal = signal;
        }

        public NFAState getStartingState ()
        {
            return startingState;
        }

        public SignalTransition getSignal ()
        {
            return signal;
        }
    }

    public static NFAState mergeSignallers (List<Signaller> signallers)
    {
        int i = 1;
        Signaller signaller = signallers.get (0);
        while (i < signallers.size ())
        {
            NFAState acc_state = signallers.get (i).getStartingState ();
            Set<NFAState> accepting = NFADetectors.detectAllAcceptingStates
                (signaller.getStartingState ());
            for (NFAState acc : accepting)
            {
                acc.replaceTransition (signaller.getSignal (), acc_state);
                acc.setAccepting (false);
            }

            signaller = signallers.get (i);
            i++;
        }

        NFAState final_acc_state = new NFAState ("final");
        final_acc_state.setAccepting (true);
        Set<NFAState> accepting = NFADetectors.detectAllAcceptingStates
                (signaller.getStartingState ());
        for (NFAState acc : accepting)
        {
            acc.replaceTransition (signaller.getSignal (), final_acc_state);
            acc.setAccepting (false);
        }

        return signallers.get (0).getStartingState ();
    }
}
