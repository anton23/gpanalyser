package uk.ac.imperial.doc.pctmc.experiments.iterate;

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
import uk.ac.imperial.doc.pctmc.expressions.ExpressionVariableSetterPCTMC;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

import com.google.common.collect.Lists;

public class PCTMCIterate {
	private List<RangeSpecification> ranges; 
	private AbstractPCTMCAnalysis analysis; 
	private List<PlotAtDescription> plots;
	private Map<String,AbstractExpression> reEvaluations; 
	private Map<ExpressionVariable,AbstractExpression> unfoldedVariables;
	
	
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
	} 
	
	public void iterate(Constants constants){
		Constants tmpConstants = constants.getCopyOf();
		
		if (ranges.size()==2||ranges.size()==1){
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
	
	private void iterate2d(Constants constants){
		PCTMCLogging.info("Running experiment " + this.toString());
		PCTMCLogging.increaseIndent(); 
		List<PlotExpression> usedExpressions = new LinkedList<PlotExpression>();
		
		List<List<PlotExpression>> plotExpressions = new LinkedList<List<PlotExpression>>(); 
		for (PlotAtDescription plot:plots){			
			List<PlotExpression> pExpressions = new LinkedList<PlotExpression>();
			List<AbstractExpression> pAExpressions = new LinkedList<AbstractExpression>(); 
			pAExpressions.add(plot.getExpression()); 
			for (PlotConstraint pc:plot.getConstraints()){
				pAExpressions.add(pc.getExpression());
			}
			for (AbstractExpression e:pAExpressions){
				ExpressionVariableSetterPCTMC setter = new ExpressionVariableSetterPCTMC(unfoldedVariables);
				
				e.accept(setter); 					
				PlotExpression pe = new PlotExpression(e);			
				pExpressions.add(pe);
				usedExpressions.add(pe); 
			}
			plotExpressions.add(pExpressions);
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
		List<AbstractExpressionEvaluator> updaters = new LinkedList<AbstractExpressionEvaluator>();
		for (int i = 0; i<plots.size(); i++){
			AbstractExpressionEvaluator updater = analysis.getExpressionEvaluator(plotExpressions.get(i), constants);
			updaters.add(updater); 
		}
		
		RangeSpecification xRange; 
		RangeSpecification yRange;
		if (ranges.size()==2){
			 xRange = ranges.get(0);
			 yRange = ranges.get(1);
		} else {
			xRange = new RangeSpecification("tmp", 0.0, 0.0, 1);
			yRange = ranges.get(0);
		}
		double[][][] data = new double[plots.size()][xRange.getSteps()][yRange.getSteps()];
		
		for (int p = 0; p<plots.size(); p++){
		    for (int i = 0; i<xRange.getSteps(); i++){
		    	for (int j = 0; j<yRange.getSteps(); j++){
		    		data[p][i][j] = Double.POSITIVE_INFINITY; 
		    	}
		    }
		}
		
		int iterations = xRange.getSteps()*yRange.getSteps();
		int show = Math.max(iterations/5,1); 
		PCTMCLogging.info("Starting " + iterations + " iterations:"); 
		PCTMCLogging.increaseIndent(); 
	    PCTMCLogging.setVisible(false);
	    
	    for (int x = 0; x<xRange.getSteps(); x++){
	    	double xValue = xRange.getStep(x);
	    	constants.setConstantValue(xRange.getConstant(), xValue); 
	    	for (int y = 0; y<yRange.getSteps(); y++){
		    	if ((x*yRange.getSteps()+y+1 )%show == 0){
		    		PCTMCLogging.setVisible(true);
		    		PCTMCLogging.info((x*yRange.getSteps()+y) + "iterations finished.");
		    		PCTMCLogging.setVisible(false);
		    	}	    		
	    		double yValue = yRange.getStep(y);	    			    		
	    		constants.setConstantValue(yRange.getConstant(), yValue);
	    		reEvaluate(constants); 
	    		analysis.analyse(constants);
	    		
	    		for (int i = 0; i<plots.size(); i++){
	    			double[][] tmp = analysis.evaluateExpressions(updaters.get(i), constants);
	    			
	    			boolean satisfied = true; 
	    			for (int j = 0; j<plots.get(i).getConstraints().size(); j++){
	    				PlotConstraint pc = plots.get(i).getConstraints().get(j); 
	    				double cValue = tmp[getTimeIndex(pc.getAtTime())][j+1];
	    				if (cValue<pc.getMinValue()){
	    					satisfied = false; 
	    				}
	    			}
	    			if (satisfied) data[i][x][y] = tmp[getTimeIndex(plots.get(i).getTime())][0];
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
	    		
	    		FileUtils.writeCSVfile(plot.getFilename(), dataset);
	    		FileUtils.writeGnuplotFile(plot.getFilename(), "", Lists.newArrayList(plot.toString()), yRange.getConstant(), "");
	    	}
	    }
	    PCTMCLogging.decreaseIndent(); 
	}
	
	private int getTimeIndex(double time){
		return (int) Math.floor(time/analysis.getStepSize());
	}
	
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
		return ret;
	}
	
}
