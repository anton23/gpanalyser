package uk.ac.imperial.doc.pctmc.analysis.plotexpressions;

import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionWalkerWithConstants;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IPopulationVisitor;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;

public class IsConstantVisitor extends ExpressionWalkerWithConstants implements
		ICombinedProductExpressionVisitor, IPopulationVisitor {

	private boolean constant = true;

	@Override
	public void visit(CombinedProductExpression e) {
		constant = false;
	}
	
	
	

	@Override
	public void visit(PopulationExpression e) {
		constant = false;
	}




	public boolean isConstant() {
		return constant;
	}
	
	
}
