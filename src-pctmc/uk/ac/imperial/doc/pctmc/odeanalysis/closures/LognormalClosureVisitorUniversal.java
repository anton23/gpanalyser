package uk.ac.imperial.doc.pctmc.odeanalysis.closures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.ejml.alg.dense.decomposition.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;

import com.google.common.collect.Multiset;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IndicatorFunction;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.jexpressions.variables.IExpressionVariableVisitor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.odeanalysis.MomentCountTransformerWithParameters;
import uk.ac.imperial.doc.pctmc.representation.State;

/***
 * 
 * @author Chris Guenther
 */
public class LognormalClosureVisitorUniversal extends MomentCountTransformerWithParameters implements ICombinedProductExpressionVisitor, IExpressionVariableVisitor
{
	protected boolean m_insert = true; 
	protected boolean m_inserted = false; 
	
	protected int m_maxOrder;
	protected double m_mfStabiliser;
	protected CombinedPopulationProduct m_moment;
	
	public LognormalClosureVisitorUniversal(CombinedPopulationProduct _moment, int _maxOrder, double _mfStabiliser)
	{
		m_moment = _moment;
		m_maxOrder = _maxOrder;
		m_mfStabiliser = _mfStabiliser;
	}

	@Override
	public void visit(PowerExpression _e)
	{
		onlyMultiply(_e);
	}

	@Override
	public void visit(ExpressionVariable _e)
	{
		if (m_moment.getOrder() == 0 )
		{
			result = _e;
		}
		else
		{
			_e.getUnfolded().accept(this);
		}
	}

	@Override
	public void visit(PEPADivExpression _e)
	{
		if (m_insert)
		{
			_e.getNumerator().accept(this);
			result = PEPADivExpression.create(result, _e.getDenominator());
			m_inserted = true;
		}
		else
		{
			boolean oldInsert = m_insert;
			m_insert = true;
			_e.getNumerator().accept(this);
			AbstractExpression newNumerator = result;
			_e.getDenominator().accept(this);
			m_insert = oldInsert;
			result = PEPADivExpression.create(newNumerator, result);
		}
	}
	
	protected void onlyMultiply(AbstractExpression _e)
	{
		if (m_insert)
		{
			result = ProductExpression.create(_e, CombinedProductExpression.create(m_moment));
			m_inserted = true;
		}
		else
		{
			result = _e;
		}
	}

	@Override
	public void visit(ConstantExpression _e)
	{
		onlyMultiply(_e);
	}

	@Override
	public void visit(DoubleExpression _e)
	{
		onlyMultiply(_e);
	}

	@Override
	public void visit(IndicatorFunction _e)
	{
		result = _e;
	}
	
	@Override
	public void visit(DivMinExpression _e)
	{
		_e.getFullExpression().accept(this);
	}
	
	@Override
	public void visit(PopulationExpression _e)
	{
		CombinedPopulationProduct product;
		if (m_insert)
		{
			product = new CombinedPopulationProduct(m_moment.getNakedProduct().getV(_e.getState()));
			m_inserted = true;
		}
		else
		{
			product = new CombinedPopulationProduct(PopulationProduct.getMeanProduct(_e.getState()));
		}
		result = CombinedProductExpression.create(product);
	}

	@Override
	public void visit(ProductExpression _e)
	{
		List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
		boolean oldInsert = m_insert;
		boolean oldInserted = m_inserted;
		boolean isInserted = false;
		AbstractExpression momentTerm = null;
		for (AbstractExpression t: _e.getTerms())
		{
			if (t instanceof CombinedProductExpression)
			{
				momentTerm = t;
			}
		}
		List<AbstractExpression> orderedTerms = new LinkedList<AbstractExpression>();
		if (momentTerm != null)
		{
			orderedTerms.add(momentTerm);
			for (AbstractExpression t:_e.getTerms())
			{
				if (t != momentTerm)
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
		if (m_insert)
		{
			result = ProductExpression.create(_e, CombinedProductExpression.create(m_moment));
			m_inserted = true;
		}
		else
		{
			result = _e;
		}
	}
	
	@Override
	public void visit(DivExpression _e)
	{
		onlyMultiply(_e);
	}

	@Override
	public void visit(CombinedProductExpression _e)
	{
		int order = m_moment.getOrder() + _e.getProduct().getOrder();
		CombinedPopulationProduct moment = CombinedPopulationProduct.getProductOf(m_moment,_e.getProduct());
		result = CombinedProductExpression.create(moment);
		if (m_insert)
		{
			m_inserted = true;

			// Mean field closure
			if (m_maxOrder == 1)
			{		
				List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
				for (Entry<State, Integer> entry : moment.getNakedProduct().getRepresentation().entrySet())
				{
					for (int i = 0; i < entry.getValue(); i++)
					{
						terms.add(CombinedProductExpression.create(CombinedPopulationProduct.getMeanPopulation(entry.getKey())));
					}
				}
				for (Multiset.Entry<PopulationProduct> entry : moment.getAccumulatedProducts().entrySet())
				{
					for (int i = 0; i < entry.getCount(); i++)
					{
						terms.add(CombinedProductExpression.create(CombinedPopulationProduct.getMeanAccumulatedProduct(entry.getElement())));
					}
				}
				result = ProductExpression.create(terms);
			}
			// Lognormal closure
			else
			{
				applyLognormalClosure((CombinedProductExpression)CombinedProductExpression.create(moment), order);
			}
		}
	}
	
	public void applyLognormalClosure(CombinedProductExpression _e, int _order)
	{
        CombinedPopulationProduct[] x = new CombinedPopulationProduct[_order];
        int i = 0;
        for (State s : _e.getProduct().getNakedProduct().asMultiset())
        {
                if (s != null)
                {
                	x[i++] = CombinedPopulationProduct.getMeanPopulation(s);
                }
        }
        for (PopulationProduct p : _e.getProduct().getAccumulatedProducts())
        {
                x[i++] = CombinedPopulationProduct.getMeanAccumulatedProduct(p);
        }	

		// Step 1: Find all moments that might be needed to close the higherOrderMoment.
		//         There at most (order + order choose 2 + ... + order choose m_maxOrder) such moments
		Set<AbstractExpression> possibleMoments = new HashSet<AbstractExpression>();
		AbstractExpression meanfield=new DoubleExpression(m_mfStabiliser);
		for (CombinedPopulationProduct cpp : x)
		{
			meanfield=ProductExpression.create(meanfield, CombinedProductExpression.create(cpp));
		}
		int[] indices = new int[_order];
		
		while (getNextIndex(indices,m_maxOrder))
		{
			CombinedPopulationProduct cpp = null;
			for (int k=0; k<_order; k++)
			{
				if (indices[k] == 0) {continue;}
				if (cpp == null)
				{
					cpp = x[k];
				}
				else
				{
					cpp = CombinedPopulationProduct.getProductOf(cpp, x[k]);
				}
			}
			possibleMoments.add(CombinedProductExpression.create(cpp));
		}
		
		// Step 2: Enumerate the moments
		Map<Integer,AbstractExpression> momentMapping = new HashMap<Integer,AbstractExpression>();
		i=0;
		for (AbstractExpression ae : possibleMoments)
		{
			momentMapping.put(i++,ae);
		}
		
		// Step 3: Create and solve system of linear equations (See Singh's paper)
		SimpleMatrix A = new SimpleMatrix(possibleMoments.size(),possibleMoments.size());
		SimpleMatrix b = new SimpleMatrix(possibleMoments.size(),1);
		
		// Create A and b 
		for (int j=0;j < possibleMoments.size();j++)
		{
			b.set(j, 0, vectorChoose(_e,(CombinedProductExpression)momentMapping.get(j)));
			for (int k=0;k < possibleMoments.size();k++)
			{
				A.set(j,k,vectorChoose((CombinedProductExpression)momentMapping.get(k),(CombinedProductExpression)momentMapping.get(j)));
			}	
		}
		
		// Step 4: Generate moments
		try
		{
			SimpleMatrix sol = A.solve(b);
			List<AbstractExpression> closedFormNum = new LinkedList<AbstractExpression>();
			List<AbstractExpression> closedFormDen = new LinkedList<AbstractExpression>();
			for (int j=0; j < possibleMoments.size(); j++)
			{
				int power = (int)sol.get(j,0);
				if (power > 0)
				{
					if (power > 1)
					{
						closedFormNum.add(PowerExpression.create(momentMapping.get(j),new IntegerExpression(power)));
					}
					else
					{
						closedFormNum.add(momentMapping.get(j));
					}
				}
				else if (power < 0)
				{
					if (power < -1)
					{
						closedFormDen.add(PowerExpression.create(momentMapping.get(j),new IntegerExpression(-power)));
					}
					else
					{
						closedFormDen.add(momentMapping.get(j));
					}
				}
			}
			if (closedFormNum.size()==0){closedFormNum.add(new IntegerExpression(1));}
			if (closedFormDen.size()==0){closedFormDen.add(new IntegerExpression(1));}
			result=PEPADivExpression.create(ProductExpression.create(closedFormNum),ProductExpression.create(closedFormDen));
			result=(m_mfStabiliser > 0) ? MinExpression.create(result,meanfield) : result;
		}
		catch(SingularMatrixException exception)
		{
			  throw new AssertionError("Singular matrix");
		}
	}

	private boolean getNextIndex(int[] _indices, int _maxOrder)
	{
		while(true)
		{
			int carry=1;
			int order=0;
			for (int i=0; i<_indices.length; i++)
			{
				_indices[i] = (_indices[i] + carry) % 2;
				if(_indices[i]==1){carry=0;}
				order += _indices[i];
			}
			if (order == _indices.length) {return false;}
			if (order <= _maxOrder) {return true;}
		}
	}

	private double vectorChoose(CombinedProductExpression _cppTop, CombinedProductExpression _cppBottom)
	{
		return vectorChoose(_cppTop.getProduct().getNakedProduct().getRepresentation(),_cppBottom.getProduct().getNakedProduct().getRepresentation());
	}	
	
	private double vectorChoose(Map<State, Integer> _top, Map<State, Integer> _bottom)
	{
		double ret=1;
		Set<State> states = new HashSet<State>();
		states.addAll(_top.keySet());
		states.addAll(_bottom.keySet());
		for (State s : states)
		{
			Integer l = _top.get(s);
			l = (l == null) ? 0 : l;
			Integer h = _bottom.get(s);
			h = (h == null) ? 0 : h;
			
			if (l < h) {return 0;}
			ret *= factorial(l)/(factorial(l-h)*factorial(h));
		}
		return ret;
	}

	private double factorial(double _fact)
	{
		double result=1;
		if (_fact < 2) {return result;}
		for (int i=2;i <= _fact;i++) {result *= i;}
		return result;
	}
}
