package uk.ac.imperial.doc.jexpressions.testing.expanded;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.expanded.Polynomial;

public class TestPolynomialsCreation extends TestPolynomialsBase {
	@Test
	public void testEqualsSimple(){
		assertNotSame(pApB, pA2p2ABpB2);
		assertEquals(pApB, pApB);
	}
	
	@Test
	public void testProduct(){		
		assertEquals(Polynomial.product(pApB, pApB), pA2p2ABpB2);
		assertEquals(Polynomial.product(new Polynomial(a), new Polynomial(d)), new Polynomial(ad));
		assertEquals(Polynomial.product(new Polynomial(d), new Polynomial(a)), new Polynomial(ad));
	}
	
	@Test
	public void testNormalise(){
		Polynomial tmp1 = new Polynomial(t1, t1);
		assertEquals(p2, tmp1);
		Polynomial tmp2 = Polynomial.plus(pApB, pApB);
		Polynomial tmp3 = Polynomial.times(pApB, 2.0);
		assertEquals(tmp3, tmp2);
	}
	
	@Test
	public void testOne(){
		Polynomial tmp1 = new Polynomial(t1);
		assertEquals(tmp1, new Polynomial(Polynomial.getOne()));
	}
	
	@Test
	public void testGetCommonFactor(){
		assertEquals(a,Polynomial.getCommonFactor(pA2p2Ap3AB));
	}
	
	@Test
	public void testDivide(){
		assertEquals(pAp2p3B, Polynomial.divide(pA2p2Ap3AB,a, 1.0));
		assertEquals(pAp2p3B, Polynomial.divide(pAp2p3B,Polynomial.getOne(), 1.0));
	}
	
	@Test
	public void testPlus(){
		assertEquals(new Polynomial(ad,bc), Polynomial.plus(new Polynomial(ad), new Polynomial(bc)));
	}
}
