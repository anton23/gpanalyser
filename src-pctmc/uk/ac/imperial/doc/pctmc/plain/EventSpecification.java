package uk.ac.imperial.doc.pctmc.plain;

import java.util.List;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;
import uk.ac.imperial.doc.pctmc.representation.State;

public class EventSpecification {
	private List<State> decreasing; 
	private List<State> increasing; 
	private AbstractExpression rate;
	public EventSpecification(List<State> decreasing,
			List<State> increasing, AbstractExpression rate) {
		super();
		this.decreasing = decreasing;
		this.increasing = increasing;
		this.rate = rate;
	}
	public List<State> getDecreasing() {
		return decreasing;
	}
	public List<State> getIncreasing() {
		return increasing;
	}
	public AbstractExpression getRate() {
		return rate;
	}
	
	
	@Override
	public String toString() {
		return ToStringUtils.iterableToSSV(decreasing, "+") + 
		 "->" + ToStringUtils.iterableToSSV(increasing, "+") + 
		 " @ " + rate; 
	}
	
	

}
