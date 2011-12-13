package uk.ac.imperial.doc.gpepa.representation.model;

import java.util.List;

import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;


public class PEPAEvolutionEvent {

	private String action;
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
}
