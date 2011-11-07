package uk.ac.imperial.doc.jexpressions.testing.expanded;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.expanded.DivisionResult;
import uk.ac.imperial.doc.jexpressions.expanded.DoubleNormaliser;
import uk.ac.imperial.doc.jexpressions.expanded.Polynomial;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;

public class TestPolynomialsCreation extends TestPolynomialsBase {
	@Test
	public void testEqualsSimple(){
		assertNotSame(new Polynomial(new DoubleNormaliser(), t1), new Polynomial(new DoubleNormaliser(), t2));
		assertNotSame(new Polynomial(new DoubleNormaliser(), t2), new Polynomial(new DoubleNormaliser(), t3));
		assertEquals(new Polynomial(new DoubleNormaliser(), t3), new Polynomial(new DoubleNormaliser(), t4));
		assertEquals(new Polynomial(new DoubleNormaliser(), t2), p2t1);
		assertNotSame(pApB, pA2p2ABpB2);
		assertEquals(pApB, pApB);
	}
	
	@Test
	public void testScalarProduct(){	
		assertEquals(pApB, Polynomial.scalarProduct(pApB, DoubleExpression.ONE));
		assertEquals(Polynomial.getEmptyPolynomial(new DoubleNormaliser()), Polynomial.scalarProduct(pApB, DoubleExpression.ZERO));
		assertEquals(Polynomial.product(pApB, p2), Polynomial.scalarProduct(pApB, new DoubleExpression(2.0)));
	}
	
	@Test
	public void testProduct(){				
		assertEquals(Polynomial.product(pApB, pApB), pA2p2ABpB2);
		assertEquals(Polynomial.product(new Polynomial(new DoubleNormaliser(), a), new Polynomial(new DoubleNormaliser(), d)), new Polynomial(new DoubleNormaliser(), ad));
		assertEquals(Polynomial.product(new Polynomial(new DoubleNormaliser(), d), new Polynomial(new DoubleNormaliser(), a)), new Polynomial(new DoubleNormaliser(), ad));
		assertEquals(new Polynomial(new DoubleNormaliser(), t0), Polynomial.product(new Polynomial(new DoubleNormaliser(), t0), new Polynomial(new DoubleNormaliser(), t1)));
	}
	
	@Test
	public void testNormalise(){
		Polynomial tmp1 = new Polynomial(new DoubleNormaliser(), t1, t1);
		assertEquals(p2, tmp1);
		Polynomial tmp2 = Polynomial.plus(pApB, pApB);
		Polynomial tmp3 = Polynomial.scalarProduct(pApB, new DoubleExpression(2.0));
		assertEquals(tmp3, tmp2);
	}
	
	@Test
	public void testOne(){
		Polynomial tmp1 = new Polynomial(new DoubleNormaliser(), t1);
		assertEquals(tmp1, Polynomial.getUnitPolynomial(new DoubleNormaliser()));
	}
	
	@Test
	public void testGetCommonFactor(){
		assertEquals(a,Polynomial.getCommonFactor(pA2p2Ap3AB));
	}
	
	@Test
	public void testDivide(){
		assertEquals(pAp2p3B, Polynomial.divide(pA2p2Ap3AB,a, DoubleExpression.ONE));
		assertEquals(pAp2p3B, Polynomial.divide(pAp2p3B,Polynomial.getOneTerm(new DoubleNormaliser()), DoubleExpression.ONE));
	}
	
	@Test
	public void testProperDivide(){
		DivisionResult tmp = Polynomial.divide(Polynomial.product(pApB, pApB), pApB);
		assertEquals(pApB, tmp.getResult());
		assertEquals(Polynomial.getEmptyPolynomial(new DoubleNormaliser()), tmp.getRemainder());
		
		DivisionResult tmp2 = Polynomial.divide(Polynomial.product(pA2p2Ap3AB, pApB), pA2p2Ap3AB);
		assertEquals(pApB, tmp2.getResult());
		assertEquals(Polynomial.getEmptyPolynomial(new DoubleNormaliser()), tmp2.getRemainder());

		Polynomial pB = new Polynomial(new DoubleNormaliser(), b);
		Polynomial pA = new Polynomial(new DoubleNormaliser(), a);
		Polynomial pAmB = Polynomial.minus(pA, pB);
		
		DivisionResult tmp3 = Polynomial.divide(pA2p2ABpB2, new Polynomial(new DoubleNormaliser(), a));
		assertEquals(new Polynomial(new DoubleNormaliser(), a, b, b), tmp3.getResult());
		assertEquals(Polynomial.product(pB, pB), tmp3.getRemainder());
		
		Polynomial product = Polynomial.product(pAmB, pApB);
		DivisionResult tmp4 = Polynomial.divide(product, pApB);
		assertEquals(pAmB, tmp4.getResult());
		assertEquals(Polynomial.getEmptyPolynomial(new DoubleNormaliser()), tmp4.getRemainder());
		
		DivisionResult tmp5 = Polynomial.divide(product, pAmB);
		assertEquals(pApB, tmp5.getResult());
		assertEquals(Polynomial.getEmptyPolynomial(new DoubleNormaliser()), tmp5.getRemainder());
		
		DivisionResult tmp6 = Polynomial.divide(Polynomial.product(pB, pApB), pApB);
		assertEquals(pB, tmp6.getResult());
		assertEquals(Polynomial.getEmptyPolynomial(new DoubleNormaliser()), tmp6.getRemainder());
	}
	
	@Test
	public void testPlus(){
		assertEquals(new Polynomial(new DoubleNormaliser(), t4), Polynomial.plus(new Polynomial(new DoubleNormaliser(), t2), p2t1));
		assertEquals(new Polynomial(new DoubleNormaliser(), ad,bc), Polynomial.plus(new Polynomial(new DoubleNormaliser(), ad), new Polynomial(new DoubleNormaliser(), bc)));
		assertEquals(Polynomial.getEmptyPolynomial(new DoubleNormaliser()), Polynomial.plus(Polynomial.getUnitPolynomial(new DoubleNormaliser()), Polynomial.getMinusUnitPolynomial(new DoubleNormaliser())));
	}
	
	@Test
	public void testCommonDivisor() {
		Polynomial pB = new Polynomial(new DoubleNormaliser(), b);
		Polynomial pA = new Polynomial(new DoubleNormaliser(), a);
		Polynomial pAmB = Polynomial.minus(pA, pB);
		Polynomial product = Polynomial.product(pAmB, pApB);
		
		assertEquals(pApB, Polynomial.greatestCommonDivisor(pA2p2ABpB2, pApB));
		assertEquals(pApB, Polynomial.greatestCommonDivisor(pA2p2ABpB2, product));
//		assertEquals(p1, Polynomial.greatestCommonDivisor(pA2p2Ap3AB, pA2p2ABpB2));
	}
}
