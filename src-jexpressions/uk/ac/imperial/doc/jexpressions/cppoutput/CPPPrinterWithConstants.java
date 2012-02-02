package uk.ac.imperial.doc.jexpressions.cppoutput;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.IConstantExpressionVisitor;

/**
 * An extension of CPPExpressionPrinter supporting constants.
 * 
 * @author as1005
 * 
 */
public class CPPPrinterWithConstants extends CPPExpressionPrinter implements
		IConstantExpressionVisitor {

	protected Constants constants;

	private static String BASENAME = "r";

	public CPPPrinterWithConstants(Constants constants) {
		super();

		this.constants = constants;
	}

	public void visit(ConstantExpression e) {
		output.append(BASENAME + "["
				+ constants.getConstantsIndex(e.getConstant()) + "]");
	}
}
