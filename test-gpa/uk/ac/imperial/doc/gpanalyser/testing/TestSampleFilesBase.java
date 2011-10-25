package uk.ac.imperial.doc.gpanalyser.testing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CentralMomentOfLinearCombinationExpression;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.MeanOfLinearCombinationExpression;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.StandardisedCentralMomentOfLinearCombinationExpression;
import uk.ac.imperial.doc.pctmc.compare.PCTMCCompareAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.CompareAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.SimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;



public class TestSampleFilesBase {
	
	@Test
	public void testSimpleClientServer(){
		//PCTMCChartUtilities.setGui(false);
		PCTMCInterpreter interpreter = GPAPMain.createGPEPAInterpreter();
		try {
			PCTMCFileRepresentation fileRepresentation = interpreter.parsePCTMCFile("test-gpa/inputs/gpepa/clientServer.gpepa");
			AbstractPCTMCAnalysis analysis = new PCTMCODEAnalysis(fileRepresentation.getPctmc());
			double stopTime = fileRepresentation.getConstants().getConstantValue("_stopTime");
			double stepSize = fileRepresentation.getConstants().getConstantValue("_stepSize");
			int density = (int)Math.floor(fileRepresentation.getConstants().getConstantValue("_density"));
			int replications = (int)Math.floor(fileRepresentation.getConstants().getConstantValue("_replications"));
			int maxOrder = (int)Math.floor(fileRepresentation.getConstants().getConstantValue("_maxOrder"));
			
			NumericalPostprocessor postprocessor = new ODEAnalysisNumericalPostprocessor(stopTime, stepSize, density);
			Assert.assertTrue(postprocessor instanceof ODEAnalysisNumericalPostprocessor);
			analysis.addPostprocessor(postprocessor);
			PCTMCSimulation simulation = new PCTMCSimulation(analysis.getPCTMC());
			SimulationAnalysisNumericalPostprocessor simPostprocessor = 
				new SimulationAnalysisNumericalPostprocessor(stopTime, stepSize, replications);
			simulation.addPostprocessor(simPostprocessor);
			PCTMCCompareAnalysis compareAnalysis = new PCTMCCompareAnalysis(analysis, simulation);
			CompareAnalysisNumericalPostprocessor comPostprocessor = 
				new CompareAnalysisNumericalPostprocessor(postprocessor, simPostprocessor);
			compareAnalysis.addPostprocessor(comPostprocessor);
			
			ArrayList<PlotDescription> newPlots = new ArrayList<PlotDescription>(maxOrder);
			Set<State> states = analysis.getPCTMC().getStateIndex().keySet();
			for (int o = 1; o<=maxOrder; o++){				
					List<AbstractExpression> tmp = new LinkedList<AbstractExpression>();
					for (State s:states){
						AbstractExpression mean = CombinedProductExpression.createMeanExpression(s);
						if (o==1){
							tmp.add(new MeanOfLinearCombinationExpression(mean, new HashMap<ExpressionVariable, AbstractExpression>()));
						} else
						if (o==2){
							tmp.add(new CentralMomentOfLinearCombinationExpression(
									mean, 2, new HashMap<ExpressionVariable, AbstractExpression>()));
						} else {
							tmp.add(new StandardisedCentralMomentOfLinearCombinationExpression(
									mean,o,new HashMap<ExpressionVariable, AbstractExpression>()));
						}
					}
				newPlots.add(new PlotDescription(tmp));
			}
			System.out.println(ToStringUtils.iterableToSSV(newPlots, "\n"));
			
			fileRepresentation.getPlots().putAll(analysis, newPlots);
			fileRepresentation.getPlots().putAll(simulation, newPlots);
			fileRepresentation.getPlots().putAll(compareAnalysis, newPlots);
			interpreter.processFileRepresentation(fileRepresentation);

			for (int i = 0; i<newPlots.size(); i++){ 
				double[][] simulationResult = simPostprocessor.getResults().get(newPlots.get(i));
				double[][] comparisonResult = comPostprocessor.getResults().get(newPlots.get(i));
				double[] maxSimAbs = new double[simulationResult[0].length];
				double[] maxComAbs = new double[simulationResult[0].length];
				boolean[] firstSim = new boolean[simulationResult[0].length];
				boolean[] firstCom = new boolean[simulationResult[0].length];
				
				double[] simInt = new double[simulationResult[0].length];
				double[] comInt = new double[simulationResult[0].length];
				Arrays.fill(firstSim, true);
				for (int j = 0; j<simulationResult.length; j++){
					for (int k = 0; k<firstSim.length; k++){
						double tmp = Math.abs(simulationResult[j][k]);
						if (Double.isNaN(tmp)) continue;
						simInt[k]+=stepSize*tmp;
						if (firstSim[k]){
							maxSimAbs[k] = tmp;
							firstSim[k] = false;
						} else {
							if (tmp>maxSimAbs[k]){
								maxSimAbs[k] = tmp;
							}
						}
						
						tmp = Math.abs(comparisonResult[j][k]);
						comInt[k] += stepSize*tmp;
						if (firstCom[k]){
							maxComAbs[k] = tmp;
							firstCom[k] = false;
						} else {
							if (tmp>maxComAbs[k]){
								maxComAbs[k] = tmp;
							}
						}
						
					}
				}
				System.out.println("Error of order " + (i+1) + " expressions:\nMax. relative:\t");
				for (int k = 0; k<firstCom.length; k++){
					System.out.format(" %.3f",maxComAbs[k]/maxSimAbs[k]);
				}
				
				System.out.println("\nIntegrated relative:\t");
				for (int k = 0; k<firstCom.length; k++){
					System.out.format(" %.3f",comInt[k]/simInt[k]);
				}
				System.out.println("");

			}			
		} catch (ParseException e) {
			System.out.println(e.toString());
			Assert.assertTrue(false);
		}		
	}

}
