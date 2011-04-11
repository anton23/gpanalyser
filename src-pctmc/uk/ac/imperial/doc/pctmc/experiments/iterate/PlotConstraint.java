package uk.ac.imperial.doc.pctmc.experiments.iterate;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

public class PlotConstraint {
	private AbstractExpression expression; 
	private double atTime;
	private double minValue;
	public PlotConstraint(AbstractExpression expression, double atTime,
			double minValue) {
		super();
		this.expression = expression;
		this.atTime = atTime;
		this.minValue = minValue;
	}
	public AbstractExpression getExpression() {
		return expression;
	}
	public double getAtTime() {
		return atTime;
	}
	public double getMinValue() {
		return minValue;
	}
	
	@Override
	public String toString() {
		return expression + " at " + atTime + " >= " + minValue;  
	}	
}
