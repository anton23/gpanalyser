package uk.ac.imperial.doc.pctmc.javaoutput.analysis;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.JavaExpressionPrinterWithVariables;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.GeneralExpectationExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IGeneralExpectationExpressionVisitor;

/**
 * Java printer for expressions with combined population products as leaves.
 * 
 * @author Anton Stefanek
 * 
 */
public class JavaPrinterCombinedProductBased extends JavaExpressionPrinterWithVariables
		implements ICombinedProductExpressionVisitor,
		IGeneralExpectationExpressionVisitor {
	
	protected Map<CombinedPopulationProduct, Integer> combinedMomentsIndex;
	protected Map<AbstractExpression, Integer> generalExpectationIndex;

	private String f;
	
	public JavaPrinterCombinedProductBased(Constants constants,
			Map<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex, String f, boolean expandVariables) {
		super(constants, expandVariables);
		this.combinedMomentsIndex = combinedMomentsIndex;
		this.generalExpectationIndex = generalExpectationIndex;
		this.f = f;
	}

	@Override
	public void visit(GeneralExpectationExpression e) {
		Integer i;
		Integer egIndex = generalExpectationIndex.get(e.getExpression());
		if (egIndex == null) {
			throw new AssertionError("Unknown general expectation "
					+ e.getExpression() + "!");
		}
		i = egIndex + combinedMomentsIndex.size();
		output.append(f + "[" + i + "]");
	}

	@Override
	public void visit(CombinedProductExpression e) {
		Integer i;
		i = combinedMomentsIndex.get(e.getProduct());
		if (i == null) {
			throw new AssertionError("Unknown combined moment "
					+ e.getProduct() + "!");
		}
		output.append(f + "[" + i + "]");
	}
}
