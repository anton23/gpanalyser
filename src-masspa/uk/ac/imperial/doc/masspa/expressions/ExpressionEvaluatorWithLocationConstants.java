package uk.ac.imperial.doc.masspa.expressions;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.masspa.representation.model.Location;
import uk.ac.imperial.doc.masspa.representation.model.VarLocation;

public class ExpressionEvaluatorWithLocationConstants extends ExpressionEvaluatorWithConstants
{
	protected Location loc;
	public ExpressionEvaluatorWithLocationConstants(Constants _constants, Location _loc)
	{
		super(_constants);
		loc=_loc;
	}

	@Override
	public void visit(ConstantExpression e)
	{
		// Try to find better match constName@location instead of const
		Double parameterValue = constants.getConstantValue(e.getConstant().toString().replace(VarLocation.getInstance().toString(), "")+loc.toString());
		if (parameterValue == null){
			super.visit(e);
			return;
		}
		result = parameterValue;
	}
}
