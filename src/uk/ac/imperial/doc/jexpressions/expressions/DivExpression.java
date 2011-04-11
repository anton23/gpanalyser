package uk.ac.imperial.doc.jexpressions.expressions;

/**
 * An expression for a ratio of two expressions. 
 * 
 * @author Anton Stefanek
 *
 */

public class DivExpression extends AbstractExpression {
	
	/**
	 * Creates and possibly simplifies a DivExpression with the given numerator and denominators.
	 * @param numerator
	 * @param denominator
	 * @return
	 */
	public static AbstractExpression create(AbstractExpression numerator,
			AbstractExpression denominator) {
		if (numerator.equals(denominator)) {
			return new DoubleExpression(1.0);
		}
		if (numerator instanceof ZeroExpression) {
			return new ZeroExpression();
		}
		return new DivExpression(numerator, denominator);
	}

	private AbstractExpression denominator;

	private AbstractExpression numerator;
	protected DivExpression(AbstractExpression numerator,
			AbstractExpression denominator) {
		super();
		this.numerator = numerator;
		this.denominator = denominator;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof DivExpression))
			return false;
		DivExpression asDiv = (DivExpression) o;
		return this.numerator.equals(asDiv.getNumerator())
				&& this.denominator.equals(asDiv.getDenominator());
	}

	public AbstractExpression getDenominator() {
		return denominator;
	}

	public AbstractExpression getNumerator() {
		return numerator;
	}

	@Override
	public int hashCode() {
		return numerator.hashCode() * 23 + denominator.hashCode();
	}

	@Override
	public String toString() {
		return numerator.toString() + "/" + denominator.toString();
	}

}
