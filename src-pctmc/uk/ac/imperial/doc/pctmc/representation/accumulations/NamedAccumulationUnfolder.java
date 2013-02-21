package uk.ac.imperial.doc.pctmc.representation.accumulations;

import java.util.Map;

import com.google.common.collect.Multiset;

import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionWalkerWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.jexpressions.variables.IExpressionVariableVisitor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;

public class NamedAccumulationUnfolder extends ExpressionWalkerWithConstants implements
		IExpressionVariableVisitor, ICombinedProductExpressionVisitor {
	
	protected Map<NamedAccumulation, AbstractExpression> accODEs;

	public NamedAccumulationUnfolder(
			 Map<NamedAccumulation, AbstractExpression> accODEs) {
		super();
		this.accODEs = accODEs;
	}

	@Override
	public void visit(ExpressionVariable e) {
		e.getUnfolded().accept(this);
	}


	@Override
	public void visit(CombinedProductExpression e) {
		Multiset<AccumulationVariable> accumulatedProducts = e.getProduct().getAccumulatedProducts();
		for (AccumulationVariable a : accumulatedProducts.elementSet()) {
			if (a instanceof NamedAccumulation) {
				((NamedAccumulation) a).setDDt(accODEs.get(a));
			}
		}
	}
}
