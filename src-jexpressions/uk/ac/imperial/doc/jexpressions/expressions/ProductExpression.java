package uk.ac.imperial.doc.jexpressions.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

/**
 * An expression for a product of a number of terms a1*a2*...*an.
 * 
 * @author as1005
 * 
 */
public class ProductExpression extends AbstractExpression {

	public static ProductExpression forceProduct(AbstractExpression e) {
		if (e instanceof ProductExpression) {
			return (ProductExpression) e;
		} else {
			return new ProductExpression(Lists.newArrayList(e));
		}
	}
			
	
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
			ret += "(" + e.toString() + ")";
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
	
	public static AbstractExpression createOrdered(Collection<AbstractExpression> t) {
		Map<String,AbstractExpression> orderedProduct = new TreeMap<String,AbstractExpression>();
		Map<String,Integer> orderedProductMult = new TreeMap<String,Integer>();
		for (AbstractExpression ae : t)
		{
			String name = ae.toString();
			orderedProduct.put(name, ae);
			Integer mult = orderedProductMult.get(name);
			mult = (mult == null) ? 1 : ++mult;
			orderedProductMult.put(name,mult);
		}
		int i=0;
		AbstractExpression[] product = new AbstractExpression[t.size()];
		for (Entry<String, Integer> e : orderedProductMult.entrySet())
		{
			for (int j = e.getValue(); j > 0; --j)
			{
				product[i++] = orderedProduct.get(e.getKey());
			}
		}
		return create(product);
	}

	/**
	 * Creates and possibly simplifies a ProductExpression.
	 * 
	 * @param t
	 *            - a list of terms in the product.
	 * @return
	 */
	public static AbstractExpression create(AbstractExpression... t) {
		for (AbstractExpression e : t) {
			if (e.equals(DoubleExpression.ZERO)) {
				return DoubleExpression.ZERO;
			}
		}
		double numericalTerms = 1.0;
		List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
		for (AbstractExpression e : t) {
			if (e instanceof ProductExpression) {
				terms.addAll(((ProductExpression) e).getTerms());
			} else if (e instanceof DoubleExpression) {
				numericalTerms *= ((DoubleExpression) e).getValue();
			} else {
				terms.add(e);
			}
		}
		if (numericalTerms != 1.0) {
			terms.add(new DoubleExpression(numericalTerms));
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
