package uk.ac.imperial.doc.gpa.fsm;

import java.util.*;

public class CartesianUtils
{
	public static List<CartesianState> getCompleteCartesianDFA
		(NFAState starting1, NFAState starting2)
	{
		return getCartesianDFA (starting1, starting2, new NoFilter ());
	}

	public static List<CartesianState> isAcceptingCartesianDFA
		(NFAState starting1, NFAState starting2)
	{
		return getCartesianDFA (starting1, starting2,
			new AcceptingStateFilter ());
	}

	public static CartesianState getCartesianState
		(NFAState state1, NFAState state2, List<CartesianState> cartesians)
	{
		for (CartesianState cartesian : cartesians)
		{
			if (cartesian.getState1 () == state1
				&& cartesian.getState2 () == state2)
			{
				return cartesian;
			}
		}

		return null;
	}

	public static NFAState convertCartesianToDFA
		(CartesianState startingCartesian, List<CartesianState> cartesianStates,
			String naming, AcceptingCartesianStateFilter filter)
	{
		int counter = 0;
		Map<CartesianState, NFAState> mappings
			= new HashMap<CartesianState, NFAState> ();
		for (CartesianState cstate : cartesianStates)
		{
			NFAState current = new NFAState (naming + ++counter);
			current.setAccepting (filter.isAccepting (cstate));
			mappings.put (cstate, current);
		}

		for (CartesianState cstate : cartesianStates)
		{
			NFAState current = mappings.get (cstate);
			Map<ITransition, CartesianState> transitions
				= cstate.getTransitions ();
			for (ITransition transition : transitions.keySet ())
			{
				current.addTransition (transition, mappings.get
					(transitions.get (transition)));
			}
		}

		return mappings.get (startingCartesian);
	}

	public static void printCartesians (List<CartesianState> cartesianStates)
	{
		for (CartesianState cartesian : cartesianStates)
		{
			System.out.println (cartesian);
		}
	}

	private static List<CartesianState> getCartesianDFA
		(NFAState starting1, NFAState starting2, StateFilter filter)
	{
		Set<NFAState> states1 = new HashSet<NFAState> ();
		getAllStatesDFA (starting1, states1, new HashSet<NFAState> (), filter);

		Set<NFAState> states2 = new HashSet<NFAState> ();
		getAllStatesDFA (starting2, states2, new HashSet<NFAState> (), filter);

		List<CartesianState> result = new ArrayList<CartesianState> ();

		for (NFAState state1 : states1)
		{
			for (NFAState state2 : states2)
			{
				result.add (new CartesianState (state1, state2));
			}
		}

		return result;
	}

	private static void getAllStatesDFA (NFAState state, Set<NFAState> results,
		Set<NFAState> visited, StateFilter filter)
	{
		if (visited.contains (state))
		{
			return;
		}
		visited.add (state);

		if (filter.accept (state))
		{
			results.add (state);
		}

		Map<ITransition, NFAState> transitions = state.getTransitions ();
		for (ITransition transition : transitions.keySet ())
		{
			getAllStatesDFA (transitions.get (transition), results,
				visited, filter);
		}
	}

	public static class CartesianState
	{
		private NFAState state1;
		private NFAState state2;
		private Map<ITransition, CartesianState> outgoings
			= new HashMap<ITransition, CartesianState> ();

		public CartesianState (NFAState state1, NFAState state2)
		{
			this.state1 = state1;
			this.state2 = state2;
		}

        public void addTransition
                (ITransition transition, CartesianState cstate)
        {
            outgoings.put (transition, cstate);
        }

        public void addTransitionIfNotExisting
                (ITransition transition, CartesianState cstate)
        {
            if (!outgoings.containsKey (transition))
            {
                addTransition (transition, cstate);
            }
        }

		public NFAState getState1 ()
		{
			return state1;
		}

		public NFAState getState2 ()
		{
			return state2;
		}

		public Map<ITransition, CartesianState> getTransitions ()
		{
			return outgoings;
		}

		public String toString ()
		{
			return "Cartesian < " + state1 + ", " + state2 + ">";
		}
	}

	public static interface AcceptingCartesianStateFilter
	{
		public boolean isAccepting (CartesianState cstate);
	}

	private static interface StateFilter
	{
		public boolean accept (NFAState state);
	}

	private static class NoFilter implements StateFilter
	{
		public boolean accept (NFAState state)
		{
			return true;
		}
	}

	private static class AcceptingStateFilter implements StateFilter
	{
		public boolean accept (NFAState state)
		{
			return (state.isAccepting ());
		}
	}
}
