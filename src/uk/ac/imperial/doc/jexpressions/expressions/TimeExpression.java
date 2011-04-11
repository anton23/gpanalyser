package uk.ac.imperial.doc.jexpressions.expressions;

/**
 * An expression for the time constant.
 * @author as1005
 *
 */
public class TimeExpression extends AbstractExpression {

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this); 
	}

	@Override
	public boolean equals(Object o) {
		if (this==o) return true; 
		return (o instanceof TimeExpression);  
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public String toString() {
		return "t"; 
	}

}
