package uk.ac.imperial.doc.gpa.fsm;

import com.google.common.collect.Multimap;

import java.io.PrintStream;
import java.util.*;

public class NFAStateToPEPA
{
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

		Collection<NFAState> derivedStates = new HashSet<NFAState> ();
		Multimap<ITransition, NFAState> transitions
			= startingState.getTransitions ();

		for (ITransition transition : transitions.keySet ())
		{
			if (derivedStates.size () > 0)
			{
				out.print (" + ");
			}
			Collection<NFAState> nextStates = transitions.get (transition);
            for (NFAState nextState : nextStates)
            {
                if (!renamed.contains (nextState))
                {
                    nextState.setName (naming + ++counter);
                    renamed.add (nextState);
                }

                out.print (transition.toPEPAString () + nextState);
                derivedStates.add (nextState);
            }
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
