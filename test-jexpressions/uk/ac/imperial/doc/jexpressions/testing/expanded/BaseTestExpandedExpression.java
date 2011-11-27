package uk.ac.imperial.doc.jexpressions.testing.expanded;

import org.junit.BeforeClass;

import uk.ac.imperial.doc.jexpressions.expanded.DoubleCoefficients;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expanded.Polynomial;

public class BaseTestExpandedExpression extends BaseTestPolynomials{
	
	protected static ExpandedExpression eAoB; // a/b
	protected static ExpandedExpression eCoD; // c/d
	
    protected static ExpandedExpression eADpBCoBD; //ad + bc/bd
    protected static ExpandedExpression eADoBD; //ad/bd
    protected static ExpandedExpression eADpBDoD; //ad + bd/d
    
    protected static ExpandedExpression eCoB; // c/b
    protected static ExpandedExpression eApCoB; //(a+c)/b
	
	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void createObjects(){
		createPolynomialObjects();
		Polynomial pA = new Polynomial(new DoubleCoefficients(), a);
		Polynomial pB = new Polynomial(new DoubleCoefficients(), b);
		eAoB = ExpandedExpression.create(pA, pB);
		eCoD = ExpandedExpression.create(new Polynomial(new DoubleCoefficients(), c), new Polynomial(new DoubleCoefficients(), d));
		eCoB = ExpandedExpression.create(new Polynomial(new DoubleCoefficients(), c), pB);
		eADpBCoBD = ExpandedExpression.create(new Polynomial(new DoubleCoefficients(), ad, bc), new Polynomial(new DoubleCoefficients(), bd));
		eADoBD = ExpandedExpression.create(new Polynomial(new DoubleCoefficients(), ad), new Polynomial(new DoubleCoefficients(), bd));
		eADpBDoD = ExpandedExpression.create(new Polynomial(new DoubleCoefficients(), ad, bd), new Polynomial(new DoubleCoefficients(), d));
		eApCoB = ExpandedExpression.create(new Polynomial(new DoubleCoefficients(), a, c), pB);
	}

}
