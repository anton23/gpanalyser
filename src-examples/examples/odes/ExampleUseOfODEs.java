package examples.odes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataset;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.RungeKutta;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.SystemOfODEs;

public class ExampleUseOfODEs {
	
	static class PredatorPreyODEs extends SystemOfODEs{
		//from http://en.wikipedia.org/wiki/Lotka%E2%80%93Volterra_equation
		
		//y[0] is prey, y[1] is predator count
		public static Map<String, Integer> getOdeVariableIndex(){
			Map<String, Integer> odeVariableIndex = new HashMap<String, Integer>();
			odeVariableIndex.put("prey", 0); odeVariableIndex.put("predator", 1);
			return odeVariableIndex;
		}
		
		//r[0] is alpha, r[1] is beta, r[2] is gamma, r[3] is delta
		public static Map<String, Integer> getConstantIndex(){
			Map<String, Integer> index = new HashMap<String, Integer>();
			index.put("alpha", 0); index.put("beta", 1); index.put("gamma", 2); index.put("delta", 3);
			index.put("predator0", 4); index.put("prey0", 5); index.put("scale", 6);
			return index;
		}
		

		@Override
		public double[] derivn(double x, double[] y) {
			double[] ret = new double[2];
			ret[0] = y[0]*(r[0]-r[1]*y[1]);
			ret[1] = -y[1]*(r[2]-r[3]*y[0]);
			return ret;
		}
	}

	
	private static Constants createConstants(){
		Map<String, Double> constantValues = new HashMap<String, Double>();
		//values from http://mathworld.wolfram.com/Lotka-VolterraEquations.html
		constantValues.put("alpha", 1.5); constantValues.put("beta", 1.0); 
		constantValues.put("gamma", 3.0); constantValues.put("delta", 1.0);
		constantValues.put("predator0", 5.0); constantValues.put("prey0", 10.0);
		constantValues.put("scale", 1.0);
		Map<String, Integer> index = PredatorPreyODEs.getConstantIndex();
		return new ConstantsWithForcedIndex(constantValues, index);
	}
	
	// The initial values are 
	// prey(0) = prey0*scale
	// predator(0) = predator0*scale
	private static Map<String, AbstractExpression> getInitialValueExpressions(){
		Map<String, AbstractExpression> ret = new HashMap<String, AbstractExpression>();
		AbstractExpression scaleExpression = new ConstantExpression("scale");
		ret.put("predator", ProductExpression.create(new ConstantExpression("predator0"), scaleExpression));
		ret.put("prey", ProductExpression.create(new ConstantExpression("prey0"), scaleExpression));
		return ret;
	}
	
	
	// Just to demonstrate, we will be interested in the value of 
	// predator(t), prey(t) and predator(t)*prey(t)
	private static List<AbstractExpression> getObjectiveFunctions(){
		List<AbstractExpression> ret = new LinkedList<AbstractExpression>();
		ret.add(new ODEVariableExpression("predator"));
		ret.add(new ODEVariableExpression("prey"));
		ret.add(ProductExpression.create(new ODEVariableExpression("predator"), new ODEVariableExpression("prey")));
		return ret;
	}
	

	

	public void example(){
		SystemOfODEs odes = new PredatorPreyODEs();
		Constants constants = createConstants();
		Map<String, AbstractExpression> initialValueExpressions = getInitialValueExpressions();
		Map<String, Integer> odeVariableIndex = PredatorPreyODEs.getOdeVariableIndex();
		//Let's solve the ODEs for different values of prey0, say 0,4,8,12,16,20,24,28,32
		for (int v = 0; v<=8; v++){
			constants.setConstantValue("prey0", v*4.0);
			double[] initial = new double[2];
			//first we set initial values
			for (Map.Entry<String, AbstractExpression> e:initialValueExpressions.entrySet()){
				ExpressionEvaluator evaluator = new ExpressionEvaluatorWithConstants(constants);
				e.getValue().accept(evaluator);
				initial[odeVariableIndex.get(e.getKey())] = evaluator.getResult();
			}
			// We tell the odes what the constants are
			odes.setRates(constants.getFlatConstants());
			// And comput the solution
			double[][] result = RungeKutta.rungeKutta(odes, initial, 20.0, 0.1, 10);
			// Just for fun we plot the solution values of the ode variables
			XYDataset dataset = AnalysisUtils.getDatasetFromArray(result, 0.1, new String[]{"prey", "predator"});
			PCTMCChartUtilities.drawChart(dataset, "time", "count", "Example", "Lotka voltera");
			
			// And we plot the value of the objective function, say at time 10.0, i.e. at index 100
			System.out.println("The values of objective functions for prey0="+constants.getConstantValue("prey0")+":");
			for (AbstractExpression of:getObjectiveFunctions()){
				ExpressionEvaluator evaluator = new ExpressionEvaluatorWithODEVariables(result[100], odeVariableIndex);
				of.accept(evaluator);
				System.out.println("   Value of " + of.toString() + " at time 10.0 is " + evaluator.getResult());
			}
		}
	}
	
	public void exampleWithoutAbstractExpressions(){
		SystemOfODEs odes = new PredatorPreyODEs();
		//Let's solve the ODEs for different values of prey0, say 0,4,8,12,16,20,24,28,32
		for (int v = 0; v<=8; v++){
			double[] initial = new double[]{v*4.0, 4.0};
			
			// We tell the odes what the constants are
			odes.setRates(new double[]{1.5, 3.0, 1.0, 1.0});
			// And comput the solution
			double[][] result = RungeKutta.rungeKutta(odes, initial, 20.0, 0.1, 10);
			// Just for fun we plot the solution values of the ode variables
			XYDataset dataset = AnalysisUtils.getDatasetFromArray(result, 0.1, new String[]{"prey", "predator"});
			PCTMCChartUtilities.drawChart(dataset, "time", "count", "Example", "Lotka voltera");
			System.out.println("The value of predator*prey at time 10.0 is " + (result[100][0]*result[100][1]));
		}
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ExampleUseOfODEs().example();
	}
}
