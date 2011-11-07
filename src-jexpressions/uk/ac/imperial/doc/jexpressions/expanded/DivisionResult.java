package uk.ac.imperial.doc.jexpressions.expanded;

public class DivisionResult {
	private final Polynomial result;
	private final Polynomial remainder;
	
	public Polynomial getResult() {
		return result;
	}
	public Polynomial getRemainder() {
		return remainder;
	}
	public DivisionResult(Polynomial result, Polynomial remainder) {
		super();
		this.result = result;
		this.remainder = remainder;
	}
}
