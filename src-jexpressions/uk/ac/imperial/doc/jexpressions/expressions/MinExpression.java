package uk.ac.imperial.doc.jexpressions.expressions;


/**
 * An expression for the terms min(a,b).
 * @author Anton Stefanek
 *
 */
public class MinExpression extends AbstractExpression {

	/**
	 * Creates and possibly simplifies a MinExpression.
	 * @param a
	 * @param b
	 * @return
	 */
	public static AbstractExpression create(AbstractExpression a,
			AbstractExpression b) {
		if (a.equals(b))
			return a;
		return new MinExpression(a, b);
	}


	private AbstractExpression a, b;

	protected MinExpression(AbstractExpression a, AbstractExpression b) {
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
		if (!(o instanceof MinExpression))
			return false;
		MinExpression asMin = (MinExpression) o;
		return this.a.equals(asMin.getA()) && this.b.equals(asMin.getB());
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
		return "min(" + a.toString() + "," + b.toString() + ")";
	}
}
