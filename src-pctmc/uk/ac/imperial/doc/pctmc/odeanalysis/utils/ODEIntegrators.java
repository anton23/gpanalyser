package uk.ac.imperial.doc.pctmc.odeanalysis.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.FixedStepHandler;
import org.apache.commons.math3.ode.sampling.StepNormalizer;

import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;


public class ODEIntegrators {
	
	public static String DENSITY = "density";
	public static String INTEGRATOR = "integrator";
	public static String GETINTEGRATOR = "getIntegrator";
	public static String MIN_STEP = "minStep";
	public static String MAX_STEP = "maxStep";
	public static String REL_TOL = "relTol";
	public static String ABS_TOL = "absTol";
	
	
	public static double[][] solveODEs(final SystemOfODEs f, double initial[],
			double stopTime, final double stepSize, Map<String, Object> parameters) {
		final int n = initial.length;
		
		double[] init = Arrays.copyOf(initial, n);
		
		
		FirstOrderIntegrator integrator = null;
		String algorithm = "ClassicalRungeKutta";
		if (parameters.containsKey(INTEGRATOR)) {
			algorithm = (String) parameters.get(INTEGRATOR);		
		}
		
		try {
			Method m = ODEIntegrators.class.getDeclaredMethod(GETINTEGRATOR+algorithm, Double.TYPE, Double.TYPE, Map.class);
			integrator = (FirstOrderIntegrator) m.invoke(null, stopTime, stepSize, parameters);
		} catch (NoSuchMethodException e1) {
			throw new AssertionError("Integrator " + algorithm + " unknown!");
		} catch (Exception e) {
			throw new AssertionError("Problem with ODE integrator!");
		}
		
		long t = System.currentTimeMillis();
		
		PCTMCLogging.info("Running " + algorithm + " ODE solver.");
		final double ret[][] = new double[(int) Math.ceil(stopTime / stepSize)][];
		
		FixedStepHandler fixedStepHandler = new FixedStepHandler() {
			int step;
		    public void init(double t0, double[] y0, double t) {step = 0;}
			public void handleStep(double t, double[] y, double[] yDot,
					boolean isLast) {
				ret[step++] = Arrays.copyOf(y, n);
			}
		};
		
		integrator.addStepHandler(new StepNormalizer(stepSize, fixedStepHandler));
		integrator.integrate(f, 0.0, init, stopTime-stepSize, init);
		PCTMCLogging.info("ODE solver finished in " + (System.currentTimeMillis()-t) + "ms");
		return ret;
	}
	
	
	@SuppressWarnings("unused")
	private static FirstOrderIntegrator getIntegratorClassicalRungeKutta(double stopTime, double stepSize, Map<String, Object> parameters) {
		int density = (Integer) parameters.get(DENSITY);
		return new ClassicalRungeKuttaIntegrator(stepSize / density);
	}
	
	@SuppressWarnings("unused")
	private static FirstOrderIntegrator getIntegratorDormandPrince853(double stopTime, double stepSize, Map<String, Object> parameters) {
		double minStep = 1.0e-8;
		if (parameters.containsKey(MIN_STEP)) {
			minStep = (Double) parameters.get(MIN_STEP);
		}
		double maxStep = stopTime;
		if (parameters.containsKey(MAX_STEP)) {
			minStep = (Double) parameters.get(MAX_STEP);
		}
		double relTol = 1.0e-10;
		if (parameters.containsKey(REL_TOL)) {
			minStep = (Double) parameters.get(REL_TOL);
		}
		double absTol = 1.0e-10;
		if (parameters.containsKey(ABS_TOL)) {
			minStep = (Double) parameters.get(ABS_TOL);
		}		
		
		return new DormandPrince853Integrator(minStep, maxStep, absTol, relTol);
	}
}
