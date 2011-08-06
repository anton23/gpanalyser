package uk.ac.imperial.doc.jexpressions.expressions;


/**
 * A PEPA specific expression of the form div(a,b)*min(b,c).
 * @author Anton Stefanek
 *
 */
public class DivMinExpression extends AbstractExpression {

	/**
	 * Creates and possibly simplifies a DivMinExpression.
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static AbstractExpression create(AbstractExpression a,
			AbstractExpression b, AbstractExpression c) {
		AbstractExpression minExpression = MinExpression.create(b, c);

		if (a.equals(b))
			return minExpression;
		else
			return new DivMinExpression(a, b, c);
	}

	private AbstractExpression a, b, c;

	protected DivMinExpression(AbstractExpression a, AbstractExpression b,
			AbstractExpression c) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this);

	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof DivMinExpression))
			return false;
		DivMinExpression asDivMin = (DivMinExpression) o;
		return (a.equals(asDivMin.getA()) && b.equals(asDivMin.getB()) && c
				.equals(asDivMin.getC()));
	}

	public AbstractExpression getA() {
		return a;
	}

	public AbstractExpression getB() {
		return b;
	}

	public AbstractExpression getC() {
		return c;
	}


	@Override
	public int hashCode() {
		return a.hashCode() * 23 + b.hashCode() * 23 * 23 + c.hashCode();
	}

	@Override
	public String toString() {
		return ("(div(" + a + "," + b + ")*min(" + c + "," + b + "))");
	}

}
