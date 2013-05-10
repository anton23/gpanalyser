package uk.ac.imperial.doc.pctmc.odeanalysis.closures;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;

public class NormalMomentClosureMinApproximation extends NormalMomentClosure
{
	public final static String NAME = "NormalClosureMinApproximation";
	
	protected Map<AbstractExpression, ExpressionVariable> m_usedVariables;
	protected int m_lastVariable;
	protected boolean alternative;

	
	@Override
	public Map<ExpressionVariable, AbstractExpression> getVariables() {
		Map<ExpressionVariable, AbstractExpression> variables = new HashMap<ExpressionVariable, AbstractExpression>();
		for (Map.Entry<AbstractExpression, ExpressionVariable> e:m_usedVariables.entrySet()) {
			variables.put(e.getValue(), e.getKey());
		}
		return variables;
	}
	


	public NormalMomentClosureMinApproximation(int _maxOrder)
	{
		super(_maxOrder);
		m_usedVariables = new HashMap<AbstractExpression, ExpressionVariable>();
		m_lastVariable = 0;
	}
	
	public NormalMomentClosureMinApproximation(Map<String, Object> _parameters)
	{
		super(_parameters);
		if (_parameters.containsKey("alternative")) {
			alternative = true;
		}
		m_usedVariables = new LinkedHashMap<AbstractExpression, ExpressionVariable>();
		m_lastVariable = 0;
	}

	@Override
	public AbstractExpression insertProductIntoRate(AbstractExpression rate, PopulationProduct moment)
	{
		NormalClosureMinApproximationVisitorUniversal visitor = new NormalClosureMinApproximationVisitorUniversal(new CombinedPopulationProduct(moment), m_maxOrder, m_usedVariables, m_lastVariable, alternative);
		rate.accept(visitor);
		m_lastVariable = visitor.getVariableIndex();
		return visitor.getResult();
	}
	
	@Override
	public AbstractExpression insertAccumulations(AbstractExpression derivative, CombinedPopulationProduct moment)
	{
		NormalClosureMinApproximationVisitorUniversal visitor = new NormalClosureMinApproximationVisitorUniversal(new CombinedPopulationProduct(null, moment.getAccumulatedProducts()), m_maxOrder, m_usedVariables, m_lastVariable, alternative);
		derivative.accept(visitor);
		m_lastVariable = visitor.getVariableIndex();
		return visitor.getResult();
	}
	
	@Override
	public String toString()
	{
		return MomentClosure.MOMENT_CLOSURE + "=" + NAME + ", " + MomentClosure.MAX_ORDER + "=" + m_maxOrder + ", alternative="+alternative ;
	}
}
