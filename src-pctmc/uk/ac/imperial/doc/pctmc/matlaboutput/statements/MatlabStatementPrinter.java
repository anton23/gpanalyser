package uk.ac.imperial.doc.pctmc.matlaboutput.statements;

import uk.ac.imperial.doc.jexpressions.expressions.IExpressionPrinterFactory;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.statements.AbstractStatement;
import uk.ac.imperial.doc.jexpressions.statements.ArrayDeclaration;
import uk.ac.imperial.doc.jexpressions.statements.ArrayElementAssignment;
import uk.ac.imperial.doc.jexpressions.statements.Comment;
import uk.ac.imperial.doc.jexpressions.statements.IStatementVisitor;
import uk.ac.imperial.doc.jexpressions.statements.SkipStatement;
import uk.ac.imperial.doc.jexpressions.statements.VariableDeclaration;

/**
 * A statement visitor printing a Java implementation of the statements.
 * 
 * @author as1005
 */
public class MatlabStatementPrinter implements IStatementVisitor {

	protected StringBuilder output;
	protected IExpressionPrinterFactory expressionPrinterFactory;

	public MatlabStatementPrinter(
			IExpressionPrinterFactory expressionPrinterFactory) {
		output = new StringBuilder();
		this.expressionPrinterFactory = expressionPrinterFactory;
	}

	@Override
	public void visit(ArrayDeclaration s) {
		IExpressionVisitor sizePrinter = expressionPrinterFactory
				.createPrinter();
		s.getSize().accept(sizePrinter);
		output.append(s.getArray() + " = zeros(" + sizePrinter + ",1);");
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
		output.append(s.getArray() + "(" + indexPrinter.toString() + "+1) = "
				+ rhsString + ";");
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
		output.append("%" + c.getComment());
	}

	@Override
	public void visit(SkipStatement s) {
	}

	@Override
	public void visit(VariableDeclaration s) {
		output.append(s.getName() + " = zeros(" + s.getN() + ",1);");
	}
}
