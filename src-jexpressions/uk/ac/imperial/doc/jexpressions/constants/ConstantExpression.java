package uk.ac.imperial.doc.jexpressions.constants;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;

/**
 * An expression for constants representing numerical values.
 * 
 * @author Anton Stefanek
 */
public class ConstantExpression extends AbstractExpression {

	private String constant;

	/**
	 * Creates a constant expression representing the constant with the given
	 * name.
	 * 
	 * @param constant
	 */
	public ConstantExpression(String constant) {
		super();
		this.constant = constant;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		if (v instanceof IConstantExpressionVisitor)
			((IConstantExpressionVisitor) v).visit(this);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ConstantExpression))
			return false;
		ConstantExpression asRate = (ConstantExpression) o;
		return this.constant.equals(asRate.getConstant());
	}

	/**
	 * Returns the name of this constant.
	 * 
	 * @return
	 */
	public String getConstant() {
		return constant;
	}

	@Override
	public int hashCode() {
		return constant.hashCode();
	}

	@Override
	public String toString() {
		return constant;
	}

}
