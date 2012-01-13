package uk.ac.imperial.doc.pctmc.javaoutput.simulation;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.javaoutput.JavaExpressionPrinterWithVariables;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IPopulationProductVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IPopulationVisitor;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProductExpression;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multiset;

/**
 * Java printer of abstract expressions in terms of population counts. 
 * @author Anton Stefanek
 *
 */
public class JavaPrinterPopulationBased extends JavaExpressionPrinterWithVariables implements
		IPopulationVisitor, IPopulationProductVisitor,ICombinedProductExpressionVisitor {
	
	private BiMap<State, Integer> stateIndex;
	private BiMap<PopulationProduct,Integer> accumulatedProductsIndex;
	private String f;
	
	public JavaPrinterPopulationBased(Constants constants,
			BiMap<State, Integer> stateIndex,BiMap<PopulationProduct,Integer> accumulatedProductsIndex, String f, boolean expandVariables) {
		super(constants, expandVariables);
		this.stateIndex = stateIndex;
		this.accumulatedProductsIndex = accumulatedProductsIndex;
		this.f = f;
	}

	
	@Override
	public void visit(CombinedProductExpression e) {
		if (e.getProduct().getNakedProduct().getOrder()>0){
			PopulationProductExpression tmp = new PopulationProductExpression(e.getProduct().getNakedProduct());
			tmp.accept(this); 
			if (!e.getProduct().getAccumulatedProducts().isEmpty()){
				output.append("*");
			}
		}
		boolean first = true;
		for (Multiset.Entry<PopulationProduct> entry:e.getProduct().getAccumulatedProducts().entrySet()){
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

	@Override
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

	@Override
	public void visit(PopulationExpression e) {
		Integer i = stateIndex.get(e.getState());
		if (i==null) throw new AssertionError("Unknown component " + e.getState() + "!");
		output.append(f + "[" + i + "]");
	}
}