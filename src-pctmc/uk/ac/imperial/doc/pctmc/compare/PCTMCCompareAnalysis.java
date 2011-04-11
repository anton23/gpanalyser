package uk.ac.imperial.doc.pctmc.compare;

import java.util.Collection;
import java.util.List;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;

public class PCTMCCompareAnalysis extends AbstractPCTMCAnalysis{

	@Override
	public void analyse(Constants constants) {		
		analysis1.analyse(constants);
		analysis2.analyse(constants);
	}

	@Override
	public void prepare(Constants constants) {
		analysis1.prepare(constants);
		analysis2.prepare(constants);	
	}

	@Override
	public String toString() {
		return "Compare("+analysis1.toString() + "," + analysis2.toString() + ")"; 
	}

	private AbstractPCTMCAnalysis analysis1, analysis2; 

	public PCTMCCompareAnalysis(AbstractPCTMCAnalysis analysis1, AbstractPCTMCAnalysis analysis2) {
		super(analysis1.getPCTMC(), analysis1.getStopTime(), analysis1.getStepSize());
		if (analysis1.getStepSize()!=analysis2.getStepSize() || analysis1.getStopTime()!=analysis2.getStopTime()){
			throw new AssertionError("Incompatible analyses in compare!"); 
		}
		this.analysis1 = analysis1; 
		this.analysis2 = analysis2;
	} 
	
	@Override
	public void setUsedMoments(
			Collection<CombinedPopulationProduct> combinedProducts) {
			analysis1.setUsedMoments(combinedProducts);
			analysis2.setUsedMoments(combinedProducts);
	}
	
	@Override
	public AbstractExpressionEvaluator getExpressionEvaluator(
			List<PlotExpression> plotExpressions, Constants constants) {
		AbstractExpressionEvaluator evaluator1 = analysis1.getExpressionEvaluator(plotExpressions, constants);
		AbstractExpressionEvaluator evaluator2 = analysis2.getExpressionEvaluator(plotExpressions, constants); 
		return new CompareExpressionEvaluator(evaluator1, evaluator2);
	}
	
	@Override
	public double[][] evaluateExpressions(AbstractExpressionEvaluator evaluator, Constants constants){
		if (!(evaluator instanceof CompareExpressionEvaluator)){
			throw new AssertionError("Unsupported expression evaluator for a PCTMC compare analysis!"); 
		}
		CompareExpressionEvaluator cevaluator = (CompareExpressionEvaluator)evaluator; 
		AbstractExpressionEvaluator evaluator1 = cevaluator.getEvaluator1(), evaluator2 = cevaluator.getEvaluator2();
		double[][] data1 = analysis1.evaluateExpressions(evaluator1, constants);
		double[][] data2 = analysis2.evaluateExpressions(evaluator2, constants);
		double[][] data = new double[data1.length][data1[0].length];
		for (int t = 0; t<data.length; t++){
			for (int e = 0; e<data[0].length; e++){
				data[t][e] = data1[t][e] - data2[t][e];
			}
		}
		return data; 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((analysis1 == null) ? 0 : analysis1.hashCode());
		result = prime * result
				+ ((analysis2 == null) ? 0 : analysis2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PCTMCCompareAnalysis other = (PCTMCCompareAnalysis) obj;
		if (analysis1 == null) {
			if (other.analysis1 != null)
				return false;
		} else if (!analysis1.equals(other.analysis1))
			return false;
		if (analysis2 == null) {
			if (other.analysis2 != null)
				return false;
		} else if (!analysis2.equals(other.analysis2))
			return false;
		return true;
	}
}
