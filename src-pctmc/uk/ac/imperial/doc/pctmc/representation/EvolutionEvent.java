package uk.ac.imperial.doc.pctmc.representation;

import java.util.List;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;


public class EvolutionEvent {
	private List<State> decreasing;
	private List<State> increasing; 
	AbstractExpression rate;
	public List<State> getDecreasing() {
		return decreasing;
	}
	public List<State> getIncreasing() {
		return increasing;
	}
	public AbstractExpression getRate() {
		return rate;
	}
	public EvolutionEvent(List<State> decreasing, List<State> increasing,
			AbstractExpression rate) {
		super();
		this.decreasing = decreasing;
		this.increasing = increasing;
		this.rate = rate;
	} 
	
	@Override
	public String toString() {
		return ToStringUtils.iterableToSSV(decreasing, "+")+"->"
		           +  ToStringUtils.iterableToSSV(increasing, "+")+" @ "+
		        rate.toString();
		
	}
}
