package uk.ac.imperial.doc.gpa.testing.quantitative;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.experiments.iterate.RangeSpecification;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

public class ClosureComparison extends RangeRunner {
	// The evaluated model and used constants

	protected Constants constants;

	// Analyses to use for evaluation and expressions
	// for comparison

	protected List<ODEAnalysisNumericalPostprocessor> postprocessors;

	protected List<AbstractExpression> expressions;

	// Simulation
	protected PCTMCSimulation simulation;
	protected NumericalPostprocessor simPostprocessor;


	protected ErrorEvaluator errorEvaluator;

	// Results
	protected double[][] maxAverage;
	protected double[][] averageAverage;
	protected int totalIterations;

	public ClosureComparison(
			List<ODEAnalysisNumericalPostprocessor> postprocessors,
			NumericalPostprocessor simPostprocessor,
			List<AbstractExpression> expressions,
			Constants constants,
			List<RangeSpecification> ranges, int nParts, boolean toplevel) {
		super(ranges, toplevel);
		this.postprocessors = postprocessors;
		this.simPostprocessor = simPostprocessor;
		this.expressions = expressions;
		this.constants = constants;
		prepareEvaluators();
		this.parts = split(nParts);
		maxAverage = new double[postprocessors.size()][expressions.size()];
		averageAverage = new double[postprocessors.size()][expressions.size()];
		totalIterations = 0;
	}
	
	public ClosureComparison(
			List<ODEAnalysisNumericalPostprocessor> postprocessors,
			NumericalPostprocessor simPostprocessor,
			List<AbstractExpression> expressions,
			Constants constants,
			List<RangeSpecification> ranges) {
		this(postprocessors, simPostprocessor, expressions, constants, ranges, PCTMCOptions.nthreads, true);
	}

	@Override
	protected RangeRunner createSlave(List<RangeSpecification> ranges,
			int nParts) {
		List<ODEAnalysisNumericalPostprocessor> newPostprocessors = new LinkedList<ODEAnalysisNumericalPostprocessor>();
		for (ODEAnalysisNumericalPostprocessor p : postprocessors) {
			newPostprocessors.add((ODEAnalysisNumericalPostprocessor) p
					.getNewPreparedPostprocessor(constants));
		}
		NumericalPostprocessor newSimPostprocessor = simPostprocessor
				.getNewPreparedPostprocessor(constants);
		return new ClosureComparison(newPostprocessors, newSimPostprocessor, expressions,
				constants, ranges, nParts, false);
	}

	@Override
	protected void join(Constants constants) {
		System.out.println("Joining data");
		for (RangeRunner r : parts) {
			ClosureComparison part = (ClosureComparison) r;
			totalIterations += part.getTotalIterations();
			for (int i = 0; i < postprocessors.size(); i++) {
				for (int j = 0; j < expressions.size(); j++) {
					if (maxAverage[i][j] < part.getMaxAverage()[i][j]) {
						maxAverage[i][j] = part.getMaxAverage()[i][j];
					}
					averageAverage[i][j] += part.getAverageAverage()[i][j];
				}
			}
		}

	}

	@Override
	protected void runSingle(Constants constants) {
		runAnalyses(constants);
	}

	@Override
	protected void processData(Constants constants) {	
		System.out.println("Final summary:");
		DecimalFormat df = new DecimalFormat("#.##");
		for (int i = 0; i < postprocessors.size(); i++) {
			System.out.println("Analysis " + i);
			for (int j = 0; j < expressions.size(); j++) {
				averageAverage[i][j] /= totalIterations;
				System.out
				.println(j
						+ "\t max: "
						+ df.format(maxAverage[i][j] * 100.0)
						+ "\t average: "
						+ df
								.format(averageAverage[i][j] * 100.0)
);
			}
		}

	}

	protected void prepareEvaluators() {
		System.out.println("Preparing evaluators");
		
		errorEvaluator = new ErrorEvaluator(postprocessors, simPostprocessor, expressions, constants);
	}
	
	

	public void runAnalyses(Constants constants) {
		System.out.println("Running analyses");
		totalIterations++;
		ErrorSummary[][] errors = errorEvaluator.calculateErrors(constants);
		for (int i = 0; i < errors.length; i++) {
			for (int j = 0; j < errors[0].length; j++) {
				if (maxAverage[i][j] < errors[i][j].getAverageRelative()) {
					maxAverage[i][j] = errors[i][j].getAverageRelative();
				}
				averageAverage[i][j] += errors[i][j].getAverageRelative();
			}
		}
		simPostprocessor.plotData("Sim", constants, expressions, null);
		for (int i = 0; i < postprocessors.size(); i++) {		
			postprocessors.get(i).plotData(i+"", constants, expressions, null);
		}
		System.out.println(ErrorEvaluator.printSummary(errors));
		System.out.println("Finished analyses");
	}

	public double[][] getMaxAverage() {
		return maxAverage;
	}

	public double[][] getAverageAverage() {
		return averageAverage;
	}

	public int getTotalIterations() {
		return totalIterations;
	}
}
