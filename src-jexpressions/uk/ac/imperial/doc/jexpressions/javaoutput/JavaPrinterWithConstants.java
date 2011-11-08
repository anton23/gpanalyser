package uk.ac.imperial.doc.jexpressions.javaoutput;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.IConstantExpressionVisitor;

/**
 * An extension of JavaExpressionPrinter supporting constants.
 * 
 * @author as1005
 * 
 */
public class JavaPrinterWithConstants extends JavaExpressionPrinter implements
		IConstantExpressionVisitor {

	protected Constants constants;

	private static String BASENAME = "r";

	public JavaPrinterWithConstants(Constants constants) {
		super();

		this.constants = constants;
	}

	public void visit(ConstantExpression e) {
		output.append(BASENAME + "["
				+ constants.getConstantsIndex(e.getConstant()) + "]");
	}
}
