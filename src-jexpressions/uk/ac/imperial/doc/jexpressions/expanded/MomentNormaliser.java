package uk.ac.imperial.doc.jexpressions.expanded;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

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
	public AbstractExpression normaliseCoefficient(AbstractExpression e) {
		ExpandingExpressionTransformer t = new ExpandingExpressionTransformer(new DoubleNormaliser());
		e.accept(t);
		return t.getResult();
	}
	
	

}
