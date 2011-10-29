package uk.ac.imperial.doc.pctmc.expressions;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;

public class GeneralExpectationExpression extends AbstractExpression {
	private AbstractExpression expression;

	public GeneralExpectationExpression(AbstractExpression expression) {
		super();
		this.expression = expression;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeneralExpectationExpression other = (GeneralExpectationExpression) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Eg[" + expression.toString() + "]";
	}

	@Override
	public void accept(IExpressionVisitor v) {
		if (v instanceof IGeneralExpectationExpressionVisitor) {
			((IGeneralExpectationExpressionVisitor) v).visit(this);
		} else {
			throw new AssertionError(
					"Not supported visit for a general expectation expression!");
		}
	}

	public AbstractExpression getExpression() {
		return expression;
	}
}
