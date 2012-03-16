package uk.ac.imperial.doc.gpa.pctmc;

import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionTransformerWithConstants;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.GeneralExpectationExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IGeneralExpectationExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IPopulationVisitor;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;

public class RatePopulationToMomentTransformer extends ExpressionTransformerWithConstants
implements ICombinedProductExpressionVisitor,
IGeneralExpectationExpressionVisitor,
IPopulationVisitor {

	@Override
	public void visit(GeneralExpectationExpression e) {
		throw new AssertionError("General expectations not allowed in rates!");
	}

	@Override
	public void visit(PopulationExpression e) {
		result = CombinedProductExpression.createMeanExpression(e.getState());		
	}

	@Override
	public void visit(CombinedProductExpression e) {
		result = e;		
	}
	

}
