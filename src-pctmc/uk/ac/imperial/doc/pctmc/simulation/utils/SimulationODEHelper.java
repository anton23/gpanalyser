package uk.ac.imperial.doc.pctmc.simulation.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.FixedStepHandler;
import org.apache.commons.math3.ode.sampling.StepNormalizer;
import org.apache.commons.math3.ode.sampling.StepNormalizerBounds;
import org.apache.commons.math3.ode.sampling.StepNormalizerMode;

import uk.ac.imperial.doc.pctmc.odeanalysis.utils.ODEIntegrators;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.SystemOfODEs;

public class SimulationODEHelper {
	
	FirstOrderIntegrator integrator;
	SystemOfODEs accumulator;
	
	double[][] ret;
	
	public SimulationODEHelper(	AggregatedStateNextEventGenerator g,
		double initial[], int np,  final double stopTime, final double stepSize, SystemOfODEs accumulator) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("integrator", DormandPrince853Integrator.class.getSimpleName().replace("Integrator", ""));
		//parameters.put("integrator", EulerIntegrator.class.getSimpleName().replace("Integrator", ""));
		//parameters.put("density", 1);

		integrator = ODEIntegrators.getIntegrator(0.0, stopTime, stepSize, parameters);
		ret = new double[(int) Math.ceil(stopTime / stepSize) + 1][initial.length];
			
		StepNormalizerBounds bounds = StepNormalizerBounds.LAST;
		
		FixedStepHandler fixedStepHandler = new FixedStepHandler() {
			int lastTime = 0;
		    public void init(double t0, double[] y0, double t) {}
			public void handleStep(double t, double[] y, double[] yDot,
					boolean isLast) {
				int i = (int)Math.round(t/stepSize);
				if  (!isLast || t - i * stepSize == 0.0){ 					
					if (t <= stopTime + 1e-10 && i > lastTime) { 
						ret[i] = Arrays.copyOf(y, y.length);
						if (i > lastTime + 1) {
							System.out.println("OUTCH");
						}					
						lastTime = i;
					}
				}
			}
		};
		
		integrator.addStepHandler(new StepNormalizer(stepSize, fixedStepHandler, StepNormalizerMode.MULTIPLES, bounds));
		this.accumulator = accumulator;
	}
	
	public void integrate(double from, double to, double[] counts) {		
        integrator.integrate(accumulator, from, counts, to, counts);
	}

	public double[][] getRet() {
		return ret;
	}
	
	

}
