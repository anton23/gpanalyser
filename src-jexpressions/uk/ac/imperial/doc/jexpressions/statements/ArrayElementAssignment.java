package uk.ac.imperial.doc.jexpressions.statements;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

public class ArrayElementAssignment extends AbstractStatement {
	private String array;
	private AbstractExpression index;
	private AbstractExpression rhs;

	public ArrayElementAssignment(String array, AbstractExpression index,
			AbstractExpression rhs) {
		super();
		this.array = array;
		this.index = index;
		this.rhs = rhs;
	}

	public String getArray() {
		return array;
	}

	public AbstractExpression getIndex() {
		return index;
	}

	public AbstractExpression getRhs() {
		return rhs;
	}

	@Override
	public void accept(IStatementVisitor v) {
		v.visit(this);

	}

	@Override
	public String toString() {
		return array + "[" + index.toString() + "] = " + rhs + ";";
	}

}
