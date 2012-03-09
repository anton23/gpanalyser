package uk.ac.imperial.doc.pctmc.odeanalysis;

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
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.Multiset;

public class AccumulatedNormalClosureVisitorUniversal extends
		MomentCountTransformerWithParameters implements
		ICombinedProductExpressionVisitor, IExpressionVariableVisitor {

	protected boolean insert;
	protected boolean inserted;
	protected int maxOrder;

	protected CombinedPopulationProduct moment;

	public AccumulatedNormalClosureVisitorUniversal(
			CombinedPopulationProduct moment, int maxOrder) {
		this.maxOrder = maxOrder;
		this.moment = moment;
		this.inserted = false;
		this.insert = true;
	}
	
	@Override
	public void visit(PowerExpression e) {
		onlyMultiply(e);
	}
	

	@Override
	public void visit(ExpressionVariable e) {
		if (moment.getOrder() == 0 ) {
			result = e;
		} else {
			e.getUnfolded().accept(this);
		}
	}

	@Override
	public void visit(PEPADivExpression e) {
		if (insert) {
			e.getNumerator().accept(this);
			result = PEPADivExpression.create(result, e.getDenominator());
			inserted = true;
		} else {
			boolean oldInsert = insert;
			insert = true;
			e.getNumerator().accept(this);
			AbstractExpression newNumerator = result;
			e.getDenominator().accept(this);
			insert = oldInsert;
			result = PEPADivExpression.create(newNumerator, result);
		}
	}
	
	protected void onlyMultiply(AbstractExpression e) {
		if (insert) {
			result = ProductExpression.create(e, CombinedProductExpression
					.create(moment));
			inserted = true;
		} else {
			result = e;
		}
	}

	@Override
	public void visit(ConstantExpression e) {
		onlyMultiply(e);
	}

	@Override
	public void visit(DoubleExpression e) {
		onlyMultiply(e);
	}

	@Override
	public void visit(IndicatorFunction e) {
		result = e;
	}
	
	@Override
	public void visit(DivMinExpression e) {
		e.getFullExpression().accept(this);
	}
	
	@Override
	public void visit(PopulationExpression e) {
		CombinedPopulationProduct product;
		if (insert) {
			// TODO handle case if (moment.getOrder()>= maxOrder)
			product = new CombinedPopulationProduct(moment.getNakedProduct()
					.getV(e.getState()));
			inserted = true;
		} else {
			product = new CombinedPopulationProduct(PopulationProduct
					.getMeanProduct(e.getState()));
		}
		result = CombinedProductExpression.create(product);
	}

	@Override
	public void visit(ProductExpression e) {
		List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
		boolean oldInsert = insert;
		boolean oldInserted = inserted;
		boolean isInserted = false;
		AbstractExpression momentTerm = null;
		for (AbstractExpression t: e.getTerms()) {
			if (t instanceof CombinedProductExpression) {
				momentTerm = t;
			}
		}
		List<AbstractExpression> orderedTerms = new LinkedList<AbstractExpression>();
		if (momentTerm != null) {
			orderedTerms.add(momentTerm);
			for (AbstractExpression t:e.getTerms()) {
				if (t != momentTerm) {
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
		if (insert) {
			result = ProductExpression.create(e, CombinedProductExpression
					.create(moment));
			inserted = true;
		} else {
			result = e;
		}
	}
	
	@Override
	public void visit(DivExpression e) {
		onlyMultiply(e);
	}

	@Override
	public void visit(CombinedProductExpression e) {
		if (insert) {
			inserted = true;
			int order = moment.getOrder() + e.getProduct().getOrder();
			if (order <= maxOrder) {
				result = CombinedProductExpression
						.create(CombinedPopulationProduct.getProductOf(moment,
								e.getProduct()));
			} else if (maxOrder == 1) {
				List<AbstractExpression> terms = new LinkedList<AbstractExpression>();

				for (Entry<State, Integer> entry : moment.getNakedProduct()
						.getRepresentation().entrySet()) {
					for (int i = 0; i < entry.getValue(); i++) {
						terms.add(CombinedProductExpression
								.create(CombinedPopulationProduct
										.getMeanPopulation(entry.getKey())));
					}
				}

				for (Multiset.Entry<PopulationProduct> entry : moment
						.getAccumulatedProducts().entrySet()) {
					for (int i = 0; i < entry.getCount(); i++) {
						terms.add(CombinedProductExpression
								.create(CombinedPopulationProduct
										.getMeanAccumulatedProduct(entry
												.getElement())));
					}
				}
				for (Entry<State, Integer> entry : e.getProduct()
						.getNakedProduct().getRepresentation().entrySet()) {
					for (int i = 0; i < entry.getValue(); i++) {
						terms.add(CombinedProductExpression
								.create(CombinedPopulationProduct
										.getMeanPopulation(entry.getKey())));
					}
				}
				for (com.google.common.collect.Multiset.Entry<PopulationProduct> entry : e
						.getProduct().getAccumulatedProducts().entrySet()) {
					for (int i = 0; i < entry.getCount(); i++) {
						terms.add(CombinedProductExpression
								.create(CombinedPopulationProduct
										.getMeanAccumulatedProduct(entry
												.getElement())));
					}
				}
				result = ProductExpression.create(terms);
			} else {
				/*
				CombinedPopulationProduct[] x = new CombinedPopulationProduct[order];
				int i = 0;
				for (State s : e.getProduct().getNakedProduct().asMultiset()) {
					if (s != null)
						x[i++] = CombinedPopulationProduct.getMeanPopulation(s);
				}
				for (State s : moment.getNakedProduct().asMultiset()) {
					if (s != null)
						x[i++] = CombinedPopulationProduct.getMeanPopulation(s);
				}
				for (PopulationProduct p : e.getProduct()
						.getAccumulatedProducts()) {
					x[i++] = CombinedPopulationProduct
							.getMeanAccumulatedProduct(p);
				}
				for (PopulationProduct p : moment.getAccumulatedProducts()) {
					x[i++] = CombinedPopulationProduct
							.getMeanAccumulatedProduct(p);
				}*/
				genClosure(maxOrder,order);
				Map<AbstractExpression, Integer> closureSummands = applyClosure(maxOrder,CombinedPopulationProduct.getProductOf(moment,e.getProduct()));
				List<AbstractExpression> closure = new LinkedList<AbstractExpression>();
				for (Entry<AbstractExpression, Integer> exp : closureSummands.entrySet())
				{
					closure.add(ProductExpression.create(new DoubleExpression((double)exp.getValue()),exp.getKey()));
				}
				result = SumExpression.create(closure);
				
				
				/*
				if (order % 2 == 1) {
					result = AccumulatedNormalClosureVisitorUniversal
							.getOddMomentInTermsOfCovariances(x);
				} else {
					result = AccumulatedNormalClosureVisitorUniversal
							.getEventMomentInTermsOfCovariances(x);
				}*/
			}
		} else {
			result = e;
		}
	}

	// This class will represent our generic normal closure
	class GenClosure
	{	
		public Map<AbstractExpression,Integer> m_summands;
		
		GenClosure()
		{
			m_summands = new HashMap<AbstractExpression,Integer>();
		}
	
		GenClosure(GenClosure _closure)
		{
			this(_closure.m_summands);
		}	
		
		GenClosure(Map<AbstractExpression,Integer> _summands)
		{
			m_summands = new HashMap<AbstractExpression,Integer>(_summands);
		}		
	}
	
	// Generic state
	class GenState extends State
	{
		String m_name;
		public GenState(String _name)
		{
			m_name = _name;
		}
		
		@Override
		public String toString()
		{
			return m_name;
		}

		@Override
		public int hashCode()
		{
			return m_name.hashCode();
		}

		@Override
		public boolean equals(Object o)
		{
			return m_name.equals(o);
		}
	}
	
	public static int getClosureId(int _maxOrder, int _order)
	{
		return _maxOrder*10+_order;
	}
	
	private static Map<Integer,GenClosure> s_genClosures = new HashMap<Integer,GenClosure>();
	private static String[] s_genMomentNames = {"M1","M2","M3","M4","M5","M6","M7","M8","M9"}; 
	private static  Map<String,Integer> s_genMomentNameId = new HashMap<String,Integer>();
	
	static
	{
		s_genMomentNameId.put("M1", 0);
		s_genMomentNameId.put("M2", 1);
		s_genMomentNameId.put("M3", 2);
		s_genMomentNameId.put("M4", 3);
		s_genMomentNameId.put("M5", 4);
		s_genMomentNameId.put("M6", 5);
		s_genMomentNameId.put("M7", 7);
		s_genMomentNameId.put("M8", 8);
		s_genMomentNameId.put("M9", 9);
	}
	
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
		GenClosure closure = new GenClosure(s_genClosures.get(getClosureId(_order-1, _order)));
		if (!s_genClosures.containsKey(closureId))
		{
			s_genClosures.put(closureId, closure);
		}

		for (int i=_order-1; i>_maxOrder; --i)
		{
			int curClosureId = getClosureId(i-1,i);
			Set<AbstractExpression> remove = new HashSet<AbstractExpression>();
			Set<AbstractExpression> removeIfNull = new HashSet<AbstractExpression>();
			for (Entry<AbstractExpression, Integer> e : closure.m_summands.entrySet())
			{
				// Break up the moment product
				ProductExpression pex = (ProductExpression)e.getKey();
				Map<AbstractExpression,Integer> summands = null;
				List<AbstractExpression> terms = new LinkedList<AbstractExpression>(pex.getTerms());
				for (AbstractExpression ae : pex.getTerms())
				{
					CombinedProductExpression cpex = (CombinedProductExpression) ae;
					CombinedPopulationProduct cpp = cpex.getProduct();
					if (cpp.getOrder() == i)
					{
						terms.remove(ae);
						summands = applyClosure(_maxOrder,cpp);
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
						ProductExpression pex2 = (ProductExpression)e2.getKey();
						List<AbstractExpression> terms2 = new LinkedList<AbstractExpression>(pex2.getTerms());
						terms2.addAll(terms);
						
						AbstractExpression expr = ProductExpression.createOrdered(terms2);
						int multiplicity = closure.m_summands.get(expr);
						multiplicity  +=  e.getValue() * e2.getValue();
						closure.m_summands.put(expr, multiplicity);
						if (multiplicity != 0)
						{
							removeIfNull.add(expr);
						}
					}
					remove.add(e.getKey());
				}
			}
			for (AbstractExpression ae : remove)
			{
				closure.m_summands.remove(ae);
			}
			for (AbstractExpression ae : removeIfNull)
			{
				if (closure.m_summands.get(ae) == 0)
				{
					closure.m_summands.remove(ae);
				}
			}
		}
	}
	
	// Use generic template to apply closure
	private Map<AbstractExpression, Integer> applyClosure(int _maxorder, CombinedPopulationProduct _cpp)
	{
		int i=0;
		State[] states = new State[_cpp.getOrder()];
		for (Entry<State, Integer> e : _cpp.getNakedProduct().getRepresentation().entrySet())
		{
			for (int j=0; j<e.getValue(); j++)
			{
				states[i++] = e.getKey();
			}
		}
		
		// Get generic closure
		GenClosure closure = s_genClosures.get(getClosureId(_maxorder,_cpp.getOrder()));
		Map<AbstractExpression, Integer> closureSummands = new HashMap<AbstractExpression, Integer>();
		
		// Now translate generic pattern for _cpp
		for (Entry<AbstractExpression, Integer> e : closure.m_summands.entrySet())
		{
			// Break up the moment product
			ProductExpression pex = (ProductExpression)e.getKey();
			List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
			for (AbstractExpression ae : pex.getTerms())
			{
				CombinedProductExpression cpex = (CombinedProductExpression) ae;
				CombinedPopulationProduct cpp = cpex.getProduct();
				Map<State, Integer> term = new HashMap<State, Integer>();
				for (Entry<State, Integer> e2 : cpp.getNakedProduct().getRepresentation().entrySet())
				{
					State s = states[s_genMomentNameId.get(e2.getKey().toString())];
					Integer mult = term.get(s);
					mult = (mult == null) ? 1 : ++mult;
					term.put(s,mult);
				}
				terms.add(CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(term))));
			}
			AbstractExpression pexNew = ProductExpression.createOrdered(terms);
			Integer mult = closureSummands.get(pexNew);
			mult = (mult == null) ? e.getValue() : mult+e.getValue();
			closureSummands.put(pexNew,mult);
		}
		
		return closureSummands;
	}

	// Generate the (_order-1,_order) closure
	private void genIsserlisClosure(int _order)
	{
		// Check if we have already computed the closure
		int closureId =  getClosureId(_order-1, _order);
		if (s_genClosures.containsKey(closureId))
		{
			return;
		}		
		
		GenClosure closure = new GenClosure();
		s_genClosures.put(closureId, closure);
	
		// Create E[M1 M2 M3 M4 ... M$(_order)]
		CombinedPopulationProduct[] states = new CombinedPopulationProduct[_order];
		for (int i=0; i<_order; ++i)
		{
			states[i] = CombinedPopulationProduct.getMeanPopulation(new GenState(s_genMomentNames[i]));
		}
		
		// Add even moment extra terms
		if (_order % 2 == 0)
		{
			Set<Set<List<CombinedPopulationProduct>>> allPartitionsIntoPairs = GetVVersionVisitorMomentClosure
					.<CombinedPopulationProduct> getAllPartitionsIntoPairs(Arrays
							.asList(states));
			for (Set<List<CombinedPopulationProduct>> partition : allPartitionsIntoPairs) {
				// Needs to evaluate the product
				// E[(X1-u1)(X2-u2)]*E[(X3-u3)(X4-u4)]*...* + E[(X1-u1)(X3-u3)]*...
				// + ...
				List<List<CombinedPopulationProduct>> tmp = new ArrayList<List<CombinedPopulationProduct>>(
						partition);
				for (long i = 0; i < Math.pow(2, partition.size()); i++) {
					long iBit = i;
					int j = 0;
					int sign = 1;
					List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
					while (j < partition.size()) {
						CombinedPopulationProduct m1 = tmp.get(j).get(0);
						CombinedPopulationProduct m2 = tmp.get(j).get(1);
						CombinedPopulationProduct m1m2 = CombinedPopulationProduct
								.getProductOf(m1, m2);
						if (iBit % 2 == 0) {
							terms.add(CombinedProductExpression.create(m1m2));
						} else {
							sign *= -1;
							terms.add(CombinedProductExpression.create(m1));
							terms.add(CombinedProductExpression.create(m2));
						}
						iBit /= 2;
						j++;
					}

					AbstractExpression expr = ProductExpression.createOrdered(terms);
					Integer multiplicity = closure.m_summands.get(expr);
					multiplicity = (multiplicity == null) ? 0 : multiplicity;
					
					if (sign == -1)
					{
						if (--multiplicity != 0)
						{
							closure.m_summands.put(expr, multiplicity);
						}
					}
					else
					{
						if (++multiplicity != 0)
						{
							closure.m_summands.put(expr, multiplicity);
						}
					}
				}
			}
		}
		
		// Remaining terms
		double numberOfTerms = Math.pow(2.0, states.length);
		for (long i = 1; i < numberOfTerms; i++) {
			List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
			CombinedPopulationProduct product = null;
			long tmp = i;
			int j = 0;
			int sign = 1;
			while (j < states.length) {
				if (tmp % 2 == 0) {
					if (product == null) {
						product = states[j];
					} else {
						product = CombinedPopulationProduct.getProductOf(
								product, states[j]);
					}
				} else {
					terms.add(CombinedProductExpression.create(states[j]));
					sign = -sign;
				}
				tmp /= 2;
				j++;
			}
			if (product != null) {
				terms.add(CombinedProductExpression.create(product));
			}
			
			AbstractExpression expr = ProductExpression.createOrdered(terms);
			Integer multiplicity = closure.m_summands.get(expr);
			multiplicity = (multiplicity == null) ? 0 : multiplicity;

			if (sign == 1)
			{
				if (--multiplicity != 0)
				{
					closure.m_summands.put(expr, multiplicity);
				}
			}
			else
			{
				if (++multiplicity != 0)
				{
					closure.m_summands.put(expr, multiplicity);
				}
			}
		}
		int i=0;
		i++;
	}

	public static AbstractExpression getOddMomentInTermsOfCovariances(
			CombinedPopulationProduct[] states) {
		List<AbstractExpression> summands = new LinkedList<AbstractExpression>();
		double numberOfTerms = Math.pow(2.0, states.length);
		for (long i = 1; i < numberOfTerms; i++) {
			List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
			CombinedPopulationProduct product = null;
			long tmp = i;
			int j = 0;
			int sign = 1;
			while (j < states.length) {
				if (tmp % 2 == 0) {
					if (product == null) {
						product = states[j];
					} else {
						product = CombinedPopulationProduct.getProductOf(
								product, states[j]);
					}
				} else {
					terms.add(CombinedProductExpression.create(states[j]));
					sign = -sign;
				}
				tmp /= 2;
				j++;
			}
			if (product != null) {
				terms.add(CombinedProductExpression.create(product));
			}
			if (sign == 1) {
				terms.add(new DoubleExpression(-1.0));
			}
			summands.add(ProductExpression.create(terms));
		}
		return SumExpression.create(summands);
	}

	public static AbstractExpression getEventMomentInTermsOfCovariances(
			CombinedPopulationProduct[] states) {
		assert (states.length % 2 == 0);

		List<AbstractExpression> summands = new LinkedList<AbstractExpression>();
		summands.add(getOddMomentInTermsOfCovariances(states));
		Set<Set<List<CombinedPopulationProduct>>> allPartitionsIntoPairs = GetVVersionVisitorMomentClosure
				.<CombinedPopulationProduct> getAllPartitionsIntoPairs(Arrays
						.asList(states));
		for (Set<List<CombinedPopulationProduct>> partition : allPartitionsIntoPairs) {
			// Needs to evaluate the product
			// E[(X1-u1)(X2-u2)]*E[(X3-u3)(X4-u4)]*...* + E[(X1-u1)(X3-u3)]*...
			// + ...
			List<List<CombinedPopulationProduct>> tmp = new ArrayList<List<CombinedPopulationProduct>>(
					partition);
			for (long i = 0; i < Math.pow(2, partition.size()); i++) {
				long iBit = i;
				int j = 0;
				int sign = 1;
				List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
				while (j < partition.size()) {
					CombinedPopulationProduct m1 = tmp.get(j).get(0);
					CombinedPopulationProduct m2 = tmp.get(j).get(1);
					CombinedPopulationProduct m1m2 = CombinedPopulationProduct
							.getProductOf(m1, m2);
					if (iBit % 2 == 0) {
						terms.add(CombinedProductExpression.create(m1m2));
					} else {
						sign *= -1;
						terms.add(CombinedProductExpression.create(m1));
						terms.add(CombinedProductExpression.create(m2));
					}
					iBit /= 2;
					j++;
				}
				if (sign == -1) {
					terms.add(new DoubleExpression(-1.0));
				}
				summands.add(ProductExpression.create(terms));
			}
		}
		return SumExpression.create(summands);
	}

}
