package uk.ac.imperial.doc.jexpressions.conditions;

public abstract class ComparisonOperator {
	
	public abstract boolean compare(double left, double right); 
	
	@Override
	public abstract String toString();
	
}
