package uk.ac.imperial.doc.gpa.gpepa;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.gpa.pctmc.GPEPAPCTMC;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.representation.model.PEPAEvolutionEvent;
import uk.ac.imperial.doc.gpepa.states.GPEPAActionCount;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ZeroExpression;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

public class GPEPAToPCTMC {
	
	public static PCTMC getPCTMC(PEPAComponentDefinitions componentDefinitions,GroupedModel model,Set<String> countActions){
		Map<State,AbstractExpression> initCounts = new HashMap<State,AbstractExpression>();
		for (GroupComponentPair p:model.getGroupComponentPairs(componentDefinitions)){
			GPEPAState s = new GPEPAState(p); 
			
			initCounts.put(s,model.getCountExpression(p));
		}
		for (String a:countActions){
			initCounts.put(new GPEPAActionCount(a), new ZeroExpression());
		}
		
		List<PEPAEvolutionEvent> observableEvolutionEvents = model.getObservableEvolutionEvents(componentDefinitions);
		List<EvolutionEvent> events = new LinkedList<EvolutionEvent>(); 
		for (PEPAEvolutionEvent event:observableEvolutionEvents){

			List<State> increasing = new LinkedList<State>();			
			List<State> decreasing = new LinkedList<State>();
			for (GroupComponentPair p:event.getDecreases()){
				decreasing.add(new GPEPAState(p));
			}
			for (GroupComponentPair p:event.getIncreases()){
				increasing.add(new GPEPAState(p));
			}
			if (countActions.contains(event.getAction())){
				increasing.add(new GPEPAActionCount(event.getAction()));
			}
			events.add(new EvolutionEvent(decreasing, increasing, event.getRate()));
		}
		return new GPEPAPCTMC(initCounts, events,componentDefinitions,model); 
	}
}
