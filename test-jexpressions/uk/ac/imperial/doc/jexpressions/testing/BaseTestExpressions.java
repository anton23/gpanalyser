package uk.ac.imperial.doc.jexpressions.testing;

import org.junit.BeforeClass;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;

public class BaseTestExpressions {
	@BeforeClass
	public static void createObjects(){
		d1 = new DoubleExpression(1.0);
		d2 = new DoubleExpression(2.0);
		d3 = new DoubleExpression(3.0);
		d0 = new DoubleExpression(0.0);
		
		s123 = SumExpression.create(d1,d2,d3);
		p223 = ProductExpression.create(d2,d2,d3);
		
		min23 = MinExpression.create(d2, d3);
		div23 = DivExpression.create(d2, d3);
		pdiv23 = PEPADivExpression.create(d2, d3);
		pdiv20 = PEPADivExpression.create(d2, d0);
	}
	
	protected static AbstractExpression d1, d2, d3, d0;
	
	protected static AbstractExpression s123, p223, min23, div23, pdiv23, pdiv20;
}
