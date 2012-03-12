package uk.ac.imperial.doc.pctmc.matlaboutput.analysis;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.matlaboutput.MatlabPrinterWithConstants;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.GeneralExpectationExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IGeneralExpectationExpressionVisitor;
import uk.ac.imperial.doc.pctmc.matlaboutput.utils.MatlabOutputUtils;

import com.google.common.collect.BiMap;

/**
 * Java printer for expressions with combined population products as leaves.
 * 
 * @author Anton Stefanek
 * 
 */
public class MatlabPrinterCombinedProductBased extends
		MatlabPrinterWithConstants implements
		ICombinedProductExpressionVisitor, IGeneralExpectationExpressionVisitor {

	protected Map<CombinedPopulationProduct, Integer> combinedMomentsIndex;
	protected Map<AbstractExpression, Integer> generalExpectationIndex;
	private String f;

	public MatlabPrinterCombinedProductBased(Constants parameters,
			Map<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex, String f) {
		super(parameters);
		this.combinedMomentsIndex = combinedMomentsIndex;
		this.generalExpectationIndex = generalExpectationIndex;
		this.f = f;
	}

	@Override
	public void visit(GeneralExpectationExpression e) {
		Integer i;
		i = generalExpectationIndex.get(e.getExpression());
		if (i == null) {
			throw new AssertionError("Unknown general expectation "
					+ e.getExpression() + "!");
		}
		output.append(f
				+ "("
				+ MatlabOutputUtils.getMatlabIndex(i
						+ combinedMomentsIndex.size()) + ")");
	}

	@Override
	public void visit(CombinedProductExpression e) {
		Integer i;
		i = combinedMomentsIndex.get(e.getProduct());
		if (i == null) {
			throw new AssertionError("Unknown combined moment "
					+ e.getProduct() + "!");
		}
		output.append(f + "(" + MatlabOutputUtils.getMatlabIndex(i) + ")");
	}
}
