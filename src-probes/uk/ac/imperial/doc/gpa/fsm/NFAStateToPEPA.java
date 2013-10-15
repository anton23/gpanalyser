package uk.ac.imperial.doc.gpa.fsm;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.gpepa.representation.components.AbstractPrefix;
import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.igpepa.representation.components.ImmediatePrefix;
import uk.ac.imperial.doc.igpepa.representation.components.iChoice;
import uk.ac.imperial.doc.igpepa.representation.components.iPassivePrefix;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;

import com.google.common.collect.Multimap;

public class NFAStateToPEPA
{
	// modifies the states branching from the starting state
	// (including the starting state)
	public static Map<String,PEPAComponent> HybridDFAtoPEPA
		(NFAState startingState, String naming)
	{
		
		startingState.setName (naming);
		
		Set<NFAState> renamed = new HashSet<NFAState> ();
		renamed.add (startingState);
		

		Map<String, PEPAComponent> ret = HybridDFAtoPEPAI (startingState, naming, 
			new HashSet<NFAState> (), renamed);

		return ret;
	}

	private static Map<String,PEPAComponent> HybridDFAtoPEPAI (NFAState startingState, String naming,
		 Set<NFAState> visited, Set<NFAState> renamed)
	{
		
		Map<String,PEPAComponent> ret = new HashMap<String, PEPAComponent>();
		visited.add (startingState);

		Collection<NFAState> derivedStates = new HashSet<NFAState> ();
		Multimap<ITransition, NFAState> transitions
			= startingState.getTransitions ();
		List<AbstractPrefix> choices = new LinkedList<AbstractPrefix>();
		for (ITransition transition : transitions.keySet ())
		{
			Collection<NFAState> nextStates = transitions.get (transition);
            for (NFAState nextState : nextStates)
            {
                if (!renamed.contains (nextState))
                {
                    nextState.setName(naming + (renamed.size()));
                    renamed.add (nextState);
                }
                
                AbstractPrefix choice;
                if (transition instanceof SignalTransition) {
                	choice = new ImmediatePrefix(transition.toString(), 
                			 new DoubleExpression(1.0), new ComponentId(nextState.toString())
                			);
                } else {
                	choice = new iPassivePrefix(transition.toString(), 
                			null, new DoubleExpression(1.0), new ComponentId(nextState.toString()),
                			new LinkedList<ImmediatePrefix>());
                }
                
                choices.add(choice);
                derivedStates.add (nextState);
            }
		}
		ret.put(startingState.toString(), new iChoice(choices));
		

		for (NFAState state : derivedStates)
		{
			if (!visited.contains (state))
			{
				Map<String,PEPAComponent> newComponents = HybridDFAtoPEPAI
					(state, naming, visited, renamed);
				
				ret.putAll(newComponents);
			}
		}

		return ret;
	}
}
