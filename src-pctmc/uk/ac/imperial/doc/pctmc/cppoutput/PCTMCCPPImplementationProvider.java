package uk.ac.imperial.doc.pctmc.cppoutput;

import com.google.common.collect.BiMap;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.cppoutput.analysis.CPPMethodPrinter;
import uk.ac.imperial.doc.pctmc.cppoutput.odeanalysis.CPPODEMethodPrinter;
import uk.ac.imperial.doc.pctmc.cppoutput.utils.CPPClassCompiler;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationPreprocessed;
import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationProvider;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.NativeSystemOfODEs;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.RungeKutta;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.SystemOfODEs;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

import java.util.HashMap;

/**
 * Provides implementation for the abstract methods used by analyses. For now
 * accepts specific method classes - UpdateMethod,ODEMethod In future, it should
 * accept any methods.
 * 
 * @author Anton Stefanek
 * 
 */
public class PCTMCCPPImplementationProvider implements
		PCTMCImplementationProvider {

	public AbstractExpressionEvaluator getEvaluatorImplementation(
			EvaluatorMethod method, String className, Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			BiMap<AbstractExpression, Integer> generalExpectationIndex) {
		CPPMethodPrinter printer = new CPPMethodPrinter(constants,
				combinedMomentsIndex, generalExpectationIndex);
		String newClassName = printer.printEvaluatorMethod(method, className);
		return (AbstractExpressionEvaluator)
                CPPClassCompiler.getInstance(printer.flushClassCode(),
                        newClassName, printer.flushNativeCode(),
                        newClassName, CPPMethodPrinter.PACKAGE);
	}

	public CPPODEsPreprocessed getPreprocessedODEImplementation(
			ODEMethod method, Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex) {
		SystemOfODEs odes = getSystemOfODEsImplementation(method,
				"GeneratedODEs", constants, combinedMomentsIndex);
		return new CPPODEsPreprocessed(odes);
	}

	public SystemOfODEs getSystemOfODEsImplementation(ODEMethod method,
			String className, Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex) {

		CPPODEMethodPrinter printer = new CPPODEMethodPrinter(constants,
				combinedMomentsIndex,
				new HashMap<AbstractExpression, Integer>());
		method.accept(printer);
		String javaCode = printer.toClassString();
        String nativeCode = printer.toString();
		return (SystemOfODEs)
                CPPClassCompiler.getInstance(javaCode,
                        printer.getNativeClassName (),
                        nativeCode, printer.getNativeClassName (),
                        CPPODEMethodPrinter.PACKAGE);
	}

	public double[][] runODEAnalysis(
			PCTMCImplementationPreprocessed preprocessed, double[] initial,
			double stopTime, double stepSize, int density, Constants constants) {
		SystemOfODEs odes = ((CPPODEsPreprocessed) preprocessed).getOdes();
		odes.setRates(constants.getFlatConstants());
		PCTMCLogging.info("Running Runge-Kutta solver.");
		return RungeKutta.rungeKutta(odes, initial, stopTime,
				stepSize, density);
	}
}
