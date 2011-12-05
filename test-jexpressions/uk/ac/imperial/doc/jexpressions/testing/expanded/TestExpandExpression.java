package uk.ac.imperial.doc.jexpressions.testing.expanded;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.expanded.DoubleCoefficients;
import uk.ac.imperial.doc.jexpressions.expanded.DoubleConstantCoefficients;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandingExpressionTransformer;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandingExpressionTransformerWithMoments;
import uk.ac.imperial.doc.jexpressions.expanded.Polynomial;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;

@SuppressWarnings("unchecked")
public class TestExpandExpression extends BaseTestExpandedExpression {

	@Test
	public void testExpandSimple() throws ParseException {
		assertEquals(ExpandedExpression.create(new Polynomial(
				new DoubleCoefficients(), a, b)),
				ExpandingExpressionTransformer
						.expandExpressionWithDoubles(SumExpression.create(
								new ConstantExpression("a"),
								new ConstantExpression("b"))));
		PCTMCInterpreter interpreter = GPAPMain.createGPEPAInterpreter();
		try {
			AbstractExpression e1 = interpreter.parseExpressionList(
					"(a+b)*(a+b)").iterator().next();
			ExpandedExpression expandExpression = ExpandingExpressionTransformer
					.expandExpressionWithDoubles(e1);
			assertEquals(ExpandedExpression.create(pA2p2ABpB2),
					expandExpression);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertEqualSimpleExpressions("3.0", "1.0+1.0*2.0", interpreter);
		assertEqualSimpleExpressions("min((a+b)*(a+b),1.0)",
				"min(a*a + 2.0*a*b + b*b,1.0)", interpreter);
		assertEqualSimpleExpressions("min(a+2.0*b+c,a+b+b+c)", "a+2.0*b+c",
				interpreter);
		assertEqualSimpleExpressions("a/a", "1.0", interpreter);
		assertEqualSimpleExpressions("(a*a+a*b)/a", "a+b", interpreter);
		assertEqualSimpleExpressions("a/(a+b) + b/(a+b)", "1.0", interpreter);
		assertEqualSimpleExpressions("1.0", "1.0", interpreter);
		assertEqualSimpleExpressions("2.0/2.0", "1.0", interpreter);
		assertEqualSimpleExpressions("a*b/b", "a", interpreter);
		assertEqualSimpleExpressions("a/b + 1.0", "(a+b)/b", interpreter);
		assertEqualSimpleExpressions("(a+b)*(a+b)*(a+b)",
				"a*a*a + 3*a*a*b + 3*a*b*b + b*b*b", interpreter);
		assertEqualSimpleExpressions("6.0/24.0", "1.0/4.0", interpreter);
		assertEqualSimpleExpressions("(6.0*a + 3.0*b)/24.0",
				"(2.0*a+1.0*b)/2.0 * (1.0/4.0)", interpreter);
		assertEqualSimpleExpressions("-1.0", "0.0-1.0", interpreter);
		assertEqualSimpleExpressions("0.0", "a-a", interpreter);
		assertEqualSimpleExpressions("0.0", "0.0/a", interpreter);
		assertEqualSimpleExpressions("0.0", "0.0*(a+b)", interpreter);
		assertEqualSimpleExpressions("a", "a+0.0", interpreter);
		assertEqualSimpleExpressions("(-1.0)*(-1.0)", "1.0", interpreter);
		assertEqualSimpleExpressions("(-2.0)*(-3.0)", "6.0", interpreter);
		assertEqualSimpleExpressions("0.0", "min(0.0, 1.0)", interpreter);	
		assertEqualSimpleExpressions("(a-b)/(a+b)", "(a*a-b*b)/(a*a + 2*a*b + b*b)", interpreter);
		assertEqualSimpleExpressions("a/b", "(a*c)/(b*c)", interpreter);
		assertEqualSimpleExpressions("1.0", "a/(a+b+c+d) + b/(a+b+c+d) + c/(a+b+c+d) + d/(a+b+c+d)", interpreter);
		
		
		assertNotEqualSimpleExpressions("-1.0", "1.0", interpreter);
		assertNotEqualSimpleExpressions("0.0", "1.0", interpreter);
		assertNotEqualSimpleExpressions("2.0", "1.0", interpreter);
		assertNotEqualSimpleExpressions("a*b", "a*b*b", interpreter);
		assertNotEqualSimpleExpressions("a*b*c", "a*b*b", interpreter);
		assertNotEqualSimpleExpressions("a/b", "a/(2.0*b)", interpreter);


		
		// The following do not work 
/*		assertEqualSimpleExpressions("(c*d+e)/(g*h+k)", 
				"(a*b*c*d + a*b*e+c*c*d+e*c+e*c*d+e*e)/(a*b*g*h+a*b*k+c*g*h+e*g*h+c*k+e*k)", interpreter);
		assertEqualSimpleExpressions("(d+e+f)/(a+c)",
				"((a+b+c)*(d+e+f))/((a+b+c)*(a+c))", interpreter);*/				

	}

	@Test
	public void testExpandWithMoments()  throws ParseException {
		PCTMCInterpreter interpreter = GPAPMain.createGPEPAInterpreter();
		assertEqualMomentExpressions("1.0*Cs:C", "Cs:C",
				interpreter);
		assertEqualMomentExpressions("2.0*Cs:C",
				"(Cs:C)+(Cs:C)", interpreter);
		assertEqualMomentExpressions("1.0",
				"(Cs:C)/(Cs:C)", interpreter);
		assertEqualMomentExpressions("a",
				"(a*a*(Cs:C))/(a*(Cs:C))", interpreter);
		assertEqualMomentExpressions("2*a*Cs:C",
				"a*(Cs:C)+a*(Cs:C)", interpreter);
		assertEqualMomentExpressions("((a+b)/d)*Cs:C",
				"(a/d)*(Cs:C)+(b/d)*(Cs:C)", interpreter);
		assertEqualMomentExpressions("(a+b)*(a+b)*Cs:C",
				"(a*a+2.0*a*b+b*b)*Cs:C", interpreter);
		assertEqualMomentExpressions("(((a+b)*Cs:C) + ((b+d)*Cs:D))*(((a+b)*Cs:C) + ((b+d)*Cs:D))",
				"a*a*Cs:C*Cs:C + 2.0*a*b*Cs:C*Cs:C + b*b*Cs:C*Cs:C + 2.0*(a+b)*Cs:C*(b+d)*Cs:D + (b+d)*(b+d)*Cs:D*Cs:D",
				interpreter);
		assertEqualMomentExpressions("((A:A)*ra/((A:A)*ra+(B:B)*rb))*min((A:A)*ra, (B:B)*rb)+((B:B)*rb/((A:A)*ra+(B:B)*rb))*min((A:A)*ra, (B:B)*rb)",
				"min((A:A)*ra, (B:B)*rb)",
				interpreter);
		assertEqualMomentExpressions("(A:A)*ra/((A:A)*ra+(B:B)*rb)*(C:C) + ((B:B)*rb/((A:A)*ra+(B:B)*rb))*(C:C)", "C:C",
				interpreter);

	}

	private void assertEqualMomentExpressions(String s1, String s2,
			PCTMCInterpreter interpreter) throws ParseException  {
			AbstractExpression e1 = interpreter.parseExpressionList(s1)
					.iterator().next();
			AbstractExpression e2 = interpreter.parseExpressionList(s2)
					.iterator().next();
			ExpandingExpressionTransformerWithMoments t = new ExpandingExpressionTransformerWithMoments(new DoubleConstantCoefficients());
			e1.accept(t);
			e1 = t.getResult();
			e2.accept(t);
			e2 = t.getResult();
			assertEquals(e1, e2);
	}
	

	private void assertEqualSimpleExpressions(String s1, String s2,
			PCTMCInterpreter interpreter) throws ParseException  {
			AbstractExpression e1 = interpreter.parseExpressionList(s1)
					.iterator().next();
			AbstractExpression e2 = interpreter.parseExpressionList(s2)
					.iterator().next();
			ExpandedExpression expE1 = ExpandingExpressionTransformer
					.expandExpressionWithDoubles(e1);
			ExpandedExpression expE2 = ExpandingExpressionTransformer
					.expandExpressionWithDoubles(e2);
			assertEquals(expE1, expE2);
	}

	private void assertNotEqualSimpleExpressions(String s1, String s2,
			PCTMCInterpreter interpreter) throws ParseException  {
			AbstractExpression e1 = interpreter.parseExpressionList(s1)
					.iterator().next();
			AbstractExpression e2 = interpreter.parseExpressionList(s2)
					.iterator().next();
			assertThat(ExpandingExpressionTransformer
					.expandExpressionWithDoubles(e1),
					not(equalTo(ExpandingExpressionTransformer
							.expandExpressionWithDoubles(e2))));
	}
}