package uk.ac.imperial.doc.pctmc.postprocessors.numerical;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.JavaExpressionPrinterWithVariables;
import uk.ac.imperial.doc.jexpressions.javaoutput.utils.JExpressionsJavaUtils;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.javaoutput.simulation.JavaPrinterPopulationBased;
import uk.ac.imperial.doc.pctmc.javaoutput.utils.ClassCompiler;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.representation.accumulations.AccumulationVariable;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.simulation.SimulationUpdater;
import uk.ac.imperial.doc.pctmc.simulation.utils.AccumulatorUpdater;
import uk.ac.imperial.doc.pctmc.simulation.utils.AggregatedStateNextEventGenerator;
import uk.ac.imperial.doc.pctmc.simulation.utils.GillespieSimulator;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

public class SimulationAnalysisNumericalPostprocessor extends NumericalPostprocessor {
	
	//private PCTMCSimulation simulation;
	
	private SimulationUpdater updater;
	private AccumulatorUpdater accUpdater; 
	private AggregatedStateNextEventGenerator eventGenerator;
	
	private String overrideCode;
	private String overrideCodeClassName;

	protected int replications; 

	public int getReplications() {
		return replications;
	}
	

	public void setReplications(int replications) {
		this.replications = replications;
	}


	public static final String generatorName = "GeneratedNextEventGenerator";
	public static final String updaterClassName = "GeneratedProductUpdater";
	
	protected Map<String, Object> parameters;
	
	protected AbstractExpression[] initialExpressions;
	
	protected boolean prepared;
	
	@Override
	public String toString() {
		return "(stopTime = " + stopTime + ", stepSize = " + stepSize + ", replications = " + replications+")";
	}
	
	
	@Override
	public NumericalPostprocessor getNewPreparedPostprocessor(Constants constants) {
		assert(prepared);
		SimulationAnalysisNumericalPostprocessor ret = new SimulationAnalysisNumericalPostprocessor(
				stopTime, stepSize, replications, parameters);
		ret.fastPrepare(momentIndex, generalExpectationIndex,
				productUpdaterCode, accumulatorUpdaterCode, eventGeneratorCode,
				initialExpressions, eventGeneratorClassName);
		return ret;
	}
	
	protected void fastPrepare(Map<CombinedPopulationProduct, Integer> momentIndex, Map<AbstractExpression, Integer> generalExpectationIndex, 
							   String productUpdaterCode, String accumulatorUpdaterCode, String eventGeneratorCode,
							   AbstractExpression[] initialExpressions, String eventGeneratorClassName) {
		this.prepared = true;
		this.momentIndex = momentIndex;
		this.generalExpectationIndex = generalExpectationIndex;
		this.productUpdaterCode = productUpdaterCode;
		this.accumulatorUpdaterCode = accumulatorUpdaterCode;
		this.eventGeneratorCode = eventGeneratorCode;
		this.eventGeneratorClassName = eventGeneratorClassName;
		this.initialExpressions = initialExpressions;
		compileUpdatersAndGenerator();
		dataPoints = new double[(int) Math.ceil(stopTime / stepSize)]
		                       [momentIndex.size() + generalExpectationIndex.size()];
	}

	
	public SimulationAnalysisNumericalPostprocessor(double stopTime,
			double stepSize, int replications) {
		super(stopTime, stepSize);
		this.replications = replications;
		this.prepared = false;
	}

	
	public PCTMCAnalysisPostprocessor regenerate() {
		return new SimulationAnalysisNumericalPostprocessor(stopTime, stepSize, replications, parameters);		
	}

	public SimulationAnalysisNumericalPostprocessor(double stopTime,
			double stepSize, int replications, Map<String, Object> parameters) {
		this(stopTime, stepSize, replications);
		this.parameters = parameters;
		if (parameters != null && parameters.containsKey("overrideCode")) {
			Object value = parameters.get("overrideCode");
			if (value instanceof String) {
				String asString = ((String) value);
				try {
					overrideCode =  FileUtils.readFile(asString);
					String[] split = asString.split("/");
					overrideCodeClassName = split[split.length-1].replace(".java", "");
				}
				catch (IOException e) {
					throw new AssertionError("File + " + asString + " cannot be open!");
				}
				
			} else {
				throw new AssertionError("Given value of 'overrideCode' has to be a filename!");
			}
		}

	}

    private void setInitialExpressions() {
        int n = simulation.getPCTMC().getStateIndex().size();
        initialExpressions = new AbstractExpression[n];
        for (int i = 0; i < n; i++) {
            initialExpressions[i] = simulation.getPCTMC().getInitCounts()[i];
        }
    }
	
	protected String productUpdaterCode, accumulatorUpdaterCode, eventGeneratorCode ;
    private PCTMCSimulation simulation;
	
	@Override
	public void prepare(AbstractPCTMCAnalysis analysis, Constants constants) {
		super.prepare(analysis, constants);
		prepared = false;
		if (analysis instanceof PCTMCSimulation) {
			prepared = true;
			simulation = (PCTMCSimulation) analysis;

			
			productUpdaterCode = getProductUpdaterCode(simulation, constants, false);
			accumulatorUpdaterCode = getAccumulatorUpdaterCode(simulation, constants);
			if (overrideCode==null) {
				eventGeneratorCode = getEventGeneratorCode(simulation, constants);
				eventGeneratorClassName = generatorName;
			} else {
				eventGeneratorCode = overrideCode;
				eventGeneratorClassName = overrideCodeClassName;
			}
			compileUpdatersAndGenerator();
			
            setInitialExpressions();

			PCTMCLogging.info("Generating one step generator.");
			PCTMCLogging.increaseIndent();

			PCTMCLogging.decreaseIndent();
		}
	}
	
	private String eventGeneratorClassName;
	
	protected void compileUpdatersAndGenerator() {
		updater = (SimulationUpdater) ClassCompiler.getInstance(
				productUpdaterCode, updaterClassName);
		accUpdater = (AccumulatorUpdater) ClassCompiler.getInstance(
				accumulatorUpdaterCode,
				accumulatorUpdaterName);
		eventGenerator = (AggregatedStateNextEventGenerator) ClassCompiler.getInstance(eventGeneratorCode,
				eventGeneratorClassName);
		
	}

	@Override
	public void calculateDataPoints(Constants constants) {
		if (prepared){
			simulate(constants);
		}		
	}
	
	
	
	private void simulate(Constants constants) {
		eventGenerator.setRates(constants.getFlatConstants());

        setInitialExpressions();
		double[] initial = new double[initialExpressions.length];
		for (int i = 0; i < initialExpressions.length; i++) {
			ExpressionEvaluatorWithConstants evaluator = new ExpressionEvaluatorWithConstants(constants);
			initialExpressions[i].accept(evaluator);
			initial[i] = evaluator.getResult();
		}

		PCTMCLogging.info("Running Gillespie simulator.");
		PCTMCLogging.increaseIndent();

		updater.setRates(constants.getFlatConstants());
		accUpdater.setRates(constants.getFlatConstants());

		int m = momentIndex.size();
		dataPoints = new double[(int) Math.ceil(stopTime / stepSize)][momentIndex
		                                          					.size()
		                                          					+ generalExpectationIndex.size()];
		double[][] tmp;
		for (int r = 0; r < replications; r++) {
			if (r > 0 && r % (replications / 5 > 0 ? replications/ 5 : 1) == 0) {
				PCTMCLogging.info(r + " replications finished.");
			}
			tmp = GillespieSimulator.simulateAccumulated(eventGenerator,
					initial, stopTime, stepSize, accUpdater);
			notifyReplicationObservers(tmp);
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

	protected List<ISimulationReplicationObserver> replicationObservers;
	
	public void addReplicationObserver(ISimulationReplicationObserver o) {
		if (replicationObservers == null) {
			replicationObservers = new LinkedList<ISimulationReplicationObserver>();
		}
		replicationObservers.add(o);
	}
	
	protected void notifyReplicationObservers(double[][] tmp) {
		double[][] data = new double[(int) Math.ceil(stopTime / stepSize)][momentIndex
			                                          					.size()
			                                          					+ generalExpectationIndex.size()];
		for (int t = 0; t < (int) Math.ceil(stopTime / stepSize); t++) {
			updater.update(data[t], tmp[t]);				
		}
		if (replicationObservers == null) return;
		for (ISimulationReplicationObserver o: replicationObservers) {
			o.newReplication(data);
		}
	}
	

	public static final String mUpdaterNoAddClassName = "GeneratedProductUpdaterNoAdd";


	public static String getProductUpdaterCode(PCTMCSimulation simulation, Constants variables, boolean noadd) {
		Map<CombinedPopulationProduct, Integer> momentIndex = simulation.getMomentIndex();
		Map<AccumulationVariable, Integer> accumulatedMomentIndex = simulation.getAccumulatedMomentIndex();
		Map<AbstractExpression, Integer> generalExpectationIndex = simulation.getGeneralExpectationIndex();
		StringBuilder ret = new StringBuilder();
		ret.append("import " + SimulationUpdater.class.getName() + ";\n");
		ret.append("import " + JExpressionsJavaUtils.class.getName() + ";\n");
		ret.append("public class "
				+ ((noadd) ? mUpdaterNoAddClassName : updaterClassName)
				+ " extends " + SimulationUpdater.class.getName() + "{\n");
		ret.append("double[] newValues = new double["
				+ (momentIndex.size()
						+ accumulatedMomentIndex.size() + generalExpectationIndex
						.size()) + "];\n");
		ret.append("    public void update(double[] values, double[] oldValues){\n");

		for (Map.Entry<CombinedPopulationProduct, Integer> entry : momentIndex
				.entrySet()) {
			ret.append("newValues[" + entry.getValue() + "]=(");
			// !!
			AbstractExpression tmp = CombinedProductExpression.create(entry
					.getKey());
			JavaPrinterPopulationBased printer = new JavaPrinterPopulationBased(
					variables, simulation.getPCTMC().getStateIndex(),
					accumulatedMomentIndex, "oldValues", true);
			tmp.accept(printer);
			ret.append(printer.toString());
			ret.append(");\n");
			ret.append("values[" + entry.getValue() + "]"
					+ ((!noadd) ? "+" : "") + "= newValues[" + entry.getValue()
					+ "]" + ";\n");
		}

		ret.append("    }");
		ret.append("}");
		return ret.toString();
	}
	
	public static final String accumulatorUpdaterName = "GeneratedAccumulatorUpdater";
	
	public static String getAccumulatorUpdaterCode(PCTMCSimulation simulation, Constants constants) {

		StringBuilder ret = new StringBuilder();
		ret.append("import " + AccumulatorUpdater.class.getName() + ";\n");
		ret.append("import " + JExpressionsJavaUtils.class.getName() + ";\n");		
		ret.append("public class " + accumulatorUpdaterName + " extends "
				+ AccumulatorUpdater.class.getName() + "{\n");
		ret.append( "       {n = " + simulation.getAccumulatedMomentIndex().size() + " ;}\n");
		ret.append("    public double[] update(double[] counts, double delta){\n");
		ret.append("      double[] values = new double[" + simulation.getAccumulatedMomentIndex().size() + "];\n");
		 
		
		for (Map.Entry<AccumulationVariable, Integer> entry : simulation.getAccumulatedMomentIndex().entrySet()) {
			ret.append("values[" + entry.getValue() + "]=delta*(");			
			JavaPrinterPopulationBased printer = new JavaPrinterPopulationBased(constants, simulation.getPCTMC().getStateIndex(),simulation.getAccumulatedMomentIndex(), "counts", true);

			AbstractExpression tmp = entry.getKey().getDdt(); 
			tmp.accept(printer); 		
			ret.append(printer.toString()); 
			ret.append(");\n");
		}
		
		ret.append("     return values;\n");
		ret.append("    }");
		ret.append("}");

		return ret.toString();
	}

	public static String getEventGeneratorCode(PCTMCSimulation simulation, Constants constants) {	
		StringBuilder code = new StringBuilder();
		code.append("import " + AggregatedStateNextEventGenerator.class.getName() + "; \n");
		code.append("import " + JExpressionsJavaUtils.class.getName() + ";\n");		
		code.append("import java.util.ArrayList; \n");
		code.append("public class " + generatorName + " extends "
				+ AggregatedStateNextEventGenerator.class.getName() + "{\n");

		PCTMC pctmc = simulation.getPCTMC();
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
						+ pctmc.getStateIndex().get(increasingState) + ");\n");
			}

			for (State decreasingState : e.getDecreasing()) {
				code.append("      decreasing[" + i + "].add("
						+ pctmc.getStateIndex().get(decreasingState) + ");\n");
			}
			i++;
		}
		code.append("   }\n");
		StringBuilder method = new StringBuilder();

		String methodHeader = "   public void recalculateWeights(double[] counts){\n";
		i = 0;
		method.append("         totalRate=0.0;\n");
		Set<ExpressionVariable> variables = new HashSet<ExpressionVariable>();
		for (EvolutionEvent e : observableEvents) {
			JavaPrinterPopulationBased ratePrinter = new JavaPrinterPopulationBased(
					constants, pctmc.getStateIndex(), simulation.getAccumulatedMomentIndex(),
					"counts", false);
			e.getRate().accept(ratePrinter);
			variables.addAll(ratePrinter.getVariables());
			String rate = ratePrinter.toString();
			method.append("      weights[" + i + "] = " + rate + ";\n");
			method.append("      totalRate+= weights[" + i + "];\n");

			i++;
		}

		method.append("   }\n");
		for (ExpressionVariable v : variables) {
			JavaPrinterPopulationBased tmp = new JavaPrinterPopulationBased(
					constants, pctmc.getStateIndex(), simulation.getAccumulatedMomentIndex(),
					"counts", true);
			v.getUnfolded().accept(tmp);
			methodHeader += "double " + JavaExpressionPrinterWithVariables.escapeName(v.getName()) + " = " + tmp.toString() + ";\n";
		}
		code.append(methodHeader);

		code.append(method);
		code.append("}\n");
		if (PCTMCOptions.debug) {
			File file = new File(PCTMCOptions.debugFolder + "/codeSim");
			File file2 = new File(PCTMCOptions.debugFolder + "/simFriendly");
			try {
				Writer out = new BufferedWriter(new FileWriter(file));
				out.write(code.toString());
				out.close();
				out = new BufferedWriter(new FileWriter(file2));
				out.write(ToStringUtils.iterableToSSV(simulation.getPCTMC().getEvolutionEvents(), "\n"));
				out.close();
			} catch (IOException e) {
				PCTMCLogging.error(e.getStackTrace().toString());
			}
		}
		return code.toString();
	}



}
