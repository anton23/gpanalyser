package uk.ac.imperial.doc.jexpressions.expanded;

import java.util.Collections;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class ExpandedExpression extends AbstractExpression {

	private Polynomial numerator;
	private Polynomial denominator;

	protected ExpandedExpression(Polynomial numerator, Polynomial denominator) {
		super();
		this.numerator = numerator;
		this.denominator = denominator;
	}

	protected ExpandedExpression(Polynomial numerator) {
		this(numerator, Polynomial.getUnitPolynomial());
	}

	public static ExpandedExpression plus(ExpandedExpression a,
			ExpandedExpression b) {
		Polynomial newNumerator = Polynomial.plus(Polynomial.product(a
				.getNumerator(), b.getDenominator()), Polynomial.product(b
				.getNumerator(), a.getDenominator()));
		Polynomial newDenominator = Polynomial.product(a.getDenominator(), b
				.getDenominator());
		return ExpandedExpression.create(newNumerator, newDenominator);
	}

	public static ExpandedExpression product(ExpandedExpression a,
			ExpandedExpression b) {
		Polynomial newNumerator = Polynomial.product(b.getNumerator(), a
				.getNumerator());
		Polynomial newDenominator = Polynomial.product(a.getDenominator(), b
				.getDenominator());
		return ExpandedExpression.create(newNumerator, newDenominator);
	}

	public static ExpandedExpression divide(ExpandedExpression a,
			ExpandedExpression b) {
		Polynomial newNumerator = Polynomial.product(a.getNumerator(), b
				.getDenominator());
		Polynomial newDenominator = Polynomial.product(a.getDenominator(), b
				.getNumerator());
		return ExpandedExpression.create(newNumerator, newDenominator);
	}

	public static ExpandedExpression getOne() {
		return new ExpandedExpression(Polynomial.getUnitPolynomial());
	}
	
	public static ExpandedExpression getMinusOne() {
		return new ExpandedExpression(Polynomial.getMinusUnitPolynomial());
	}

	public static ExpandedExpression create(Polynomial numerator) {
		return create(numerator, Polynomial.getUnitPolynomial());
	}

	// Always have the smallest coefficient in numerator equal to 1
	public static ExpandedExpression create(Polynomial numerator,
			Polynomial denominator) {
		if (numerator.equals(Polynomial.getEmptyPolynomial())){			
			return new UnexpandableExpression(new DoubleExpression(0.0));
		}
		if (denominator.equals(Polynomial.getEmptyPolynomial())) {
			throw new AssertionError("Division by zero!");
		}
		
		// Small hack before proper polynomial division is implemneted:
		if (numerator.equals(denominator)) {
			return new UnexpandableExpression(new DoubleExpression(1.0));
		}
		Multiset<ExpandedExpression> commonFactorNumerator = Polynomial
				.getCommonFactor(numerator);
		Multiset<ExpandedExpression> commonFactorDenomiator = Polynomial
				.getCommonFactor(denominator);
		Multiset<ExpandedExpression> commonFactor = Multisets.intersection(
				commonFactorNumerator, commonFactorDenomiator);


		numerator = Polynomial.divide(numerator, commonFactor, 1.0);
		denominator = Polynomial.divide(denominator, commonFactor, 1.0);

		if (denominator.isNumber()) {
			numerator = Polynomial.divide(numerator, Polynomial.getOneTerm(),
					denominator.numericalValue());
			if (numerator.isNumber()) {
				return new UnexpandableExpression(new DoubleExpression(
						numerator.numericalValue()));
			} else if ((numerator.getRepresentation().size() == 1
					&& numerator.getRepresentation().entrySet().iterator()
							.next().getValue() == 1.0)
				    && numerator.getRepresentation().entrySet().iterator().next().getKey().size()==1){
				return numerator.getRepresentation().keySet().iterator().next().iterator().next();
			} else {
				return new ExpandedExpression(numerator);
			}
		} else
		if (!numerator.equals(Polynomial.getEmptyPolynomial())){
			Double minCoefficient = Collections.min(numerator.getRepresentation()
					.values());
			numerator = Polynomial.divide(numerator, Polynomial.getOneTerm(),
					minCoefficient);
			denominator = Polynomial.divide(denominator, Polynomial
					.getOneTerm(), minCoefficient);
		} 
		return new ExpandedExpression(numerator, denominator);
	}

	public boolean isNumber() {
		return false;
	}

	public Double numericalValue() {
		return null;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		String ret = "[" + numerator.toString() + "]";
		if (!denominator.equals(Polynomial.getUnitPolynomial())) {
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
			if (other.getDenominator() != null)
				return false;
		} else if (!denominator.equals(other.getDenominator()))
			return false;
		if (numerator == null) {
			if (other.getNumerator() != null)
				return false;
		} else if (!numerator.equals(other.getNumerator()))
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