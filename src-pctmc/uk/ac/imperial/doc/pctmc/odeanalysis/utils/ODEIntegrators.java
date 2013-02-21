package uk.ac.imperial.doc.pctmc.odeanalysis.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.MultistepIntegrator;
import org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegrator;
import org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegrator;
import org.apache.commons.math3.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince54Integrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.nonstiff.EulerIntegrator;
import org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegrator;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;
import org.apache.commons.math3.ode.nonstiff.MidpointIntegrator;
import org.apache.commons.math3.ode.nonstiff.RungeKuttaIntegrator;
import org.apache.commons.math3.ode.sampling.FixedStepHandler;
import org.apache.commons.math3.ode.sampling.StepNormalizer;
import org.apache.commons.math3.ode.sampling.StepNormalizerBounds;
import org.apache.commons.math3.ode.sampling.StepNormalizerMode;

import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

import com.google.common.collect.Sets;


@SuppressWarnings({ "unchecked" })
public class ODEIntegrators {
	
	public static String INTEGRATOR = "integrator";
	public static String GETINTEGRATOR = "getIntegrator";
	public static String BOUNDS = "bounds";

	
	public static String INTEGRATORC = "Integrator";
	
	
	static final Set<Class<? extends RungeKuttaIntegrator>> RUNGEKUTTA = 
			Sets.<Class<? extends RungeKuttaIntegrator>>
				newHashSet(ClassicalRungeKuttaIntegrator.class, MidpointIntegrator.class, EulerIntegrator.class);
	
	
	static final Set<Class<? extends AdaptiveStepsizeIntegrator>> ADAPTIVESTEP =
			Sets.<Class<? extends AdaptiveStepsizeIntegrator>>
				newHashSet(DormandPrince853Integrator.class, DormandPrince54Integrator.class, 
						GraggBulirschStoerIntegrator.class, HighamHall54Integrator.class);
	
	static final Set<Class<? extends MultistepIntegrator>> MULTISTEP =
		Sets.<Class<? extends MultistepIntegrator>>
				newHashSet(AdamsBashforthIntegrator.class, AdamsMoultonIntegrator.class);
	
	static final Map<String, ODEIntegratorsGroup> algorithms = new HashMap<String, ODEIntegratorsGroup>();	
	
	static {
		
		for (Class<?> c : RUNGEKUTTA) {
			String name = c.getSimpleName().replace(INTEGRATORC, "");
			algorithms.put(name, new RungeKuttaIntegrators(c));
		}
		
		for (Class<?> c : ADAPTIVESTEP) {
			String name = c.getSimpleName().replace(INTEGRATORC, "");
			algorithms.put(name, new AdaptiveStepsizeIntegrators(c));
		}
		
		for (Class<?> c : MULTISTEP) {
			String name = c.getSimpleName().replace(INTEGRATORC, "");
			algorithms.put(name, new MultistepIntegrators(c));
		}
	}
	
	
	public static double[][] solveODEs(final SystemOfODEs f, double initial[],
			double stopTime, final double stepSize, Map<String, Object> parameters) {
		return solveODEs(f, initial, 0.0, stopTime, stepSize, parameters);
	}

	public static double[][] solveODEs(final SystemOfODEs f, double initial[],
			double startTime, final double stopTime, final double stepSize, Map<String, Object> parameters) {
		final int n = initial.length;
		
		double[] init = Arrays.copyOf(initial, n);
		
		FirstOrderIntegrator integrator = getIntegrator(startTime, stopTime, stepSize, parameters);
		
		long t = System.currentTimeMillis();
		
		
		StepNormalizerBounds bounds = StepNormalizerBounds.FIRST;
		
		if (parameters.containsKey(BOUNDS)) {
			String option = (String)parameters.get(BOUNDS);
			try {				
				bounds = StepNormalizerBounds.valueOf(option);
			} catch (Exception e) {
				throw new AssertionError("Not a valid bounds option: " + option);
			}
		}
		StepNormalizerMode mode = StepNormalizerMode.MULTIPLES;
		
		
		double firstAfter = (Math.floor(startTime / stepSize) + 1) * stepSize;
		double lastBefore = Math.floor(stopTime / stepSize) * stepSize;
		
		int length;
		if (firstAfter <= lastBefore) {
			length = (int) Math.round((lastBefore - firstAfter) / stepSize) + 1;
		}
		else {
			length = 0;
		}
		
		if (bounds.firstIncluded()) length++;

		if (bounds.lastIncluded() && lastBefore != stopTime) length++;
		
		
		final double ret[][] = new double[length][];
		
		FixedStepHandler fixedStepHandler = new FixedStepHandler() {
			int step;
		    public void init(double t0, double[] y0, double t) {step = 0;}
			public void handleStep(double t, double[] y, double[] yDot,
					boolean isLast) {
				if (t <= stopTime + 1e-10 && step < ret.length) { // For some reason this is not always true... TODO
					ret[step++] = Arrays.copyOf(y, n);
				}
			}
		};
		
		
		integrator.addStepHandler(new StepNormalizer(stepSize, fixedStepHandler, mode, bounds));
		integrator.integrate(f, startTime, init, stopTime, init);
		PCTMCLogging.info("ODE solver finished in " + (System.currentTimeMillis()-t) + "ms");
		return ret;
	}
	
	public static FirstOrderIntegrator getIntegrator(double startTime, final double stopTime, 
			final double stepSize, Map<String, Object> parameters) {
		
		
		FirstOrderIntegrator integrator = null;
		String algorithm = ClassicalRungeKuttaIntegrator.class.getSimpleName().replace(INTEGRATORC, "");
		if (parameters.containsKey(INTEGRATOR)) {
			algorithm = (String) parameters.get(INTEGRATOR);		
		}
		
		try {
			ODEIntegratorsGroup odeIntegratorsGroup = algorithms.get(algorithm);
			integrator = odeIntegratorsGroup.getInstance(startTime, stopTime, stepSize, parameters);
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			throw new AssertionError("Integrator " + algorithm + " unknown!");
		} catch (NullPointerException e1) {
			throw new AssertionError("Integrator " + algorithm + " unknown!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new AssertionError("Problem with ODE integrator!");
		}
		PCTMCLogging.info("Running " + algorithm + " ODE solver.");

		return integrator;
	}
	
	
	
	
			
	
}
