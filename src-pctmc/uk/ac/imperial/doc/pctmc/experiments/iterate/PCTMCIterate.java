package uk.ac.imperial.doc.pctmc.experiments.iterate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultiStartMultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer.PopulationSize;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer.Sigma;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomVectorGenerator;
import org.jfree.data.xy.XYDataset;

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

import com.cureos.numerics.Calcfc;
import com.cureos.numerics.Cobyla;
import com.cureos.numerics.CobylaExitStatus;
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
		if (PCTMCOptions.nthreads == 1)
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
		if (PCTMCOptions.nthreads == 1)
			PCTMCLogging.setVisible(true);
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
				XYDataset dataset = AnalysisUtils.getDatasetFromArray(
						newData, null, range.getFrom(), range.getDc(),
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
	private PointValuePair globalOptimiseCOBYLA(final Constants constants) {
		 int n = minRanges.size();
		 final int[] neval = new int[]{0};
		Calcfc calcfc = new Calcfc() {
	            @Override
	            public double Compute(int n, int m, double[] v, double[] con) {
	            	neval[0]++;
	        		boolean satisfied = true;

	            	for (int s = 0; s < v.length; s++) {
						constants.setConstantValue(minRangesArray[s].getConstant(),
								v[s]);
					}
					postprocessor.calculateDataPoints(constants);

	            	double[] values = postprocessor.evaluateExpressionsAtTimes(minSpecification
	        				.getEvaluator(), minSpecification.getAtTimes(), constants);
	        		int ncon = minSpecification.getConstraints().size();
	        		for (int j = 0; j < ncon; j++) {
	        			PlotConstraint pc = minSpecification.getConstraints().get(j);
	        			double cValue = values[j + 1];
	        			con[j] = cValue - pc.getMinValue() - 1e-5;	  
	        			if (con[j] < 0) {
	        				satisfied = false;
	        			}
	        		}
	        		for (int r = 0; r < n; r++) {
	    				con[ncon + 2*r] = v[r] - minRangesArray[r].getFrom();	
	    				con[ncon + 2*r + 1] = minRangesArray[r].getTo() - v[r];					
	    			}
	        		/*
	        		if (!satisfied) {
	        			return Double.POSITIVE_INFINITY;	        				
	        		}*/
	        		return values[0];
	            }
	        };
	        
	        
	        int m = minSpecification
					.getConstraints().size() + n * 2;
	    double best = Double.MAX_VALUE; 
	    double[] bestPoint = null;
	    for (int s = 0; s < 1; s++) {    
		double[] initial = new double[n];
		for (int r = 0; r < n; r++) {
				double lb = minRangesArray[r].getFrom();
				double ub = minRangesArray[r].getTo();
				initial[r] = lb + (-lb + ub) * Math.random();							
			}

		
		CobylaExitStatus result = Cobyla
				.FindMinimum(calcfc, n, m,
						initial, 0.5, 1.0e-6, 0, 500);
		double[] con = new double[m];
		double value = calcfc.Compute(n, m, initial, con);
		/*for (int i = 0; i < con.length; i++) {
			if (con[i] < 0) {
				value = Double.POSITIVE_INFINITY;
			}
		}*/
		if (value < best) {
			best = value;
			bestPoint = initial;
		}
	    }
		System.out.println("COBYLA " + Arrays.toString(bestPoint) + " " + best);
		System.out.println("with neval " + neval[0]);

		return new PointValuePair(bestPoint, best);
	}
	
	
	private PointValuePair globalOptimise(final Constants constants) {
		 SimpleValueChecker cchecker = new SimpleValueChecker(1e-8, 1e-8);
		CMAESOptimizer optimiser = new CMAESOptimizer(10000, Double.NEGATIVE_INFINITY, false, 0, 1, new MersenneTwister(), true, 
				null);
		
		RandomVectorGenerator generator = new RandomVectorGenerator() {
			
			@Override
			public double[] nextVector() {
				double [] ret = new double[minRanges.size()];
				for (int r = 0; r < minRanges.size(); r++) {
					double l = minRangesArray[r].getFrom();
					double u = minRangesArray[r].getTo();
					ret[r] = l + Math.random()*(u - l);					
				}
				return ret;
			}
		};
		
		MultiStartMultivariateOptimizer moptimiser =
				new MultiStartMultivariateOptimizer(optimiser, 3, generator);
		final int[] neval = new int[1];
		final double[] bestEver = new double[]{Double.POSITIVE_INFINITY};
		final double[][] bestEverP = new double[1][];
		MultivariateFunction f = new MultivariateFunction() {
			@Override
			public double value(double[] v) {
				for (int s = 0; s < v.length; s++) {
					constants.setConstantValue(minRangesArray[s].getConstant(),
							v[s]);
				}
				postprocessor.calculateDataPoints(constants);
				neval[0]++;
				double reward = evaluateConstrainedReward(minSpecification,
						constants);
				if (reward == Double.POSITIVE_INFINITY) {
					reward = Double.NaN;
				}
				//System.out.println("Call " + Arrays.toString(v) + " " + reward);
				if (reward < bestEver[0]) {
					bestEver[0] = reward;
					bestEverP[0] = v;
				}
				return reward;
			}
		};
		
		double[] lb = new double[minRanges.size()];
		double[] ub = new double[minRanges.size()];
		double[] initial = new double[minRanges.size()];
		double[] sigma = new double[minRanges.size()];


		for (int r = 0; r < minRanges.size(); r++) {
			lb[r] = minRangesArray[r].getFrom();
			ub[r] = minRangesArray[r].getTo();
			initial[r] = lb[r] + (ub[r] - ub[r]) * Math.random();
			//sigma[r] = (ub[r] - lb[r]) / 3.0;
			sigma[r] = minRangesArray[r].getDc();
		}
		int lambda = 4 + (int)(3.*Math.log(minRanges.size()));
		PointValuePair optim = moptimiser.optimize(new ObjectiveFunction(f), 
				GoalType.MINIMIZE, new InitialGuess(initial), 
				new MaxEval(10000),
				new PopulationSize(lambda),
				new Sigma(sigma),
				new SimpleBounds(lb, ub)		
				);
		/*System.out.println(optimiser.getStatisticsFitnessHistory());
		System.out.println(optimiser.getStatisticsDHistory());
		System.out.println(optimiser.getStatisticsSigmaHistory());
		System.out.println(optimiser.getStatisticsMeanHistory());*/
		System.out.println("Optimum " + Arrays.toString(optim.getPoint()) + " " + optim.getValue());
		System.out.println(" with neval " + neval[0]);
		System.out.println("Optimum " + Arrays.toString(bestEverP[0]) + " " + bestEver[0]);

		return new PointValuePair(bestEverP[0], bestEver[0]);
		//return optim;
		
	}
	
	
	private boolean minimise(Constants constants) {
		//PointValuePair optim =globalOptimiseCOBYLA(constants);
		PointValuePair optim = globalOptimise(constants);
		if (optim.getValue() == Double.MAX_VALUE)
			return false;
		else {
			for (int s = 0; s < minRanges.size(); s++) {
				constants.setConstantValue(minRangesArray[s].getConstant(),
						optim.getPoint()[s]);
			}
			return true;
		}
		/*int step[] = new int[minRanges.size()];
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
			if ((iterations) % show == 0 && PCTMCOptions.nthreads == 1) {
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
		return true;*/
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
