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
		if (numerator.getRepresentation().isEmpty()) return;
		Multiset<ExpandedExpression> commonFactorNumerator = Polynomial.getCommonFactor(numerator);
		Multiset<ExpandedExpression> commonFactorDenomiator = Polynomial.getCommonFactor(denominator);
		Multiset<ExpandedExpression> commonFactor = Multisets.intersection(commonFactorNumerator, commonFactorDenomiator);
		Double minCoefficient = Collections.min(numerator.getRepresentation().values());
		numerator = Polynomial.divide(numerator, commonFactor, minCoefficient);
		denominator = Polynomial.divide(denominator, commonFactor, 1.0/minCoefficient);
		if (denominator.isNumber()){
			numerator = Polynomial.divide(denominator, Polynomial.getOne(), denominator.numericalValue());
			denominator = Polynomial.getEmptyPolynomial();
		}
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
	public String toString() {
		String ret = "[" + numerator.toString() +"]";
		if (!denominator.equals(Polynomial.getEmptyPolynomial())){
			ret += "/[" + denominator.toString() + "]";
		}
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = ((denominator == null) ? 0 : denominator.hashCode());
		result = prime * result
				+ ((numerator == null) ? 0 : numerator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		ExpandedExpression other = (ExpandedExpression) obj;
		if (denominator == null) {
			if (other.denominator != null)
				return false;
		} else if (!denominator.equals(other.denominator))
			return false;
		if (numerator == null) {
			if (other.numerator != null)
				return false;
		} else if (!numerator.equals(other.numerator))
			return false;
		return true;
	}
	
	
}