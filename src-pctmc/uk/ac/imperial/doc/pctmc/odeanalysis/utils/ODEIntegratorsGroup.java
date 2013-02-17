package uk.ac.imperial.doc.pctmc.odeanalysis.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.math3.ode.FirstOrderIntegrator;

public abstract class ODEIntegratorsGroup {
	protected Class<?> integratorClass;
	
	
	public ODEIntegratorsGroup(Class<?> integratorClass) {
		super();
		this.integratorClass = integratorClass;
	}

	public abstract Class<?>[] parameterTypes();
	
	public abstract Object[] processParameters(double stopTime, double stepSize, Map<String, Object> parameters);
	
	public FirstOrderIntegrator getInstance(double stopTime, double stepSize, Map<String, Object> parameters)
		throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			Constructor<?> constructor = integratorClass.getConstructor(parameterTypes());
			return (FirstOrderIntegrator) constructor.newInstance(processParameters(stopTime, stepSize, parameters));	
	}
}
