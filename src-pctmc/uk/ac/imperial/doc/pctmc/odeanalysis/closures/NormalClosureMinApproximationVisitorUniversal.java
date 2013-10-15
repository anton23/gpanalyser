package uk.ac.imperial.doc.pctmc.odeanalysis.closures;

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

public class NormalClosureMinApproximationVisitorUniversal extends NormalClosureVisitorUniversal
{
	
	
	Map<AbstractExpression, ExpressionVariable> m_usedVariables;
	int m_variableIndex;
	boolean alternative;
	
	String minProduct;
	
	public NormalClosureMinApproximationVisitorUniversal(CombinedPopulationProduct _moment, int _maxOrder, Map<AbstractExpression, ExpressionVariable> _usedVariables, int _variableIndex, boolean alternative)
	{
		super(_moment, _maxOrder);
		m_usedVariables = _usedVariables;
		m_variableIndex = _variableIndex;
		this.alternative = alternative;
		minProduct = "normalMinProduct";
		if (alternative) {
			minProduct = "normalMinProduct2";
		} 
	}
	
	public int getVariableIndex()
	{
		return m_variableIndex;
	}
	
	protected AbstractExpression considerVariable(AbstractExpression _a)
	{
		if (_a instanceof CombinedProductExpression) {
			return _a;
		}
		if (m_usedVariables.containsKey(_a))
		{
			return m_usedVariables.get(_a);
		}
		else
		{
			ExpressionVariable var = new ExpressionVariable("var" + String.format("%05d", m_variableIndex++));
			var.setUnfolded(_a);
			m_usedVariables.put(_a, var);
			return var;
		}		
	}
	
	@Override
	public void visit(DivMinExpression _e)
	{
		_e.getFullExpression().accept(this);
	}
	
	@Override
	public void visit(MinExpression _e)
	{
		AbstractExpression muA = considerVariable(_e.getA());
		AbstractExpression muB = considerVariable(_e.getB());

		Map<ExpressionVariable, AbstractExpression> var = new HashMap<ExpressionVariable, AbstractExpression>();
		AbstractExpression covAB = new CovarianceOfLinearCombinationsExpression(muA, muB, var);
		AbstractExpression varA = new CentralMomentOfLinearCombinationExpression(muA, 2, var);
		AbstractExpression varB = new CentralMomentOfLinearCombinationExpression(muB, 2, var);
		AbstractExpression theta = SumExpression.create(varA, varB, ProductExpression.create(new DoubleExpression(-2.0), covAB));
		
		AbstractExpression muA2 = _e.getA();
		AbstractExpression muB2 = _e.getB();
		theta = considerVariable(theta);

		if (m_moment.getOrder() > 0 && m_insert)
		{
			m_inserted = false;
			muA.accept(this);
			muA2 = considerVariable(result);
			m_inserted = false;
			muB.accept(this);
			muB2 = considerVariable(result);

			result = considerVariable(FunctionCallExpression.create(minProduct,	Lists.newArrayList(muA, muB, theta, muA2, muB2, CombinedProductExpression.create(m_moment))));
		}
		else
		{ 
			result = considerVariable(FunctionCallExpression.create("normalMin",Lists.newArrayList(muA, muB, theta)));
		}
		m_inserted = true;
	}
	
	/*
	@Override
	public void visit(IndicatorFunction e) {
		AbstractExpression left =e.getCondition().getLeft();
		AbstractExpression right = e.getCondition().getRight();
		Map<ExpressionVariable, AbstractExpression> var = new HashMap<ExpressionVariable, AbstractExpression>();
		AbstractExpression varLeft = new CentralMomentOfLinearCombinationExpression(left, 2, var);
		AbstractExpression varRight = new CentralMomentOfLinearCombinationExpression(right, 2, var);
		AbstractExpression cov = new CovarianceOfLinearCombinationsExpression(left, right, var);
		result = considerVariable(FunctionCallExpression.create("normalInequality", Lists.newArrayList(left, varLeft, right, varRight, cov)));
	}*/
	
	@Override
	public void visit(ProductExpression _e)
	{
		List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
		boolean oldInsert = m_insert;
		boolean oldInserted = m_inserted;
		boolean isInserted = false;
		AbstractExpression minTerm = null;
		for (AbstractExpression t : _e.getTerms())
		{
			if (t instanceof MinExpression)
			{
				minTerm = t;
			}
		}
		List<AbstractExpression> orderedTerms = new LinkedList<AbstractExpression>();
		if (minTerm != null)
		{
			orderedTerms.add(minTerm);
			for (AbstractExpression t:_e.getTerms())
			{
				if (t != minTerm)
				{
					orderedTerms.add(t);
				}
			}
		} 
		else
		{
			orderedTerms = _e.getTerms();
		}
		for (AbstractExpression t : orderedTerms)
		{
			m_inserted = false;
			t.accept(this);
			isInserted |= m_inserted;
			if (isInserted)
			{
				m_insert = false;
			}
			terms.add(result);
		}
		m_insert = oldInsert;
		m_inserted = oldInserted | isInserted;
		result = ProductExpression.create(terms);
	}
	
	@Override
	public void visit(FunctionCallExpression _e)
	{
		List<AbstractExpression> args = _e.getArguments();
		if (_e.getName().equals("normalMin") && m_insert)
		{
			AbstractExpression muA = args.get(0);
			AbstractExpression muB = args.get(1);
			AbstractExpression theta = args.get(2);
			m_inserted = false;
			muA.accept(this);
			AbstractExpression muA2 = considerVariable(result);
			m_inserted = false;
			muB.accept(this);
			AbstractExpression muB2 = considerVariable(result);
			if (m_moment.getAccumulatedProducts().isEmpty()) {
				result = FunctionCallExpression.create(minProduct,Lists.newArrayList(muA, muB, theta, muA2, muB2, CombinedProductExpression.create(m_moment)));
			} else {
				result = FunctionCallExpression.create(minProduct+"Acc",Lists.newArrayList(
						muA, muB, theta, muA, muB, DoubleExpression.ONE, muA2, muB2, 
						CombinedProductExpression.create(m_moment)));
			}
			m_inserted = true;
		}
		else if (_e.getName().equals(minProduct) && m_insert)
		{
			AbstractExpression muA = args.get(0);
			AbstractExpression muB = args.get(1);
			AbstractExpression muA2 = args.get(3);
			AbstractExpression muB2 = args.get(4);
			AbstractExpression theta = args.get(2);
			AbstractExpression add = args.get(5);
			
			m_inserted = false;
			muA2.accept(this);
			AbstractExpression muA3 = considerVariable(result);
			m_inserted = false;
			muB2.accept(this);
			AbstractExpression muB3 = considerVariable(result);
			m_inserted = false;
			add.accept(this);
			AbstractExpression add2 = considerVariable(result);
			if (m_moment.getAccumulatedProducts().isEmpty()) {			
				result = FunctionCallExpression.create(minProduct,Lists.newArrayList(muA, muB, theta, muA3, muB3,add2));
			} else {
				result = FunctionCallExpression.create(
						minProduct+"Acc",Lists.newArrayList(muA, muB, theta, muA2, muB2,add, muA3, muB3, add2));
			}
			m_inserted = true;
		}
		else
		{
			super.visit(_e);
		}
	}
}
