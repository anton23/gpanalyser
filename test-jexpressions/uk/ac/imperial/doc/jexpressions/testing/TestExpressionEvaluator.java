package uk.ac.imperial.doc.jexpressions.testing;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionEvaluator;

public class TestExpressionEvaluator extends TestExpressionsBase{
	
	protected double evaluate(AbstractExpression expression){
		ExpressionEvaluator evaluator = new ExpressionEvaluator(); 
		expression.accept(evaluator);
		return evaluator.getResult();
	}
	
	@Test
	public void testEvaluatorSum(){ 
		assertTrue(6.0==evaluate(s123));
	}
	
	@Test
	public void testEvaluatorProduct(){
		assertTrue(12.0==evaluate(p223));
	}
	
	@Test
	public void testEvaluatorMin(){
		assertTrue(2.0==evaluate(min23));
	}
	
	@Test
	public void testEvaluatorDivs(){
		assertTrue(evaluate(div23)==evaluate(pdiv23));
		assertTrue(0.0==evaluate(pdiv20));
	}

}
