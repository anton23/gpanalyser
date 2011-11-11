package uk.ac.imperial.doc.jexpressions.expanded;

import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;

import com.google.common.collect.Multiset;

public class ExpandedExpression extends AbstractExpression {

	private Polynomial numerator;
	private Polynomial denominator;

	protected ExpandedExpression(Polynomial numerator, Polynomial denominator) {
		super();
		this.numerator = numerator;
		this.denominator = denominator;
	}

	protected ExpandedExpression(Polynomial numerator) {
		this(numerator, Polynomial.getUnitPolynomial(numerator
				.getCoefficientSpecification()));
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

	public static ExpandedExpression getOne(ICoefficientSpecification normaliser) {
		return new ExpandedExpression(Polynomial.getUnitPolynomial(normaliser));
	}

	public static ExpandedExpression getMinusOne(
			ICoefficientSpecification normaliser) {
		return new ExpandedExpression(Polynomial
				.getMinusUnitPolynomial(normaliser));
	}

	public static ExpandedExpression create(Polynomial numerator) {
		return create(numerator, Polynomial.getUnitPolynomial(numerator
				.getCoefficientSpecification()));
	}

	// Always have the smallest coefficient in numerator equal to 1
	public static ExpandedExpression create(Polynomial numerator,
			Polynomial denominator) {
		if (numerator.equals(Polynomial.getEmptyPolynomial(numerator
				.getCoefficientSpecification()))) {
			return new UnexpandableExpression(new DoubleExpression(0.0),
					numerator.getCoefficientSpecification());
		}
		if (denominator.equals(Polynomial.getEmptyPolynomial(denominator
				.getCoefficientSpecification()))) {
			throw new AssertionError("Division by zero!");
		}

		// Small hack before proper polynomial division is implemneted:
		if (numerator.equals(denominator)) {
			return new UnexpandableExpression(DoubleExpression.ONE, numerator
					.getCoefficientSpecification());
		}
/*		Multiset<UnexpandableExpression> commonFactor = Polynomial
				.getGreatestCommonFactor(numerator, denominator);
		numerator = Polynomial.divide(numerator, commonFactor);
		denominator = Polynomial.divide(denominator, commonFactor);*/
		
		Polynomial gcd = Polynomial.greatestCommonDivisor(numerator, denominator);
		DivisionResult tmpN = Polynomial.divide(numerator, gcd);
		numerator = tmpN.getResult();			
		denominator = Polynomial.divide(denominator, gcd).getResult();

		/*DivisionResult divide = Polynomial.divide(numerator, denominator);
		if (divide.getRemainder().equals(
				Polynomial.getEmptyPolynomial(numerator
						.getCoefficientSpecification()))) {
			numerator = divide.getResult();
			denominator = Polynomial.getUnitPolynomial(denominator
					.getCoefficientSpecification());
		}*/

		if (denominator.isNumber()) {
			numerator = Polynomial.scalarProduct(numerator, DivExpression.create(DoubleExpression.ONE, denominator.numericalValue()));
			if (numerator.isNumber()) {
				return new UnexpandableExpression(numerator.numericalValue(),
						numerator.getCoefficientSpecification());
			} else {
				Entry<Multiset<UnexpandableExpression>, AbstractExpression> next = numerator
						.getRepresentation().entrySet().iterator().next();
				if ((numerator.getRepresentation().size() == 1 && numerator
						.getCoefficientSpecification().isOne(next.getValue()))
						&& next.getKey().size() == 1) {
					return numerator.getRepresentation().keySet().iterator()
							.next().iterator().next();
				} else {
					return new ExpandedExpression(numerator);
				}
			}
		} else
			/*
			 * TODO sort out normal form for fractions if
			 * (!numerator.equals(Polynomial.getEmptyPolynomial())){
			 * AbstractExpression minCoefficient =
			 * Collections.min(numerator.getRepresentation() .values());
			 * numerator = Polynomial.divide(numerator, Polynomial.getOneTerm(),
			 * minCoefficient); denominator = Polynomial.divide(denominator,
			 * Polynomial .getOneTerm(), minCoefficient); }
			 */
			return new ExpandedExpression(numerator, denominator);
	}

	public AbstractExpression toAbstractExpression() {
		return DivExpression.create(numerator.toAbstractExpression(),
				denominator.toAbstractExpression());
	}

	public boolean isNumber() {
		return false;
	}

	public AbstractExpression numericalValue() {
		return null;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		if (v instanceof IExpandedExpressionVisitor) {
			((IExpandedExpressionVisitor) v).visit(this);
		}
	}

	@Override
	public String toString() {
		String ret = "[" + numerator.toString() + "]";
		if (!denominator.equals(Polynomial.getUnitPolynomial(denominator
				.getCoefficientSpecification()))) {
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