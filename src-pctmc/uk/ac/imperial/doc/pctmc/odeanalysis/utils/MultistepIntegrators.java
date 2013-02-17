package uk.ac.imperial.doc.pctmc.odeanalysis.utils;

import java.util.Map;

public class MultistepIntegrators extends AdaptiveStepsizeIntegrators {

	public static String NSTEP = "nSteps";
	
	public MultistepIntegrators(Class<?> integratorClass) {
		super(integratorClass);
	}
	
	@Override
	public  Object[] processParameters(double stopTime, double stepSize,
			Map<String, Object> parameters) {
		Object[] processParameters = super.processParameters(stopTime, stepSize, parameters);
		Integer nSteps = 4;
		if (parameters.containsKey(NSTEP)) {
			nSteps = (Integer) parameters.get(NSTEP);
		}
		Object[] ret = new Object[processParameters.length+1];
		ret[0] = nSteps;
		for (int i = 0; i < processParameters.length; i++) {
			ret[1 + i] = processParameters[i];
		}
		
		return ret;
	}
	
	@Override
	public Class<?>[] parameterTypes() {
		return new Class<?>[] {Integer.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE};
	}

}
