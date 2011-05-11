package uk.ac.imperial.doc.jexpressions.matlaboutput;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.IConstantExpressionVisitor;

/**
 * An extension of JavaExpressionPrinter supporting constants.
 * @author as1005
 *
 */
public class MatlabPrinterWithConstants extends MatlabExpressionPrinter implements
		IConstantExpressionVisitor {

	protected Constants constants;
	
	public static String param="param";

	public MatlabPrinterWithConstants(Constants constants) {
		super();

		this.constants = constants;
	}

	public void visit(ConstantExpression e) {
		output.append(param + "."
				+ (e.getConstant()));
	}
}
