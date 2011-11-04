package uk.ac.imperial.doc.jexpressions.testing.expanded;

import static org.junit.Assert.assertEquals;

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
		assertEquals(new ExpandedExpression(new Polynomial(a, b)),
				ExpandingExpressionTransformer.expandExpression(SumExpression
						.create(new ConstantExpression("a"),
								new ConstantExpression("b"))));
		PCTMCInterpreter interpreter = GPAPMain.createGPEPAInterpreter();
		try {
			AbstractExpression e1 = interpreter.parseExpressionList("(a+b)*(a+b)").iterator().next();
			ExpandedExpression expandExpression = ExpandingExpressionTransformer.expandExpression(e1);
			assertEquals(new ExpandedExpression(pA2p2ABpB2), expandExpression);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		compareTwoExpressions("min((a+b)*(a+b),1.0)", "min(a*a + 2.0*a*b + b*b,1.0)", interpreter);
        compareTwoExpressions("min(a+2.0*b+c,a+b+b+c)", "a+2.0*b+c", interpreter);
        compareTwoExpressions("a/a", "1.0", interpreter);
        compareTwoExpressions("(a*a+a*b)/a", "a+b", interpreter);
        compareTwoExpressions("a/(a+b) + b/(a+b)", "1.0", interpreter);
		
	}
	
	private void compareTwoExpressions(String s1, String s2, PCTMCInterpreter interpreter){
		try {
			AbstractExpression e1 = interpreter.parseExpressionList(s1).iterator().next();
			AbstractExpression e2 = interpreter.parseExpressionList(s2).iterator().next();
			assertEquals(ExpandingExpressionTransformer.expandExpression(e1), ExpandingExpressionTransformer.expandExpression(e2));
		} catch (ParseException e) {

			e.printStackTrace();
		}
	}

}
