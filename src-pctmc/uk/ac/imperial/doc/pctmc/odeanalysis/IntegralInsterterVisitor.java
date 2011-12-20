package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.IConstantExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivDivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ExpressionCondition;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.expressions.IndicatorFunction;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MaxExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinusExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.expressions.TimeExpression;
import uk.ac.imperial.doc.jexpressions.expressions.UMinusExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;


// TODO Generalise this for other closures
public class IntegralInsterterVisitor implements IExpressionVisitor, IConstantExpressionVisitor,
		ICombinedProductExpressionVisitor {

	protected AbstractExpression result;
	protected CombinedPopulationProduct toInsert; // always without a naked moment
	protected boolean insert;
	protected boolean foundMinimum;
	protected boolean foundMoment;

	public IntegralInsterterVisitor(CombinedPopulationProduct toInsert) {
		super();
		this.toInsert = toInsert;
		foundMinimum = false;
		foundMoment = false;
		insert = true;
	}

	@Override
	public void visit(IntegerExpression e) {
		if (insert) {
			result = ProductExpression.create(e, CombinedProductExpression
					.create(toInsert));
		}
	}

	@Override
	public void visit(CombinedProductExpression e) {
		// assumes the combined product has no accumulated products
		foundMoment = true;
		if (insert) {
			result = CombinedProductExpression
					.create(CombinedPopulationProduct.getProductOf(e.getProduct(), toInsert));			
		} else {
			result = e;
		}
	}

	@Override
	public void visit(AbstractExpression e) {}

	public AbstractExpression getResult() {
		return result;
	}

	@Override
	public void visit(DoubleExpression e) {
		if (insert) {
			result = ProductExpression.create(e, CombinedProductExpression
					.create(toInsert));
		} else {
			result = e;
		}
	}

	@Override
	public void visit(DivDivMinExpression e) {
		foundMinimum = true;
		if (insert) {
			e.getA().accept(this);
			AbstractExpression newA = result;
			e.getB().accept(this);
			AbstractExpression newB = result;
			e.getC().accept(this);
			AbstractExpression newC = result;
			e.getD().accept(this);
			AbstractExpression newD = result;

			result = ProductExpression.create(PEPADivExpression.create(
					ProductExpression.create(newA, newB), ProductExpression
							.create(newC, newD)), MinExpression.create(newC,
					newD));
		} else {
			result = e;
		}

	}

	@Override
	public void visit(DivExpression e) {
		if (insert) {
			e.getNumerator().accept(this);
			AbstractExpression newNumerator = result;
			result = DivExpression.create(newNumerator, e.getDenominator());
		} else {
			result = e;
		}
	}

	@Override
	public void visit(DivMinExpression e) {
		foundMinimum = true;
		if (insert) {
			e.getA().accept(this);
			AbstractExpression newA = result;
			e.getB().accept(this);
			AbstractExpression newB = result;
			e.getC().accept(this);
			AbstractExpression newC = result;

			result = ProductExpression.create(PEPADivExpression.create(newA,
					newB), MinExpression.create(newC, newB));
		} else {
			result = e;
		}
	}

	@Override
	public void visit(FunctionCallExpression e) {
		result = e;
	}

	@Override
	public void visit(MinExpression e) {
		foundMinimum = true;
		if (insert) {
			e.getA().accept(this);
			AbstractExpression newA = result;
			e.getB().accept(this);
			AbstractExpression newB = result;
			result = MinExpression.create(newA, newB);
		} else {
			result = e;
		}
	}

	@Override
	public void visit(MaxExpression e) {
		foundMinimum = true;
		if (insert) {
			e.getA().accept(this);
			AbstractExpression newA = result;
			e.getB().accept(this);
			AbstractExpression newB = result;
			result = MaxExpression.create(newA, newB);
		} else {
			result = e;
		}
	}
	
	@Override
	public void visit(MinusExpression e) {
		AbstractExpression a;
		e.getA().accept(this);
		a = result;
		AbstractExpression b;
		e.getB().accept(this);
		b = result;
		if (insert) {
			result = new MinusExpression(a, b);
		} else {
			result = e;
		}
		
	}

	@Override
	public void visit(PEPADivExpression e) {
		if (insert) {
			e.getNumerator().accept(this);
			AbstractExpression newNumerator = result;
			result = DivExpression.create(newNumerator, e.getDenominator());
		} else {
			e.getNumerator().accept(this);
		}
	}

	@Override
	public void visit(PowerExpression e) {
		throw new AssertionError(
				"Powers not supported in right hand sides of ODEs");
	}

	@Override
	public void visit(ProductExpression e) {
		// this is the most crucial - will
		// have to go through the terms and find one with a min
		// if not then find one with a moment
		// if not then just append the combined moment to the product

		if (!insert) {
			for (AbstractExpression term : e.getTerms()) {
				term.accept(this);
			}
		} else {
			foundMinimum = false;
			AbstractExpression termToInsert = null;
			insert = false;
			for (AbstractExpression term : e.getTerms()) {
				term.accept(this);
				if (foundMinimum) {
					termToInsert = term;
					break;
				}
			}
			if (!foundMinimum) {
				for (AbstractExpression term : e.getTerms()) {
					foundMoment = false;
					term.accept(this);
					if (foundMoment) {
						termToInsert = term;
						break;
					}
				}
			}

			insert = true;
			List<AbstractExpression> newTerms = new LinkedList<AbstractExpression>();
			for (AbstractExpression term : e.getTerms()) {
				if (term == termToInsert) {
					term.accept(this);
					newTerms.add(result);
				} else {
					newTerms.add(term);
				}
			}
			if (termToInsert == null) {
				newTerms.add(CombinedProductExpression.create(toInsert));
			}
			result = ProductExpression.create(newTerms);
		}

	}

	@Override
	public void visit(SumExpression e) {
		AbstractExpression[] ts = new AbstractExpression[e.getSummands().size()];
		int i = 0;
		for (AbstractExpression t : e.getSummands()) {
			t.accept(this);
			ts[i++] = result;
		}
		if (insert) {
			result = SumExpression.create(ts);
		} else {
			result = e;
		}
	}

	@Override
	public void visit(TimeExpression e) {
		if (insert) {
			result = ProductExpression.create(e, CombinedProductExpression
					.create(toInsert));
		}
	}

	@Override
	public void visit(UMinusExpression e) {
		e.getE().accept(this);
		if (insert) {
			result = new UMinusExpression(result);
		} else {
			result = e;
		}
	}

	@Override
	public void visit(IndicatorFunction e) {
		/*boolean oldInsert = insert;
		insert = true;
		e.getCondition().getLeft().accept(this);
		AbstractExpression newLeft = result;
		
		e.getCondition().getRight().accept(this);
		AbstractExpression newRight = result;
		insert = oldInsert;
		result = new IndicatorFunction(
				new ExpressionCondition(newLeft, e.getCondition().getOperator(), newRight));*/
		result = e;
	}
	

	@Override
	public void visit(ConstantExpression e) {
		if (insert) {
			result = ProductExpression.create(e, CombinedProductExpression
					.create(toInsert));
			foundMoment = true;
		} else {
			result = e;
		}
	}

}
