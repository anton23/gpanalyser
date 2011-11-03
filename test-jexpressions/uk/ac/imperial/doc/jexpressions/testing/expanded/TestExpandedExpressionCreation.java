package uk.ac.imperial.doc.jexpressions.testing.expanded;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expanded.Polynomial;


public class TestExpandedExpressionCreation extends TestExpandedExpressionBase{
	
	@Test
	public void testNormalise(){
		assertNotSame(eAoB,eCoD);
		assertEquals(eAoB,eADoBD);		
		assertEquals(new ExpandedExpression(new Polynomial(a,b), Polynomial.getEmptyPolynomial()),eADpBDoD);
	}
	
	@Test
	public void testPlus(){
		assertEquals(eADpBCoBD,ExpandedExpression.plus(eAoB, eCoD));
		assertEquals(eApCoB, ExpandedExpression.plus(eAoB, eCoB));
		assertEquals(new ExpandedExpression(new Polynomial(a, b)),
				ExpandedExpression.plus(new ExpandedExpression(new Polynomial(a)), new ExpandedExpression(new Polynomial(b))));
	}
	
	@Test
	public void testProduct(){
		assertEquals(new ExpandedExpression(new Polynomial(a, c)),
				ExpandedExpression.product(eApCoB, new ExpandedExpression(new Polynomial(b))));
		assertEquals(new ExpandedExpression(new Polynomial(ab)),
				ExpandedExpression.product(new ExpandedExpression(new Polynomial(a)), new ExpandedExpression(new Polynomial(b))));
	}
	
	@Test
	public void testDivide(){
		assertEquals(new ExpandedExpression(new Polynomial(Polynomial.getOne())),
				ExpandedExpression.divide(eADpBCoBD, eADpBCoBD));
	}

}
