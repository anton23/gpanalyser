package uk.ac.imperial.doc.pctmc.implementation;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.ISystemOfODEs;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;

import java.util.Map;

public interface PCTMCImplementationProvider {

	public AbstractExpressionEvaluator getEvaluatorImplementation(
			EvaluatorMethod method, String className, Constants constants,
			Map<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex);

	public ISystemOfODEs getSystemOfODEsImplementation(ODEMethod method,
			String className, Constants constants,
			Map<CombinedPopulationProduct, Integer> combinedMomentsIndex);

	public abstract double[][] runODEAnalysis(
			PCTMCImplementationPreprocessed preprocessed, double[] initial,
			double stopTime, double stepSize, int density, Constants constants);

	public PCTMCImplementationPreprocessed getPreprocessedODEImplementation(
			ODEMethod method, Constants constants,
			Map<CombinedPopulationProduct, Integer> combinedMomentsIndex);
}
