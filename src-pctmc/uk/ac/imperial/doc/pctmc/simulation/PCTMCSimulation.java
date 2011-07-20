package uk.ac.imperial.doc.pctmc.simulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.javaoutput.simulation.JavaPrinterPopulationBased;
import uk.ac.imperial.doc.pctmc.javaoutput.utils.ClassCompiler;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.simulation.utils.AggregatedStateNextEventGenerator;
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

	private BiMap<PopulationProduct, Integer> accumulatedMomentIndex;
	
	

	public BiMap<PopulationProduct, Integer> getAccumulatedMomentIndex() {
		return accumulatedMomentIndex;
	}

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
	
	private void prepareGeneralExpectationIndex(){
		int j = 0; 
		generalExpectationIndex = HashBiMap.<AbstractExpression,Integer>create(); 
		for (AbstractExpression eg: usedGeneralExpectations){
			generalExpectationIndex.put(eg,j++);
		}
	}

	int n;

	public void prepare(Constants variables) {		
		prepareAccumulatedIndex();
		prepareGeneralExpectationIndex();

		PCTMCLogging.info("Generating one step generator.");
		PCTMCLogging.increaseIndent();

		String code = getEventGeneratorCode(variables);
		PCTMCLogging.decreaseIndent();

		eventGenerator = (AggregatedStateNextEventGenerator) ClassCompiler.getInstance(code,
				generatorName);
	}

	private AggregatedStateNextEventGenerator eventGenerator;

	

	public AggregatedStateNextEventGenerator getEventGenerator() {
		return eventGenerator;
	}

	private final String generatorName = "GeneratedNextEventGenerator";
	
	public int getReplications() {
		return replications;
	}


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
