package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.odeanalysis.closures.LognormalMomentClosure;
import uk.ac.imperial.doc.pctmc.odeanalysis.closures.MomentClosure;
import uk.ac.imperial.doc.pctmc.odeanalysis.closures.NormalMomentClosure;
import uk.ac.imperial.doc.pctmc.odeanalysis.closures.NormalMomentClosureMinApproximation;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;

public class PCTMCODEAnalysis extends AbstractPCTMCAnalysis
{
	protected MomentClosure m_momentClosure;
	protected boolean m_autoClosure;
	private NewODEGenerator m_odeGenerator;
	private ODEMethod m_odeMethod;
	
	protected static Map<String, Class<? extends MomentClosure>> s_momentClosures;
	static
	{
		s_momentClosures = new HashMap<String, Class<? extends MomentClosure>>();
		s_momentClosures.put(NormalMomentClosure.NAME, NormalMomentClosure.class);
		s_momentClosures.put(LognormalMomentClosure.NAME, LognormalMomentClosure.class);
		s_momentClosures.put(NormalMomentClosureMinApproximation.NAME, NormalMomentClosureMinApproximation.class);
	}
	
	@Override
	public String toString()
	{
		String ret = "ODEs";
		if (!m_autoClosure)
		{
			ret += "[" + m_momentClosure.toString() + "]"; 
		}
		return ret;
	}

	@Override
	public void setUsedMoments(Collection<CombinedPopulationProduct> combinedProducts)
	{
		if (usedCombinedProducts != null)
		{
			usedCombinedProducts.addAll(combinedProducts);
		}
		else
		{
			usedCombinedProducts = new HashSet<CombinedPopulationProduct>(combinedProducts);
		}
		if (m_autoClosure)
		{
			int order = 1;
			for (CombinedPopulationProduct product : combinedProducts)
			{
				int o = product.getOrder();
				if (o > order)
				{
					order = o;
				}
			}
			m_momentClosure = new NormalMomentClosure(order);
		}		
	}

	public PCTMCODEAnalysis(PCTMC pctmc)
	{
		super(pctmc);
		m_autoClosure = true;
	}
	
	public PCTMCODEAnalysis(PCTMC pctmc, Map<String, Object> parameters)
	{
		super(pctmc);
		m_autoClosure = true;
		if (parameters.containsKey("momentClosure"))
		{
			m_autoClosure = false;
			Object nameO = parameters.get("momentClosure");
			if (!(nameO instanceof String))
			{
				throw new AssertionError("Name of the moment closure has to be a string");
			}
			String name = (String) nameO;
			if (s_momentClosures.containsKey(name))
			{
				try
				{
					m_momentClosure = s_momentClosures.get(name).getConstructor(Map.class).newInstance(parameters);
				}
				catch (Exception e)
				{
					throw new AssertionError("Unexpected internal error " + e);
				} 
			}
			else
			{
				throw new AssertionError("Unknown moment closure " + name);
			}			
		}
	}

	@Override
	public void prepare(Constants variables)
	{
		m_odeGenerator = new NewODEGenerator(pctmc, m_momentClosure);
		m_odeMethod = m_odeGenerator.getODEMethodWithCombinedMoments(usedCombinedProducts);
		momentIndex = m_odeGenerator.getMomentIndex();
	}

	public ODEMethod getOdeMethod()
	{
		return m_odeMethod;
	}
}
