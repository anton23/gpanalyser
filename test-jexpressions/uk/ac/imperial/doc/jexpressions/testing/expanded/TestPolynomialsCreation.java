package uk.ac.imperial.doc.jexpressions.testing.expanded;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.expanded.DivisionResult;
import uk.ac.imperial.doc.jexpressions.expanded.DoubleCoefficients;
import uk.ac.imperial.doc.jexpressions.expanded.Polynomial;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;

public class TestPolynomialsCreation extends TestPolynomialsBase {
	@Test
	public void testEqualsSimple(){
		assertNotSame(new Polynomial(new DoubleCoefficients(), t1), new Polynomial(new DoubleCoefficients(), t2));
		assertNotSame(new Polynomial(new DoubleCoefficients(), t2), new Polynomial(new DoubleCoefficients(), t3));
		assertEquals(new Polynomial(new DoubleCoefficients(), t3), new Polynomial(new DoubleCoefficients(), t4));
		assertEquals(new Polynomial(new DoubleCoefficients(), t2), p2t1);
		assertNotSame(pApB, pA2p2ABpB2);
		assertEquals(pApB, pApB);
	}
	
	@Test
	public void testScalarProduct(){	
		assertEquals(pApB, Polynomial.scalarProduct(pApB, DoubleExpression.ONE));
		assertEquals(Polynomial.getEmptyPolynomial(new DoubleCoefficients()), Polynomial.scalarProduct(pApB, DoubleExpression.ZERO));
		assertEquals(Polynomial.product(pApB, p2), Polynomial.scalarProduct(pApB, new DoubleExpression(2.0)));
	}
	
	@Test
	public void testProduct(){				
		assertEquals(Polynomial.product(pApB, pApB), pA2p2ABpB2);
		assertEquals(Polynomial.product(new Polynomial(new DoubleCoefficients(), a), new Polynomial(new DoubleCoefficients(), d)), new Polynomial(new DoubleCoefficients(), ad));
		assertEquals(Polynomial.product(new Polynomial(new DoubleCoefficients(), d), new Polynomial(new DoubleCoefficients(), a)), new Polynomial(new DoubleCoefficients(), ad));
		assertEquals(new Polynomial(new DoubleCoefficients(), t0), Polynomial.product(new Polynomial(new DoubleCoefficients(), t0), new Polynomial(new DoubleCoefficients(), t1)));
	}
	
	@Test
	public void testNormalise(){
		Polynomial tmp1 = new Polynomial(new DoubleCoefficients(), t1, t1);
		assertEquals(p2, tmp1);
		Polynomial tmp2 = Polynomial.plus(pApB, pApB);
		Polynomial tmp3 = Polynomial.scalarProduct(pApB, new DoubleExpression(2.0));
		assertEquals(tmp3, tmp2);
	}
	
	@Test
	public void testOne(){
		Polynomial tmp1 = new Polynomial(new DoubleCoefficients(), t1);
		assertEquals(tmp1, Polynomial.getUnitPolynomial(new DoubleCoefficients()));
	}
	
	@Test
	public void testGetCommonFactor(){
		assertEquals(a,pA2p2Ap3AB.getCommonFactor());
	}
	
	@Test
	public void testDivide(){
		assertEquals(pAp2p3B, Polynomial.divide(pA2p2Ap3AB,a));
		assertEquals(pAp2p3B, Polynomial.divide(pAp2p3B,Polynomial.getOneTerm(new DoubleCoefficients())));
	}
	
	@Test
	public void testProperDivide(){
		DivisionResult tmp = Polynomial.divide(Polynomial.product(pApB, pApB), pApB);
		assertEquals(pApB, tmp.getResult());
		assertEquals(Polynomial.getEmptyPolynomial(new DoubleCoefficients()), tmp.getRemainder());
		
		DivisionResult tmp2 = Polynomial.divide(Polynomial.product(pA2p2Ap3AB, pApB), pA2p2Ap3AB);
		assertEquals(pApB, tmp2.getResult());
		assertEquals(Polynomial.getEmptyPolynomial(new DoubleCoefficients()), tmp2.getRemainder());

		Polynomial pB = new Polynomial(new DoubleCoefficients(), b);
		Polynomial pA = new Polynomial(new DoubleCoefficients(), a);
		Polynomial pAmB = Polynomial.minus(pA, pB);
		
		DivisionResult tmp3 = Polynomial.divide(pA2p2ABpB2, new Polynomial(new DoubleCoefficients(), a));
		assertEquals(new Polynomial(new DoubleCoefficients(), a, b, b), tmp3.getResult());
		assertEquals(Polynomial.product(pB, pB), tmp3.getRemainder());
		
		Polynomial product = Polynomial.product(pAmB, pApB);
		DivisionResult tmp4 = Polynomial.divide(product, pApB);
		assertEquals(pAmB, tmp4.getResult());
		assertEquals(Polynomial.getEmptyPolynomial(new DoubleCoefficients()), tmp4.getRemainder());
		
		DivisionResult tmp5 = Polynomial.divide(product, pAmB);
		assertEquals(pApB, tmp5.getResult());
		assertEquals(Polynomial.getEmptyPolynomial(new DoubleCoefficients()), tmp5.getRemainder());
		
		DivisionResult tmp6 = Polynomial.divide(Polynomial.product(pB, pApB), pApB);
		assertEquals(pB, tmp6.getResult());
		assertEquals(Polynomial.getEmptyPolynomial(new DoubleCoefficients()), tmp6.getRemainder());
	}
	
	@Test
	public void testPlus(){
		assertEquals(new Polynomial(new DoubleCoefficients(), t4), Polynomial.plus(new Polynomial(new DoubleCoefficients(), t2), p2t1));
		assertEquals(new Polynomial(new DoubleCoefficients(), ad,bc), Polynomial.plus(new Polynomial(new DoubleCoefficients(), ad), new Polynomial(new DoubleCoefficients(), bc)));
		assertEquals(Polynomial.getEmptyPolynomial(new DoubleCoefficients()), Polynomial.plus(Polynomial.getUnitPolynomial(new DoubleCoefficients()), Polynomial.getMinusUnitPolynomial(new DoubleCoefficients())));
	}
	
	@Test
	public void testCommonDivisor() {
		Polynomial pB = new Polynomial(new DoubleCoefficients(), b);
		Polynomial pA = new Polynomial(new DoubleCoefficients(), a);
		Polynomial pC = new Polynomial(new DoubleCoefficients(), c);
		Polynomial pAB = Polynomial.product(pA, pB);
		Polynomial pAC = Polynomial.product(pA, pC);

		Polynomial pAmB = Polynomial.minus(pA, pB);
		Polynomial product = Polynomial.product(pAmB, pApB);
		
		assertEquals(pApB, Polynomial.greatestCommonDivisor(pA2p2ABpB2, pApB));
		assertEquals(pApB, Polynomial.greatestCommonDivisor(pA2p2ABpB2, product));
		assertEquals(p1, Polynomial.greatestCommonDivisor(pA2p2Ap3AB, pA2p2ABpB2));
		assertEquals(pA, Polynomial.greatestCommonDivisor(pAB, pAC));
	}
}
