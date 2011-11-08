package uk.ac.imperial.doc.jexpressions.expressions;

public class ExpressionWrapper extends AbstractExpression {

	protected final AbstractExpression internalExpression;

	public ExpressionWrapper(AbstractExpression internalExpression) {
		super();
		this.internalExpression = internalExpression;
	}

	@Override
	public int hashCode() {
		return internalExpression.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpressionWrapper other = (ExpressionWrapper) obj;
		if (internalExpression == null) {
			if (other.internalExpression != null)
				return false;
		} else if (!internalExpression.equals(other.internalExpression))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return internalExpression.toString();
	}

	@Override
	public void accept(IExpressionVisitor v) {
		internalExpression.accept(v);
	}
}
