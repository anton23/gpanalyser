package uk.ac.imperial.doc.jexpressions.conditions;

public class GreaterThan extends ComparisonOperator {
	
	public GreaterThan() {
		
	}

	@Override
	public boolean compare(double left, double right) {
		return left > right;
	}

	@Override
	public String toString() {
		return ">";
	}
}
