package uk.ac.imperial.doc.jexpressions.expanded;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

@SuppressWarnings("unchecked")
public class UnexpandableExpression extends ExpandedExpression {

	private ICoefficientSpecification normaliser;

	protected final AbstractExpression expression;

	public UnexpandableExpression(AbstractExpression expression,
			ICoefficientSpecification normaliser) {
		super(Polynomial.getEmptyPolynomial(normaliser), Polynomial
				.getEmptyPolynomial(normaliser));
		this.normaliser = normaliser;
		if (expression instanceof UnexpandableExpression) {
			this.expression = ((UnexpandableExpression) expression)
					.getExpression();
		} else {
			this.expression = expression;
		}
	}

	@Override
	public AbstractExpression toAbstractExpression() {
		return expression;
	}

	@Override
	public Polynomial getNumerator() {
		Multiset<UnexpandableExpression> tmp = HashMultiset
				.<UnexpandableExpression> create();
		tmp.add(this);
		return new Polynomial(normaliser, tmp);
	}

	@Override
	public Polynomial getDenominator() {
		return Polynomial.getUnitPolynomial(normaliser);
	}

	@Override
	public boolean isNumber() {
		return normaliser.isCoefficient(expression);
		// return scalarClasses.contains(expression.getClass());
	}

	@Override
	public AbstractExpression numericalValue() {
		if (isNumber()) {
			return expression;
		}
		return null;
	}

	@Override
	public String toString() {
		return "{" + expression.toString() + "}";
	}

	@Override
	public int hashCode() {
		return expression.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UnexpandableExpression)) {
			return false;
		}
		UnexpandableExpression asUE = (UnexpandableExpression) obj;
		return expression.equals(asUE.expression);
	}

	public AbstractExpression getExpression() {
		return expression;
	}

}