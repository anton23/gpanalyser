package uk.ac.imperial.doc.pctmc.cppoutput.statements;

import uk.ac.imperial.doc.jexpressions.expressions.IExpressionPrinterFactory;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.statements.*;

/**
 * A statement visitor printing a Java implementation of the statements.
 * 
 * @author as1005
 */
public class CPPStatementPrinter implements IStatementVisitor {

	protected StringBuilder output;
	protected IExpressionPrinterFactory expressionPrinterFactory;

	public CPPStatementPrinter(
            IExpressionPrinterFactory expressionPrinterFactory) {
		output = new StringBuilder();
		this.expressionPrinterFactory = expressionPrinterFactory;
	}

	@Override
	public void visit(ArrayDeclaration s) {
		IExpressionVisitor sizePrinter = expressionPrinterFactory
				.createPrinter();
		s.getSize().accept(sizePrinter);
		output.append("double *" + s.getArray()
                + " = new double [" + sizePrinter + "];\n");
        //output.append("for (int i = 0; i < " + sizePrinter + "; ++i) {\n");
        //output.append(s.getArray() + "[i] = 0;\n");
        //output.append("}\n");
	}

	@Override
	public void visit(ArrayElementAssignment s) {
		IExpressionVisitor rhsPrinter = expressionPrinterFactory
				.createPrinter();
		s.getRhs().accept(rhsPrinter);
		String rhsString = rhsPrinter.toString();
		IExpressionVisitor indexPrinter = expressionPrinterFactory
				.createPrinter();
		s.getIndex().accept(indexPrinter);
		output.append(s.getArray() + "[" + indexPrinter.toString() + "] = "
				+ rhsString + ";\n");
	}

	@Override
	public String toString() {
		return output.toString();
	}

	@Override
	public void visit(AbstractStatement s) {
		throw new AssertionError("Unsupported visit to statement "
				+ s.toString() + "!");
	}

	@Override
	public void visit(Comment c) {
        output.append("/*" + c.getComment() + "*/");
	}

	@Override
	public void visit(SkipStatement s) {
	}

	@Override
	public void visit(VariableDeclaration s) {
        output.append("double *" + s.getName()
                + " = new double [" + s.getN() + "];\n");
        //output.append("for (int i = 0; i < " + s.getN() + "; ++i) {\n");
        //output.append(s.getName() + "[i] = 0;\n");
        //output.append("}\n");
	}
}
