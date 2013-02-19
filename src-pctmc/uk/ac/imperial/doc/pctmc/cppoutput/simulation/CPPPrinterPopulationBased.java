package uk.ac.imperial.doc.pctmc.cppoutput.simulation;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.cppoutput.CPPPrinterWithConstants;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IPopulationProductVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IPopulationVisitor;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProductExpression;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.representation.accumulations.AccumulationVariable;

import com.google.common.collect.Multiset;

/**
 * C++ printer of abstract expressions in terms of population counts.
 * @author Anton Stefanek
 *
 */
public class CPPPrinterPopulationBased extends CPPPrinterWithConstants implements
        IPopulationVisitor, IPopulationProductVisitor,ICombinedProductExpressionVisitor {

    private final Map<State, Integer> stateIndex;
    private final Map<AccumulationVariable,Integer> accumulatedProductsIndex;
    private final String f;

    public CPPPrinterPopulationBased(Constants constants,
                                     Map<State, Integer> stateIndex, Map<AccumulationVariable,
            Integer> accumulatedProductsIndex, String f) {
        super(constants);
        this.stateIndex = stateIndex;
        this.accumulatedProductsIndex = accumulatedProductsIndex;
        this.f = f;
    }


    public void visit(CombinedProductExpression e) {
        if (e.getProduct().getPopulationProduct().getOrder()>0){
            PopulationProductExpression tmp = new PopulationProductExpression(e.getProduct().getPopulationProduct());
            tmp.accept(this);
            if (!e.getProduct().getAccumulatedProducts().isEmpty()){
                output.append("*");
            }
        }
        boolean first = true;
        for (Multiset.Entry<AccumulationVariable> entry:e.getProduct().getAccumulatedProducts().entrySet()){
            for (int i = 0; i<entry.getCount(); i++){
                if (first){
                    first=false;
                } else {
                    output.append("*");
                }
                output.append(f+"[" + (stateIndex.size()+accumulatedProductsIndex.get(entry.getElement())) + "]");
            }
        }
    }

    public void visit(PopulationProductExpression e) {
        PopulationProduct moment = e.getProduct();
        boolean first = true;
        for (Map.Entry<State, Integer> entry:moment.getRepresentation().entrySet()){
            for (int i = 0; i<entry.getValue(); i++){
                if (first){
                    first=false;
                } else {
                    output.append("*");
                }
                Integer j = stateIndex.get(entry.getKey());
                if (j==null) throw new AssertionError("Unknown component " + entry.getKey() + "!");
                output.append(f+"[" + j + "]");
            }
        }

    }

    public void visit(PopulationExpression e) {
        Integer i = stateIndex.get(e.getState());
        if (i==null) throw new AssertionError("Unknown component " + e.getState() + "!");
        output.append(f + "[" + i + "]");
    }
}
