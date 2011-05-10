package uk.ac.imperial.doc.pctmc.matlaboutput.analysis;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.JavaPrinterWithConstants;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;

import com.google.common.collect.BiMap;

/**
 * Java printer for expressions with combined population products as leaves. 
 * @author Anton Stefanek
 *
 */
public class MatlabPrinterCombinedProductBased extends JavaPrinterWithConstants implements
		  ICombinedProductExpressionVisitor {


	@Override
	public void visit(CombinedProductExpression e) {
		Integer i;		
		i = combinedMomentsIndex.get(e.getProduct());
		if (i==null){
			throw new AssertionError("Unknown combined moment " + e.getProduct() + "!");
		}
		output.append(f + "[" + i + "]");		
	}

 
	protected BiMap<CombinedPopulationProduct,Integer> combinedMomentsIndex;
	protected Map<AbstractExpression,Integer> generalExpectationIndex;	 
	
	String f;

	public MatlabPrinterCombinedProductBased(Constants parameters,BiMap<CombinedPopulationProduct,Integer> combinedMomentsIndex,Map<AbstractExpression,Integer> generalExpectationIndex,String f) {
		super(parameters);	
		this.combinedMomentsIndex = combinedMomentsIndex; 
		this.generalExpectationIndex = generalExpectationIndex;
		this.f = f;
	}

}
