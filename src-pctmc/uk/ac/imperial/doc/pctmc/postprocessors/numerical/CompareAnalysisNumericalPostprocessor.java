package uk.ac.imperial.doc.pctmc.postprocessors.numerical;

import java.util.List;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.compare.CompareExpressionEvaluator;

public class CompareAnalysisNumericalPostprocessor extends
		NumericalPostprocessor {

	NumericalPostprocessor postprocessor1; 
	NumericalPostprocessor postprocessor2; 
	
	public CompareAnalysisNumericalPostprocessor(NumericalPostprocessor postprocessor1,
			NumericalPostprocessor postprocessor2) {
		super(postprocessor1.getStopTime(), postprocessor1.getStepSize());
		this.postprocessor1 = postprocessor1;
		this.postprocessor2 = postprocessor2;
	}


	@Override
	public void prepare(AbstractPCTMCAnalysis analysis, Constants constants) {

	}


	@Override
	public void calculateDataPoints(Constants constants) {
		dataPoints=new double[1][1];
		
	}
	
	@Override
	public double[][] evaluateExpressions(
			List<AbstractExpression> plotExpressions, Constants constants) {
		CompareExpressionEvaluator evaluator =  new CompareExpressionEvaluator(
				postprocessor1.getExpressionEvaluator(plotExpressions, constants), 
				postprocessor2.getExpressionEvaluator(plotExpressions, constants));
		return this.evaluateExpressions(evaluator, constants);
	}
	
	@Override
	public double[][] evaluateExpressions(
			AbstractExpressionEvaluator evaluator, Constants constants) {
		CompareExpressionEvaluator asCompare = (CompareExpressionEvaluator) evaluator;
        AbstractExpressionEvaluator evaluator1 = asCompare.getEvaluator1(), evaluator2 = asCompare.getEvaluator2();
		double[][] data1 = postprocessor1.evaluateExpressions(evaluator1, constants);
        double[][] data2 = postprocessor2.evaluateExpressions(evaluator2, constants);
        double[][] data = new double[data1.length][data1[0].length];
        for (int t = 0; t<data.length; t++){
                for (int e = 0; e<data[0].length; e++){
                        data[t][e] = data1[t][e] - data2[t][e];
                }
        }
        return data; 
	}
}
