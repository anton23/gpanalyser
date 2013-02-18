package uk.ac.imperial.doc.jexpressions.expressions.visitors;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivDivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.expressions.IndicatorFunction;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MaxExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinusExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.expressions.TimeExpression;
import uk.ac.imperial.doc.jexpressions.expressions.UMinusExpression;

/**
 * An general expression visitor that traverses the syntax tree of the given
 * expression.
 * 
 * @author as1005
 * 
 */
public class ExpressionWalker implements IExpressionVisitor {
	@Override
	public void visit(IntegerExpression e) {
	}

	@Override
	public void visit(UMinusExpression e) {
		e.getE().accept(this);
	}

	@Override
	public void visit(TimeExpression e) {
	}

	@Override
	public void visit(DivExpression e) {
		e.getNumerator().accept(this);
		e.getDenominator().accept(this);
	}

	@Override
	public void visit(MinusExpression e) {
		e.getA().accept(this);
		e.getB().accept(this);
	}

	@Override
	public void visit(PowerExpression e) {
		e.getExpression().accept(this);
		e.getExponent().accept(this);
	}

	@Override
	public void visit(AbstractExpression e) {
		throw new AssertionError("Unsupported visit to expression "
				+ e.toString());
	}

	@Override
	public void visit(DoubleExpression e) {
	}

	@Override
	public void visit(PEPADivExpression e) {
		e.getNumerator().accept(this);
		e.getDenominator().accept(this);
	}

	@Override
	public void visit(MinExpression e) {
		e.getA().accept(this);
		e.getB().accept(this);
	}
	
	@Override
	public void visit(MaxExpression e) {
		e.getA().accept(this);
		e.getB().accept(this);
	}

	public void visit(DivMinExpression e) {
		e.getA().accept(this);
		e.getB().accept(this);
		e.getC().accept(this);
	}

	public void visit(DivDivMinExpression e) {
		e.getA().accept(this);
		e.getB().accept(this);
		e.getC().accept(this);
		e.getD().accept(this);
	}

	@Override
	public void visit(ProductExpression e) {
		for (AbstractExpression t : e.getTerms()) {
			t.accept(this);
		}
	}

	@Override
	public void visit(SumExpression e) {
		for (AbstractExpression t : e.getSummands()) {
			t.accept(this);
		}
	}

	@Override
	public void visit(FunctionCallExpression e) {
		for (AbstractExpression arg : e.getArguments()) {
			arg.accept(this);
		}
	}

	@Override
	public void visit(IndicatorFunction e) {
		e.getCondition().getLeft().accept(this);
		e.getCondition().getRight().accept(this);
	}


	
	
}
