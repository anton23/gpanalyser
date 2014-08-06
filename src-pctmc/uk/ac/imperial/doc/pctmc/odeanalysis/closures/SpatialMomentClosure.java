package uk.ac.imperial.doc.pctmc.odeanalysis.closures;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

public class SpatialMomentClosure extends MomentClosure
{
	public final static String NAME = "SpatialClosure";
	protected int m_maxOrder;
  protected int m_maxDist;
	
  protected Map<State, Map<State, Integer>> m_distance;
  
	@Override
	public Map<ExpressionVariable, AbstractExpression> getVariables()
	{	
		return null;
	}

	public SpatialMomentClosure(int _maxOrder, int _maxDist)
	{
		super(new HashMap<String, Object>());		
		this.m_maxOrder = _maxOrder;
    this.m_maxOrder = _maxDist;
	}
		
	public SpatialMomentClosure(PCTMC pctmc, Map<String, Object> _parameters)
	{
		super(_parameters);
		if (_parameters.containsKey(MomentClosure.MAX_ORDER))
		{
			this.m_maxOrder = (Integer) _parameters.get(MomentClosure.MAX_ORDER);
		}
    if (_parameters.containsKey(MomentClosure.MAX_DIST))
    {
      this.m_maxDist = (Integer) _parameters.get(MomentClosure.MAX_DIST);
    }
    
    // Create the distance map
    m_distance = new HashMap<State, Map<State, Integer>>();
    for (State s : pctmc.getStateIndex().keySet()) {
      m_distance.put(s, new HashMap<State, Integer>());
      LinkedList<EvolutionEvent> evts =
        new LinkedList<EvolutionEvent>(pctmc.getEvolutionEvents());
      Set<State> seenPops = new HashSet<State>();
      seenPops.add(s);
      boolean done = false;
      m_distance.get(s).put(s, 0);
      int dist = 1;
      while (!done && dist <= m_maxDist) {
        done = true;
        HashSet<State> newPops = new HashSet<State>();
        for (EvolutionEvent evo : evts) {
          HashSet<State> pops = new HashSet<State>(evo.getChangeVector().keySet());
          int size = pops.size();
          pops.removeAll(seenPops);
          if (size == pops.size()) {
            continue;
          }
          pops.removeAll(newPops);
          if (pops.size() > 0) {
            newPops.addAll(pops);
            for (State s2 : pops) {
              m_distance.get(s).put(s2, dist);
              done = false;
            }
          }
        }
        seenPops.addAll(newPops);
        dist = dist + 1;
      }
      //System.out.println(s + " => " + Arrays.toString(m_distance.get(s).entrySet().toArray()));
    }
	}

	@Override
	public AbstractExpression insertProductIntoRate(AbstractExpression _rate, PopulationProduct _moment)
	{
		SpatialClosureVisitorUniversal visitor = new SpatialClosureVisitorUniversal(new CombinedPopulationProduct(_moment), m_maxOrder, m_distance);
		_rate.accept(visitor);
		return visitor.getResult();
	}

	@Override
	public AbstractExpression insertAccumulations(AbstractExpression _derivative,CombinedPopulationProduct _moment)
	{
	  SpatialClosureVisitorUniversal visitor = new SpatialClosureVisitorUniversal(new CombinedPopulationProduct(null, _moment.getAccumulatedProducts()), m_maxOrder, m_distance);
		_derivative.accept(visitor);
		return visitor.getResult();
	}

	@Override
	public String toString()
	{
		return MomentClosure.MOMENT_CLOSURE + "=" + NAME + ", " + MomentClosure.MAX_ORDER + "=" + m_maxOrder + ", " + MomentClosure.MAX_DIST + "=" + m_maxDist;
	}	
}
