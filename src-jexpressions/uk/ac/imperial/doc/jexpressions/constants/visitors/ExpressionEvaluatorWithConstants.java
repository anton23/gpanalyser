package uk.ac.imperial.doc.jexpressions.constants.visitors;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.IConstantExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionEvaluator;

/**
 * An extension of ExpressionEvaluator supporting evaluation of constants.
 * 
 * @author as1005
 * 
 */
public class ExpressionEvaluatorWithConstants extends ExpressionEvaluator
		implements IConstantExpressionVisitor {

	@Override
	public void visit(ConstantExpression e) {
		Double parameterValue = constants.getConstantValue(e.getConstant());
		if (parameterValue == null) {
			throw new AssertionError("Constant " + e + " unknown!");
		}
		result = parameterValue;
	}

	protected Constants constants;

	public ExpressionEvaluatorWithConstants(Constants constants) {
		super();
		this.constants = constants;
	}

}
