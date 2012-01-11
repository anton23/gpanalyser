package uk.ac.imperial.doc.jexpressions.expressions.visitors;

import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.jexpressions.conditions.ExpressionCondition;
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
 * A generic transformer of basic expressions.
 * 
 * @author as1005
 * 
 */
public class ExpressionTransformer implements IExpressionVisitor {

	@Override
	public void visit(IntegerExpression e) {
		result = new IntegerExpression(e.getValue());
	}

	@Override
	public void visit(UMinusExpression e) {
		e.getE().accept(this);
		result = new UMinusExpression(result);

	}

	@Override
	public void visit(TimeExpression e) {
		result = e;
	}

	@Override
	public void visit(DivExpression e) {
		e.getNumerator().accept(this);
		AbstractExpression newNumerator = result;
		e.getDenominator().accept(this);
		AbstractExpression newDenominator = result;
		result = DivExpression.create(newNumerator, newDenominator);
	}

	@Override
	public void visit(MinusExpression e) {
		e.getA().accept(this);
		AbstractExpression newA = result;
		e.getB().accept(this);
		result = new MinusExpression(newA, result);
	}

	@Override
	public void visit(PowerExpression e) {
		e.getExpression().accept(this);
		AbstractExpression newExpression = result;
		e.getExponent().accept(this);
		result = new PowerExpression(newExpression, result);
	}

	protected AbstractExpression result;

	@Override
	public void visit(AbstractExpression e) {
		throw new AssertionError("Unsupported visit to expression "
				+ e.toString());
	}

	@Override
	public void visit(DoubleExpression e) {
		result = e;
	}

	@Override
	public void visit(PEPADivExpression e) {
		e.getNumerator().accept(this);
		AbstractExpression newNumerator = result;
		e.getDenominator().accept(this);
		AbstractExpression newDenominator = result;
		result = PEPADivExpression.create(newNumerator, newDenominator);
	}

	@Override
	public void visit(MinExpression e) {
		e.getA().accept(this);
		AbstractExpression newA = result;
		e.getB().accept(this);
		AbstractExpression newB = result;
		result = MinExpression.create(newA, newB);
	}
	
	@Override
	public void visit(MaxExpression e) {
		e.getA().accept(this);
		AbstractExpression newA = result;
		e.getB().accept(this);
		AbstractExpression newB = result;
		result = MaxExpression.create(newA, newB);
	}

	public void visit(DivMinExpression e) {
		e.getA().accept(this);
		AbstractExpression newA = result;
		e.getB().accept(this);
		AbstractExpression newB = result;
		e.getC().accept(this);
		AbstractExpression newC = result;
		result = DivMinExpression.create(newA, newB, newC);
	}

	public void visit(DivDivMinExpression e) {
		e.getA().accept(this);
		AbstractExpression newA = result;
		e.getB().accept(this);
		AbstractExpression newB = result;
		e.getC().accept(this);
		AbstractExpression newC = result;
		e.getD().accept(this);
		AbstractExpression newD = result;
		result = DivDivMinExpression.create(newA, newB, newC, newD);
	}

	@Override
	public void visit(ProductExpression e) {
		AbstractExpression[] ts = new AbstractExpression[e.getTerms().size()];
		int i = 0;
		for (AbstractExpression t : e.getTerms()) {
			t.accept(this);
			ts[i++] = result;
		}
		result = ProductExpression.create(ts);
	}

	@Override
	public void visit(SumExpression e) {
		AbstractExpression[] ts = new AbstractExpression[e.getSummands().size()];
		int i = 0;
		for (AbstractExpression t : e.getSummands()) {
			t.accept(this);
			ts[i++] = result;
		}
		result = SumExpression.create(ts);
	}

	public AbstractExpression getResult() {
		return result;
	}

	@Override
	public void visit(FunctionCallExpression e) {
		List<AbstractExpression> newArguments = new LinkedList<AbstractExpression>();
		for (AbstractExpression arg : e.getArguments()) {
			arg.accept(this);
			newArguments.add(result);
		}
		result = FunctionCallExpression.create(e.getName(), newArguments);
	}

	@Override
	public void visit(IndicatorFunction e) {
		e.getCondition().getLeft().accept(this);
		AbstractExpression newLeft = result;
		
		e.getCondition().getRight().accept(this);
		AbstractExpression newRight = result;
		
		result = new IndicatorFunction(
				new ExpressionCondition(newLeft, e.getCondition().getOperator(), newRight)); 
	}
	
	
}
