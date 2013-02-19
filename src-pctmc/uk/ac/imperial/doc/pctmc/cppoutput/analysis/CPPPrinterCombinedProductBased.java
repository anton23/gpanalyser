package uk.ac.imperial.doc.pctmc.cppoutput.analysis;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.cppoutput.CPPPrinterWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.GeneralExpectationExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IGeneralExpectationExpressionVisitor;

/**
 * C++ printer for expressions with combined population products as leaves.
 *
 * @author Anton Stefanek
 *
 */
public class CPPPrinterCombinedProductBased extends CPPPrinterWithConstants
        implements ICombinedProductExpressionVisitor,
        IGeneralExpectationExpressionVisitor {

    private final Map<CombinedPopulationProduct, Integer> combinedMomentsIndex;
    private final Map<AbstractExpression, Integer> generalExpectationIndex;
    private final String f;

    public CPPPrinterCombinedProductBased(Constants parameters,
                                          Map<CombinedPopulationProduct, Integer> combinedMomentsIndex,
                                          Map<AbstractExpression, Integer> generalExpectationIndex, String f) {
        super(parameters);
        this.combinedMomentsIndex = combinedMomentsIndex;
        this.generalExpectationIndex = generalExpectationIndex;
        this.f = f;
    }

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
