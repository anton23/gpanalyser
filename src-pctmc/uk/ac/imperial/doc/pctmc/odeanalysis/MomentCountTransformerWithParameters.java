package uk.ac.imperial.doc.pctmc.odeanalysis;

import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionTransformerWithConstants;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IPopulationVisitor;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;

public class MomentCountTransformerWithParameters extends ExpressionTransformerWithConstants
		implements ICombinedProductExpressionVisitor, IPopulationVisitor {

	@Override
	public void visit(PopulationExpression e) {
		result = e;
	}

	@Override
	public void visit(CombinedProductExpression e) {
		result = e;
	}

}
