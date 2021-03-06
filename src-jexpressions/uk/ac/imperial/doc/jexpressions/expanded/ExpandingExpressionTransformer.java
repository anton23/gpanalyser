package uk.ac.imperial.doc.jexpressions.expanded;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.IConstantExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivDivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ExpressionCondition;
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
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.jexpressions.variables.IExpressionVariableVisitor;

public class ExpandingExpressionTransformer implements IExpressionVisitor,
		IConstantExpressionVisitor, IExpressionVariableVisitor {

	protected ExpandedExpression result;

	protected ICoefficientSpecification normaliser;

	public ExpandingExpressionTransformer(ICoefficientSpecification normaliser) {
		super();
		this.normaliser = normaliser;
	}

	public static ExpandedExpression expandExpressionWithDoubles(
			AbstractExpression e) {
		ExpandingExpressionTransformer t = new ExpandingExpressionTransformer(
				new DoubleCoefficients());
		e.accept(t);
		return t.getResult();
	}

	@Override
	public void visit(ConstantExpression e) {
		result = new UnexpandableExpression(e, normaliser);
	}

	@Override
	public void visit(DoubleExpression e) {
		result = new UnexpandableExpression(e, normaliser);
	}

	@Override
	public void visit(SumExpression e) {
		ExpandedExpression ret = new UnexpandableExpression(
				new DoubleExpression(0.0), normaliser);
		for (AbstractExpression s : e.getSummands()) {
			s.accept(this);
			ret = ExpandedExpression.plus(ret, result);
		}
		result = ret;
	}

	@Override
	public void visit(ProductExpression e) {
		ExpandedExpression ret = ExpandedExpression.getOne(normaliser);
		for (AbstractExpression t : e.getTerms()) {
			t.accept(this);
			ret = ExpandedExpression.product(result, ret);
		}
		result = ret;
	}

	@Override
	public void visit(MinExpression e) {
		e.getA().accept(this);
		ExpandedExpression eA = result;
		e.getB().accept(this);
		ExpandedExpression eB = result;
		if (eA.equals(eB)) {
			result = eA;
		} else {
			if (eA.isNumber() && eB.isNumber()) {
				result = new UnexpandableExpression(MinExpression.create(eA
						.numericalValue(), eB.numericalValue()), normaliser);
			} else {
				result = new UnexpandableExpression(MinExpression
						.create(eA, eB), normaliser);
			}
		}

		/*
		 * // This would only work if the common factors were positive
		 * 
		 * Multiset<ExpandedExpression> numeratorCommonFactor = Polynomial
		 * .getGreatestCommonFactor(eA.getNumerator(), eA.getNumerator());
		 * Multiset<ExpandedExpression> denominatorCommonFactor = Polynomial
		 * .getGreatestCommonFactor(eA.getDenominator(), eA .getDenominator());
		 * 
		 * 
		 * ExpandedExpression commonFactor = ExpandedExpression.create( new
		 * Polynomial(numeratorCommonFactor), new Polynomial(
		 * denominatorCommonFactor));
		 * 
		 * if (eA.equals(eB)) { result = eA; } else { ExpandedExpression newA =
		 * ExpandedExpression.divide(eA, commonFactor); ExpandedExpression newB
		 * = ExpandedExpression.divide(eB, commonFactor); if (newA.isNumber() &&
		 * newB.isNumber()) { result = ExpandedExpression.product(commonFactor,
		 * new UnexpandableExpression(new DoubleExpression(Math
		 * .min(newA.numericalValue(), newB .numericalValue())))); } else {
		 * result = ExpandedExpression.product(commonFactor, new
		 * UnexpandableExpression(MinExpression.create(newA, newB))); } }
		 */
	}

	public void visit(MaxExpression e) {
		e.getA().accept(this);
		ExpandedExpression eA = result;
		e.getB().accept(this);
		ExpandedExpression eB = result;
		if (eA.equals(eB)) {
			result = eA;
		} else {
			if (eA.isNumber() && eB.isNumber()) {
				result = new UnexpandableExpression(MinExpression.create(eA
						.numericalValue(), eB.numericalValue()), normaliser);
			} else {
				result = new UnexpandableExpression(MinExpression
						.create(eA, eB), normaliser);
			}
		}
	}
	
	// We treat Div as PEPADiv
	@Override
	public void visit(DivExpression e) {
		e.getNumerator().accept(this);
		ExpandedExpression eN = result;
		e.getDenominator().accept(this);
		ExpandedExpression eD = result;
		result = ExpandedExpression.divide(eN, eD);
	}

	@Override
	public void visit(PEPADivExpression e) {
		e.getNumerator().accept(this);
		ExpandedExpression eN = result;
		e.getDenominator().accept(this);
		ExpandedExpression eD = result;
		result = ExpandedExpression.divide(eN, eD);
	}

	@Override
	public void visit(MinusExpression e) {
		e.getA().accept(this);
		ExpandedExpression eA = result;
		e.getB().accept(this);
		ExpandedExpression eB = result;
		result = ExpandedExpression.plus(eA, ExpandedExpression.product(
				ExpandedExpression.getMinusOne(normaliser), eB));
	}

	@Override
	public void visit(UMinusExpression e) {
		e.getE().accept(this);
		ExpandedExpression eE = result;
		result = ExpandedExpression.product(ExpandedExpression
				.getMinusOne(normaliser), eE);

	}

	@Override
	public void visit(DivDivMinExpression e) {
		e.getFullExpression().accept(this);
	}

	@Override
	public void visit(AbstractExpression e) {
	}

	@Override
	public void visit(DivMinExpression e) {
		e.getFullExpression().accept(this);

	}

	@Override
	public void visit(FunctionCallExpression e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IntegerExpression e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(PowerExpression e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TimeExpression e) {
		// TODO Auto-generated method stub
	}
	

	@Override
	public void visit(ExpressionVariable e) {
		result = new UnexpandableExpression(e, normaliser);		
	}

	@Override
	public void visit(IndicatorFunction e) {
		e.getCondition().getLeft().accept(this);
		ExpandedExpression eL = result;
		e.getCondition().getRight().accept(this);
		ExpandedExpression eR = result;
        result = new UnexpandableExpression(new IndicatorFunction(
        		new ExpressionCondition(eL, e.getCondition().getOperator(), eR)), normaliser);

		
	}

	public ExpandedExpression getResult() {
		return result;
	}
}