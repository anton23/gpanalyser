package uk.ac.imperial.doc.jexpressions.expressions;

/**
 * An expression for a (double) numerical constant.
 * 
 * @author Anton Stefanek
 * 
 */
public class DoubleExpression extends AbstractExpression {

	public static final AbstractExpression ZERO = new DoubleExpression(0.0);
	public static final AbstractExpression ONE = new DoubleExpression(1.0);
    public static final AbstractExpression MAX
        = new DoubleExpression(Double.MAX_VALUE);

	private Double value;

	/**
	 * Creates a new DoubleExpression with the given Double value.
	 * 
	 * @param value
	 *            of the expression
	 */
	public DoubleExpression(Double value) {
		super();
		this.value = value;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DoubleExpression))
			return false;
		DoubleExpression asConstant = (DoubleExpression) o;
		return this.value.equals(asConstant.getValue());
	}

	public Double getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
