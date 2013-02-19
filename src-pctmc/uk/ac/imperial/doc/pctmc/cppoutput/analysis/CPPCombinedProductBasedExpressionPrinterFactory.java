package uk.ac.imperial.doc.pctmc.cppoutput.analysis;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionPrinterFactory;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;

/**
 * Factory for printers of expressions based on counts.
 *
 * @author Anton Stefanek
 *
 */
public class CPPCombinedProductBasedExpressionPrinterFactory implements
        IExpressionPrinterFactory {

    private final Constants parameters;
    private final Map<CombinedPopulationProduct, Integer> combinedMomentsIndex;
    private final Map<AbstractExpression, Integer> generalExpectationIndex;
    private final String f;

    public CPPCombinedProductBasedExpressionPrinterFactory(
            Constants constants,
            Map<CombinedPopulationProduct, Integer> combinedMomentsIndex,
            Map<AbstractExpression, Integer> generalExpectationIndex, String f) {
        super();
        this.parameters = constants;
        this.combinedMomentsIndex = combinedMomentsIndex;
        this.generalExpectationIndex = generalExpectationIndex;
        this.f = f;
    }

    public IExpressionVisitor createPrinter() {
        return new CPPPrinterCombinedProductBased(parameters,
                combinedMomentsIndex, generalExpectationIndex, f);
    }

}
