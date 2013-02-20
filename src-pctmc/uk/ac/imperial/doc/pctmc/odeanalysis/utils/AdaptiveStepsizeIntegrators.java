package uk.ac.imperial.doc.pctmc.odeanalysis.utils;

import java.util.Map;

public class AdaptiveStepsizeIntegrators extends ODEIntegratorsGroup {
	
	public static String MIN_STEP = "minStep";
	public static String MAX_STEP = "maxStep";
	public static String REL_TOL = "relTol";
	public static String ABS_TOL = "absTol";

	public AdaptiveStepsizeIntegrators(Class<?> integratorClass) {
		super(integratorClass);
	}
	
	@Override
	public Class<?>[] parameterTypes() {
		return new Class<?>[] {Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE};		
	}
	
			
	@Override
	public Object[] processParameters(double startTime, double stopTime, double stepSize,
			Map<String, Object> parameters) {
		double minStep = 1.0e-8;
		if (parameters.containsKey(MIN_STEP)) {
			minStep = (Double) parameters.get(MIN_STEP);
		}
		double maxStep = stopTime;
		if (parameters.containsKey(MAX_STEP)) {
			maxStep = (Double) parameters.get(MAX_STEP);
		}
		double relTol = 1.0e-10;
		if (parameters.containsKey(REL_TOL)) {
			relTol = (Double) parameters.get(REL_TOL);
		}
		double absTol = 1.0e-10;
		if (parameters.containsKey(ABS_TOL)) {
			absTol = (Double) parameters.get(ABS_TOL);
		}		
		return new Object[] {minStep, maxStep, relTol, absTol};
	}

}
