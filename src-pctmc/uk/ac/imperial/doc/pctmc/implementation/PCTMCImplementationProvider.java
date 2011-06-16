package uk.ac.imperial.doc.pctmc.implementation;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.SystemOfODEs;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;

import com.google.common.collect.BiMap;

public interface PCTMCImplementationProvider {

	public AbstractExpressionEvaluator getEvaluatorImplementation(EvaluatorMethod method,String className,Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex);
	
	public SystemOfODEs getSystemOfODEsImplementation(ODEMethod method, String className,Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex);
	
	public abstract double[][] runODEAnalysis(PCTMCImplementationPreprocessed preprocessed,double[] initial,double stopTime, double stepSize, int density,Constants constants);
	
	public PCTMCImplementationPreprocessed getPreprocessedODEImplementation(ODEMethod method,Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex);
}
