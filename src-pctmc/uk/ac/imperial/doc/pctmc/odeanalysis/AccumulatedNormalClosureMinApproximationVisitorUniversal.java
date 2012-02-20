package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CentralMomentOfLinearCombinationExpression;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CovarianceOfLinearCombinationsExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;

import com.google.common.collect.Lists;

public class AccumulatedNormalClosureMinApproximationVisitorUniversal extends AccumulatedNormalClosureVisitorUniversal {
	
	Map<AbstractExpression, ExpressionVariable> usedVariables;
	int variableIndex;

	
	public AccumulatedNormalClosureMinApproximationVisitorUniversal(
			CombinedPopulationProduct moment, int maxOrder, Map<AbstractExpression, ExpressionVariable> usedVariables, int variableIndex) {
		super(moment, maxOrder);
		this.usedVariables = usedVariables;
		this.variableIndex = variableIndex;
	}
	
	public int getVariableIndex() {
		return variableIndex;
	}
	
	protected AbstractExpression considerVariable(AbstractExpression a) {
		if (usedVariables.containsKey(a)) {
			return usedVariables.get(a);
		} else {
			ExpressionVariable var = new ExpressionVariable("var" + String.format("%05d", variableIndex++));
			var.setUnfolded(a);
			usedVariables.put(a, var);
			return var;
		}		
	}
	
	@Override
	public void visit(DivMinExpression e) {
		e.getFullExpression().accept(this);
	}
	
	@Override
	public void visit(MinExpression e) {

		AbstractExpression muA = considerVariable(e.getA());
		AbstractExpression muB = considerVariable(e.getB());

		Map<ExpressionVariable, AbstractExpression> var = new HashMap<ExpressionVariable, AbstractExpression>();
		AbstractExpression covAB = new CovarianceOfLinearCombinationsExpression(
				muA, muB, var);
		AbstractExpression varA = new CentralMomentOfLinearCombinationExpression(
				muA, 2, var);
		AbstractExpression varB = new CentralMomentOfLinearCombinationExpression(
				muB, 2, var);
		AbstractExpression theta = 
				SumExpression.create(
				varA, varB, ProductExpression.create(
						new DoubleExpression(-2.0), covAB));
		
		AbstractExpression muA2 = e.getA();
		AbstractExpression muB2 = e.getB();
		theta = considerVariable(theta);

		if (moment.getOrder() > 0 && insert) {
			inserted = false;
			muA.accept(this);
			muA2 = considerVariable(result);
			inserted = false;
			muB.accept(this);
			muB2 = considerVariable(result);

			result = considerVariable(FunctionCallExpression.create("normalMinProduct",
				Lists.newArrayList(muA, muB, theta, muA2, muB2, CombinedProductExpression
						.create(moment))	
			));
		} else { 
		
  	 /*  MinusExpression mAmB = new MinusExpression(muA, muB);

	   MinusExpression mBmA = new MinusExpression(muB, muA);

		AbstractExpression phiC1 = (FunctionCallExpression.create("safe_Phi", Lists.newArrayList(mBmA, theta)));
		AbstractExpression phiC2 = (FunctionCallExpression.create("safe_Phi", Lists.newArrayList(mAmB, theta)));
		AbstractExpression phi   = (FunctionCallExpression.create("safe_phi", Lists.newArrayList(mBmA, theta)));
		result = SumExpression.create(ProductExpression.create(muA2,
				phiC1), ProductExpression.create(muB2,
				phiC2), ProductExpression.create(
				new DoubleExpression(-1.0), theta2, phi));*/
				
		result = considerVariable(FunctionCallExpression.create("normalMin",
				Lists.newArrayList(muA, muB, theta)	
			));
		}
		inserted = true;
	}
	
	@Override
	public void visit(ProductExpression e) {
		List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
		boolean oldInsert = insert;
		boolean oldInserted = inserted;
		boolean isInserted = false;
		AbstractExpression minTerm = null;
		for (AbstractExpression t: e.getTerms()) {
			if (t instanceof MinExpression) {
				minTerm = t;
			}
		}
		List<AbstractExpression> orderedTerms = new LinkedList<AbstractExpression>();
		if (minTerm != null) {
			orderedTerms.add(minTerm);
			for (AbstractExpression t:e.getTerms()) {
				if (t != minTerm) {
					orderedTerms.add(t);
				}
			}
		} else {
			orderedTerms = e.getTerms();
		}
		for (AbstractExpression t : orderedTerms) {
			inserted = false;
			t.accept(this);
			isInserted |= inserted;
			if (isInserted) {
				insert = false;
			}
			terms.add(result);
		}
		insert = oldInsert;
		inserted = oldInserted | isInserted;
		result = ProductExpression.create(terms);
	}
	
	
	@Override
	public void visit(FunctionCallExpression e) {
		if (e.getName().equals("normalMin") && insert) {
			AbstractExpression muA = e.getArguments().get(0);
			AbstractExpression muB = e.getArguments().get(1);
			AbstractExpression theta = e.getArguments().get(2);
			inserted = false;
			muA.accept(this);
			AbstractExpression muA2 = considerVariable(result);
			inserted = false;
			muB.accept(this);
			AbstractExpression muB2 = considerVariable(result);

			result = FunctionCallExpression.create("normalMinProduct",
				Lists.newArrayList(muA, muB, theta, muA2, muB2, CombinedProductExpression
						.create(moment))	
			);
		} else if (e.getName().equals("normalMinProduct") && insert) {
			throw new AssertionError("This should not happen!");
/*			AbstractExpression muA = e.getArguments().get(0);
			AbstractExpression muB = e.getArguments().get(1);
			AbstractExpression theta = e.getArguments().get(2);
			AbstractExpression muA2 = e.getArguments().get(3);
			AbstractExpression muB2 = e.getArguments().get(4);
			AbstractExpression theta2 = e.getArguments().get(5);
			
			inserted = false;
			muA2.accept(this);
			muA2 = result;
			inserted = false;
			muB2.accept(this);
			muB2 = result;
			theta2 = (ProductExpression.create(CombinedProductExpression
					.create(moment), theta2));
			result = FunctionCallExpression.create("normalMinProduct",
				Lists.newArrayList(muA, muB, theta, muA2, muB2, theta2)	
			);*/
		}  else {
			super.visit(e);
		}
	}

}
