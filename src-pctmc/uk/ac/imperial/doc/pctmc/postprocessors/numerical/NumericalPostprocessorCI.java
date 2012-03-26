package uk.ac.imperial.doc.pctmc.postprocessors.numerical;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataset;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;

public abstract class NumericalPostprocessorCI extends NumericalPostprocessor {
	
	public NumericalPostprocessorCI(double stopTime, double stepSize) {
		super(stopTime, stepSize);
	}


	protected double[][][] absHalfCIWidth;
	
	protected List<PlotDescription> plotDescriptions;
	
	public void setPlotDescriptions(List<PlotDescription> plotDescriptions) {
		this.plotDescriptions = plotDescriptions;
	}

	@Override
	public final void postprocessAnalysis(Constants constants,
			AbstractPCTMCAnalysis analysis,
			List<PlotDescription> _plotDescriptions){
		plotDescriptions = _plotDescriptions;
		prepare(analysis, constants);
		calculateDataPoints(constants);
		if (dataPoints!=null)
		{
			results = new LinkedHashMap<PlotDescription, double[][]>();
			resultsCI = new LinkedHashMap<PlotDescription, double[][]>();
			int index=0;
			for (PlotDescription pd:plotDescriptions)
			{
				double[][] ci = null;
				if (absHalfCIWidth!=null)
				{
					ci = absHalfCIWidth[index++];
					resultsCI.put(pd, ci);
				}
				double[][] data = plotData(analysis.toString(), constants, ci, pd.getExpressions(), pd.getFilename());
				results.put(pd, data);
			}
		}
		setResults(constants, _plotDescriptions);
	}
	
	public void setResults(Constants constants,			
			List<PlotDescription> _plotDescriptions) {
		if (dataPoints!=null)
		{
			results = new LinkedHashMap<PlotDescription, double[][]>();
			resultsCI = new LinkedHashMap<PlotDescription, double[][]>();
			int index=0;
			for (PlotDescription pd:plotDescriptions)
			{
				double[][] ci = null;
				if (absHalfCIWidth!=null)
				{
					ci = absHalfCIWidth[index++];
					resultsCI.put(pd, ci);
				}
				results.put(pd, evaluateExpressions(pd.getExpressions(), constants));
			}
		}
		
	}
	
	protected Map<PlotDescription, double[][]> resultsCI;	
	
	public Map<PlotDescription, double[][]> getResultsCI() {
		return resultsCI;
	}

	public double[][] plotData(String analysisTitle,
			Constants constants, double[][] dataCI, List<AbstractExpression> expressions,
			String filename) {
		
		if (dataCI == null)
		{
			return super.plotData(analysisTitle, constants, expressions, filename);
		}
		else
		{
			String[] names = new String[expressions.size()];
			for (int i = 0; i < expressions.size(); i++) {
				names[i] = expressions.get(i).toString();
			}
			double[][] data = evaluateExpressions(expressions, constants);
			XYDataset dataset = AnalysisUtils.getDatasetFromArray(data, dataCI, stepSize, names);
			PCTMCChartUtilities.drawDeviationChart(dataset, "time", "count", "",	analysisTitle+this.toString());
			if (filename != null && !filename.equals("")) {
				List<String> labels = new LinkedList<String>();
				for (AbstractExpression e : expressions) {
					labels.add(e.toString());
				}
				FileUtils.writeGnuplotFile(filename, "", labels, "time", "count");
				FileUtils.writeCSVfile(filename, dataset);
			}
			return data;
		}
	}

	public double[] evaluateExpressions(AbstractExpressionEvaluator evaluator,final double[] data, int t, Constants constants){
		//evaluator.setRates(constants.getFlatConstants());
		
		double[] selectedData = new double[evaluator.getNumberOfExpressions()];
		selectedData = evaluator.update(constants.getFlatConstants(),data, t * stepSize);

		return selectedData;
	}
}
