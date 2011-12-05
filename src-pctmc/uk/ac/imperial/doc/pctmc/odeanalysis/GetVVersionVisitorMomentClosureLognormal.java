package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ejml.alg.dense.decomposition.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.State;

/***
 * 
 * @author Chris Guenther
 */
public class GetVVersionVisitorMomentClosureLognormal extends GetVVersionVisitor
{
	protected boolean m_insert = true; 
	protected boolean m_inserted = false; 
	
	protected int m_maxOrder;
	protected double m_mfStabiliser;
	
	public GetVVersionVisitorMomentClosureLognormal(PopulationProduct moment, int _maxOrder, double _mfStabiliser)
	{
		super(moment);
		m_maxOrder = _maxOrder;
		m_mfStabiliser = _mfStabiliser;
	}
		
	@Override
	public void visit(PEPADivExpression _e)
	{		
		if (m_insert)
		{
			_e.getNumerator().accept(this);
			result = PEPADivExpression.create(result, _e.getDenominator());
		} 
		else 
		{
			_e.getNumerator().accept(this);
			AbstractExpression newNumerator = result; 
			_e.getDenominator().accept(this); 
			result = PEPADivExpression.create(newNumerator,result); 
		}		
	}

	@Override
	public void visit(PopulationExpression _e)
	{
		CombinedPopulationProduct product;
		if (m_insert)
		{
			// TODO handle case if (moment.getOrder()>= m_maxOrder)
			product = new CombinedPopulationProduct(moment.getV(_e.getState()));
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
		boolean isInserted = false; 
		for (AbstractExpression t:_e.getTerms())
		{
			m_inserted = false; 
			t.accept(this);
			isInserted = m_inserted; 
			m_insert = (isInserted) ? false : m_insert;
			terms.add(result); 
		}
		m_insert = oldInsert; 
		result =  ProductExpression.create(terms); 
	}
	
	@Override
	public void visit(CombinedProductExpression _e)
	{
		if (_e.getProduct().getAccumulatedProducts().size()>0)
		{
			throw new AssertionError("Accumulations not allowed in rates!"); 
		}
		PopulationProduct nakedProduct = _e.getProduct().getNakedProduct();
		int order = moment.getOrder() + nakedProduct.getOrder();
		if (order <= m_maxOrder)
		{
			result = CombinedProductExpression.create(new CombinedPopulationProduct(PopulationProduct.getProduct(moment, nakedProduct)));
			return;
		}
	
		Map<State,Integer> higherOrderMoment = PopulationProduct.getProduct(moment, nakedProduct).getRepresentation();
		// Step 1: Find all moments that might be needed to close the higherOrderMoment.
		//         There at most (order + order choose 2 + ... + order choose m_maxOrder) such moments
		Set<AbstractExpression> possibleMoments = new HashSet<AbstractExpression>();
		State[] x = new State[order];
		int i = 0;
		for (State s:PopulationProduct.getProduct(moment, nakedProduct).asMultiset())
		{
			if (s!=null) x[i++] = s;
		}
		int[] indices = new int[order];
		
		while (getNextIndex(indices,m_maxOrder))
		{
			Map<State, Integer> prod = new HashMap<State, Integer>();
			for (int k=0; k<order; k++)
			{
				if (indices[k] == 0) {continue;}
				if (prod.containsKey(x[k]))
				{
					prod.put(x[k], prod.get(x[k])+1);
				}
				else
				{
					prod.put(x[k], 1);
				}
			}
			possibleMoments.add(CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(prod))));
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
			b.set(j, 0, vectorChoose(higherOrderMoment,((CombinedProductExpression)momentMapping.get(j)).getProduct().getNakedProduct().getRepresentation()));
			for (int k=0;k < possibleMoments.size();k++)
			{
				A.set(j,k,vectorChoose(((CombinedProductExpression)momentMapping.get(k)).getProduct().getNakedProduct().getRepresentation(),((CombinedProductExpression)momentMapping.get(j)).getProduct().getNakedProduct().getRepresentation()));
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
			result=(m_mfStabiliser > 0) ? MinExpression.create(result,new DoubleExpression(m_mfStabiliser)) : result;
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
			if (order <= _maxOrder) {return true;}
			if (order == _indices.length) {return false;}
		}
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
