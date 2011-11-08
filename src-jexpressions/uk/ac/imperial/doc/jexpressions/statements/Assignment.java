package uk.ac.imperial.doc.jexpressions.statements;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

public class Assignment extends AbstractStatement {

	@Override
	public void accept(IStatementVisitor v) {
		if (v instanceof IAssignmentVisitor)
			((IAssignmentVisitor) v).visit(this);
	}

	public AbstractExpression getLhs() {
		return lhs;
	}

	public AbstractExpression getRhs() {
		return rhs;
	}

	AbstractExpression lhs;
	AbstractExpression rhs;

	public Assignment(AbstractExpression lhs, AbstractExpression rhs) {
		super();
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override
	public String toString() {
		return "d" + lhs.toString() + "/dt = " + rhs.toString();
	}

}
