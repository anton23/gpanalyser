package uk.ac.imperial.doc.jexpressions.expanded;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;

public class MomentNormaliser implements ICoefficientSpecification{

	@Override
	public boolean isCoefficient(AbstractExpression e) {
/*		ContractingExpressionTransformer t = new ContractingExpressionTransformer();
		e.accept(t);
		e = t.getResult();*/
		IsDoubleOrConstantExpressionWalker w = new IsDoubleOrConstantExpressionWalker();
		e.accept(w);
		return w.isIs();
	}
	
	

	@Override
	public boolean isOne(AbstractExpression e) {
		ContractingExpressionTransformer t = new ContractingExpressionTransformer();
		e.accept(t);
		e = t.getResult();
		return e.equals(DoubleExpression.ONE);
	}

	


	@Override
	public boolean isZero(AbstractExpression e) {
		ContractingExpressionTransformer t = new ContractingExpressionTransformer();
		e.accept(t);
		e = t.getResult();
		return e.equals(DoubleExpression.ZERO);
	}



	@Override
	public AbstractExpression normaliseCoefficient(AbstractExpression e) {
		ExpandingExpressionTransformer t = new ExpandingExpressionTransformer(new DoubleNormaliser());
		e.accept(t);
		return t.getResult();
	}
	
	

}
