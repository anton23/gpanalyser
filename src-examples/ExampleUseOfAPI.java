import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataset;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.charts.ChartUtils3D;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCExperiment;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCIterate;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotAtDescription;
import uk.ac.imperial.doc.pctmc.experiments.iterate.RangeSpecification;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;


public class ExampleUseOfAPI {
	public static void main(String[] args){
		PCTMCChartUtilities.jogl = true;
		PCTMCInterpreter gpepaInterpreter = GPAPMain.createGPEPAInterpreter();

		try {
			PCTMCFileRepresentation fileRepresentation = gpepaInterpreter.parsePCTMCFile("src-examples/clientServer.gpepa");
			System.out.println("Parsed a GPEPA file with following conents:");
			System.out.println("Constants:\n"+fileRepresentation.getConstants().toString());
			System.out.println("PCTMC:\n" + fileRepresentation.getPctmc());
			System.out.println("Analyses:\n" + fileRepresentation.getPlots());
			
			AbstractPCTMCAnalysis analysis = fileRepresentation.getPlots().keySet().iterator().next();
			System.out.println("The first analysis has these postprocessors:\n"+analysis.getPostprocessors());
			
			NumericalPostprocessor numericalPostprocessor = (NumericalPostprocessor)analysis.getPostprocessors().iterator().next();
			numericalPostprocessor.setStopTime(50.0);
			
			fileRepresentation.getPlots().put(analysis,new PlotDescription(gpepaInterpreter.parseExpressionList("Var[Clients:Client]^0.5, Var[Servers:Server]^0.5")));
			
			gpepaInterpreter.processFileRepresentation(fileRepresentation);
			
			Map<PlotDescription, double[][]> results = numericalPostprocessor.getResults();
						
			for (Map.Entry<PlotDescription, double[][]> e:results.entrySet()){
				double stepSize = numericalPostprocessor.getStepSize();
				XYDataset dataset = AnalysisUtils.getDatasetFromArray(e.getValue(), null, stepSize, e.getKey().getLabels());
				PCTMCChartUtilities.nextBatch();
				PCTMCChartUtilities.drawChart(dataset, "time", "count", "",
						"Example plots from postprocessors");
			}
			
			for (PCTMCExperiment experiment:fileRepresentation.getExperiments()){
				PCTMCIterate asIterate = (PCTMCIterate)experiment;
				int dim = asIterate.getRanges().size();
			    System.out.println("\nExperiment " + experiment);
			    System.out.println("The sweeping dimension is " + dim);
				List<RangeSpecification> ranges = asIterate.getRanges();
				for (RangeSpecification r:ranges){
					System.out.println("Constant: " + r.getConstant());
					System.out.println("From: " + r.getFrom());
					System.out.println("To: " + r.getTo());
					System.out.println("Steps: " + r.getSteps());
					System.out.println("Dc: " + r.getDc());
				}
				
				if (dim==1){
					System.out.println("The results are standard plots:");
					for (Map.Entry<PlotAtDescription, double[][]> e:asIterate.getResults().entrySet()){
						RangeSpecification range = asIterate.getRanges().get(0);
						XYDataset dataset = AnalysisUtils.getDatasetFromArray(e.getValue(), null,
								range.getFrom(),range.getDc(), 
			    		  new String[]{e.getKey().toString()});
			    		PCTMCChartUtilities.drawChart(dataset, range.getConstant(), "count", "", "Example " + asIterate.toShortString());
					}
				}
				if (dim==2){
					for (Map.Entry<PlotAtDescription, double[][]> e:asIterate.getResults().entrySet()){
						RangeSpecification xRange = asIterate.getRanges().get(0);
						RangeSpecification yRange = asIterate.getRanges().get(1);
						ChartUtils3D.drawChart("Example "+asIterate.toShortString(),
								e.getKey().toString(),e.getValue(),xRange.getFrom(),xRange.getDc(),yRange.getFrom(),yRange.getDc(),
					    		xRange.getConstant(),yRange.getConstant(),e.getKey().getExpression().toString());
					}
				}
				if (dim==0){
					for (Map.Entry<PlotAtDescription, double[][]> e:asIterate.getResults().entrySet()){
						System.out.println("The optimal value of " + e.getKey() + " is " + e.getValue()[0][0]);
					}
				}
				
			}
			
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
