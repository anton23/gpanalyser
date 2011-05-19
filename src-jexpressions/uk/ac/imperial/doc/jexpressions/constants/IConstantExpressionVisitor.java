package uk.ac.imperial.doc.jexpressions.constants;

/**
 * Visitor interface for visitors supporting ConstantExpression expressions.
 * @author Anton Stefanek
 *
 */
public interface IConstantExpressionVisitor {
	public void visit(ConstantExpression e);
}
