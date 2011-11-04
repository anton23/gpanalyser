package uk.ac.imperial.doc.jexpressions.expanded;

import java.util.Set;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.IConstantExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionTransformerWithConstants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionWalkerWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivDivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinusExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.expressions.TimeExpression;
import uk.ac.imperial.doc.jexpressions.expressions.UMinusExpression;
import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionWalker;

import com.google.common.collect.Multiset;

public class ExpandingExpressionTransformer implements IExpressionVisitor, IConstantExpressionVisitor{
	private ExpandedExpression result;

	public static ExpandedExpression expandExpression(AbstractExpression e){	
		ExpandingExpressionTransformer t = new ExpandingExpressionTransformer();
		e.accept(t);
		return t.getResult();
	}
	
	@Override
	public void visit(ConstantExpression e) {
		result = new UnexpandableExpression(e);
	}
	
	@Override
	public void visit(DoubleExpression e) {
		result = new UnexpandableExpression(e);
	}
	
	@Override
	public void visit(SumExpression e) {
		ExpandedExpression ret = null;
		for (AbstractExpression s:e.getSummands()){
			s.accept(this);
			if (ret==null){
				ret = result;
			} else {
				ret = ExpandedExpression.plus(ret, result);
			}
		}
		result = ret;
	}

	
	@Override
	public void visit(ProductExpression e) {
		ExpandedExpression ret = ExpandedExpression.getOne();
		for (AbstractExpression t:e.getTerms()){
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
		if (eA.equals(eB)){
			result = eA;
		} else {
			result = new UnexpandableExpression(MinExpression.create(eA, eB));
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
	public void visit(AbstractExpression e) {}

	@Override
	public void visit(DivDivMinExpression e) {
		// TODO Auto-generated method stub		
	}

	

	@Override
	public void visit(DivMinExpression e) {
		// TODO Auto-generated method stub
		
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
	public void visit(MinusExpression e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(PEPADivExpression e) {
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
	public void visit(UMinusExpression e) {
		// TODO Auto-generated method stub
		
	}

	public ExpandedExpression getResult() {
		return result;
	}
	
	
	
}