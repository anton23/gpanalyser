package uk.ac.imperial.doc.pctmc.odeanalysis.closures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IndicatorFunction;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.jexpressions.variables.IExpressionVariableVisitor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.plain.PlainState;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.representation.accumulations.AccumulationVariable;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class NormalClosureVisitorUniversal extends MomentCountTransformerWithParameters implements ICombinedProductExpressionVisitor, IExpressionVariableVisitor
{
	protected boolean m_insert;
	protected boolean m_inserted;
	protected int m_maxOrder;
	protected Map<State,Map<State, Integer>> m_distMap;
	protected CombinedPopulationProduct m_moment;

	private static Map<Integer,Map<AbstractExpression,Integer>> s_genClosures = new HashMap<Integer,Map<AbstractExpression,Integer>>();
	private static String[] s_genMomentNames = {"M1","M2","M3","M4","M5","M6","M7","M8","M9", "M10", "M11", "M12"}; 
	private static  Map<String,Integer> s_genMomentNameId = new HashMap<String,Integer>();
	static
	{
		s_genMomentNameId.put("M1", 0);
		s_genMomentNameId.put("M2", 1);
		s_genMomentNameId.put("M3", 2);
		s_genMomentNameId.put("M4", 3);
		s_genMomentNameId.put("M5", 4);
		s_genMomentNameId.put("M6", 5);
		s_genMomentNameId.put("M7", 6);
		s_genMomentNameId.put("M8", 7);
		s_genMomentNameId.put("M9", 8);
		s_genMomentNameId.put("M10", 9);
		s_genMomentNameId.put("M11", 10);
		s_genMomentNameId.put("M12", 10);
	}
	
	public NormalClosureVisitorUniversal(CombinedPopulationProduct _moment, int _maxOrder, Map<State, Map<State, Integer>> _distMap)
	{
		m_maxOrder = _maxOrder;
		m_distMap = _distMap;
		m_moment = _moment;
		m_inserted = false;
		m_insert = true;
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
			product = new CombinedPopulationProduct(m_moment.getPopulationProduct().getV(_e.getState()));
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
		if (m_insert)
		{
			m_inserted = true;
			int order = m_moment.getOrder() + _e.getProduct().getOrder();
			if (order <= m_maxOrder)
			{
				CombinedPopulationProduct product = CombinedPopulationProduct.getProductOf(m_moment,_e.getProduct());
				result = CombinedProductExpression.create(product);
				
				// Finally we separate moments that are distances independent
				// Currently this only works with order 2 closures
				if (m_distMap.size() > 0 && product.getOrder() == 2) {
					// Should we assume independence - if so we separate
					Multiset<State> ms = product.getPopulationProduct().getRepresentation();
					Object[] state = ms.toArray();
					
					Map<State, Integer> m = m_distMap.get(state[0]);
					if (m != null && m.containsKey(state[1])) {
						result = ProductExpression.create(
							CombinedProductExpression.createMeanExpression((State)state[0]),
							CombinedProductExpression.createMeanExpression((State)state[1])
						);
					}
				}
			}

			// Mean field closure
			else if (m_maxOrder == 1)
			{		
				List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
				for (Multiset.Entry<State> entry : m_moment.getPopulationProduct().getRepresentation().entrySet())
				{
					for (int i = 0; i < entry.getCount(); i++)
					{
						terms.add(CombinedProductExpression.create(CombinedPopulationProduct.getMeanPopulation(entry.getElement())));
					}
				}
				for (Multiset.Entry<AccumulationVariable> entry : m_moment.getAccumulatedProducts().entrySet())
				{
					for (int i = 0; i < entry.getCount(); i++)
					{
						terms.add(CombinedProductExpression.create(CombinedPopulationProduct.getMeanAccumulatedProduct(entry.getElement())));
					}
				}
				for (Multiset.Entry<State> entry : _e.getProduct().getPopulationProduct().getRepresentation().entrySet())
				{
					for (int i = 0; i < entry.getCount(); i++)
					{
						terms.add(CombinedProductExpression.create(CombinedPopulationProduct.getMeanPopulation(entry.getElement())));
					}
				}
				for (com.google.common.collect.Multiset.Entry<AccumulationVariable> entry : _e.getProduct().getAccumulatedProducts().entrySet())
				{
					for (int i = 0; i < entry.getCount(); i++)
					{
						terms.add(CombinedProductExpression.create(CombinedPopulationProduct.getMeanAccumulatedProduct(entry.getElement())));
					}
				}
				result = ProductExpression.create(terms);
			}
			// Normal closure
			else
			{
				genClosure(m_maxOrder,order);
				Map<AbstractExpression, Integer> closureSummands = applyNormalClosure(m_maxOrder,CombinedPopulationProduct.getProductOf(m_moment,_e.getProduct()));
				List<AbstractExpression> closure = new LinkedList<AbstractExpression>();
				for (Entry<AbstractExpression, Integer> exp : closureSummands.entrySet())
				{
					ProductExpression pe = (ProductExpression)exp.getKey();
					AbstractExpression prod = null;
					for (AbstractExpression ae : pe.getTerms()) {
						CombinedProductExpression cpe = (CombinedProductExpression)ae;
						CombinedPopulationProduct product = cpe.getProduct();
						// Finally we separate moments that are distances independent
						// Currently this only works with order 2 closures
						if (m_distMap.size() > 0 && product.getOrder() == 2) {
							// Should we assume independence - if so we separate
							Multiset<State> ms = product.getPopulationProduct().getRepresentation();
							Object[] state = ms.toArray();
							
							Map<State, Integer> m = m_distMap.get(state[0]);
							if (m != null && m.containsKey(state[1])) {
								ae = ProductExpression.create(
									CombinedProductExpression.createMeanExpression((State)state[0]),
									CombinedProductExpression.createMeanExpression((State)state[1])
								);
							}
						}
						prod = (prod == null) ? ae : ProductExpression.create(prod,ae);
					}					
					closure.add(ProductExpression.create(new DoubleExpression((double)exp.getValue()),prod));
				}
				result = SumExpression.create(closure);
			}
		}
		else
		{
			result = _e;
		}
	}
	
	
	
	/**
	 * Load template for (_maxorder, order(_cpp)) normal closure and replace
	 * place-holder names in template by state names in _cpp
	 * @param _maxorder
	 * @param _cpp
	 * @return
	 */
	private Map<AbstractExpression, Integer> applyNormalClosure(int _maxorder, CombinedPopulationProduct _cpp)
	{
		int i=0;
		PopulationProduct[] pops = new PopulationProduct[_cpp.getPopulationProduct().getOrder()];
		for (Multiset.Entry<State> e : _cpp.getPopulationProduct().getRepresentation().entrySet())
		{
			for (int j=0; j<e.getCount(); j++)
			{
				pops[i++] = PopulationProduct.getMeanProduct(e.getElement());
			}
		}
		int accStartIndex = i;
		AccumulationVariable[] accVars = new AccumulationVariable[_cpp.getAccumulatedProducts().size()];
		i = 0;
		for (com.google.common.collect.Multiset.Entry<AccumulationVariable> e : _cpp.getAccumulatedProducts().entrySet())
		{
			accVars[i++] = e.getElement();
		}
		
		// Get generic closure
		Map<AbstractExpression,Integer> closure = s_genClosures.get(getClosureId(_maxorder,_cpp.getOrder()));
		Map<AbstractExpression, Integer> closureSummands = new HashMap<AbstractExpression, Integer>();
		
		// Now translate generic pattern for _cpp
		for (Entry<AbstractExpression, Integer> e : closure.entrySet())
		{
			// Break up the moment product
			ProductExpression pex = ProductExpression.forceProduct(e.getKey());
			List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
			for (AbstractExpression ae : pex.getTerms())
			{
				CombinedProductExpression cpex = (CombinedProductExpression) ae;
				CombinedPopulationProduct cpp = cpex.getProduct();
				PopulationProduct popTemp = null;
				Multiset<AccumulationVariable> accumulatedProdTerms = HashMultiset.create();
				for (Multiset.Entry<State> e2 : cpp.getPopulationProduct().getRepresentation().entrySet())
				{
					int index = s_genMomentNameId.get(e2.getElement().toString());
					if (index < accStartIndex)
					{
						popTemp = PopulationProduct.getProduct(popTemp,pops[index]);
					}
					else
					{
						accumulatedProdTerms.add(accVars[index - accStartIndex]);
					}
				}
				terms.add(CombinedProductExpression.create(new CombinedPopulationProduct(popTemp,accumulatedProdTerms)));
			}
			AbstractExpression pexNew = ProductExpression.createOrdered(terms);
			Integer mult = closureSummands.get(pexNew);
			mult = (mult == null) ? e.getValue() : mult+e.getValue();
			closureSummands.put(pexNew,mult);
		}
		
		return closureSummands;
	}
	
	//****************************************************************************
	//	Normal moment closure template expression generation 
	//****************************************************************************
	/**
	 * @param _maxOrder
	 * @param _order
	 * @return a unique id for the (_maxOrder, _order) normal closure
	 */
	public static int getClosureId(int _maxOrder, int _order)
	{
		return _maxOrder*100+_order;
	}
	
	/**
	 * Generate a generic template for the (_maxOrder, _order) normal closure expression 
	 * @param _maxOrder
	 * @param _order
	 */
	private void genClosure(int _maxOrder, int _order)
	{
		// Check if we have already computed the closure
		int closureId = getClosureId(_maxOrder, _order);
		if (s_genClosures.containsKey(closureId))
		{
			return;
		}
		
		// Generate all closures (maxOrder,maxOrder+1),...,(_order-1,_order)
		for (int i=_order; i>_maxOrder; --i)
		{
			genIsserlisClosure(i);
		}
		
		// Now we generate the generic (_maxOrder, _order) closure
		Map<AbstractExpression,Integer> closure = new HashMap<AbstractExpression,Integer>(s_genClosures.get(getClosureId(_order-1, _order)));
		if (!s_genClosures.containsKey(closureId))
		{
			s_genClosures.put(closureId, closure);
		}

		for (int i=_order-1; i>_maxOrder; --i)
		{
			Set<AbstractExpression> remove = new HashSet<AbstractExpression>();
			Set<AbstractExpression> removeIfNull = new HashSet<AbstractExpression>();
			Map<AbstractExpression,Integer> tmpClosure = new HashMap<AbstractExpression,Integer>(closure);
			for (Entry<AbstractExpression, Integer> e : tmpClosure.entrySet())
			{
				// Break up the moment product
				ProductExpression pex = ProductExpression.forceProduct(e.getKey());
				Map<AbstractExpression,Integer> summands = null;
				List<AbstractExpression> terms = new LinkedList<AbstractExpression>(pex.getTerms());
				for (AbstractExpression ae : pex.getTerms())
				{
					CombinedProductExpression cpex = (CombinedProductExpression) ae;
					CombinedPopulationProduct cpp = cpex.getProduct();
					if (cpp.getOrder() == i)
					{
						terms.remove(ae);
						summands = applyNormalClosure(i-1,cpp);
						break;
					}
				}
				// Replace former moment by its closed expressions
				if (summands != null)
				{
					// First we multiply the closure terms with the remaining terms
					// and add the resulting terms to the closure summands
					for (Entry<AbstractExpression, Integer> e2 : summands.entrySet())
					{
						ProductExpression pex2 = ProductExpression.forceProduct(e2.getKey());
						List<AbstractExpression> terms2 = new LinkedList<AbstractExpression>(pex2.getTerms());
						terms2.addAll(terms);
						
						AbstractExpression expr = ProductExpression.createOrdered(terms2);
						Integer multiplicity = closure.get(expr);
						multiplicity  = (multiplicity == null) ? 0 : multiplicity;
						multiplicity  += e.getValue() * e2.getValue();
						closure.put(expr, multiplicity);
						if (multiplicity == 0)
						{
							removeIfNull.add(expr);
						}
					}
					remove.add(e.getKey());
				}
			}
			for (AbstractExpression ae : remove)
			{
				closure.remove(ae);
			}
			for (AbstractExpression ae : removeIfNull)
			{
				if (closure.get(ae) == 0)
				{
					closure.remove(ae);
				}
			}
		}
	}

	/**
	 * Generate the (_order-1,_order) normal closure according to Isserli's theorem
	 * @param _order
	 */
	private void genIsserlisClosure(int _order)
	{
		// Check if we have already computed the closure
		int closureId =  getClosureId(_order-1, _order);
		if (s_genClosures.containsKey(closureId))
		{
			return;
		}		
		
		Map<AbstractExpression,Integer> closure = new HashMap<AbstractExpression,Integer>();
		s_genClosures.put(closureId, closure);
	
		// Create E[M1 M2 M3 M4 ... M$(_order)]
		CombinedPopulationProduct[] states = new CombinedPopulationProduct[_order];
		for (int i=0; i<_order; ++i)
		{
			states[i] = CombinedPopulationProduct.getMeanPopulation(new PlainState(s_genMomentNames[i]));
		}
		
		// Add even moment extra terms
		if (_order % 2 == 0)
		{
			Set<Set<List<CombinedPopulationProduct>>> allPartitionsIntoPairs = NormalClosureVisitorUniversal.<CombinedPopulationProduct> getAllPartitionsIntoPairs(Arrays.asList(states));
			for (Set<List<CombinedPopulationProduct>> partition : allPartitionsIntoPairs)
			{
				// Needs to evaluate the product
				// E[(X1-u1)(X2-u2)]*E[(X3-u3)(X4-u4)]*...* + E[(X1-u1)(X3-u3)]*...
				// + ...
				List<List<CombinedPopulationProduct>> tmp = new ArrayList<List<CombinedPopulationProduct>>(partition);
				for (long i = 0; i < Math.pow(2, partition.size()); i++)
				{
					long iBit = i;
					int j = 0;
					int sign = 1;
					List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
					while (j < partition.size())
					{
						CombinedPopulationProduct m1 = tmp.get(j).get(0);
						CombinedPopulationProduct m2 = tmp.get(j).get(1);
						CombinedPopulationProduct m1m2 = CombinedPopulationProduct.getProductOf(m1, m2);
						if (iBit % 2 == 0)
						{
							terms.add(CombinedProductExpression.create(m1m2));
						} else
						{
							sign *= -1;
							terms.add(CombinedProductExpression.create(m1));
							terms.add(CombinedProductExpression.create(m2));
						}
						iBit /= 2;
						j++;
					}

					AbstractExpression expr = ProductExpression.createOrdered(terms);
					Integer multiplicity = closure.get(expr);
					multiplicity = (multiplicity == null) ? 0 : multiplicity;
					
					if (sign == -1)
					{
						if (--multiplicity != 0)
						{
							closure.put(expr, multiplicity);
						}
					}
					else
					{
						if (++multiplicity != 0)
						{
							closure.put(expr, multiplicity);
						}
					}
				}
			}
		}
		
		// Remaining terms
		double numberOfTerms = Math.pow(2.0, states.length);
		for (long i = 1; i < numberOfTerms; i++)
		{
			List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
			CombinedPopulationProduct product = null;
			long tmp = i;
			int j = 0;
			int sign = 1;
			while (j < states.length)
			{
				if (tmp % 2 == 0)
				{
					if (product == null)
					{
						product = states[j];
					}
					else
					{
						product = CombinedPopulationProduct.getProductOf(product, states[j]);
					}
				}
				else
				{
					terms.add(CombinedProductExpression.create(states[j]));
					sign = -sign;
				}
				tmp /= 2;
				j++;
			}
			if (product != null)
			{
				terms.add(CombinedProductExpression.create(product));
			}
			
			AbstractExpression expr = ProductExpression.createOrdered(terms);
			Integer multiplicity = closure.get(expr);
			multiplicity = (multiplicity == null) ? 0 : multiplicity;

			if (sign == 1)
			{
				if (--multiplicity != 0)
				{
					closure.put(expr, multiplicity);
				}
			}
			else
			{
				if (++multiplicity != 0)
				{
					closure.put(expr, multiplicity);
				}
			}
		}
	}	
	
	/**
	 * Find all pair partition pair required for even terms in normal closure
	 * @param l
	 * @return set of partitions
	 */
	public static <T> Set<Set<List<T>>> getAllPartitionsIntoPairs(List<T> l)
	{
		assert(l.size() % 2 == 0);
		Set<Set<List<T>>> ret = new HashSet<Set<List<T>>>();
		if (l.size()==2)
		{
			Set<List<T>> tmp = new HashSet<List<T>>();
			tmp.add(l);
			ret.add(tmp);
			return ret;
		}
		for (int i = 0; i<l.size(); i++)
		{
			for (int j = i+1; j<l.size(); j++)
			{
				List<T> smaller = new ArrayList<T>(l.size()-2);
				for (int k = 0; k<l.size(); k++)
				{
					if (k!=j && k!=i)
					{
						smaller.add(l.get(k));
					}
				}
				List<T> pair = new LinkedList<T>();
				pair.add(l.get(i));
				pair.add(l.get(j));
				Set<Set<List<T>>> smallerPartitions = getAllPartitionsIntoPairs(smaller);
				for (Set<List<T>> partition:smallerPartitions)
				{
					partition.add(pair);
					ret.add(partition);
				}
			}
		}
		return ret;
	}
}
