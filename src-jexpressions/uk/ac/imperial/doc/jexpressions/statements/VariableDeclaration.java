package uk.ac.imperial.doc.jexpressions.statements;

public class VariableDeclaration extends AbstractStatement {

	@Override
	public void accept(IStatementVisitor v) {
		v.visit(this);
	}

	private int n;
	private String name;

	public VariableDeclaration(int n, String name) {
		super();
		this.n = n;
		this.name = name;
	}

	public int getN() {
		return n;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "double[] " + name + " = " + " new double[" + n + "];";
	}

}
