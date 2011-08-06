package uk.ac.imperial.doc.jexpressions.testing;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;

public class TestConstructors {
	@BeforeClass
	public static void createObjects(){
		d1 = new DoubleExpression(1.0);
		d2 = new DoubleExpression(2.0);
		d3 = new DoubleExpression(3.0);
		d0 = new DoubleExpression(0.0);
	}
	
	private static AbstractExpression d1, d2, d3, d0;
	
	@Test
	public void testCreateZeroExpression(){
		assertEquals(DoubleExpression.ZERO, new DoubleExpression(0.0));
	}
	
	@Test
	public void testCreateSumExpression(){
		assertEquals(d1, SumExpression.create(d1));
		String sumString = SumExpression.create(d1, d2).toString();
		System.out.println(sumString);
		assertEquals(sumString,"(1.0+2.0)");
		assertEquals(SumExpression.create(d1,SumExpression.create(d2,d3)),SumExpression.create(d1,d2,d3));
		assertEquals(SumExpression.create(d1,d0,d2,d0,d3),SumExpression.create(d1,d2,d3));
		assertEquals(SumExpression.create(), DoubleExpression.ZERO);
	}
	
	@Test
	public void testCreateProductExpression(){
		assertEquals(d1, ProductExpression.create(d1));
		assertEquals(ProductExpression.create(), ProductExpression.create(d1));
		assertEquals(ProductExpression.create(d2,d1,d1,d2,d3,d1), ProductExpression.create(d2,d2,d3));
		assertEquals(ProductExpression.create(d0,d1,d2,d3),DoubleExpression.ZERO);
		assertEquals(ProductExpression.create(d2,ProductExpression.create(d2,d3)),ProductExpression.create(d2,d2,d3));
	}
	
	@Test
	public void testCreateDivExpression(){
		assertEquals(DivExpression.create(d0,d1),DoubleExpression.ZERO);
		assertEquals(DivExpression.create(d2,d2),new DoubleExpression(1.0));
	}
	
	@Test
	public void testCreatePowerExpression(){
		assertEquals(PowerExpression.create(d2,d1),d2);
		assertEquals(PowerExpression.create(d2,d0),d1);
	}
	
	@Test
	public void testCreateMinExpression(){
		assertEquals(MinExpression.create(d2, d2),d2);
	}
	
	@Test
	public void testCreateDivMinExpression(){
		assertEquals(DivMinExpression.create(d2,d2,d3),MinExpression.create(d2,d3));
		assertEquals(DivMinExpression.create(d1, d2, d3).toString(),"(div(1.0,2.0)*min(3.0,2.0))");
	}

}
