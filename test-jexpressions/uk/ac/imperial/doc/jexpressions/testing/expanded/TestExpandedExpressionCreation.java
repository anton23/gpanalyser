package uk.ac.imperial.doc.jexpressions.testing.expanded;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expanded.Polynomial;

public class TestExpandedExpressionCreation extends TestExpandedExpressionBase {

	@Test
	public void testNormalise() {
		assertNotSame(eAoB, eCoD);
		assertEquals(eAoB, eADoBD);
		assertEquals(ExpandedExpression.create(new Polynomial(a, b)), eADpBDoD);
	}

	@Test
	public void testPlus() {
		assertEquals(eADpBCoBD, ExpandedExpression.plus(eAoB, eCoD));
		assertEquals(eApCoB, ExpandedExpression.plus(eAoB, eCoB));
		assertEquals(ExpandedExpression.create(new Polynomial(a, b)),
				ExpandedExpression.plus(ExpandedExpression
						.create(new Polynomial(a)), ExpandedExpression
						.create(new Polynomial(b))));
	}

	@Test
	public void testProduct() {
		assertEquals(ExpandedExpression
				.create(new Polynomial(t2)),
				ExpandedExpression.product(ExpandedExpression
				.create(new Polynomial(t1)), ExpandedExpression
				.create(new Polynomial(t2))));
		assertEquals(ExpandedExpression.create(new Polynomial(a, c)),
				ExpandedExpression.product(eApCoB, ExpandedExpression
						.create(new Polynomial(b))));
		assertEquals(ExpandedExpression.create(new Polynomial(ab)),
				ExpandedExpression.product(ExpandedExpression
						.create(new Polynomial(a)), ExpandedExpression
						.create(new Polynomial(b))));
	}

	@Test
	public void testDivide() {
		assertEquals(ExpandedExpression.create(Polynomial.getUnitPolynomial()), ExpandedExpression.divide(eADpBCoBD, eADpBCoBD));
	}

}
