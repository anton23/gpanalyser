package uk.ac.imperial.doc.jexpressions.expanded;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;

public class UnexpandableExpression extends ExpandedExpression {
	
	protected AbstractExpression expression;
	
	protected UnexpandableExpression(AbstractExpression expression) {
		super(null, null);
		this.expression = expression;
	}
	
	@Override
	public boolean isNumber() {
		return (expression instanceof DoubleExpression);
	}
	
	@Override
	public Double numericalValue() {
		if (expression instanceof DoubleExpression){
			return ((DoubleExpression)expression).getValue();
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		return expression.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (!(obj instanceof UnexpandableExpression)){
			return false;
		}
		UnexpandableExpression asUE = (UnexpandableExpression) obj;
		return expression.equals(asUE.expression);
	}
}