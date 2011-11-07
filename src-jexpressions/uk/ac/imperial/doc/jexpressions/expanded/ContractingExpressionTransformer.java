package uk.ac.imperial.doc.jexpressions.expanded;

import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionTransformerWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;

public class ContractingExpressionTransformer extends ExpressionTransformerWithConstants implements IExpandedExpressionVisitor, ICombinedProductExpressionVisitor {

	public static AbstractExpression contractExpression(AbstractExpression e){
		ContractingExpressionTransformer t = new ContractingExpressionTransformer();
		e.accept(t);
		return t.getResult();
	}
	
	@Override
	public void visit(ExpandedExpression e) {
		result = e.toAbstractExpression();
	}

	@Override
	public void visit(CombinedProductExpression e) {
		// TODO Auto-generated method stub
		
	}
}
