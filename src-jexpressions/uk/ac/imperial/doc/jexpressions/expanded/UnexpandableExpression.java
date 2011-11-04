package uk.ac.imperial.doc.jexpressions.expanded;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class UnexpandableExpression extends ExpandedExpression {
	
	protected AbstractExpression expression;
	public UnexpandableExpression(AbstractExpression expression) {
		super(Polynomial.getEmptyPolynomial(), Polynomial.getEmptyPolynomial());
		this.expression = expression;
		Multiset<ExpandedExpression> tmp = HashMultiset.<ExpandedExpression>create();
		tmp.add(this);
	}
	
	@Override
	public Polynomial getNumerator() {
		Multiset<ExpandedExpression> tmp = HashMultiset.<ExpandedExpression>create();
		tmp.add(this);
		return new Polynomial(tmp);
	}
	
	@Override
	public Polynomial getDenominator() {
		return Polynomial.getUnitPolynomial();
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
	public String toString() {
		return expression.toString();
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