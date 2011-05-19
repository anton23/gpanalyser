package uk.ac.imperial.doc.jexpressions.expressions;


/**
 * An expression for the numerical constant 0. 
 * @author as1005
 *
 */
public class ZeroExpression extends AbstractExpression {

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this);
	}

	@Override
	public String toString() {
		return "0.0";
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof ZeroExpression);
	}

}
