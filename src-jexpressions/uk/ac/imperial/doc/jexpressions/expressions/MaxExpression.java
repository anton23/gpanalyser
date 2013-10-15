package uk.ac.imperial.doc.jexpressions.expressions;

/**
 * An expression for the terms max(a,b).
 * 
 * @author Chris Guenther
 * 
 */
public class MaxExpression extends AbstractExpression {

	/**
	 * Creates and possibly simplifies a MaxExpression.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static AbstractExpression create(AbstractExpression a,
			AbstractExpression b) {
		if (a.equals(b))
			return a;
		if (a instanceof DoubleExpression && b instanceof DoubleExpression) {
			double value = Math.max(((DoubleExpression) a).getValue(),
					((DoubleExpression) b).getValue());
			return new DoubleExpression(value);
		}
		return new MaxExpression(a, b);
	}

	private AbstractExpression a, b;

	protected MaxExpression(AbstractExpression a, AbstractExpression b) {
		super();
		this.a = a;
		this.b = b;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MaxExpression))
			return false;
		MaxExpression asMax = (MaxExpression) o;
		return this.a.equals(asMax.getA()) && this.b.equals(asMax.getB());
	}

	public AbstractExpression getA() {
		return a;
	}

	public AbstractExpression getB() {
		return b;
	}

	@Override
	public int hashCode() {
		return a.hashCode() * 23 + b.hashCode();
	}

	@Override
	public String toString() {
		return "max(" + a.toString() + "," + b.toString() + ")";
	}
}
