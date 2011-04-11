package uk.ac.imperial.doc.jexpressions.statements;

public class Comment extends AbstractStatement {

	@Override
	public void accept(IStatementVisitor v) {
		v.visit(this);
	}

	String comment;

	public Comment(String comment) {
		super();
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public String toString() {
		return "//" + comment;
	}

}
