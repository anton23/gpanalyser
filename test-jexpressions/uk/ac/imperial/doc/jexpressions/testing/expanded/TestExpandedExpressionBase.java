package uk.ac.imperial.doc.jexpressions.testing.expanded;

import org.junit.BeforeClass;

import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expanded.Polynomial;

public class TestExpandedExpressionBase extends TestPolynomialsBase{
	
	protected static ExpandedExpression e1; // a/b
	protected static ExpandedExpression e2; // c/d
	
	@BeforeClass
	public static void createObjects(){
		createPolynomialObjects();
		e1 = new ExpandedExpression(new Polynomial(a), new Polynomial(b));
		e2 = new ExpandedExpression(new Polynomial(c), new Polynomial(d));
	}

}
