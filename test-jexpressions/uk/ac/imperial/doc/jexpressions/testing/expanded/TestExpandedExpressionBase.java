package uk.ac.imperial.doc.jexpressions.testing.expanded;

import org.junit.BeforeClass;

import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expanded.Polynomial;

public class TestExpandedExpressionBase extends TestPolynomialsBase{
	
	protected static ExpandedExpression eAoB; // a/b
	protected static ExpandedExpression eCoD; // c/d
	
    protected static ExpandedExpression eADpBCoBD; //ad + bc/bd
    protected static ExpandedExpression eADoBD; //ad/bd
    protected static ExpandedExpression eADpBDoD; //ad + bd/d
    
    protected static ExpandedExpression eCoB; // c/b
    protected static ExpandedExpression eApCoB; //(a+c)/b
	
	@BeforeClass
	public static void createObjects(){
		createPolynomialObjects();
		eAoB = new ExpandedExpression(new Polynomial(a), new Polynomial(b));
		eCoD = new ExpandedExpression(new Polynomial(c), new Polynomial(d));
		eCoB = new ExpandedExpression(new Polynomial(c), new Polynomial(b));
		eADpBCoBD = new ExpandedExpression(new Polynomial(ad, bc), new Polynomial(bd));
		eADoBD = new ExpandedExpression(new Polynomial(ad), new Polynomial(bd));
		eADpBDoD = new ExpandedExpression(new Polynomial(ad, bd), new Polynomial(d));
		eApCoB = new ExpandedExpression(new Polynomial(a, c), new Polynomial(b));
	}

}
