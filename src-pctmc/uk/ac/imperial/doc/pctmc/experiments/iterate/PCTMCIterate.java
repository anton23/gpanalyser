package uk.ac.imperial.doc.pctmc.experiments.iterate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CollectUsedMomentsVisitor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotExpression;
import uk.ac.imperial.doc.pctmc.charts.ChartUtils3D;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

import com.google.common.collect.Lists;

public class PCTMCIterate {
	private List<RangeSpecification> ranges; 
	private AbstractPCTMCAnalysis analysis; 
	private List<PlotAtDescription> plots;
	private Map<String,AbstractExpression> reEvaluations; 
	private Map<ExpressionVariable,AbstractExpression> unfoldedVariables;
	
	private PlotAtDescription minSpecification; 
	private List<RangeSpecification> minRanges; 
	
	
	
	
	
	
	public AbstractPCTMCAnalysis getAnalysis() {
		return analysis;
	}

	public PlotAtDescription getMinSpecification() {
		return minSpecification;
	}

	public List<RangeSpecification> getMinRanges() {
		return minRanges;
	}

	public List<PlotAtDescription> getPlots(){
		return plots; 
	}
	
	public PCTMCIterate(List<RangeSpecification> ranges,Map<String,AbstractExpression> reEvaluations,
			AbstractPCTMCAnalysis analysis, List<PlotAtDescription> plots,Map<ExpressionVariable,AbstractExpression> unfoldedVariables) {
		super();
		this.ranges = ranges;
		this.reEvaluations = reEvaluations;
		this.analysis = analysis;
		this.plots = plots;
		this.unfoldedVariables = unfoldedVariables;
		minRanges = new LinkedList<RangeSpecification>();
	} 
	
	public PCTMCIterate(List<RangeSpecification> ranges,PlotAtDescription minSpecification,List<RangeSpecification> minRanges,Map<String,AbstractExpression> reEvaluations,
			AbstractPCTMCAnalysis analysis, List<PlotAtDescription> plots,Map<ExpressionVariable,AbstractExpression> unfoldedVariables){
		this(ranges,reEvaluations,analysis,plots,unfoldedVariables);
		this.minSpecification = minSpecification; 
		this.minRanges = minRanges; 
	}
	
	public void iterate(Constants constants){
		Constants tmpConstants = constants.getCopyOf();
		
		if (ranges.size()==2||ranges.size()==1||ranges.size()==0){
			iterate2d(tmpConstants); 
		}
	}
	
	private void reEvaluate(Constants constants){
		for (Map.Entry<String, AbstractExpression> e:reEvaluations.entrySet()){
			ExpressionEvaluatorWithConstants evaluator = new ExpressionEvaluatorWithConstants(constants); 
			e.getValue().accept(evaluator); 
			constants.setConstantValue(e.getKey(), evaluator.getResult()); 
		}
	}
	
	RangeSpecification[] minRangesArray;
	int[] steps; 
	
	int iterations;
	
	int show; 
	
	
	public void prepare(Constants constants){
		List<PlotExpression> usedExpressions = new LinkedList<PlotExpression>();
		

		List<PlotAtDescription> tmpPlots = new LinkedList<PlotAtDescription>(plots); 
		if (!minRanges.isEmpty()){
			tmpPlots.add(minSpecification);
		}
		for (PlotAtDescription plot:tmpPlots){
			plot.unfoldExpressions(unfoldedVariables); 
			usedExpressions.addAll(plot.getPlotExpressions());
		}
		Set<CombinedPopulationProduct> usedProducts = new HashSet<CombinedPopulationProduct>();
		for (PlotExpression exp:usedExpressions){
			CollectUsedMomentsVisitor visitor = new CollectUsedMomentsVisitor(); 
			exp.getExpression().accept(visitor); 
			usedProducts.addAll(visitor.getUsedCombinedMoments());
		}
		
		PCTMCLogging.info("Preparing analysis:"); 
		PCTMCLogging.increaseIndent();
		analysis.setUsedMoments(usedProducts);
		analysis.prepare(constants); 
		PCTMCLogging.decreaseIndent();


		for (PlotAtDescription p:tmpPlots){
			AbstractExpressionEvaluator updater = analysis.getExpressionEvaluator(p.getPlotExpressions(), constants);
			p.setEvaluator(updater);
		}

	}
	
	private void iterate2d(Constants constants){
		PCTMCLogging.info("Running experiment " + this.toString());
		PCTMCLogging.increaseIndent(); 
		
		RangeSpecification xRange; 
		RangeSpecification yRange;
		if (ranges.size()>=2){
			 xRange = ranges.get(0);
			 yRange = ranges.get(1);
		} else 
		if (ranges.size()==1){
			xRange = new RangeSpecification("tmp", 0.0, 0.0, 1);
			yRange = ranges.get(0);
		}
		else{
			xRange = new RangeSpecification("tmp", 0.0, 0.0, 1);
			yRange = new RangeSpecification("tmp2",0.0,0.0,1);
		}
		double[][][] data = new double[plots.size()][xRange.getSteps()][yRange.getSteps()];
		
		for (int p = 0; p<plots.size(); p++){
		    for (int i = 0; i<xRange.getSteps(); i++){
		    	for (int j = 0; j<yRange.getSteps(); j++){
		    		data[p][i][j] = Double.POSITIVE_INFINITY; 
		    	}
		    }
		}
		
		
 
		
	    
	    
	    steps = new int[minRanges.size()];
	    minRangesArray = new RangeSpecification[minRanges.size()];
	    int r = 0; 
	    int totalSteps = 1; 
	    for (RangeSpecification ra:minRanges){
	    	steps[r] = ra.getSteps();
	    	totalSteps*=steps[r];
	    	minRangesArray[r] = ra; 
	    	r++;
	    }
		int totalIterations = xRange.getSteps()*yRange.getSteps()*totalSteps;
		show = Math.max(totalIterations/5,1);
	    
	    PCTMCLogging.info("Starting " + totalIterations + " iterations:"); 
		PCTMCLogging.increaseIndent();

 
	    PCTMCLogging.setVisible(false);
/*		long before = System.nanoTime();
		for (int i = 0; i<10; i++){analysis.analyse(constants);}
		long after = System.nanoTime(); 
		PCTMCLogging.setVisible(true);
		double mil = after-before;
		PCTMCLogging.info("Expected run time is " + ((double)(mil*iterations*totalSteps)/10000000000.0) + " seconds");
		PCTMCLogging.setVisible(false);*/
	    

	    
	    iterations = 0; 
	    for (int x = 0; x<xRange.getSteps(); x++){
	    	double xValue = xRange.getStep(x);
	    	constants.setConstantValue(xRange.getConstant(), xValue); 
	    	for (int y = 0; y<yRange.getSteps(); y++){   		
	    		double yValue = yRange.getStep(y);	    			    		
	    		constants.setConstantValue(yRange.getConstant(), yValue);
 

	    		if (!minRanges.isEmpty()){
	    			if (!minimise(constants)) continue; 
	    		} else {
	    			iterations++;
	    		}
	    		
	    		reEvaluate(constants);	
	    		analysis.analyse(constants);
	    		
	    		for (int i = 0; i<plots.size(); i++){
	    			data[i][x][y] = evaluateConstrainedReward(plots.get(i), constants);
	    		}
	    	}
	    }
	    PCTMCLogging.decreaseIndent(); 
	    PCTMCLogging.setVisible(true);
	    for (int i = 0; i<plots.size(); i++){
	    	PlotAtDescription plot = plots.get(i);
	    	PCTMCLogging.info("Plotting " + plot);

 
	    	if (ranges.size()==2 ){
	    		ChartUtils3D.drawChart(toShortString(),plot.toString(),data[i],xRange.getFrom(),xRange.getDc(),yRange.getFrom(),yRange.getDc(),
		    		xRange.constant,yRange.constant,plot.getExpression().toString());
		    	
	    		if (!plot.getFilename().isEmpty()){
	    			FileUtils.write3Dfile(plot.getFilename(),data[i],xRange.getFrom(),xRange.getDc(),yRange.getFrom(),yRange.getDc());
	    			FileUtils.write3DGnuplotFile(plot.getFilename(), xRange.getConstant(), yRange.getConstant(), plot.toString());
	    		}
	    	}
	    	if (ranges.size()==1 ){
	    		double[][] newData = new double[yRange.getSteps()][1];
	    		for (int j = 0; j<yRange.getSteps(); j++){
	    			newData[j][0] = data[i][0][j]; 
	    		}
	    		XYSeriesCollection dataset = AnalysisUtils.getDataset(newData, yRange.getFrom(),yRange.getDc(), 
	    				new String[]{plot.toString()});
	    		
	    		
	    		PCTMCChartUtilities.drawChart(dataset, yRange.getConstant(), "count", "", toShortString());
	    		if (!plot.getFilename().isEmpty()){
	    			FileUtils.writeCSVfile(plot.getFilename(), dataset);
	    			FileUtils.writeGnuplotFile(plot.getFilename(), "", Lists.newArrayList(plot.toString()), yRange.getConstant(), "");
	    		}
	    	}
	    	if (ranges.size()==0){
	    		PCTMCLogging.info("The value of " + plot.toString() + " at optimum is " + data[i][0][0]);
	    	}
	    }
	    PCTMCLogging.decreaseIndent(); 
	}
	
	private boolean minimise(Constants constants){
		int step[] = new int[minRanges.size()];
		double min = 0.0; 
		boolean notYet = true;
		int[] minStep = null;
		do{	
			for (int s = 0; s<step.length; s++){
				constants.setConstantValue(minRangesArray[s].getConstant(),
						minRangesArray[s].getStep(step[s]));
			}
			reEvaluate(constants);
			analysis.analyse(constants);
			iterations++;
	    	if ((iterations )%show == 0){
	    		PCTMCLogging.setVisible(true);
	    		PCTMCLogging.info(iterations + "iterations finished.");
	    		PCTMCLogging.setVisible(false);
	    	}
			
			double reward = evaluateConstrainedReward(minSpecification, constants);
			if (!Double.isNaN(reward)){

				if (notYet||reward<min){	    						
					min = reward;
					notYet = false; 
					minStep = Arrays.copyOf(step, step.length);
				} 
			}
		} while(next(step,steps));
		if (notYet) return false; 
		else{
			for (int s = 0; s<step.length; s++){
				constants.setConstantValue(minRangesArray[s].getConstant(),
						minRangesArray[s].getStep(minStep[s]));
			}
		}
		return true; 
	}
	
	private double evaluateConstrainedReward(PlotAtDescription plot, Constants constants){
		double[] values = analysis.evaluateExpressionsAtTimes(plot.getEvaluator(), plot.getAtTimes(), constants);
		boolean satisfied = true;
		for (int j = 0; j<plot.getConstraints().size(); j++){
			PlotConstraint pc = plot.getConstraints().get(j); 
			double cValue = values[j+1];
			if (cValue<pc.getMinValue()){
				satisfied = false; 
			}
		}	
		if (satisfied) return values[0];
		else return Double.NaN; 
		
	}
	
	private boolean next(int[] is,int[] steps){
		int i = 0; 
		
		while(i<steps.length && ++is[i]==steps[i]){
			is[i]=0;
			i++;
		}
		return i < steps.length;
	}

	
	
	/*private int getTimeIndex(double time){
		return (int) Math.floor(time/analysis.getStepSize());
	}*/
	
	@Override
	public String toString() {
		return "Iterate " + ToStringUtils.iterableToSSV(ranges, ",") + "\n"
		+ analysis.toString();/* + " plot {\n" +
		ToStringUtils.iterableToSSV(plots, "\n") + "}";*/
	}
	
	public String toShortString(){
		String ret = "Iterate";
		for (RangeSpecification r:ranges){
			ret+= " ";
			ret+=r.getConstant() + "("+r.getFrom() + "-"+r.getTo() + "," + r.getSteps()+" steps"+")";
		}
		if (!minRanges.isEmpty()){
			ret+=" minimise "+minSpecification;
		}
		return ret;
	}
	
}
