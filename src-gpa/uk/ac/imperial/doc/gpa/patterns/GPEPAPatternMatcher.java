package uk.ac.imperial.doc.gpa.patterns;

import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.gpa.pctmc.GPEPAPCTMC;
import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternMatcher;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.PCTMCWithAccumulations;
import uk.ac.imperial.doc.pctmc.representation.State;

public class GPEPAPatternMatcher extends PatternMatcher{
	@Override
	public AbstractExpression getMatchingExpression(State s) {
		if (!(s instanceof GPEPAState)){
			throw new AssertionError("Pattern is not a GPEPA state!");
		}
		GroupComponentPair pattern = ((GPEPAState)s).getPair();
		List<GroupComponentPair> pairs = new LinkedList<GroupComponentPair>();
		for (PEPAComponent c : model.getComponentDerivatives(
				pattern.getGroup(), componentDefinitions)) {

			if (c.matchPattern(pattern.getComponent())) {
				pairs.add(new GroupComponentPair(pattern.getGroup(), c));
			} else if (c instanceof ComponentId) {
				PEPAComponent d = componentDefinitions
						.getComponentDefinition(((ComponentId) c).getName());
				if (d.matchPattern(pattern.getComponent())) {
					pairs.add(new GroupComponentPair(pattern.getGroup(), c));
				}
			}
		}
		List<AbstractExpression> summands = new LinkedList<AbstractExpression>();  
		for (GroupComponentPair p:pairs){
			summands.add(CombinedProductExpression.createMeanExpression(new GPEPAState(p))); 
		}
		return SumExpression.create(summands); 
	}
	GroupedModel model; 
	PEPAComponentDefinitions componentDefinitions;
	
	public GPEPAPatternMatcher(PCTMC pctmc) {		
		super(pctmc);
		if (pctmc instanceof PCTMCWithAccumulations) {
			pctmc = ((PCTMCWithAccumulations) pctmc).getPctmc();
		}
		if (pctmc instanceof GPEPAPCTMC){
			GPEPAPCTMC gpctmc = (GPEPAPCTMC)pctmc; 
			this.model=gpctmc.getModel(); 
			this.componentDefinitions = gpctmc.getComponentDefinitions(); 
		} else {
			throw new AssertionError("GPEPA matcher not compatible with the PCTMC!");
		}
	} 
	
	
	
	
}
