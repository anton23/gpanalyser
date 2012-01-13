package uk.ac.imperial.doc.jexpressions.expressions;

/**
 * An expression for a PEPA specific expression of the form
 * div(a*b,c*d)*min(c,d)
 * 
 * @author Anton Stefanek
 * 
 */

public class DivDivMinExpression extends AbstractExpression {

	public AbstractExpression getFullExpression() {
		return ProductExpression.create(
				PEPADivExpression.create(ProductExpression.create(a, b),
						ProductExpression.create(c, d)), MinExpression.create(
						c, d));
	}

	private AbstractExpression a, b, c, d;

	protected DivDivMinExpression(AbstractExpression a, AbstractExpression b,
			AbstractExpression c, AbstractExpression d) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	/**
	 * Creates and possibly simplifies an DivDivMin expression.
	 * 
	 * @param rateJointLeft
	 * @param rateJointRight
	 * @param rateLeft
	 * @param rateRight
	 * @return
	 */
	public static AbstractExpression create(AbstractExpression rateJointLeft,
			AbstractExpression rateJointRight, AbstractExpression rateLeft,
			AbstractExpression rateRight) {

		AbstractExpression minTerm = MinExpression.create(rateLeft, rateRight);

		boolean leftEquals = rateJointLeft.equals(rateLeft);
		boolean rightEquals = rateJointRight.equals(rateRight);

		if (leftEquals && rightEquals) {
			return minTerm;
		} else if (!leftEquals && rightEquals) {
			return DivMinExpression.create(rateJointLeft, rateLeft, rateRight);
		} else if (leftEquals && !rightEquals) {
			return DivMinExpression.create(rateJointRight, rateRight, rateLeft);
		} else {
			return new DivDivMinExpression(rateJointLeft, rateJointRight,
					rateLeft, rateRight);
		}
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

	public AbstractExpression getD() {
		return d;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof DivDivMinExpression))
			return false;
		DivDivMinExpression asDivDivMin = (DivDivMinExpression) o;
		return (a.equals(asDivDivMin.getA()) && b.equals(asDivDivMin.getB())
				&& c.equals(asDivDivMin.getC()) && d.equals(asDivDivMin.getD()));
	}

	@Override
	public int hashCode() {
		return a.hashCode() * 23 + b.hashCode() * 23 * 23 + c.hashCode() * 23
				* 23 * 23 + d.hashCode();
	}

	@Override
	public String toString() {
		return ("(div((" + a + ")*(" + b + "),(" + c + ")*(" + d + ")))*(min("
				+ c + "," + d + "))");
	}

}
