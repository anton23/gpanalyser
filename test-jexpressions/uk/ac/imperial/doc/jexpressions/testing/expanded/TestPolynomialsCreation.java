package uk.ac.imperial.doc.jexpressions.testing.expanded;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.expanded.Polynomial;

public class TestPolynomialsCreation extends TestPolynomialsBase {
	@Test
	public void testEqualsSimple(){
		assertNotSame(p1, p2);
		assertEquals(p1, p1);
	}
	
	@Test
	public void testProduct(){		
		assertEquals(Polynomial.product(p1, p1), p2);
		assertEquals(Polynomial.product(new Polynomial(a), new Polynomial(d)), new Polynomial(ad));
	}
	
	@Test
	public void testNormalise(){
		Polynomial tmp1 = new Polynomial(t1, t1);
		assertEquals(tmp1, p4);
		Polynomial tmp2 = Polynomial.plus(p1, p1);
		Polynomial tmp3 = Polynomial.times(p1, 2.0);
		assertEquals(tmp3, tmp2);
	}
	
	@Test
	public void testOne(){
		Polynomial tmp1 = new Polynomial(t1);
		assertEquals(tmp1, new Polynomial(Polynomial.getOne()));
	}
	
	@Test
	public void testGetCommonFactor(){
		assertEquals(a,Polynomial.getCommonFactor(p5));
	}
	
	@Test
	public void testDivide(){
		assertEquals(p6, Polynomial.divide(p5,a, 1.0));
	}
}
