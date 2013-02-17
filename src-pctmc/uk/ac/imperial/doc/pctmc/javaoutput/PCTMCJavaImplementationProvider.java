package uk.ac.imperial.doc.pctmc.javaoutput;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationPreprocessed;
import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationProvider;
import uk.ac.imperial.doc.pctmc.javaoutput.analysis.JavaMethodPrinter;
import uk.ac.imperial.doc.pctmc.javaoutput.odeanalysis.JavaODEMethodPrinter;
import uk.ac.imperial.doc.pctmc.javaoutput.utils.ClassCompiler;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.ODEIntegrators;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.SystemOfODEs;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

/**
 * Provides implementation for the abstract methods used by analyses. For now
 * accepts specific method classes - UpdateMethod,ODEMethod In future, it should
 * accept any methods.
 * 
 * @author Anton Stefanek
 * 
 */
public class PCTMCJavaImplementationProvider implements
		PCTMCImplementationProvider {

	public AbstractExpressionEvaluator getEvaluatorImplementation(
			EvaluatorMethod method, String className, Constants constants,
			Map<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex) {
		JavaMethodPrinter printer = new JavaMethodPrinter(constants,
				combinedMomentsIndex, generalExpectationIndex);
		String code = printer.printEvaluatorMethod(method, className);
		AbstractExpressionEvaluator updater = (AbstractExpressionEvaluator) ClassCompiler
				.getInstance(code, className);
		return updater;
	}

	public JavaODEsPreprocessed getPreprocessedODEImplementation(
			ODEMethod method, Constants constants,
			Map<CombinedPopulationProduct, Integer> combinedMomentsIndex) {
		SystemOfODEs odes = getSystemOfODEsImplementation(method,
				"GeneratedODEs", constants, combinedMomentsIndex);
		return new JavaODEsPreprocessed(odes);
	}
	
	public JavaODEsPreprocessed getPreprocessedODEImplementationFromCode(String code, String className) {
		SystemOfODEs ret = (SystemOfODEs) ClassCompiler.getInstance(code,
				className);
		return new JavaODEsPreprocessed(ret);
	}

	public SystemOfODEs getSystemOfODEsImplementation(ODEMethod method,
			String className, Constants constants,
			Map<CombinedPopulationProduct, Integer> combinedMomentsIndex) {

		JavaODEMethodPrinter printer = new JavaODEMethodPrinter(constants,
				combinedMomentsIndex,
				new HashMap<AbstractExpression, Integer>());
		method.accept(printer);
		String code = printer.toString();
		SystemOfODEs ret = (SystemOfODEs) ClassCompiler.getInstance(code,
				className);
		return ret;
	}

	public double[][] runODEAnalysis(
			PCTMCImplementationPreprocessed preprocessed, double[] initial,
			double stopTime, double stepSize, Map<String, Object> parameters, Constants constants) {
		SystemOfODEs odes = ((JavaODEsPreprocessed) preprocessed).getOdes();
		odes.setRates(constants.getFlatConstants());
		double[][] dataPoints = ODEIntegrators.solveODEs(odes, initial, stopTime,
				stepSize, parameters);
		return dataPoints;
	}
}
