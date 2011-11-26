package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;

public class PCTMCODEAnalysis extends AbstractPCTMCAnalysis {
	
	protected MomentClosure momentClosure;

	protected static Map<String, Class<? extends MomentClosure>> momentClosures;
	
	protected boolean autoClosure;
	
	static {
		momentClosures = new HashMap<String, Class<? extends MomentClosure>>();
		momentClosures.put("NormalClosure", NormalMomentClosure.class);
	}
	
	@Override
	public String toString() {
		return "ODEs";
	}

	@Override
	public void setUsedMoments(
			Collection<CombinedPopulationProduct> combinedProducts) {
		if (usedCombinedProducts != null) {
			usedCombinedProducts.addAll(combinedProducts);
		} else {
			usedCombinedProducts = new HashSet<CombinedPopulationProduct>(
					combinedProducts);
		}
		if (autoClosure) {
			int order = 1;
			for (CombinedPopulationProduct product : combinedProducts) {
				int o = product.getOrder();
				if (o > order)
					order = o;
			}
			momentClosure = new NormalMomentClosure(order);
		}		
	}

	public PCTMCODEAnalysis(PCTMC pctmc) {
		super(pctmc);
		autoClosure = true;
	}
	
	public PCTMCODEAnalysis(PCTMC pctmc, Map<String, Object> parameters) {
		super(pctmc);
		autoClosure = true;
		if (parameters.containsKey("momentClosure")) {
			autoClosure = false;
			Object nameO = parameters.get("momentClosure");
			if (!(nameO instanceof String)) {
				throw new AssertionError("Name of the moment closure has to be a string");
			}
			String name = (String) nameO;
			if (momentClosures.containsKey(name)) {
				try {
					momentClosure = momentClosures.get(name).getConstructor(Map.class).newInstance(parameters);
				} catch (Exception e) {
					throw new AssertionError("Unexpected internal error " + e);
				} 
			} else {
				throw new AssertionError("Unknown moment closure " + name);
			}			
		}
	}


	private NewODEGenerator odeGenerator;

	@Override
	public void prepare(Constants variables) {
		this.odeGenerator = new NewODEGenerator(pctmc, momentClosure);
		odeMethod = odeGenerator.getODEMethodWithCombinedMoments(usedCombinedProducts);
		momentIndex = odeGenerator.getMomentIndex();
	}
	
	private ODEMethod odeMethod;

	public ODEMethod getOdeMethod() {
		return odeMethod;
	}
}
