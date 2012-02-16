package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;

/***
 * Applies Singh's (2006) Lognormal closure
 * to transform infinite linear system of ODEs
 * into finite system of non-linear ODEs. The
 * Lognormal closure is a form of
 * Derivative Matching closure. In contrast to the
 * Normal closure, it is purely multiplicative.
 * 
 * @author Chris Guenther
 */
public class LognormalMomentClosure extends MomentClosure {
	
	public final static String NAME = "LognormalClosure";
	protected int m_maxOrder;
	protected double m_mfStabiliser;
	
	public LognormalMomentClosure(int _maxOrder, double _meanFieldStabilisation)
	{
		super(new HashMap<String, Object>());		
		m_maxOrder = _maxOrder;
		m_mfStabiliser = _meanFieldStabilisation;
	}
		
	public LognormalMomentClosure(Map<String, Object> _parameters)
	{
		super(_parameters);
		if (_parameters.containsKey(MomentClosure.MAX_ORDER))
		{
			m_maxOrder = (Integer) _parameters.get(MomentClosure.MAX_ORDER);
		}
		m_mfStabiliser = 0.0;
		if (_parameters.containsKey(MomentClosure.MEAN_FIELD_STABILISER))
		{
			m_mfStabiliser = (Double) _parameters.get(MomentClosure.MEAN_FIELD_STABILISER);
		}
	}

	@Override
	public AbstractExpression insertProductIntoRate(AbstractExpression _rate, PopulationProduct _moment)
	{
		GetVVersionVisitorMomentClosureLognormal visitor = new GetVVersionVisitorMomentClosureLognormal(_moment, m_maxOrder, m_mfStabiliser);
		_rate.accept(visitor);
		return visitor.getResult();
	}

	@Override
	public AbstractExpression insertAccumulations(AbstractExpression _derivative, CombinedPopulationProduct _moment)
	{
		IntegralInsterterVisitor visitor = new IntegralInsterterVisitor(new CombinedPopulationProduct(null, _moment.getAccumulatedProducts()));
		_derivative.accept(visitor);
		return visitor.getResult();
	}
	
	@Override
	public String toString()
	{
		return MomentClosure.MOMENT_CLOSURE + "=" + NAME + ", " + MomentClosure.MAX_ORDER + "=" + m_maxOrder + ", " + MomentClosure.MEAN_FIELD_STABILISER + "=" + m_mfStabiliser;
	}

	@Override
	public Map<ExpressionVariable, AbstractExpression> getVariables() {
		return null;
	}		
}
