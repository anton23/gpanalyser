package uk.ac.imperial.doc.jexpressions.statements;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

public class Increment extends AbstractStatement {

	@Override
	public void accept(IStatementVisitor v) {
		if (v instanceof IIncrementVisitor)
			((IIncrementVisitor) v).visit(this);
	}

	public AbstractExpression getLhs() {
		return lhs;
	}

	public AbstractExpression getRhs() {
		return rhs;
	}

	AbstractExpression lhs;
	AbstractExpression rhs;

	public Increment(AbstractExpression lhs, AbstractExpression rhs) {
		super();
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override
	public String toString() {
		return "d" + lhs.toString() + "/dt += " + rhs.toString();
	}

}
