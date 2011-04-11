package uk.ac.imperial.doc.jexpressions.expressions;

/**
 * An expression for a^b.
 * @author Anton Stefanek
 *
 */
public class PowerExpression extends AbstractExpression {

	public PowerExpression(AbstractExpression expression,
			AbstractExpression exponent) {
		super();
		this.exponent = exponent;
		this.expression = expression;
	}
	
	/**
	 * Creates and possibly simplifies a PowerExpression.
	 * @param expression
	 * @param exponent
	 * @return
	 */
	public static AbstractExpression create(AbstractExpression expression,
			AbstractExpression exponent){
		if (exponent.equals(new DoubleExpression(1.0))){
			return expression;
		}
		return new PowerExpression(expression, exponent);
	}

	private AbstractExpression exponent;
	private AbstractExpression expression;

	public AbstractExpression getExponent() {
		return exponent;
	}

	public AbstractExpression getExpression() {
		return expression;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof PowerExpression))
			return false;
		PowerExpression asPower = (PowerExpression) o;
		return (expression.equals(asPower.getExpression()) && exponent
				.equals(asPower.getExponent()));
	}

	@Override
	public int hashCode() {
		return expression.hashCode() * 23 + 2;
	}

	@Override
	public String toString() {
		return "(" + expression + ")^(" + exponent + ")";
	}

}
