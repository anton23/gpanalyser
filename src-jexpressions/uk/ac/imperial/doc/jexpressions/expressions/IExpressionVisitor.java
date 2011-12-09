package uk.ac.imperial.doc.jexpressions.expressions;

/**
 * An interface for visitors to basic expressions.
 * 
 * @author Anton Stefanek
 * 
 */
public interface IExpressionVisitor {
	void visit(AbstractExpression e);

	void visit(DoubleExpression e);

	void visit(IntegerExpression e);

	void visit(PEPADivExpression e);

	void visit(MinExpression e);
	
	void visit(MaxExpression e);

	void visit(DivMinExpression e);

	void visit(DivDivMinExpression e);

	void visit(ProductExpression e);

	void visit(SumExpression e);

	void visit(PowerExpression e);

	void visit(MinusExpression e);

	void visit(UMinusExpression e);

	void visit(DivExpression e);

	void visit(TimeExpression e);

	void visit(FunctionCallExpression e);
}
