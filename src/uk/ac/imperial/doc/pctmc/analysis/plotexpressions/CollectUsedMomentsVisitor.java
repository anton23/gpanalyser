package uk.ac.imperial.doc.pctmc.analysis.plotexpressions;

import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivDivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinusExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.expressions.TimeExpression;
import uk.ac.imperial.doc.jexpressions.expressions.UMinusExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ZeroExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;


/**
 * Expression visitor for collecting used moments. 
 * @author Anton Stefanek
 *
 */
public class CollectUsedMomentsVisitor implements IExpressionVisitor,
		  ICombinedProductExpressionVisitor {
	@Override
	public void visit(IntegerExpression e) {}

	@Override
	public void visit(CombinedProductExpression e) {
			usedCombinedMoments.add(e.getProduct());		
	}

	@Override
	public void visit(UMinusExpression e) {
		e.getE().accept(this); 		
	}

	@Override
	public void visit(FunctionCallExpression e) {
		for (AbstractExpression arg:e.getArguments()){
			arg.accept(this); 
		}
		
	}

	@Override
	public void visit(TimeExpression e) {
		
	}

	@Override
	public void visit(DivExpression e) {
		e.getNumerator().accept(this);
		e.getDenominator().accept(this);
	}
	
	private Set<CombinedPopulationProduct> usedCombinedMoments; 
	private Set<AbstractExpression> usedGeneralExpectations;

	public Set<AbstractExpression> getUsedGeneralExpectations() {
		return usedGeneralExpectations;
	}


	public Set<CombinedPopulationProduct> getUsedCombinedMoments() {
		return usedCombinedMoments;
	}

	public CollectUsedMomentsVisitor() {
		usedGeneralExpectations = new HashSet<AbstractExpression>(); 
		usedCombinedMoments = new HashSet<CombinedPopulationProduct>();
	}

	@Override
	public void visit(AbstractExpression e) {
		throw new AssertionError("Unsupported expression!"); 
	}



	@Override
	public void visit(DoubleExpression e) {
	}

	@Override
	public void visit(DivDivMinExpression e) {
		e.getA().accept(this);
		e.getB().accept(this);
		e.getC().accept(this);
		e.getD().accept(this);
	}

	@Override
	public void visit(PEPADivExpression e) {
		e.getNumerator().accept(this);
		e.getDenominator().accept(this);
	}

	@Override
	public void visit(DivMinExpression e) {
		e.getA().accept(this);
		e.getB().accept(this);
		e.getC().accept(this);
	}

	@Override
	public void visit(MinExpression e) {
		e.getA().accept(this);
		e.getB().accept(this);
	}

	@Override
	public void visit(MinusExpression e) {
		e.getA().accept(this);
		e.getB().accept(this);
	}

	@Override
	public void visit(PowerExpression e) {
		e.getExpression().accept(this);
		e.getExponent().accept(this);
	}

	@Override
	public void visit(ProductExpression e) {
		for (AbstractExpression t : e.getTerms()) {
			t.accept(this);
		}
	}

	@Override
	public void visit(SumExpression e) {
		for (AbstractExpression s : e.getSummands()) {
			s.accept(this);
		}

	}

	@Override
	public void visit(ZeroExpression e) {

	}

}
