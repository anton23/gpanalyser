package uk.ac.imperial.doc.jexpressions.expanded;

import java.util.Collections;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class ExpandedExpression extends AbstractExpression{
	
	private Polynomial numerator;
	private Polynomial denominator;
	
	public ExpandedExpression(Polynomial numerator, Polynomial denominator) {
		super();
		this.numerator = numerator;
		this.denominator = denominator;
		normalise();
	}
	
	// Always have the smallest coefficient in numerator equal to 1
	private void normalise(){
		Multiset<ExpandedExpression> commonFactorNumerator = Polynomial.getCommonFactor(numerator);
		Multiset<ExpandedExpression> commonFactorDenomiator = Polynomial.getCommonFactor(denominator);
		Multiset<ExpandedExpression> commonFactor = Multisets.intersection(commonFactorNumerator, commonFactorDenomiator);
		Double minCoefficient = Collections.min(numerator.getRepresentation().values());
		numerator = Polynomial.divide(numerator, commonFactor, minCoefficient);
		denominator = Polynomial.divide(denominator, commonFactor, 1.0/minCoefficient);
	}
	
	public boolean isNumber(){
		return false;
	}
	
	public Double numericalValue(){
		return null;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		// TODO Auto-generated method stub	
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
}