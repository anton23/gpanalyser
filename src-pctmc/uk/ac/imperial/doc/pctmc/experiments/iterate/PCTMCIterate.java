package uk.ac.imperial.doc.pctmc.experiments.iterate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CollectUsedMomentsVisitor;
import uk.ac.imperial.doc.pctmc.charts.ChartUtils3D;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import com.google.common.collect.Lists;

public class PCTMCIterate extends PCTMCExperiment {
	private List<RangeSpecification> ranges;
	private AbstractPCTMCAnalysis analysis;
	private List<PlotAtDescription> plots;
	private Map<String, AbstractExpression> reEvaluations;
	private Map<ExpressionVariable, AbstractExpression> unfoldedVariables;

	private PlotAtDescription minSpecification;
	private List<RangeSpecification> minRanges;

	protected NumericalPostprocessor postprocessor;

	private Map<PlotAtDescription, double[][]> results;
	
	private double[][][] data;
	
	private boolean processPlots;

	private RangeSpecification[] minRangesArray;
	private int[] steps;
	private int iterations;
	private int show;
	
	private List<PCTMCIterate> parts;
	
	public List<PCTMCIterate> split(int n) {
		List<PCTMCIterate> ret = new ArrayList<PCTMCIterate>(n);
		if (n==1) {
			ret.add(this);
			return ret;
		}
		if (ranges.isEmpty()) {
			ret.add(new PCTMCIterate(minRanges, reEvaluations, analysis, postprocessor, plots, unfoldedVariables, 1, true));
			return ret;
		} else {
			List<RangeSpecification> firstRangeParts = ranges.get(0).split(n);
			List<RangeSpecification> restOfRanges = ranges.subList(1, ranges.size());
			for (RangeSpecification r:firstRangeParts) {
				List<RangeSpecification> tmpRanges = new ArrayList<RangeSpecification>();
				tmpRanges.add(r);
				tmpRanges.addAll(restOfRanges);
				ret.add(new PCTMCIterate(tmpRanges, minSpecification, minRanges, reEvaluations, analysis, postprocessor, plots, unfoldedVariables, 1, false));
			}
		}
		return ret;		
	}
	
	public PCTMCIterate(List<RangeSpecification> ranges,
			Map<String, AbstractExpression> reEvaluations,
			AbstractPCTMCAnalysis analysis,
			NumericalPostprocessor postprocessor,
			List<PlotAtDescription> plots,
			Map<ExpressionVariable, AbstractExpression> unfoldedVariables, int nParts, boolean processPlots) {
		this(ranges, null, null, reEvaluations, analysis, postprocessor, plots, unfoldedVariables, nParts, processPlots);
	}
	
	public PCTMCIterate(List<RangeSpecification> ranges,
			Map<String, AbstractExpression> reEvaluations,
			AbstractPCTMCAnalysis analysis,
			NumericalPostprocessor postprocessor,
			List<PlotAtDescription> plots,
			Map<ExpressionVariable, AbstractExpression> unfoldedVariables) {
		this(ranges, reEvaluations, analysis, postprocessor, plots, unfoldedVariables, PCTMCOptions.nthreads, true);
	}

	public PCTMCIterate(List<RangeSpecification> ranges,
			PlotAtDescription minSpecification,
			List<RangeSpecification> minRanges,
			Map<String, AbstractExpression> reEvaluations,
			AbstractPCTMCAnalysis analysis,
			NumericalPostprocessor postprocessor,
			List<PlotAtDescription> plots,
			Map<ExpressionVariable, AbstractExpression> unfoldedVariables, int nParts, boolean processPlots) {
		this.ranges = ranges;
		this.reEvaluations = reEvaluations;
		this.analysis = analysis;
		this.postprocessor = postprocessor;
		this.plots = plots;
		this.unfoldedVariables = unfoldedVariables;
		this.minSpecification = minSpecification;
		this.minRanges = minRanges;
		minRanges = new LinkedList<RangeSpecification>();
		this.parts = split(nParts);
		this.processPlots = processPlots;
	}
	
	public PCTMCIterate(List<RangeSpecification> ranges,
			PlotAtDescription minSpecification,
			List<RangeSpecification> minRanges,
			Map<String, AbstractExpression> reEvaluations,
			AbstractPCTMCAnalysis analysis,
			NumericalPostprocessor postprocessor,
			List<PlotAtDescription> plots,
			Map<ExpressionVariable, AbstractExpression> unfoldedVariables) {
		this(ranges, minSpecification, minRanges, reEvaluations, analysis, postprocessor, plots, unfoldedVariables, PCTMCOptions.nthreads, true);
	}

	public void run(final Constants constants) {
		PCTMCLogging.info("Running experiment\n" + this.toString()); 
		if (parts.size() > 1) {
			PCTMCLogging.setVisible(false);
			List<Thread> threads = new LinkedList<Thread>();
			for (final PCTMCIterate part:parts) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						part.run(constants);						
					}
				});
				threads.add(t); 
				t.start();				
			}
			try {
			for (Thread t:threads) {
				t.join();
			}
			PCTMCLogging.setVisible(true);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			joinData();
		} else {
			Constants tmpConstants = constants.getCopyOf();
			if (ranges.size() == 2 || ranges.size() == 1 || ranges.size() == 0) {
				iterate2d(tmpConstants);
			}
		}
		if (processPlots) {
			switch(ranges.size()) {
				case 0:processPlots0D(data); break;
				case 1:processPlots1D(ranges.get(0), data); break;
				case 2:processPlots2D(ranges.get(0), ranges.get(1), data); break;
			}
		}
	}
	
	protected void joinData() {
		int steps2 = ranges.size()>1 ? ranges.get(1).getSteps():1;		                                   			
		data = new double[plots.size()][ranges.get(0).getSteps()][steps2];
		for (int plot = 0; plot < plots.size(); plot++) {
			int tmp = 0;
			for (PCTMCIterate part:parts) {
				for (int i = 0; i<part.getRanges().get(0).getSteps(); i++,tmp++) {
					data[plot][tmp] = part.getData()[plot][i];
				}
			}
		}
	}

	public static void reEvaluate(Constants constants, Map<String, AbstractExpression> reEvaluations) {
		for (Map.Entry<String, AbstractExpression> e : reEvaluations.entrySet()) {
			ExpressionEvaluatorWithConstants evaluator = new ExpressionEvaluatorWithConstants(
					constants);
			e.getValue().accept(evaluator);
			constants.setConstantValue(e.getKey(), evaluator.getResult());
		}
	}

	public void prepare(Constants constants) {
		// Prepares analysis only if it's a top level iterate		
		if (processPlots) {
			List<AbstractExpression> usedExpressions = new LinkedList<AbstractExpression>();
			List<PlotAtDescription> tmpPlots = new LinkedList<PlotAtDescription>(
					plots);
			if (!minRanges.isEmpty()) {
				tmpPlots.add(minSpecification);
			}
			for (PlotAtDescription plot : tmpPlots) {
				plot.unfoldExpressions(unfoldedVariables);
				usedExpressions.addAll(plot.getPlotExpressions());
			}
			Set<CombinedPopulationProduct> usedProducts = new HashSet<CombinedPopulationProduct>();
			Set<AbstractExpression> usedGeneralExpectations = new HashSet<AbstractExpression>();
			for (AbstractExpression exp : usedExpressions) {
				CollectUsedMomentsVisitor visitor = new CollectUsedMomentsVisitor();
				exp.accept(visitor);
				usedProducts.addAll(visitor.getUsedCombinedMoments());
				usedGeneralExpectations
						.addAll(visitor.getUsedGeneralExpectations());
			}
	
			PCTMCLogging.info("Preparing analysis:");
			PCTMCLogging.increaseIndent();
			analysis.setUsedMoments(usedProducts);
	
			analysis.prepare(constants);
			PCTMCLogging.decreaseIndent();
			postprocessor.prepare(analysis, constants);
	
			for (PlotAtDescription p : tmpPlots) {
				AbstractExpressionEvaluator updater = postprocessor
						.getExpressionEvaluator(p.getPlotExpressions(), constants);
				p.setEvaluator(updater);
			}
		} else {
			this.postprocessor = this.postprocessor.getNewPreparedPostprocessor(constants);
		}
		if (parts.size() > 1) {
			for (PCTMCIterate part:parts) {
				part.prepare(constants);
			}
		}
	}

	private void iterate2d(Constants constants) {
		PCTMCLogging.increaseIndent();
		RangeSpecification xRange;
		RangeSpecification yRange;
		if (ranges.size() >= 2) {
			xRange = ranges.get(0);
			yRange = ranges.get(1);
		} else if (ranges.size() == 1) {
			yRange = new RangeSpecification("_tmp", 0.0, 0.0, 1);
			xRange = ranges.get(0);
		} else {
			xRange = new RangeSpecification("_tmp", 0.0, 0.0, 1);
			yRange = new RangeSpecification("_tmp2", 0.0, 0.0, 1);
		}
		data = new double[plots.size()][xRange.getSteps()][yRange
				.getSteps()];

		for (int p = 0; p < plots.size(); p++) {
			for (int i = 0; i < xRange.getSteps(); i++) {
				for (int j = 0; j < yRange.getSteps(); j++) {
					data[p][i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		steps = new int[minRanges.size()];
		minRangesArray = new RangeSpecification[minRanges.size()];
		int r = 0;
		int totalSteps = 1;
		for (RangeSpecification ra : minRanges) {
			steps[r] = ra.getSteps();
			totalSteps *= steps[r];
			minRangesArray[r] = ra;
			r++;
		}
		int totalIterations = xRange.getSteps() * yRange.getSteps()
				* totalSteps;
		show = Math.max(totalIterations / 5, 1);

		PCTMCLogging.info("Starting " + totalIterations + " iterations:");
		PCTMCLogging.increaseIndent();
		PCTMCLogging.setVisible(false);
		iterations = 0;
		for (int x = 0; x < xRange.getSteps(); x++) {
			double xValue = xRange.getStep(x);
			constants.setConstantValue(xRange.getConstant(), xValue);
			for (int y = 0; y < yRange.getSteps(); y++) {
				double yValue = yRange.getStep(y);
				
				constants.setConstantValue(yRange.getConstant(), yValue);
				
				if (!minimise(constants))
					continue;
				

				reEvaluate(constants, reEvaluations);
				postprocessor.calculateDataPoints(constants);
				
				for (int i = 0; i < plots.size(); i++) {
					data[i][x][y] = evaluateConstrainedReward(plots.get(i),
							constants);
				}			

				
			}
		}
		PCTMCLogging.decreaseIndent();
		PCTMCLogging.decreaseIndent();
	}
	 
	
	private void processPlots2D(RangeSpecification xRange, RangeSpecification yRange, double[][][] data) {
		results = new HashMap<PlotAtDescription, double[][]>();
		for (int i = 0; i < plots.size(); i++) {
			PlotAtDescription plot = plots.get(i);
			PCTMCLogging.info("Plotting " + plot);
				results.put(plot, data[i]);
				ChartUtils3D.drawChart(toShortString(), plot.toString(),
						data[i], xRange.getFrom(), xRange.getDc(), yRange
								.getFrom(), yRange.getDc(), xRange
								.getConstant(), yRange.getConstant(), plot
								.getExpression().toString());

				if (!plot.getFilename().isEmpty()) {
					FileUtils.write3Dfile(plot.getFilename(), data[i], xRange
							.getFrom(), xRange.getDc(), yRange.getFrom(),
							yRange.getDc());
					FileUtils.write3DGnuplotFile(plot.getFilename(), xRange
							.getConstant(), yRange.getConstant(), plot
							.toString());
				}
		}
	}
	
	private void processPlots1D(RangeSpecification range, double[][][] data) {
		results = new HashMap<PlotAtDescription, double[][]>();
		for (int i = 0; i < plots.size(); i++) {
			PlotAtDescription plot = plots.get(i);
			PCTMCLogging.info("Plotting " + plot);

				double[][] newData = new double[range.getSteps()][1];
				for (int j = 0; j < range.getSteps(); j++) {
					newData[j][0] = data[i][j][0];
				}
				results.put(plot, newData);
				XYSeriesCollection dataset = AnalysisUtils.getDatasetFromArray(
						newData, range.getFrom(), range.getDc(),
						new String[] { plot.toString() });

				PCTMCChartUtilities.drawChart(dataset, range.getConstant(),
						"count", "", toShortString());
				if (!plot.getFilename().isEmpty()) {
					FileUtils.writeCSVfile(plot.getFilename(), dataset);
					FileUtils.writeGnuplotFile(plot.getFilename(), "", Lists
							.newArrayList(plot.toString()), range
							.getConstant(), "");
				}

		}
	}
	
	private void processPlots0D(double[][][] data) {
		results = new HashMap<PlotAtDescription, double[][]>();
		for (int i = 0; i < plots.size(); i++) {
			PlotAtDescription plot = plots.get(i);
			PCTMCLogging.info("Plotting " + plot);
				results.put(plot, data[i]);
				PCTMCLogging.info("The value of " + plot.toString()
						+ " at optimum is " + data[i][0][0]);

		}
		
	}
	
	
	private boolean minimise(Constants constants) {
		int step[] = new int[minRanges.size()];
		double min = 0.0;
		boolean notYet = true;
		int[] minStep = null;
		do {
			for (int s = 0; s < step.length; s++) {
				constants.setConstantValue(minRangesArray[s].getConstant(),
						minRangesArray[s].getStep(step[s]));
			}
			reEvaluate(constants, reEvaluations);
			postprocessor.calculateDataPoints(constants);
			iterations++;
			if ((iterations) % show == 0) {
				PCTMCLogging.infoForce(iterations + " iterations finished.");
			}
			if (minSpecification == null) {
				return true;
			}
			double reward = evaluateConstrainedReward(minSpecification,
					constants);
			if (!Double.isNaN(reward)) {

				if (notYet || reward < min) {
					min = reward;
					notYet = false;
					minStep = Arrays.copyOf(step, step.length);
				}
			}
		} while (next(step, steps));
		if (notYet)
			return false;
		else {
			for (int s = 0; s < step.length; s++) {
				constants.setConstantValue(minRangesArray[s].getConstant(),
						minRangesArray[s].getStep(minStep[s]));
			}
		}
		return true;
	}

	private double evaluateConstrainedReward(PlotAtDescription plot,
			Constants constants) {
		double[] values = postprocessor.evaluateExpressionsAtTimes(plot
				.getEvaluator(), plot.getAtTimes(), constants);
		boolean satisfied = true;
		for (int j = 0; j < plot.getConstraints().size(); j++) {
			PlotConstraint pc = plot.getConstraints().get(j);
			double cValue = values[j + 1];
			if (cValue < pc.getMinValue()) {
				satisfied = false;
			}
		}
		if (satisfied)
			return values[0];
		else
			return Double.POSITIVE_INFINITY;
	}

	public static boolean next(int[] is, int[] steps) {
		int i = 0;

		while (i < steps.length && ++is[i] == steps[i]) {
			is[i] = 0;
			i++;
		}
		return i < steps.length;
	}

	@Override
	public String toString() {
		return "Iterate " + ToStringUtils.iterableToSSV(ranges, "\n        ") + "\n" + 
		   	 	(minRanges.isEmpty()?"":("Minimise " + minSpecification + " " + ToStringUtils.iterableToSSV(minRanges, "\n         ") + "\n"))
				+ analysis.toString();
	}

	public String toShortString() {
		String ret = "Iterate";
		for (RangeSpecification r : ranges) {
			ret += " ";
			ret += r.getConstant();
		}
		if (!minRanges.isEmpty()) {
			ret += " minimise " + minSpecification + " ";
			for (RangeSpecification r : minRanges) {
				ret += " ";
				ret += r.getConstant();
			}
		}
		return ret;
	}

	public AbstractPCTMCAnalysis getAnalysis() {
		return analysis;
	}

	public PlotAtDescription getMinSpecification() {
		return minSpecification;
	}

	public List<RangeSpecification> getMinRanges() {
		return minRanges;
	}

	public List<RangeSpecification> getRanges() {
		return ranges;
	}

	public List<PlotAtDescription> getPlots() {
		return plots;
	}

	public Map<PlotAtDescription, double[][]> getResults() {
		return results;
	}

	public double[][][] getData() {
		return data;
	}
}
