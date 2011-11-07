package uk.ac.imperial.doc.jexpressions.testing.expanded;

import org.junit.BeforeClass;

import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expanded.DoubleNormaliser;
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
		eAoB = ExpandedExpression.create(new Polynomial(new DoubleNormaliser(), a), new Polynomial(new DoubleNormaliser(), b));
		eCoD = ExpandedExpression.create(new Polynomial(new DoubleNormaliser(), c), new Polynomial(new DoubleNormaliser(), d));
		eCoB = ExpandedExpression.create(new Polynomial(new DoubleNormaliser(), c), new Polynomial(new DoubleNormaliser(), b));
		eADpBCoBD = ExpandedExpression.create(new Polynomial(new DoubleNormaliser(), ad, bc), new Polynomial(new DoubleNormaliser(), bd));
		eADoBD = ExpandedExpression.create(new Polynomial(new DoubleNormaliser(), ad), new Polynomial(new DoubleNormaliser(), bd));
		eADpBDoD = ExpandedExpression.create(new Polynomial(new DoubleNormaliser(), ad, bd), new Polynomial(new DoubleNormaliser(), d));
		eApCoB = ExpandedExpression.create(new Polynomial(new DoubleNormaliser(), a, c), new Polynomial(new DoubleNormaliser(), b));
	}

}
