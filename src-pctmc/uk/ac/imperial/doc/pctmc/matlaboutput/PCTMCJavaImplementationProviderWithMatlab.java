package uk.ac.imperial.doc.pctmc.matlaboutput;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationPreprocessed;
import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationProvider;
import uk.ac.imperial.doc.pctmc.javaoutput.PCTMCJavaImplementationProvider;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.SystemOfODEs;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;

import com.google.common.collect.BiMap;

public class PCTMCJavaImplementationProviderWithMatlab implements PCTMCImplementationProvider{
	
	PCTMCJavaImplementationProvider javaImplementation; 

	@Override
	public AbstractExpressionEvaluator getEvaluatorImplementation(
			EvaluatorMethod method, String className, Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex) {
		return javaImplementation.getEvaluatorImplementation(method, className, constants, combinedMomentsIndex, generalExpectationIndex);
	}

	@Override
	public PCTMCImplementationPreprocessed getPreprocessedODEImplementation(
			ODEMethod method, Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex) {
		return javaImplementation.getPreprocessedODEImplementation(method, constants, combinedMomentsIndex, generalExpectationIndex);
	}

	@Override
	public SystemOfODEs getSystemOfODEsImplementation(ODEMethod method,
			String className, Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex) {
		return javaImplementation.getSystemOfODEsImplementation(method, className, constants, combinedMomentsIndex, generalExpectationIndex);
	}

	@Override
	public double[][] runODEAnalysis(
			PCTMCImplementationPreprocessed preprocessed, double[] initial,
			double stopTime, double stepSize, int density, Constants constants) {
		return javaImplementation.runODEAnalysis(preprocessed, initial, stopTime, stepSize, density, constants);
	}

}
