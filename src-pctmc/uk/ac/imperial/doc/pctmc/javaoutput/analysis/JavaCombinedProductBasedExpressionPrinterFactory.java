package uk.ac.imperial.doc.pctmc.javaoutput.analysis;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionPrinterFactory;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;

import java.util.Map;

/**
 * Factory for printers of expressions based on counts.
 * 
 * @author Anton Stefanek
 * 
 */
public class JavaCombinedProductBasedExpressionPrinterFactory implements
		IExpressionPrinterFactory {

	private Constants parameters;
	private Map<CombinedPopulationProduct, Integer> combinedMomentsIndex;
	private Map<AbstractExpression, Integer> generalExpectationIndex;
	private String f;
	private boolean expandVariables;

	public JavaCombinedProductBasedExpressionPrinterFactory(
			Constants constants,
			Map<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex, String f, boolean expandVariables) {
		this.parameters = constants;
		this.combinedMomentsIndex = combinedMomentsIndex;
		this.generalExpectationIndex = generalExpectationIndex;
		this.f = f;
		this.expandVariables = expandVariables;
	}

	@Override
	public JavaPrinterCombinedProductBased createPrinter() {
		return new JavaPrinterCombinedProductBased(parameters,
				combinedMomentsIndex, generalExpectationIndex, f, expandVariables);
	}

}
