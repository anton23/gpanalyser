package uk.ac.imperial.doc.pctmc.matlaboutput.analysis;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.matlaboutput.MatlabPrinterWithConstants;
import uk.ac.imperial.doc.jexpressions.statements.AbstractStatement;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.matlaboutput.statements.MatlabStatementPrinter;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;

/**
 * Java printer for expression evaluator methods.
 * 
 * @author Anton Stefanek
 * 
 */
public class MatlabMethodPrinter {

	private Constants constants;
	private Map<CombinedPopulationProduct, Integer> combinedMomentsIndex;
	private Map<AbstractExpression, Integer> generalExpectationIndex;
	public static String evaluatorName = "evaluator";

	public MatlabMethodPrinter(Constants constants,
			Map<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex) {
		super();
		this.constants = constants;
		this.combinedMomentsIndex = combinedMomentsIndex;
		this.generalExpectationIndex = generalExpectationIndex;
	}

	public String printEvaluatorMethod(EvaluatorMethod method, String suffix) {
		StringBuilder ret = new StringBuilder();
		ret.append("function z = " + evaluatorName + suffix + "(y,t,"
				+ MatlabPrinterWithConstants.param + ")\n");
		for (AbstractStatement s : method.getBody()) {
			MatlabStatementPrinter printer = new MatlabStatementPrinter(
					new MatlabCombinedProductBasedExpressionPrinterFactory(
							constants, combinedMomentsIndex,
							generalExpectationIndex, "y"));
			s.accept(printer);
			ret.append("    " + printer + "\n");
		}
		ret.append("    z=" + method.getReturnArray() + ";\n");
		ret.append("end\n");
		return ret.toString();
	}
}
