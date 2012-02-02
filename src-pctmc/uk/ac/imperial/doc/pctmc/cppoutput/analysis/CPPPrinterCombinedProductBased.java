package uk.ac.imperial.doc.pctmc.cppoutput.analysis;

import com.google.common.collect.BiMap;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.cppoutput.CPPPrinterWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.expressions.*;

import java.util.Map;

/**
 * CPP printer for expressions with combined population products as leaves.
 * 
 * @author Anton Stefanek
 * 
 */
public class CPPPrinterCombinedProductBased extends CPPPrinterWithConstants
		implements ICombinedProductExpressionVisitor,
		IGeneralExpectationExpressionVisitor {
	
	protected BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex;
	protected Map<AbstractExpression, Integer> generalExpectationIndex;

	private String f;
	
	public CPPPrinterCombinedProductBased(Constants parameters,
          BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
          Map<AbstractExpression, Integer> generalExpectationIndex, String f) {
		super(parameters);
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
