package uk.ac.imperial.doc.jexpressions.statements;

public interface IStatementVisitor {
	public void visit(AbstractStatement s);

	public void visit(Comment c);

	public void visit(SkipStatement s);

	public void visit(VariableDeclaration s);

	public void visit(ArrayElementAssignment s);

	public void visit(ArrayDeclaration s);

}
