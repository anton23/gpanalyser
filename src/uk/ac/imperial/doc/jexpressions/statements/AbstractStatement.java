package uk.ac.imperial.doc.jexpressions.statements;

public abstract class AbstractStatement {

	@Override
	public abstract String toString();

	public abstract void accept(IStatementVisitor v);
}
