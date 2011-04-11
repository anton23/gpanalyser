package uk.ac.imperial.doc.pctmc.javaoutput.analysis;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionPrinterFactory;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;

import com.google.common.collect.BiMap;

/**
 * Factory for printers of expressions based on counts. 
 * @author Anton Stefanek
 *
 */
public class JavaCombinedProductBasedExpressionPrinterFactory implements
		IExpressionPrinterFactory {

	private Constants parameters;

	
	private BiMap<CombinedPopulationProduct,Integer> combinedMomentsIndex; 
	private Map<AbstractExpression,Integer> generalExpectationIndex;
	private String f;

	public JavaCombinedProductBasedExpressionPrinterFactory(
			Constants constants, BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression,Integer> generalExpectationIndex,
			String f) {
		super();

		this.parameters = constants;

		this.combinedMomentsIndex = combinedMomentsIndex;
		this.generalExpectationIndex = generalExpectationIndex; 
		this.f = f;
	}

	@Override
	public IExpressionVisitor createPrinter() {
		return new JavaPrinterCombinedProductBased(parameters,combinedMomentsIndex,generalExpectationIndex,f);
	}

}
