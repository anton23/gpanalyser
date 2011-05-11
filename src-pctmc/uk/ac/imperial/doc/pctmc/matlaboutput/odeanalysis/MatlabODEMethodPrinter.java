package uk.ac.imperial.doc.pctmc.matlaboutput.odeanalysis;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.matlaboutput.MatlabPrinterWithConstants;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.matlaboutput.analysis.MatlabStatementPrinterCombinedProductBased;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.IODEMethodVisitor;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;

import com.google.common.collect.BiMap;

/**
 * Java ODE method printer.
 * @author Anton Stefanek
 *
 */
public class MatlabODEMethodPrinter implements IODEMethodVisitor {

	private Constants parameters;
	
	
	private BiMap<CombinedPopulationProduct,Integer> combinedMomentsIndex; 
	private Map<AbstractExpression,Integer> generalExpectationIndex;

	@Override
	public String toString() {

		return output.toString();
	}

	public MatlabODEMethodPrinter(Constants parameters,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,Map<AbstractExpression,Integer> generalExpectationIndex) {

		this.parameters = parameters;
		this.combinedMomentsIndex = combinedMomentsIndex;
		this.generalExpectationIndex = generalExpectationIndex; 
		output = new StringBuilder();
	}



	public static final String ODESNAME = "odes";
	private static final String OLDY = "y";
	private static final String NEWY = "dydt";

	StringBuilder output;

	@Override
	public void visit(ODEMethod s) {
		StringBuilder code = new StringBuilder();
		
		code.append("function "+NEWY + " = " + ODESNAME + "(t,"+OLDY+","+MatlabPrinterWithConstants.param+")\n");
		code.append(NEWY+"=zeros("+combinedMomentsIndex.size()+",1);\n");

		for (int i = 0; i < s.getBody().length; i++) {
			MatlabStatementPrinterCombinedProductBased tmp = new MatlabStatementPrinterCombinedProductBased(
					parameters,  combinedMomentsIndex,generalExpectationIndex, OLDY, NEWY);
			s.getBody()[i].accept(tmp);
			code.append("   " + tmp.toString() + "\n");
		}
		code.append("end\n");
		output.append(code.toString());
	}
}
