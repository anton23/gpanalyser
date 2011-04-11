package uk.ac.imperial.doc.jexpressions.expressions;

/**
 * An expression for a difference between two expressions.
 * @author Anton Stefanek
 *
 */
public class MinusExpression extends AbstractExpression {

	private AbstractExpression a;
	private AbstractExpression b;

	public MinusExpression(AbstractExpression a, AbstractExpression b) {
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
		if (!(o instanceof MinusExpression))
			return false;
		MinusExpression asMinus = (MinusExpression) o;
		return a.equals(asMinus.getA()) && b.equals(asMinus.getB());
	}

	public AbstractExpression getA() {
		return a;
	}

	public AbstractExpression getB() {
		return b;
	}

	@Override
	public int hashCode() {
		return a.hashCode() * 23 + b.hashCode() + 7;
	}

	@Override
	public String toString() {
		return a.toString() + "-(" + b.toString() + ")";
	}
}
