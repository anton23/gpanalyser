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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((decreasing == null) ? 0 : decreasing.hashCode());
		result = prime * result
				+ ((increasing == null) ? 0 : increasing.hashCode());
		result = prime * result + ((rate == null) ? 0 : rate.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EvolutionEvent other = (EvolutionEvent) obj;
		if (decreasing == null) {
			if (other.decreasing != null)
				return false;
		} else if (!decreasing.equals(other.decreasing))
			return false;
		if (increasing == null) {
			if (other.increasing != null)
				return false;
		} else if (!increasing.equals(other.increasing))
			return false;
		if (rate == null) {
			if (other.rate != null)
				return false;
		} else if (!rate.equals(other.rate))
			return false;
		return true;
	}
}
