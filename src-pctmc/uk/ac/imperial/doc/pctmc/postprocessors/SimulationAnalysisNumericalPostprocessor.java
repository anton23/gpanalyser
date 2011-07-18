package uk.ac.imperial.doc.pctmc.postprocessors;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProductExpression;
import uk.ac.imperial.doc.pctmc.javaoutput.simulation.JavaPrinterPopulationBased;
import uk.ac.imperial.doc.pctmc.javaoutput.utils.ClassCompiler;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.simulation.SimulationUpdater;
import uk.ac.imperial.doc.pctmc.simulation.utils.AccumulatorUpdater;
import uk.ac.imperial.doc.pctmc.simulation.utils.GillespieSimulator;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class SimulationAnalysisNumericalPostprocessor extends NumericalPostprocessor {
	
	private PCTMCSimulation simulation;

	
	
	private SimulationUpdater updater;
	private AccumulatorUpdater accUpdater; 
	
	@Override
	protected void prepare(AbstractPCTMCAnalysis analysis, Constants constants) {
		simulation = null;
		if (analysis instanceof PCTMCSimulation){
		this.simulation = (PCTMCSimulation) analysis;  
		
		dataPoints = new double[(int) Math.ceil(stopTime / stepSize)][momentIndex
		                                              				.size() + generalExpectationIndex.size()];
		updater = (SimulationUpdater) ClassCompiler
		.getInstance(getProductUpdaterCode(constants), updaterClassName);
		
		accUpdater = (AccumulatorUpdater) ClassCompiler
		.getInstance(getAccumulatorUpdaterCode(constants), accumulatorUpdaterName);
		}
	}

	@Override
	protected void calculateDataPoints(Constants constants) {
		if (simulation!=null){
			simulate(constants);
		}		
	}
	
	double[] initial;
	
	private void simulate(Constants constants) {

		simulation.getEventGenerator().setRates(constants.getFlatConstants());

		int n = simulation.getPCTMC().getStateIndex().size();
		initial = new double[n];
		for (int i = 0; i < n; i++) {
			ExpressionEvaluatorWithConstants evaluator = new ExpressionEvaluatorWithConstants(constants);
			simulation.getPCTMC().getInitCounts()[i].accept(evaluator);
			initial[i] = evaluator.getResult();
		}

		PCTMCLogging.info("Running Gillespie simulator.");
		PCTMCLogging.increaseIndent();

		updater.setRates(constants.getFlatConstants());
		accUpdater.setRates(constants.getFlatConstants());

		int m = momentIndex.size();

		double[][] tmp;
		for (int r = 0; r < simulation.getReplications(); r++) {
			if (r > 0 && r % (simulation.getReplications() / 5 > 0 ? simulation.getReplications() / 5 : 1) == 0) {
				PCTMCLogging.info(r + " replications finished.");
			}
			tmp = GillespieSimulator.simulateAccumulated(simulation.getEventGenerator(),
					initial, stopTime, stepSize, accUpdater);
			for (int t = 0; t < (int) Math.ceil(stopTime / stepSize); t++) {
				updater.update(dataPoints[t], tmp[t]);				
			}
		}

		for (int t = 0; t < dataPoints.length; t++) {
			for (int i = 0; i < m + generalExpectationIndex.size(); i++) {
				dataPoints[t][i] = dataPoints[t][i] / simulation.getReplications();
			}
		}
		PCTMCLogging.decreaseIndent();
	}

	private String updaterClassName = "GeneratedProductUpdater";
	
	private String getProductUpdaterCode(Constants variables) {
		StringBuilder ret = new StringBuilder();
		ret.append("import " + SimulationUpdater.class.getName() + ";\n");
		ret.append("public class " + updaterClassName + " extends "
				+ SimulationUpdater.class.getName() + "{\n");
		ret.append("double[] newValues = new double[" + 
				(momentIndex.size() + simulation.getAccumulatedMomentIndex().size() + generalExpectationIndex.size()) + "];\n");
		ret.append("    public void update(double[] values, double[] oldValues){\n");
		
		for (Map.Entry<CombinedPopulationProduct, Integer> entry:momentIndex.entrySet()){
			ret.append("newValues[" + entry.getValue() + "]=(");
			//!!
			AbstractExpression tmp = CombinedProductExpression.create(entry.getKey());
			JavaPrinterPopulationBased printer = new JavaPrinterPopulationBased(variables, simulation.getPCTMC().getStateIndex(), simulation.getAccumulatedMomentIndex(), "oldValues");
			tmp.accept(printer); 
			ret.append(printer.toString()); 
			ret.append(");\n");
			ret.append("values[" +entry.getValue() + "]+= newValues["+entry.getValue() +"];\n");
			
		}
		
		for (Map.Entry<AbstractExpression, Integer> entry:generalExpectationIndex.entrySet()){
			ret.append("values["+(momentIndex.size() + entry.getValue()) + "]+=");
			JavaPrinterPopulationBased printer =
				  new JavaPrinterPopulationBased(variables, simulation.getPCTMC().getStateIndex(), 
						  simulation.getAccumulatedMomentIndex(), "oldValues");
			entry.getKey().accept(printer); 
			ret.append(printer.toString()); 
			ret.append(";\n"); 
		}
		
		ret.append("    }");
		ret.append("}");
		return ret.toString();
	}
	
	private final String accumulatorUpdaterName = "GeneratedAccumulatorUpdater";
	
	private String getAccumulatorUpdaterCode(Constants variables) {

		StringBuilder ret = new StringBuilder();
		ret.append("import " + AccumulatorUpdater.class.getName() + ";\n");
		ret.append("public class " + accumulatorUpdaterName + " extends "
				+ AccumulatorUpdater.class.getName() + "{\n");
		ret.append( "       {n = " + simulation.getAccumulatedMomentIndex().size() + " ;}\n");
		ret.append("    public double[] update(double[] counts, double delta){\n");
		ret.append("      double[] values = new double[" + simulation.getAccumulatedMomentIndex().size() + "];\n");
		 
		
		for (Map.Entry<PopulationProduct, Integer> entry : simulation.getAccumulatedMomentIndex().entrySet()) {
			ret.append("values[" + entry.getValue() + "]=delta*(");			
			JavaPrinterPopulationBased printer = new JavaPrinterPopulationBased(variables, simulation.getPCTMC().getStateIndex(),simulation.getAccumulatedMomentIndex(), "counts");
			PopulationProductExpression tmp = new PopulationProductExpression(entry.getKey()); 
			tmp.accept(printer); 		
			ret.append(printer.toString()); 
			ret.append(");\n");
		}
		
		ret.append("     return values;\n");
		ret.append("    }");
		ret.append("}");

		return ret.toString();
	}




}
