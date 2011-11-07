package uk.ac.imperial.doc.jexpressions.testing.expanded;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expanded.DoubleNormaliser;
import uk.ac.imperial.doc.jexpressions.expanded.Polynomial;

public class TestExpandedExpressionCreation extends TestExpandedExpressionBase {

	@Test
	public void testNormalise() {
		assertNotSame(eAoB, eCoD);
		assertEquals(eAoB, eADoBD);
		assertEquals(ExpandedExpression.create(new Polynomial(new DoubleNormaliser(), a, b)), eADpBDoD);
	}

	@Test
	public void testPlus() {
		assertEquals(eADpBCoBD, ExpandedExpression.plus(eAoB, eCoD));
		assertEquals(eApCoB, ExpandedExpression.plus(eAoB, eCoB));
		assertEquals(ExpandedExpression.create(new Polynomial(new DoubleNormaliser(), a, b)),
				ExpandedExpression.plus(ExpandedExpression
						.create(new Polynomial(new DoubleNormaliser(), a)), ExpandedExpression
						.create(new Polynomial(new DoubleNormaliser(), b))));
	}

	@Test
	public void testProduct() {
		assertEquals(ExpandedExpression
				.create(new Polynomial(new DoubleNormaliser(), t2)),
				ExpandedExpression.product(ExpandedExpression
				.create(new Polynomial(new DoubleNormaliser(), t1)), ExpandedExpression
				.create(new Polynomial(new DoubleNormaliser(), t2))));
		assertEquals(ExpandedExpression.create(new Polynomial(new DoubleNormaliser(), a, c)),
				ExpandedExpression.product(eApCoB, ExpandedExpression
						.create(new Polynomial(new DoubleNormaliser(), b))));
		assertEquals(ExpandedExpression.create(new Polynomial(new DoubleNormaliser(), ab)),
				ExpandedExpression.product(ExpandedExpression
						.create(new Polynomial(new DoubleNormaliser(), a)), ExpandedExpression
						.create(new Polynomial(new DoubleNormaliser(), b))));
	}

	@Test
	public void testDivide() {
		assertEquals(ExpandedExpression.create(Polynomial.getUnitPolynomial(new DoubleNormaliser())), ExpandedExpression.divide(eADpBCoBD, eADpBCoBD));
	}

}
