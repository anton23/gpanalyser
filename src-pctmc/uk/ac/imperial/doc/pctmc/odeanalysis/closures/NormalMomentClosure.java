package uk.ac.imperial.doc.pctmc.odeanalysis.closures;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.odeanalysis.MomentClosure;

public class NormalMomentClosure extends MomentClosure
{
	public final static String NAME = "NormalClosure";
	protected int m_maxOrder;
	
	@Override
	public Map<ExpressionVariable, AbstractExpression> getVariables()
	{	
		return null;
	}

	public NormalMomentClosure(int _maxOrder)
	{
		super(new HashMap<String, Object>());		
		this.m_maxOrder = _maxOrder;		
	}
		
	public NormalMomentClosure(Map<String, Object> _parameters)
	{
		super(_parameters);
		if (_parameters.containsKey(MomentClosure.MAX_ORDER))
		{
			this.m_maxOrder = (Integer) _parameters.get(MomentClosure.MAX_ORDER);
		}
	}

	@Override
	public AbstractExpression insertProductIntoRate(AbstractExpression _rate, PopulationProduct _moment)
	{
		NormalClosureVisitorUniversal visitor = new NormalClosureVisitorUniversal(new CombinedPopulationProduct(_moment), m_maxOrder);
		_rate.accept(visitor);
		return visitor.getResult();
	}

	@Override
	public AbstractExpression insertAccumulations(AbstractExpression _derivative,CombinedPopulationProduct _moment)
	{
		NormalClosureVisitorUniversal visitor = new NormalClosureVisitorUniversal(new CombinedPopulationProduct(null, _moment.getAccumulatedProducts()), m_maxOrder);
		_derivative.accept(visitor);
		return visitor.getResult();
	}

	@Override
	public String toString()
	{
		return MomentClosure.MOMENT_CLOSURE + "=" + NAME + ", " + MomentClosure.MAX_ORDER + "=" + m_maxOrder;
	}	
}
