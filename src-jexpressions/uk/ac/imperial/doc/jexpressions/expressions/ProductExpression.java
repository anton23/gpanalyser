package uk.ac.imperial.doc.jexpressions.expressions;


import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * An expression for a product of a number of terms a1*a2*...*an. 
 * @author as1005
 *
 */
public class ProductExpression extends AbstractExpression {

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this);
	}


	@Override
	public String toString() {
		String ret = "";
		boolean first = true;
		for (AbstractExpression e : terms) {
			if (first) {
				first = false;
			} else {
				ret += "*";
			}
			ret += "("+e.toString()+")";
		}
		return ret;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ProductExpression))
			return false;
		ProductExpression asProduct = (ProductExpression) o;
		return terms.equals(asProduct.terms);
	}

	@Override
	public int hashCode() {
		return terms.hashCode() + 23;
	}

	private List<AbstractExpression> terms;

	public List<AbstractExpression> getTerms() {
		return terms;
	}

	protected ProductExpression(Collection<AbstractExpression> t) {
		terms = new ArrayList<AbstractExpression>(t);

	}

	public static AbstractExpression create(Collection<AbstractExpression> t) {
		return create(t.toArray(new AbstractExpression[0]));
	}

	/**
	 * Creates and possibly simplifies a ProductExpression. 
	 * @param t - a list of terms in the product.
	 * @return
	 */
	public static AbstractExpression create(AbstractExpression... t) {
		for (AbstractExpression e : t) {
			if (e instanceof ZeroExpression) {
				return new ZeroExpression();
			}
		}
		List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
		for (AbstractExpression e : t) {
			if (e instanceof ProductExpression){
				terms.addAll(((ProductExpression)e).getTerms()); 
			} else
			if (!((e instanceof DoubleExpression) && ((DoubleExpression) e)
					.getValue() == 1.0)) {
				terms.add(e);
			}
		}
		if (terms.isEmpty()) {
			return new DoubleExpression(1.0);
		}
		if (terms.size() == 1) {
			return terms.get(0);
		}
		return new ProductExpression(terms);
	}

}
