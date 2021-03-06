package uk.ac.imperial.doc.gpa.testing.quantitative;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCIterate;
import uk.ac.imperial.doc.pctmc.experiments.iterate.RangeSpecification;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessorCI;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;

public class ClosureComparisonMain {

	protected String modelFile;// =
								// "src-examples/scripts/closurecomparison/models/clientServer.gpepa";
	protected String outputFolder;

	protected PCTMCInterpreter interpreter;
	protected PCTMCFileRepresentation fileRepresentation;

	protected Constants constants;
	protected Map<ExpressionVariable, AbstractExpression> unfoldedVariables;

	protected List<PlotDescription> plots;

	// Analyses to use for evaluation and expressions
	// for comparison
	protected List<PCTMCODEAnalysis> analyses;
	protected List<ODEAnalysisNumericalPostprocessor> postprocessors;

	// Simulation
	protected PCTMCSimulation simulation;
	protected NumericalPostprocessor simPostprocessor;

	// Ranges
	protected List<RangeSpecification> ranges;

	public ClosureComparisonMain(String[] args) {
		OptionParser optionParser = GPAPMain.createOptionParser();
		optionParser.accepts("output", "Saves results into a file")
				.withRequiredArg().ofType(String.class).describedAs(
						"output file");
		OptionSet options = optionParser.parse(args);
		this.interpreter = GPAPMain.processOptions(optionParser, options);
		if (options.has("output")) {
			outputFolder = options.valueOf("output").toString();
			System.out.println("Output file: " + outputFolder);
		}
		modelFile = options.nonOptionArguments().iterator().next();
	}

	private void loadAnalyses() {
		analyses = new LinkedList<PCTMCODEAnalysis>();
		postprocessors = new LinkedList<ODEAnalysisNumericalPostprocessor>();
		double stopTime = 0.0;
		double stepSize = 0.0;
		for (AbstractPCTMCAnalysis a : fileRepresentation.getPlots().keySet()) {
			if (a instanceof PCTMCODEAnalysis) {
				analyses.add((PCTMCODEAnalysis) a);
				ODEAnalysisNumericalPostprocessor postprocessor = (ODEAnalysisNumericalPostprocessor) a
						.getPostprocessors().get(0);
				postprocessors.add(postprocessor);

			}
			if (a instanceof PCTMCSimulation) {
				if (simulation != null) {
					throw new AssertionError(
							"The model file can contain only one simulation!");
				} else {
					simulation = (PCTMCSimulation) a;
					simPostprocessor = (NumericalPostprocessor) simulation
							.getPostprocessors().get(0);
				}
			}
			double tmp = ((NumericalPostprocessor) a.getPostprocessors()
					.iterator().next()).getStopTime();
			if (stopTime == 0.0) {
				stopTime = tmp;
			} else {
				if (tmp != stopTime) {
					throw new AssertionError("All stop times must be the same!");
				}
			}
			tmp = ((NumericalPostprocessor) a.getPostprocessors().iterator()
					.next()).getStepSize();
			if (stepSize == 0.0) {
				stepSize = tmp;
			} else {
				if (tmp != stepSize) {
					throw new AssertionError("All step sizes must be the same!");
				}
			}
			AbstractPCTMCAnalysis.unfoldVariablesAndSetUsedProducts(a, plots,
					unfoldedVariables);
		}
		System.out.println("Found " + analyses.size() + " analyses.");
		for (int i = 0; i < analyses.size(); i++) {
			PCTMCODEAnalysis analysis = analyses.get(i);
			analysis.prepare(constants);
			postprocessors.get(i).prepare(analysis, constants);
		}
		simulation.prepare(constants);
		simPostprocessor.prepare(simulation, constants);
		// Dirty hack for now
		if (simPostprocessor instanceof NumericalPostprocessorCI) {
			((NumericalPostprocessorCI) simPostprocessor).setPlotDescriptions(
					new ArrayList<PlotDescription>(fileRepresentation.getPlots().get(
							fileRepresentation.getPlots().keySet().iterator().next())));
		}

		ranges = ((PCTMCIterate) fileRepresentation.getExperiments().iterator()
				.next()).getRanges();
	}

	private void loadModel() {
		try {
			fileRepresentation = interpreter.parsePCTMCFile(modelFile);

			constants = fileRepresentation.getConstants();

			unfoldedVariables = fileRepresentation.getUnfoldedVariables();
			//Set<AbstractExpression> allExp = new HashSet<AbstractExpression>();
			//expressions = new LinkedList<AbstractExpression>();
			
			plots = new LinkedList<PlotDescription>(
					fileRepresentation.getPlots().get(fileRepresentation.getPlots().keys().elementSet().iterator().next()));

			/*for (PlotDescription d : fileRepresentation.getPlots().values()) {
				for (AbstractExpression p : d.getExpressions()) {
					if (!allExp.contains(p)) {
						allExp.add(p);
						expressions.add(p);
					}
				}
			}*/
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void compareInitial() {
		/*System.out.println("Comparing closures on the initial constants:");
		ErrorEvaluator errorEvaluator = new ErrorEvaluator(postprocessors, simPostprocessor, expressions, constants);
		errorEvaluator.calculateErrors(constants);
		ErrorSummary[][] errors = errorEvaluator.getAccumulatedErrors();
		System.out.println(ErrorEvaluator.printSummary(errors));
		
		for (PlotDescription pd : plots) {
			simPostprocessor.plotData(simulation.toString(), constants, pd.getExpressions(), null);
		}
		for (int i = 0; i < postprocessors.size(); i++) {
			for (PlotDescription pd : plots) {
				postprocessors.get(i).plotData(analyses.get(i).toString(), constants, pd.getExpressions(), null);
			}
		}*/
		
	}

	public void compareClosures() {
		loadModel();
		loadAnalyses();
		compareInitial();
		ClosureComparison closureComparison = new ClosureComparison(postprocessors, simPostprocessor, plots,
				constants, ranges, outputFolder);

		closureComparison.run(constants);
		System.out.println("Finished.");
		StringBuilder out = new StringBuilder();
		if (outputFolder != null) {
			System.out.println("Saving results in the file " + outputFolder);
			double[][] maxAverage = closureComparison.getMaxAverage();
			double[][] averageAverage = closureComparison.getAverageAverage();
			for (int i = 0; i < maxAverage[0].length; i++) {
				out.append(i);
				for (int j = 0; j < analyses.size(); j++) {
					out.append("\t" + averageAverage[j][i] + "\t" + maxAverage[j][i]);
				}
				out.append("\n");
			}
			FileUtils.writeGeneralFile(out.toString(), outputFolder+"/summary");
		}

	}

	public static void main(String[] args) {
		new ClosureComparisonMain(args).compareClosures();
	}
}
