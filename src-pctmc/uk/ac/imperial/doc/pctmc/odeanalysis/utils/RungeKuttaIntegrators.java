package uk.ac.imperial.doc.pctmc.odeanalysis.utils;

import java.util.Map;

public class RungeKuttaIntegrators extends ODEIntegratorsGroup {

	public static String DENSITY = "density";

	
	public RungeKuttaIntegrators(Class<?> integratorClass) {
		super(integratorClass);
	}

	@Override
	public Class<?>[] parameterTypes() {
		return new Class<?>[] {Double.TYPE};
	}

	@Override
	public Object[] processParameters(double stopTime, double stepSize,
			Map<String, Object> parameters)	{
		int density = (Integer) parameters.get(DENSITY);
		return new Object[] {stepSize / density};
	}



}
