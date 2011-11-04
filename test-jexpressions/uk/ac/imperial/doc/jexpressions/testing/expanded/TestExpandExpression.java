package uk.ac.imperial.doc.jexpressions.testing.expanded;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Test;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandingExpressionTransformer;
import uk.ac.imperial.doc.jexpressions.expanded.Polynomial;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;

public class TestExpandExpression extends TestExpandedExpressionBase {

	@Test
	public void testExpand() {
		assertEquals(ExpandedExpression.create(new Polynomial(a, b)),
				ExpandingExpressionTransformer.expandExpression(SumExpression
						.create(new ConstantExpression("a"),
								new ConstantExpression("b"))));
		PCTMCInterpreter interpreter = GPAPMain.createGPEPAInterpreter();
		try {
			AbstractExpression e1 = interpreter.parseExpressionList("(a+b)*(a+b)").iterator().next();
			ExpandedExpression expandExpression = ExpandingExpressionTransformer.expandExpression(e1);
			assertEquals(ExpandedExpression.create(pA2p2ABpB2), expandExpression);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertEqualExpressions("3.0", "1.0+1.0*2.0", interpreter);
		assertEqualExpressions("min((a+b)*(a+b),1.0)", "min(a*a + 2.0*a*b + b*b,1.0)", interpreter);
        assertEqualExpressions("min(a+2.0*b+c,a+b+b+c)", "a+2.0*b+c", interpreter);
        assertEqualExpressions("a/a", "1.0", interpreter);
        assertEqualExpressions("(a*a+a*b)/a", "a+b", interpreter);
        assertEqualExpressions("a/(a+b) + b/(a+b)", "1.0", interpreter);
        assertEqualExpressions("1.0", "1.0", interpreter);
        assertEqualExpressions("2.0/2.0", "1.0", interpreter);
        assertEqualExpressions("a*b/b", "a", interpreter);
        assertEqualExpressions("a/b + 1.0", "(a+b)/b", interpreter);
        assertEqualExpressions("(a+b)*(a+b)*(a+b)", "a*a*a + 3*a*a*b + 3*a*b*b + b*b*b", interpreter);
        assertEqualExpressions("6.0/24.0", "1.0/4.0", interpreter);
        assertEqualExpressions("(6.0*a + 3.0*b)/24.0", "(2.0*a+1.0*b)/2.0 * (1.0/4.0)", interpreter);
        assertEqualExpressions("-1.0", "0.0-1.0", interpreter);
        assertEqualExpressions("0.0", "a-a", interpreter);
        assertNotEqualExpressions("2.0", "1.0", interpreter);
        assertNotEqualExpressions("a*b", "a*b*b", interpreter);
        assertNotEqualExpressions("a*b*c", "a*b*b", interpreter);
        assertNotEqualExpressions("a/b", "a/(2.0*b)", interpreter);
	}
	
	private void assertEqualExpressions(String s1, String s2, PCTMCInterpreter interpreter){
		try {
			AbstractExpression e1 = interpreter.parseExpressionList(s1).iterator().next();
			AbstractExpression e2 = interpreter.parseExpressionList(s2).iterator().next();
			ExpandedExpression expE1 = ExpandingExpressionTransformer.expandExpression(e1);
			ExpandedExpression expE2 = ExpandingExpressionTransformer.expandExpression(e2);
			assertEquals(expE1, expE2);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private void assertNotEqualExpressions(String s1, String s2, PCTMCInterpreter interpreter){
		try {
			AbstractExpression e1 = interpreter.parseExpressionList(s1).iterator().next();
			AbstractExpression e2 = interpreter.parseExpressionList(s2).iterator().next();
			assertThat(ExpandingExpressionTransformer.expandExpression(e1),
					not(equalTo(ExpandingExpressionTransformer.expandExpression(e2))));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}