package uk.ac.imperial.doc.gpa.fsm;

import java.io.PrintStream;
import java.util.*;

public class NFAStateToPEPA
{
	public static Set<NFAState> detectBlindPaths (NFAState startingState)
	{
		Set<NFAState> blinds = new HashSet<NFAState> ();
		detectBlindPathsI (startingState, blinds,
			new HashSet<NFAState> ());
		return blinds;
	}

	private static void detectBlindPathsI
		(NFAState startingState, Set<NFAState> blinds, Set<NFAState> visited)
	{
		Map<ITransition, NFAState> transitions
			= startingState.getTransitions ();

		if (transitions.size () == 0)
		{
			blinds.add (startingState);
		}

		for (ITransition transition : transitions.keySet ())
		{
			NFAState nextState = transitions.get (transition);
			if (!visited.contains (nextState))
			{
				visited.add (nextState);
				detectBlindPathsI (nextState, blinds, visited);
			}
		}
	}

	// modifies the states branching from the starting state
	// (including the starting state)
	public static void HybridDFAtoPEPA
		(NFAState startingState, String naming, int counter, PrintStream out)
	{
		if (counter > 0)
		{
			startingState.setName (naming + counter);
		}
		else
		{
			startingState.setName (naming);
		}
		Set<NFAState> renamed = new HashSet<NFAState> ();
		renamed.add (startingState);
		HybridDFAtoPEPAI (startingState, naming, counter,
			new HashSet<NFAState> (), renamed, out);
	}

	private static int HybridDFAtoPEPAI (NFAState startingState, String naming,
		int counter, Set<NFAState> visited, Set<NFAState> renamed,
		PrintStream out)
	{
		out.print (startingState + " = ");
		visited.add (startingState);

		List<NFAState> derivedStates = new ArrayList<NFAState> ();
		Map<ITransition, NFAState> transitions
			= startingState.getTransitions ();

		for (ITransition transition : transitions.keySet ())
		{
			if (derivedStates.size () > 0)
			{
				out.print (" + ");
			}
			NFAState nextState = transitions.get (transition);
			if (!renamed.contains (nextState))
			{
				nextState.setName (naming + ++counter);
				renamed.add (nextState);
			}

            out.print (transition.toPEPAString () + nextState);
			derivedStates.add (nextState);
		}

		out.println (";");

		for (NFAState state : derivedStates)
		{
			if (!visited.contains (state))
			{
				counter = HybridDFAtoPEPAI
					(state, naming, counter, visited, renamed, out);
			}
		}

		return counter;
	}
}
