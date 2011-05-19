package uk.ac.imperial.doc.jexpressions.javaoutput.statements;

/**
 * An abstract class for specific implementations of Java based expression evaluators.
 * @author as1005
 *
 */
public abstract class AbstractExpressionEvaluator {
	protected double[] r;

	public void setRates(double[] r) {
		this.r = r;
	}
	
	public abstract int getNumberOfExpressions();
	
	public abstract double[] update(double[] values, double t);
}
