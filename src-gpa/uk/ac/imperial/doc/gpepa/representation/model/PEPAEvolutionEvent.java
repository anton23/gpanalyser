package uk.ac.imperial.doc.gpepa.representation.model;

import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.states.GPEPAActionCount;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.ExpressionVariableSetterPCTMC;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.State;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class PEPAEvolutionEvent {

    protected String action;
	private AbstractExpression rate;
	private List<GroupComponentPair> increases;
	private List<GroupComponentPair> decreases;

	public PEPAEvolutionEvent(String action, AbstractExpression rate,
			List<GroupComponentPair> increases,
			List<GroupComponentPair> decreases) {
		super();
        this.action = action;
		this.rate = rate;
		this.increases = increases;
		this.decreases = decreases;
	}

	@Override
	public String toString() {
		return "(" + action + "," + decreases + "->" + increases + ")";
	}

    public String getAction() {
        return action;
    }

	public AbstractExpression getRate() {
		return rate;
	}

	public List<GroupComponentPair> getIncreases() {
		return increases;
	}

	public List<GroupComponentPair> getDecreases() {
		return decreases;
	}

    public EvolutionEvent getEvolutionEvent(
            Set<String> countActions,
            Map<ExpressionVariable,AbstractExpression> unfoldedVariables) {
        List<State> increasing = new LinkedList<State>();
        List<State> decreasing = new LinkedList<State>();
        for (GroupComponentPair p : decreases){
            decreasing.add(new GPEPAState(p));
        }
        for (GroupComponentPair p : increases){
            increasing.add(new GPEPAState(p));
        }
        if (countActions.contains(action)){
            increasing.add(new GPEPAActionCount(action));
        }

        ExpressionVariableSetterPCTMC setter
                = new ExpressionVariableSetterPCTMC(unfoldedVariables);
        rate.accept(setter);
        RatePopulationToMomentTransformer transformer
                = new RatePopulationToMomentTransformer();
        rate.accept(transformer);
        rate = transformer.getResult();

        return new EvolutionEvent (decreasing, increasing, rate);
    }
}
