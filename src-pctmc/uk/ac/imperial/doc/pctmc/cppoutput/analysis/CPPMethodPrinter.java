package uk.ac.imperial.doc.pctmc.cppoutput.analysis;

import com.google.common.collect.BiMap;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.statements.AbstractStatement;
import uk.ac.imperial.doc.pctmc.cppoutput.statements.CPPStatementPrinter;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;

import java.util.Map;

/**
 * Java printer for expression evaluator methods.
 * 
 * @author Anton Stefanek
 * 
 */
public class CPPMethodPrinter {

    private Constants constants;
    private BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex;
    private Map<AbstractExpression, Integer> generalExpectationIndex;
    public static String evaluatorName = "cppevaluator";

    public CPPMethodPrinter(Constants constants,
           BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
           Map<AbstractExpression, Integer> generalExpectationIndex) {
        super();
        this.constants = constants;
        this.combinedMomentsIndex = combinedMomentsIndex;
        this.generalExpectationIndex = generalExpectationIndex;
    }

    public String printEvaluatorMethod(EvaluatorMethod method, String suffix) {
        StringBuilder ret = new StringBuilder();
        ret.append("    int getNumberOfExpressions(){\n");
        ret.append("      return " + method.getNumberOfExpressions() + ";\n");
        ret.append("    }\n");

        ret.append("    double[] update(double[] r, double[] values, double t){\n");
        for (AbstractStatement s : method.getBody()) {
            CPPStatementPrinter printer = new CPPStatementPrinter(
                    new CPPCombinedProductBasedExpressionPrinterFactory(
                            constants, combinedMomentsIndex,
                            generalExpectationIndex, "values"));
            s.accept(printer);
            ret.append("    " + printer + "\n");
        }
        ret.append("    return " + method.getReturnArray() + ";\n");
        ret.append("}\n}");
        return ret.toString();
    }
}
