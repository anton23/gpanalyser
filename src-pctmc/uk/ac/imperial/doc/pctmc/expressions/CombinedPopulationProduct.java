package uk.ac.imperial.doc.pctmc.expressions;

import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.representation.accumulations.AccumulatedProduct;
import uk.ac.imperial.doc.pctmc.representation.accumulations.AccumulationVariable;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * Representation of a combined product of populations and accumulated
 * populations.
 * 
 * @author Anton Stefanek
 * 
 */
public class CombinedPopulationProduct {
	private PopulationProduct populationProduct;
	private Multiset<AccumulationVariable> accumulatedProducts;

	public CombinedPopulationProduct(PopulationProduct populationProduct,
			Multiset<AccumulationVariable> accumulatedProducts) {
		super();
		this.populationProduct = populationProduct;
		if (populationProduct == null)
			this.populationProduct = new PopulationProduct(
					HashMultiset.<State>create());
		this.accumulatedProducts = accumulatedProducts;
		if (accumulatedProducts == null) {
			this.accumulatedProducts = HashMultiset
					.<AccumulationVariable> create();
		}
	}

	public CombinedPopulationProduct(PopulationProduct populationProduct) {
		super();
		this.populationProduct = populationProduct;
		if (populationProduct == null)
			populationProduct = new PopulationProduct(HashMultiset.<State>create());
		this.accumulatedProducts = HashMultiset.<AccumulationVariable> create();
	}
	
	public static CombinedPopulationProduct getConstantProduct() {
		return new CombinedPopulationProduct(new PopulationProduct(HashMultiset.<State>create()));
	}

	/**
	 * Returns the representation of a mean population.
	 * 
	 * @param s
	 * @return
	 */
	public static CombinedPopulationProduct getMeanPopulation(State s) {
		PopulationProduct nakedProduct = PopulationProduct.getMeanProduct(s);
		return new CombinedPopulationProduct(nakedProduct);
	}

	/**
	 * Returns the representation of a mean accumulated population.
	 * 
	 * @param s
	 * @return
	 */
	public static CombinedPopulationProduct getMeanAccumulatedPopulation(State s) {
		PopulationProduct accProduct = PopulationProduct.getMeanProduct(s);
		Multiset<AccumulationVariable> accProducts = HashMultiset
				.<AccumulationVariable> create();
		accProducts.add(new AccumulatedProduct(accProduct));
		return new CombinedPopulationProduct(null, accProducts);
	}
	
	public static CombinedPopulationProduct getMeanAccumulatedProduct(AccumulationVariable p) {
		Multiset<AccumulationVariable> accProducts = HashMultiset
				.<AccumulationVariable> create();
		accProducts.add(p);
		return new CombinedPopulationProduct(null, accProducts);
	}

	int order = -1;

	public int getOrder() {
		if (order != -1)
			return order;
		order = populationProduct != null ? populationProduct.getOrder() : 0;
		for (AccumulationVariable product : accumulatedProducts) {
			order += product.getOrder();
		}
		return order;
	}

	/**
	 * Returns the representation of a product of two combined products.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static CombinedPopulationProduct getProductOf(
			CombinedPopulationProduct a, CombinedPopulationProduct b) {
		if (a == null)
			return b;
		if (b == null)
			return a;
		PopulationProduct newNakedProduct = PopulationProduct.getProduct(a
				.getPopulationProduct(), b.getPopulationProduct());
		Multiset<AccumulationVariable> newAccumulatedProducts = HashMultiset
				.<AccumulationVariable> create();
		for (Multiset.Entry<AccumulationVariable> e : a.getAccumulatedProducts()
				.entrySet()) {
			newAccumulatedProducts.add(e.getElement(), e.getCount());
		}
		for (Multiset.Entry<AccumulationVariable> e : b.getAccumulatedProducts()
				.entrySet()) {
			newAccumulatedProducts.add(e.getElement(), e.getCount());
		}
		return new CombinedPopulationProduct(newNakedProduct,
				newAccumulatedProducts);
	}

	/**
	 * Returns the representation of this product raised to a given power.
	 * 
	 * @param p
	 * @return
	 */
	public CombinedPopulationProduct getPower(int p) {
		PopulationProduct newNakedProduct = populationProduct.toThePower(p);
		Multiset<AccumulationVariable> newAccumulatedProducts = HashMultiset
				.<AccumulationVariable> create();
		for (Multiset.Entry<AccumulationVariable> e : accumulatedProducts
				.entrySet()) {
			newAccumulatedProducts.add(e.getElement(), e.getCount() * p);
		}
		return new CombinedPopulationProduct(newNakedProduct,
				newAccumulatedProducts);
	}

	@Override
	public int hashCode() {
		return populationProduct.hashCode() * 31 + accumulatedProducts.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof CombinedPopulationProduct))
			return false;
		CombinedPopulationProduct asFCM = (CombinedPopulationProduct) obj;
		return populationProduct.equals(asFCM.getPopulationProduct())
				&& accumulatedProducts.equals(asFCM.getAccumulatedProducts());
	}

	@Override
	public String toString() {
		String ret = "";
		if (populationProduct != null)
			ret += populationProduct.toString()
					+ (accumulatedProducts.isEmpty() ? "" : " ");
		boolean first = true;
		for (Multiset.Entry<AccumulationVariable> e : accumulatedProducts
				.entrySet()) {
			if (e.getCount() > 0) {
				if (first)
					first = false;
				else
					ret += " ";
				ret += e.getElement().toString();
				if (e.getCount() > 1)
					ret += "^" + e.getCount();
			}
		}
		return ret;
	}

	public PopulationProduct getPopulationProduct() {
		return populationProduct;
	}

	public Multiset<AccumulationVariable> getAccumulatedProducts() {
		return accumulatedProducts;
	}

}
