package uk.ac.imperial.doc.jexpressions.statements;

public class SkipStatement extends AbstractStatement {

	@Override
	public void accept(IStatementVisitor v) {
		v.visit(this);
	}

	@Override
	public String toString() {
		return "";
	}

}
