package uk.ac.imperial.doc.gpa.testing.quantitative;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import org.jfree.data.xy.XYSeriesCollection;

import com.google.common.collect.Lists;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.experiments.iterate.RangeSpecification;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessorCI;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

public class ClosureComparison extends RangeRunner {
	// The evaluated model and used constants

	protected Constants constants;

	// Analyses to use for evaluation and expressions
	// for comparison

	protected List<ODEAnalysisNumericalPostprocessor> postprocessors;

	
	protected List<PlotDescription> plots;
	
	protected List<AbstractExpression> expressions;
	protected List<Integer> newPlotIndices;
	

	// Simulation
	protected PCTMCSimulation simulation;
	protected NumericalPostprocessor simPostprocessor;


	protected ErrorEvaluator errorEvaluator;

	// Accumulated Results
	protected double[][] maxAverage;  // postprocessor x expression
	protected double[][] averageAverage;
	protected int totalIterations;
	
	// Transient results
	
	protected double[][][] maxT; // postprocessor x expression x t  
	protected double[][][] avgT; // postprocessor x expression x t
	
	protected String outputFolder;

	public ClosureComparison(
			List<ODEAnalysisNumericalPostprocessor> postprocessors,
			NumericalPostprocessor simPostprocessor,
			List<PlotDescription> plots,
			Constants constants,
			List<RangeSpecification> ranges, 
			String outputFolder,
			int nParts, boolean toplevel) {
		super(ranges, toplevel);
		this.postprocessors = postprocessors;
		this.simPostprocessor = simPostprocessor;
		this.plots = plots;
		this.expressions = new LinkedList<AbstractExpression>();
		this.outputFolder = outputFolder;
		newPlotIndices = new LinkedList<Integer>();		
		for (PlotDescription pd : plots) {
			newPlotIndices.add(expressions.size());
			expressions.addAll(pd.getExpressions());
		}
		newPlotIndices.add(expressions.size());
		this.constants = constants;
		prepareEvaluators();
		this.parts = split(nParts);
		maxAverage = new double[postprocessors.size()][expressions.size()];
		averageAverage = new double[postprocessors.size()][expressions.size()];
		maxT = new double[postprocessors.size()]
		                 [(int)Math.ceil(simPostprocessor.getStopTime()/simPostprocessor.getStepSize())]
		                 [expressions.size()]
		                ;
		avgT = new double[postprocessors.size()]
		                 [(int)Math.ceil(simPostprocessor.getStopTime()/simPostprocessor.getStepSize())] 
		                 [expressions.size()]
		                ;
		totalIterations = 0;
	}
	
	public ClosureComparison(
			List<ODEAnalysisNumericalPostprocessor> postprocessors,
			NumericalPostprocessor simPostprocessor,
			List<PlotDescription> plots,			
			Constants constants,
			List<RangeSpecification> ranges,
			String outputFolder) {
		this(postprocessors, simPostprocessor, plots, constants, ranges, outputFolder, PCTMCOptions.nthreads, true);
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
		return new ClosureComparison(newPostprocessors, newSimPostprocessor, plots,
				constants, ranges, outputFolder, nParts, false);
	}

	@Override
	protected void join(Constants constants) {
		System.out.println("Joining data");
		for (RangeRunner r : parts) {
			ClosureComparison part = (ClosureComparison) r;
			totalIterations += part.getTotalIterations();
			for (int i = 0; i < postprocessors.size(); i++) {
				for (int j = 0; j < expressions.size(); j++) {
					maxAverage[i][j] = Math.max(maxAverage[i][j], part.getMaxAverage()[i][j]);
					averageAverage[i][j] += part.getAverageAverage()[i][j];
					
					for (int t = 0; t < part.getMaxT()[0][0].length; t++) {
						maxT[i][t][j] = Math.max(maxT[i][t][j], part.getMaxT()[i][t][j]);
						avgT[i][t][j] += part.getAvgT()[i][t][j];
					}
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
			if (outputFolder != null) {
				FileUtils.createNeededDirectories(outputFolder+"/" + i + "/tmp");
			}
			int k = -1;
			double[][] maxAggregateT = new double[maxT[0].length][plots.size()]; 
			double[][] avgAggregateT = new double[maxT[0].length][plots.size()];
			
			System.out.println("Analysis " + i);
			for (int j = 0; j < expressions.size(); j++) {
				if (k + 1 < newPlotIndices.size() && j == newPlotIndices.get(k+1)) {
					k++;
				}
				averageAverage[i][j] /= totalIterations;
				System.out
				.println(j
						+ "\t max: "
						+ df.format(maxAverage[i][j] * 100.0)
						+ "\t average: "
						+ df
								.format(averageAverage[i][j] * 100.0)
				);

				for (int t = 0; t < maxT[0].length; t++) {					
					maxAggregateT[t][k] = Math.max(maxAggregateT[t][k], maxT[i][t][j]);
					avgAggregateT[t][k] += avgT[i][t][j];
					
					avgT[i][t][j] /= totalIterations;
				}
				
			}
			for (int t = 0; t < maxT[0].length; t++) {
				for (int l = 0; l < plots.size(); l++) {
					avgAggregateT[t][l] /= plots.get(l).getExpressions().size() * totalIterations;
				}
			}
			
			for (int l = 0; l < plots.size(); l++) {
				double[][] dataMax = new double[maxT[0].length][plots.get(l).getExpressions().size()];
				double[][] dataAvg = new double[maxT[0].length][plots.get(l).getExpressions().size()];
				Integer start = newPlotIndices.get(l);
				for (int j = start; j < newPlotIndices.get(l+1); j++) {
					for (int t = 0; t < dataMax.length; t++) {
						dataMax[t][j - start] = maxT[i][t][j];
						dataAvg[t][j - start] = avgT[i][t][j];
					}
				}
				String[] names = new String[plots.get(l).getExpressions().size()];
				int eI = 0;
				for (AbstractExpression e : plots.get(l).getExpressions()) {
					names[eI++] = e.toString();
				}
				XYSeriesCollection datasetMax = AnalysisUtils.getDatasetFromArray(dataMax,
						simPostprocessor.getStepSize(), names);
				PCTMCChartUtilities.drawChart(datasetMax, "time", "count", "Max",
						i + "");
				
				XYSeriesCollection datasetAvg = AnalysisUtils.getDatasetFromArray(dataAvg,
						simPostprocessor.getStepSize(), names);
				PCTMCChartUtilities.drawChart(datasetAvg, "time", "count", "Avg",
						i + "");
				
				if (outputFolder != null) {
					FileUtils.writeCSVfile(outputFolder + "/" + i + "/max" + l, datasetMax);
					FileUtils.writeGnuplotFile(outputFolder  + "/" + i + "/max" + l, "", Lists.newArrayList(names), "time", "count");
					
					FileUtils.writeCSVfile(outputFolder + "/" + i + "/avg" + l, datasetAvg);
					FileUtils.writeGnuplotFile(outputFolder  + "/" + i + "/avg" + l, "", Lists.newArrayList(names), "time", "count");
				}
			}
			
			
			for (int l = 0; l < plots.size(); l++) {
				double[][] data = new double[maxAggregateT.length][2];
				for (int t = 0; t < data.length; t++) {
					data[t][0] = maxAggregateT[t][l];
					data[t][1] = avgAggregateT[t][l];
				}
				String[] names = new String[]{plots.get(l).toString() + " max max", plots.get(l).toString() + " avg avg"};
				XYSeriesCollection datasetAggregate = AnalysisUtils.getDatasetFromArray(data,
						simPostprocessor.getStepSize(), names);
				PCTMCChartUtilities.drawChart(datasetAggregate, "time", "count", "Aggregate",
						i + "");
				
				if (outputFolder != null) {
					FileUtils.writeCSVfile(outputFolder + "/" + i + "/aggregate" + l, datasetAggregate);
					FileUtils.writeGnuplotFile(outputFolder  + "/" + i + "/aggregate" + l, "", Lists.newArrayList(names), "time", "count");

				}
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
		errorEvaluator.calculateErrors(constants);
		ErrorSummary[][] errors = errorEvaluator.getAccumulatedErrors();
		double[][][] transientErrors = errorEvaluator.getTransientErrors();
		for (int i = 0; i < errors.length; i++) {
			for (int j = 0; j < errors[0].length; j++) {
				maxAverage[i][j] = Math.max(maxAverage[i][j], errors[i][j].getAverageRelative());
				averageAverage[i][j] += errors[i][j].getAverageRelative();
				
				for (int t = 0; t < transientErrors[0].length; t++) {
					maxT[i][t][j] = Math.max(maxT[i][t][j], transientErrors[i][t][j]);
					avgT[i][t][j] += transientErrors[i][t][j];
				}
			}
		}
		for (PlotDescription pd : plots) {
			if (simPostprocessor instanceof NumericalPostprocessorCI) {
				((NumericalPostprocessorCI)simPostprocessor).plotData("Sim", constants, ((NumericalPostprocessorCI) simPostprocessor).getResultsCI().get(pd), pd.getExpressions(), null);
			} else {
				simPostprocessor.plotData("Sim", constants, pd.getExpressions(), null);
			}				
		}
		for (int i = 0; i < postprocessors.size(); i++) {
			for (PlotDescription pd : plots) {
				postprocessors.get(i).plotData(i+"", constants, pd.getExpressions(), null);
			}
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

	
	
	
	public double[][][] getMaxT() {
		return maxT;
	}

	public double[][][] getAvgT() {
		return avgT;
	}

	public int getTotalIterations() {
		return totalIterations;
	}
	
	
}
