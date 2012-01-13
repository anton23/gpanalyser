package uk.ac.imperial.doc.jexpressions.conditions;

public class LessThan extends ComparisonOperator {
	
	public LessThan() {
		
	}

	@Override
	public boolean compare(double left, double right) {
		return left < right;
	}

	@Override
	public String toString() {
		return "<";
	}
}
