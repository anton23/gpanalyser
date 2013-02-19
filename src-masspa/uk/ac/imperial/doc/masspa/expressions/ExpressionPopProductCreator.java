package uk.ac.imperial.doc.masspa.expressions;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.IConstantExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MaxExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinusExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.expressions.TimeExpression;
import uk.ac.imperial.doc.jexpressions.expressions.UMinusExpression;
import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionTransformer;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.jexpressions.variables.IExpressionVariableVisitor;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAAgentPop;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IPopulationProductVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IPopulationVisitor;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProductExpression;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * This class inlines both functions and variables
 * 
 * @author Chris Guenther
 * @param <d>
 */
public class ExpressionPopProductCreator extends ExpressionTransformer  implements IConstantExpressionVisitor, IPopulationVisitor, IPopulationProductVisitor, ICombinedProductExpressionVisitor, IExpressionVariableVisitor
{
	private MASSPAAgentPop m_pop;
	private boolean m_popPushThrough = false;
	
	public boolean hasPushedThrough() {return m_popPushThrough;}
	
	public ExpressionPopProductCreator(MASSPAAgentPop _pop)
	{
		m_pop = _pop;
	}

	@Override
	public void visit(IntegerExpression e)
	{
		result = new IntegerExpression(e.getValue());
	}

	@Override
	public void visit(UMinusExpression e)
	{
		e.getE().accept(this);
		result = new UMinusExpression(result);
	}
	
	@Override
	public void visit(TimeExpression e)
	{
		result = e;
	}
	
	@Override
	public void visit(AbstractExpression e)
	{
		throw new AssertionError(String.format(Messages.s_COMPILER_INTERNAL_UNSUPPORTED_EXPR_VISIT,e));
	}

	@Override
	public void visit(DoubleExpression e)
	{
		result = e;
	}
	
	@Override
	public void visit(ConstantExpression e)
	{
		result = e;
	}

	@Override
	public void visit(PEPADivExpression e)
	{
		e.getNumerator().accept(this);
		AbstractExpression newNumerator = (m_popPushThrough) ? result : ProductExpression.create(result,new PopulationExpression(m_pop));
		//e.getDenominator().accept(this);
		//AbstractExpression newDenominator = result;
		
		result = PEPADivExpression.create(newNumerator, e.getDenominator());
		m_popPushThrough=true;
	}
	
	@Override
	public void visit(DivExpression e)
	{
		e.getNumerator().accept(this);
		AbstractExpression newNumerator = (m_popPushThrough) ? result : ProductExpression.create(result,new PopulationExpression(m_pop));
		//e.getDenominator().accept(this);
		//AbstractExpression newDenominator = result;
		
		result = PEPADivExpression.create(newNumerator, e.getDenominator());
		m_popPushThrough=true;
	}

	@Override
	public void visit(MinusExpression e)
	{
		e.getA().accept(this);
		AbstractExpression newA = (m_popPushThrough) ? result : ProductExpression.create(result,new PopulationExpression(m_pop));
		m_popPushThrough = false;
		e.getB().accept(this);
		AbstractExpression newB = (m_popPushThrough) ? result : ProductExpression.create(result,new PopulationExpression(m_pop));
		
		result = new MinusExpression(newA, newB);
		m_popPushThrough=true;
	}

	@Override
	public void visit(PowerExpression e)
	{
		result = e;
		/*
		e.getExpression().accept(this);
		AbstractExpression newExpression = (m_popPushThrough) ? result : ProductExpression.create(result,new PopulationExpression(m_pop));
		m_popPushThrough = false;
		e.getExponent().accept(this);
		AbstractExpression newExpo = (m_popPushThrough) ? result : ProductExpression.create(result,new PopulationExpression(m_pop));;
		
		result = new PowerExpression(newExpression, newExpo);
		m_popPushThrough=true;*/
	}
	
	@Override
	public void visit(MaxExpression e)
	{
		e.getA().accept(this);
		AbstractExpression newA = (m_popPushThrough) ? result : ProductExpression.create(result,new PopulationExpression(m_pop));
		if (result.equals(new DoubleExpression(0.0)))
		{
			newA = new DoubleExpression(0.0);
		}
		m_popPushThrough = false;
		e.getB().accept(this);
		AbstractExpression newB = (m_popPushThrough) ? result : ProductExpression.create(result,new PopulationExpression(m_pop));
		
		result = MaxExpression.create(newA, newB);
		m_popPushThrough=true;
	}

	@Override
	public void visit(MinExpression e)
	{
		e.getA().accept(this);
		AbstractExpression newA = (m_popPushThrough) ? result : ProductExpression.create(result,new PopulationExpression(m_pop));
		m_popPushThrough = false;
		e.getB().accept(this);
		AbstractExpression newB = (m_popPushThrough) ? result : ProductExpression.create(result,new PopulationExpression(m_pop));
		
		result = MinExpression.create(newA, newB);
		m_popPushThrough=true;
	}

	@Override
	public void visit(ProductExpression e)
	{
		AbstractExpression[] ts = new AbstractExpression[e.getTerms().size()];
		int i = 0;
		for (AbstractExpression t : e.getTerms())
		{
			ts[i] = t;
			if (!m_popPushThrough)
			{
				t.accept(this);
				ts[i] = result;
			}
			i++;
		}
		if (!m_popPushThrough)
		{
			ts[0] =  ProductExpression.create(ts[0],new PopulationExpression(m_pop));
			m_popPushThrough = true;
		}
		
		result = ProductExpression.create(ts);
	}

	@Override
	public void visit(SumExpression e)
	{
		AbstractExpression[] ts = new AbstractExpression[e.getSummands().size()];
		int i = 0;
		for (AbstractExpression t : e.getSummands())
		{
			t.accept(this);
			ts[i++] = (m_popPushThrough) ? result : ProductExpression.create(result,new PopulationExpression(m_pop));
			m_popPushThrough=false;
		}

		result = SumExpression.create(ts);
		m_popPushThrough=true;
	}
	
	@Override
	public void visit(CombinedProductExpression e)
	{
		Multiset<State> product = HashMultiset.create(e.getProduct().getPopulationProduct().getRepresentation());
		product.add(m_pop);

		result = CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(product)));
		m_popPushThrough=true;
	}

	@Override
	public void visit(PopulationExpression e)
	{
		Multiset<State> product = HashMultiset.create();
		if (e.getState().equals(m_pop))
		{
			product.add(m_pop, 2);
		}
		else
		{
			product.add(e.getState(), 1);
			product.add(m_pop, 1);
		}

		result = CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(product)));
		m_popPushThrough=true;
	}

	@Override
	public void visit(PopulationProductExpression e)
	{
		throw new AssertionError(String.format(Messages.s_COMPILER_INTERNAL_UNSUPPORTED_EXPR_VISIT,e));
	}
	
	@Override
	public void visit(ExpressionVariable e)
	{
		throw new AssertionError(String.format(Messages.s_COMPILER_INTERNAL_UNSUPPORTED_EXPR_VISIT,e));
	}
	
	@Override
	public void visit(FunctionCallExpression e)
	{
		throw new AssertionError(String.format(Messages.s_COMPILER_INTERNAL_UNSUPPORTED_EXPR_VISIT,e));
	}
}
