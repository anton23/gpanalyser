package uk.ac.imperial.doc.jexpressions.expressions.visitors;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.IConstantExpressionVisitor;



/**
 * An extension of ExpressionEvaluator supporting evaluation of constants.
 * @author as1005
 *
 */
public class ExpressionEvaluatorWithConstants extends ExpressionEvaluator implements IConstantExpressionVisitor {
	
	@Override
	public void visit(ConstantExpression e) {
		Double parameterValue = parameters.getConstantValue(e.getConstant());
		if (parameterValue == null){
			throw new AssertionError("Parameter " + e + " unknown!");
		}
		result = parameterValue;
	}


	private Constants parameters; 

	public ExpressionEvaluatorWithConstants(Constants constants) {
		super();
		this.parameters = constants;
	}


}
