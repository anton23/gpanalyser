package scripts.closurecomparison;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.SimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;

import com.google.common.collect.Lists;

public class ClosureComparisonMain {

	protected String modelFile;// =
								// "src-examples/scripts/closurecomparison/models/clientServer.gpepa";
	protected String outputFile;

	protected PCTMCInterpreter interpreter;
	protected PCTMCFileRepresentation fileRepresentation;

	protected Constants constants;
	protected Map<ExpressionVariable, AbstractExpression> unfoldedVariables;
	protected List<AbstractExpression> expressions;

	// Analyses to use for evaluation and expressions
	// for comparison
	protected List<PCTMCODEAnalysis> analyses;
	protected List<ODEAnalysisNumericalPostprocessor> postprocessors;

	// Simulation
	protected PCTMCSimulation simulation;
	protected SimulationAnalysisNumericalPostprocessor simPostprocessor;

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
			outputFile = options.valueOf("output").toString();
			System.out.println("Output file: " + outputFile);
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
					simPostprocessor = (SimulationAnalysisNumericalPostprocessor) simulation
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
			AbstractPCTMCAnalysis.unfoldVariablesAndSetUsedProducts(a, Lists
					.newArrayList(new PlotDescription(expressions)),
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

		ranges = ((PCTMCIterate) fileRepresentation.getExperiments().iterator()
				.next()).getRanges();
	}

	private void loadModel() {
		try {
			fileRepresentation = interpreter.parsePCTMCFile(modelFile);

			constants = fileRepresentation.getConstants();

			unfoldedVariables = fileRepresentation.getUnfoldedVariables();
			Set<AbstractExpression> allExp = new HashSet<AbstractExpression>();
			expressions = new LinkedList<AbstractExpression>();

			for (PlotDescription d : fileRepresentation.getPlots().values()) {
				for (AbstractExpression p : d.getExpressions()) {
					if (!allExp.contains(p)) {
						allExp.add(p);
						expressions.add(p);
					}
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void compareClosures() {
		loadModel();
		loadAnalyses();
		ClosureComparison closureComparison = new ClosureComparison(postprocessors, simPostprocessor, expressions,
				constants, ranges);
		closureComparison.run(constants);
		System.out.println("Finished.");
		StringBuilder out = new StringBuilder();
		if (outputFile != null) {
			System.out.println("Saving results in the file " + outputFile);
			double[][] maxAverage = closureComparison.getMaxAverage();
			double[][] averageAverage = closureComparison.getAverageAverage();
			for (int i = 0; i < expressions.size(); i++) {
				out.append(i);
				for (int j = 0; j < analyses.size(); j++) {
					out.append("\t" + averageAverage[j][i] + "\t" + maxAverage[j][i]);
				}
				out.append("\n");
			}
		}
		FileUtils.writeGeneralFile(out.toString(), outputFile);
	}

	public static void main(String[] args) {
		new ClosureComparisonMain(args).compareClosures();
	}
}
