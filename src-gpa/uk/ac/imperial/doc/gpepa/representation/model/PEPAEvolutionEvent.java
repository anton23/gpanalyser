package uk.ac.imperial.doc.gpepa.representation.model;

import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

import java.util.List;


public class PEPAEvolutionEvent {

    private String action;
	private List<String> immediateActions;
	private AbstractExpression rate;
	private List<GroupComponentPair> increases;
	private List<GroupComponentPair> decreases;

	public PEPAEvolutionEvent(String action,
            List<String> immediateActions, AbstractExpression rate,
			List<GroupComponentPair> increases,
			List<GroupComponentPair> decreases) {
		super();
        this.action = action;
		this.immediateActions = immediateActions;
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

	public List<String> getImmediateActions() {
		return immediateActions;
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
}
