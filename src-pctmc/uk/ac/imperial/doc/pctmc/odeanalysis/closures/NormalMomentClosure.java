package uk.ac.imperial.doc.pctmc.odeanalysis.closures;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.State;

public class NormalMomentClosure extends MomentClosure
{
	public final static String NAME = "NormalClosure";
	protected int m_maxOrder;
	protected int m_evtIndependenceDist;
	protected Map<State,Map<State, Integer>> m_distMap;
	
	@Override
	public Map<ExpressionVariable, AbstractExpression> getVariables()
	{	
		return null;
	}

	public NormalMomentClosure(int _maxOrder)
	{
		super(new HashMap<String, Object>());		
		this.m_maxOrder = _maxOrder;	
		this.m_distMap = new HashMap<State,Map<State,Integer>>();
	}
		
	public NormalMomentClosure(Map<String, Object> _parameters)
	{
		super(_parameters);
		if (_parameters.containsKey(MomentClosure.MAX_ORDER))
		{
			this.m_maxOrder = (Integer) _parameters.get(MomentClosure.MAX_ORDER);
		}
		if (_parameters.containsKey(MomentClosure.EVENT_INDEPENDENCE_DIST))
		{
			this.m_evtIndependenceDist = (Integer) _parameters.get(MomentClosure.EVENT_INDEPENDENCE_DIST);
			this.m_distMap = (Map<State,Map<State, Integer>>) _parameters.get(MomentClosure.DISTANCE_MAP);
		}
	}

	@Override
	public AbstractExpression insertProductIntoRate(AbstractExpression _rate, PopulationProduct _moment)
	{
		NormalClosureVisitorUniversal visitor = new NormalClosureVisitorUniversal(new CombinedPopulationProduct(_moment), m_maxOrder, m_distMap);
		_rate.accept(visitor);
		return visitor.getResult();
	}

	@Override
	public AbstractExpression insertAccumulations(AbstractExpression _derivative, CombinedPopulationProduct _moment)
	{
		NormalClosureVisitorUniversal visitor = new NormalClosureVisitorUniversal(new CombinedPopulationProduct(null, _moment.getAccumulatedProducts()), m_maxOrder, m_distMap);
		_derivative.accept(visitor);
		return visitor.getResult();
	}

	@Override
	public String toString()
	{
		return MomentClosure.MOMENT_CLOSURE + "=" + NAME + ", " + MomentClosure.MAX_ORDER + "=" + m_maxOrder + ", "+ MomentClosure.EVENT_INDEPENDENCE_DIST + "=" + m_evtIndependenceDist;
	}	
}
