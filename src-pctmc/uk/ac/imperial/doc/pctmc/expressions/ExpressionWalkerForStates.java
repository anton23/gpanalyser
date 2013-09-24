package uk.ac.imperial.doc.pctmc.expressions;

import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionWalkerWithConstants;
import uk.ac.imperial.doc.pctmc.representation.State;

public class ExpressionWalkerForStates extends ExpressionWalkerWithConstants implements
ICombinedProductExpressionVisitor, IPopulationVisitor {

	private Set<State> states = new HashSet<State>();

	@Override
	public void visit(CombinedProductExpression e) {
		for (State s : e.getProduct().getPopulationProduct().asMultiset()) {
			states.add(s);
		}
	}
	
	@Override
	public void visit(PopulationExpression e) {
		states.add(e.getState());
	}
	
	public Set<State> getStates() {
		return states;
	}
}
