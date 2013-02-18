package uk.ac.imperial.doc.gpa.pctmc;

import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.representation.model.PEPAEvolutionEvent;
import uk.ac.imperial.doc.gpepa.states.GPEPAActionCount;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

import java.util.*;

public class GPEPAToPCTMC {
    public static void updatePCTMC (PCTMC pctmc,
            PEPAComponentDefinitions componentDefinitions, GroupedModel model) {
        Map<State,Integer> stateIndices = pctmc.getStateIndex();
        AbstractExpression[] counts = pctmc.getInitCounts();
        for (GroupComponentPair p : model.getGroupComponentPairs(componentDefinitions)){
            GPEPAState state = new GPEPAState(p);
            AbstractExpression count = model.getCountExpression(p);
            Integer index = stateIndices.get (state);
            if (index == null)
            {
                if (!count.equals (DoubleExpression.ZERO))
                    throw new Error("State " + state + " is not inside " +
                            "the PCTMC you are trying to update.");
            }
            else
            {
                counts[index] = count;
            }
            pctmc.getInitMap().put(state, count);
        }
        // here we should assign model to this GPEPAPCTMC, but not for testing
    }
    
	public static PCTMC getPCTMC(PEPAComponentDefinitions componentDefinitions,
                 GroupedModel model, Set<String> countActions,
                 Map<ExpressionVariable, AbstractExpression> unfoldedVariables) {
		Map<State,AbstractExpression> initCounts = new LinkedHashMap<State,AbstractExpression>();
		for (GroupComponentPair p : model.getGroupComponentPairs(componentDefinitions)) {
			initCounts.put(new GPEPAState(p), model.getCountExpression(p));
		}
		for (String a : countActions) {
			initCounts.put(new GPEPAActionCount(a), DoubleExpression.ZERO);
		}
		
		List<PEPAEvolutionEvent> observableEvolutionEvents = model.getObservableEvolutionEvents(componentDefinitions);
		List<EvolutionEvent> events = new LinkedList<EvolutionEvent>(); 

		for (PEPAEvolutionEvent event : observableEvolutionEvents) {
			events.add(event.getEvolutionEvent(countActions, unfoldedVariables));
		}

        // remove duplicate events
        Set<EvolutionEvent> hs = new LinkedHashSet<EvolutionEvent>(events);
        events.clear();
        events.addAll(hs);

		return new GPEPAPCTMC(initCounts, events, componentDefinitions, model, countActions);
    }
}
