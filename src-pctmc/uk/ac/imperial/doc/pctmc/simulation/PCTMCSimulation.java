package uk.ac.imperial.doc.pctmc.simulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
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
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.simulation.utils.AccumulatorUpdater;
import uk.ac.imperial.doc.pctmc.simulation.utils.AggregatedStateNextEventGenerator;
import uk.ac.imperial.doc.pctmc.simulation.utils.GillespieSimulator;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class PCTMCSimulation extends AbstractPCTMCAnalysis {

	private int replications;

	@Override
	public String toString() {
		return "Simulation"; 
	}

	public PCTMCSimulation(PCTMC pctmc, double stopTime, double stepSize,int replications) {
		super(pctmc,stopTime,stepSize);
		this.replications = replications;
		n = pctmc.getStateIndex().size();
	}

	@Override
	public void analyse(Constants variables) {
		prepareAccumulatedIndex();
		prepare(variables);
		simulate(variables);
	}

	private BiMap<PopulationProduct, Integer> accumulatedMomentIndex;
	//private BiMap<StateProduct, Integer> productIndex;

	private void prepareAccumulatedIndex() {

		int j = 0;
		accumulatedMomentIndex = HashBiMap.<PopulationProduct, Integer> create();
		for (CombinedPopulationProduct combinedProduct : usedCombinedProducts) {
			for (PopulationProduct accumulatedProduct : combinedProduct
					.getAccumulatedProducts()) {
				if (!accumulatedMomentIndex.containsKey(accumulatedProduct)) 
					accumulatedMomentIndex.put(accumulatedProduct, j++);
			}
		}
	}

	int n;

	public void prepare(Constants variables) {

		PCTMCLogging.info("Generating one step generator.");
		PCTMCLogging.increaseIndent();

		String code = getEventGeneratorCode(variables);
		PCTMCLogging.decreaseIndent();

		eventGenerator = (AggregatedStateNextEventGenerator) ClassCompiler.getInstance(code,
				generatorName);
	}

	private AggregatedStateNextEventGenerator eventGenerator;
	private double[] initial;

	private final String generatorName = "GeneratedNextEventGenerator";
	
	private final String accumulatorUpdaterName = "GeneratedAccumulatorUpdater";

	private String getEventGeneratorCode(Constants variables) {
		StringBuilder code = new StringBuilder();
		code.append("import " + AggregatedStateNextEventGenerator.class.getName() + "; \n");
		code.append("import java.util.ArrayList; \n");
		code.append("public class " + generatorName + " extends "
				+ AggregatedStateNextEventGenerator.class.getName() + "{\n");

		Collection<EvolutionEvent> observableEvents = pctmc
				.getEvolutionEvents();

		int nevents = observableEvents.size();
		code.append("   @SuppressWarnings(\"unchecked\")");
		code.append("   public void initCoefficients(){\n");
		code.append("       increasing =  (ArrayList<Integer>[])(new ArrayList["
				+ nevents + "]);\n");
		code.append("       decreasing =  (ArrayList<Integer>[])(new ArrayList["
				+ nevents + "]);\n");
		code.append("       weights = new double[" + nevents + "];\n");
		int i = 0;
		
		for (EvolutionEvent e : observableEvents) {
			//dirty hack around the java method size problem
			if (i==500){
				code.append("initCoefficients2();\n};");
				code.append("private void initCoefficients2(){\n");
			}
			code.append("      increasing[" + i + "] = new ArrayList<Integer>("
					+ e.getIncreasing().size() + ");\n");
			code.append("      decreasing[" + i + "] = new ArrayList<Integer>("
					+ e.getDecreasing().size() + ");\n");
			for (State increasingState : e.getIncreasing()) {
				code.append("      increasing[" + i + "].add("
						+ stateIndex.get(increasingState) + ");\n");
			}

			for (State decreasingState : e.getDecreasing()) {
				code.append("      decreasing[" + i + "].add("
						+ stateIndex.get(decreasingState) + ");\n");
			}
			i++;
		}
		code.append("   }\n");
		StringBuilder method = new StringBuilder();

		String methodHeader = "   public void recalculateWeights(double[] counts){\n";
		i = 0;
		method.append("         totalRate=0.0;\n");

		for (EvolutionEvent e : observableEvents) {
			JavaPrinterPopulationBased ratePrinter = new JavaPrinterPopulationBased(
					variables, stateIndex, accumulatedMomentIndex,
					"counts");
			e.getRate().accept(ratePrinter);
			String rate = ratePrinter.toString();
			method.append("      weights[" + i + "] = " + rate + ";\n");
			method.append("      totalRate+= weights[" + i + "];\n");

			i++;
		}

		method.append("   }\n");

		code.append(methodHeader);

		code.append(method);
		code.append("}\n");
		if (PCTMCOptions.debug) {
			File file = new File(PCTMCOptions.debugFolder + "/codeSim");
			try {
				Writer out = new BufferedWriter(new FileWriter(file));
				out.write(code.toString());
				out.close();
			} catch (IOException e) {
				PCTMCLogging.error(e.getStackTrace().toString());
			}
		}
		return code.toString();
	}
	
	private String updaterClassName = "GeneratedProductUpdater";
	
	private String getProductUpdaterCode(Constants variables) {
		StringBuilder ret = new StringBuilder();
		ret.append("import " + SimulationUpdater.class.getName() + ";\n");
		ret.append("public class " + updaterClassName + " extends "
				+ SimulationUpdater.class.getName() + "{\n");
		ret.append("double[] newValues = new double[" + 
				(momentIndex.size() + accumulatedMomentIndex.size() + generalExpectationIndex.size()) + "];\n");
		ret.append("    public void update(double[] values, double[] oldValues){\n");
		
		for (Map.Entry<CombinedPopulationProduct, Integer> entry:momentIndex.entrySet()){
			ret.append("newValues[" + entry.getValue() + "]=(");
			//!!
			AbstractExpression tmp = CombinedProductExpression.create(entry.getKey());
			JavaPrinterPopulationBased printer = new JavaPrinterPopulationBased(variables, stateIndex, accumulatedMomentIndex, "oldValues");
			tmp.accept(printer); 
			ret.append(printer.toString()); 
			ret.append(");\n");
			ret.append("values[" +entry.getValue() + "]+= newValues["+entry.getValue() +"];\n");
			
		}
		
		for (Map.Entry<AbstractExpression, Integer> entry:generalExpectationIndex.entrySet()){
			ret.append("values["+(momentIndex.size() + entry.getValue()) + "]+=");
			JavaPrinterPopulationBased printer =
				  new JavaPrinterPopulationBased(variables, stateIndex, 
						  accumulatedMomentIndex, "oldValues");
			entry.getKey().accept(printer); 
			ret.append(printer.toString()); 
			ret.append(";\n"); 
		}
		
		ret.append("    }");
		ret.append("}");
		return ret.toString();
	}
	
		
	private String getAccumulatorUpdaterCode(Constants variables) {

		StringBuilder ret = new StringBuilder();
		ret.append("import " + AccumulatorUpdater.class.getName() + ";\n");
		ret.append("public class " + accumulatorUpdaterName + " extends "
				+ AccumulatorUpdater.class.getName() + "{\n");
		ret.append( "       {n = " + accumulatedMomentIndex.size() + " ;}\n");
		ret.append("    public double[] update(double[] counts, double delta){\n");
		ret.append("      double[] values = new double[" + accumulatedMomentIndex.size() + "];\n");
		 
		
		for (Map.Entry<PopulationProduct, Integer> entry : accumulatedMomentIndex.entrySet()) {
			ret.append("values[" + entry.getValue() + "]=delta*(");			
			JavaPrinterPopulationBased printer = new JavaPrinterPopulationBased(variables, stateIndex,accumulatedMomentIndex, "counts");
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


	private void simulate(Constants variables) {
		if (eventGenerator == null) {
			prepare(variables);
		}

		dataPoints = new double[(int) Math.ceil(stopTime / stepSize)][momentIndex
				.size() + generalExpectationIndex.size()];

		eventGenerator.setRates(variables.getFlatConstants());

		initial = new double[stateIndex.size()];
		for (int i = 0; i < n; i++) {
			ExpressionEvaluatorWithConstants evaluator = new ExpressionEvaluatorWithConstants(variables);
			pctmc.getInitCounts()[i].accept(evaluator);
			initial[i] = evaluator.getResult();
		}

		PCTMCLogging.info("Running Gillespie simulator.");
		PCTMCLogging.increaseIndent();

		SimulationUpdater updater = (SimulationUpdater) ClassCompiler
				.getInstance(getProductUpdaterCode(variables), updaterClassName);
		updater.setRates(variables.getFlatConstants());

		AccumulatorUpdater accUpdater = (AccumulatorUpdater) ClassCompiler
				.getInstance(getAccumulatorUpdaterCode(variables), accumulatorUpdaterName);
		accUpdater.setRates(variables.getFlatConstants());

		int m = momentIndex.size();

		double[][] tmp;
		for (int r = 0; r < replications; r++) {
			if (r > 0 && r % (replications / 5 > 0 ? replications / 5 : 1) == 0) {
				PCTMCLogging.info(r + " replications finished.");
			}
			tmp = GillespieSimulator.simulateAccumulated(eventGenerator,
					initial, stopTime, stepSize, accUpdater);
			for (int t = 0; t < (int) Math.ceil(stopTime / stepSize); t++) {
				updater.update(dataPoints[t], tmp[t]);				
			}
		}

		for (int t = 0; t < dataPoints.length; t++) {
			for (int i = 0; i < m + generalExpectationIndex.size(); i++) {
				dataPoints[t][i] = dataPoints[t][i] / replications;
			}
		}
		PCTMCLogging.decreaseIndent();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + replications;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PCTMCSimulation other = (PCTMCSimulation) obj;
		if (replications != other.replications)
			return false;
		return true;
	}
	
	
}
