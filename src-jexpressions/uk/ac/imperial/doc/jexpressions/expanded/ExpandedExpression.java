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
	
	public ExpandedExpression(Polynomial numerator) {
		this(numerator, Polynomial.getEmptyPolynomial());	
	}
	
	public static ExpandedExpression plus(ExpandedExpression a, ExpandedExpression b){
		Polynomial newNumerator = Polynomial.plus(Polynomial.product(a.getNumerator(),b.getDenominator()),
				        Polynomial.product(b.getNumerator(), a.getDenominator()));
		Polynomial newDenominator = Polynomial.product(a.getDenominator(), b.getDenominator());
		return new ExpandedExpression(
				newNumerator,
				newDenominator
				);
	}
	
	public static ExpandedExpression product(ExpandedExpression a,
			ExpandedExpression b) {
		Polynomial newNumerator = Polynomial.product(
				b.getNumerator(), a.getNumerator());
		Polynomial newDenominator = Polynomial.product(
				a.getDenominator(), b.getDenominator());
		return new ExpandedExpression(newNumerator, newDenominator);
	}
	
	public static ExpandedExpression divide(ExpandedExpression a, ExpandedExpression b){
		Polynomial newNumerator = Polynomial.product(
				a.getNumerator(), b.getDenominator());
		Polynomial newDenominator = Polynomial.product(
				a.getDenominator(), b.getNumerator());
		return new ExpandedExpression(newNumerator, newDenominator);
	}
	
	public static ExpandedExpression getOne(){
		return new ExpandedExpression(new Polynomial(Polynomial.getOne()));
	}

	// Always have the smallest coefficient in numerator equal to 1
	private void normalise(){
		if (numerator.getRepresentation().isEmpty()) return;
		// Small hack before proper polynomial division is implemneted:
		if (numerator.equals(denominator)){
			numerator = new Polynomial(Polynomial.getOne());
			denominator = Polynomial.getEmptyPolynomial();
		}
		Multiset<ExpandedExpression> commonFactorNumerator = Polynomial.getCommonFactor(numerator);
		Multiset<ExpandedExpression> commonFactorDenomiator = Polynomial.getCommonFactor(denominator);
		Multiset<ExpandedExpression> commonFactor = Multisets.intersection(commonFactorNumerator, commonFactorDenomiator);
		Double minCoefficient = Collections.min(numerator.getRepresentation().values());
		numerator = Polynomial.divide(numerator, commonFactor, minCoefficient);
		denominator = Polynomial.divide(denominator, commonFactor, 1.0/minCoefficient);
		if (denominator.isNumber()){
			numerator = Polynomial.divide(numerator, Polynomial.getOne(), denominator.numericalValue());
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

	public Polynomial getNumerator() {
		return numerator;
	}

	public Polynomial getDenominator() {
		return denominator;
	}
	
	
}