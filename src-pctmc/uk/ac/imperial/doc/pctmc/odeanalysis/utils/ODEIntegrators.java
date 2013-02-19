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
import org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegrator;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;
import org.apache.commons.math3.ode.nonstiff.MidpointIntegrator;
import org.apache.commons.math3.ode.nonstiff.RungeKuttaIntegrator;
import org.apache.commons.math3.ode.sampling.FixedStepHandler;
import org.apache.commons.math3.ode.sampling.StepNormalizer;

import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

import com.google.common.collect.Sets;


@SuppressWarnings({ "unchecked" })
public class ODEIntegrators {
	
	public static String INTEGRATOR = "integrator";
	public static String GETINTEGRATOR = "getIntegrator";

	
	public static String INTEGRATORC = "Integrator";
	
	
	static final Set<Class<? extends RungeKuttaIntegrator>> RUNGEKUTTA = 
			Sets.<Class<? extends RungeKuttaIntegrator>>
				newHashSet(ClassicalRungeKuttaIntegrator.class, MidpointIntegrator.class);
	
	
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
		final int n = initial.length;
		
		double[] init = Arrays.copyOf(initial, n);
		
		
		FirstOrderIntegrator integrator = null;
		String algorithm = ClassicalRungeKuttaIntegrator.class.getSimpleName().replace(INTEGRATORC, "");
		if (parameters.containsKey(INTEGRATOR)) {
			algorithm = (String) parameters.get(INTEGRATOR);		
		}
		
		try {
			ODEIntegratorsGroup odeIntegratorsGroup = algorithms.get(algorithm);
			integrator = odeIntegratorsGroup.getInstance(stopTime, stepSize, parameters);
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			throw new AssertionError("Integrator " + algorithm + " unknown!");
		} catch (NullPointerException e1) {
			throw new AssertionError("Integrator " + algorithm + " unknown!");
		} catch (Exception e) {
			e.printStackTrace();
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
			
	
}
