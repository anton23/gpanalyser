package uk.ac.imperial.doc.jexpressions.expressions;

/**
 * An interface for ExpressionPrinter factories.
 * @author Anton Stefanek
 *
 */
public interface IExpressionPrinterFactory {
	IExpressionVisitor createPrinter();
}
