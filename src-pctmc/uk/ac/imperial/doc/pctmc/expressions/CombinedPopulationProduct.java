package uk.ac.imperial.doc.pctmc.expressions;

import java.util.HashMap;

import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * Representation of a combined product of populations and accumulated populations. 
 * @author Anton Stefanek
 *
 */
public class CombinedPopulationProduct{
	private PopulationProduct nakedProduct; 
	private Multiset<PopulationProduct> accumulatedProducts; 
	
	public CombinedPopulationProduct(PopulationProduct nakedProduct,
			Multiset<PopulationProduct> accumulatedProducts) {
		super();
		this.nakedProduct = nakedProduct;
		if (nakedProduct==null) this.nakedProduct = new PopulationProduct(new HashMap<State, Integer>());
		this.accumulatedProducts = accumulatedProducts;
		if (accumulatedProducts == null){
			this.accumulatedProducts = HashMultiset.<PopulationProduct>create();
		}
	}
	
	public CombinedPopulationProduct(PopulationProduct nakedProduct) {
		super();
		this.nakedProduct = nakedProduct;
		if (nakedProduct==null) nakedProduct = new PopulationProduct(new HashMap<State, Integer>());
		this.accumulatedProducts = HashMultiset.<PopulationProduct>create();
	}
	
	
	public PopulationProduct getNakedProduct() {
		return nakedProduct;
	}
	public Multiset<PopulationProduct> getAccumulatedProducts() {
		return accumulatedProducts;
	}
	@Override
	public int hashCode() {
		return nakedProduct.hashCode()*31 + accumulatedProducts.hashCode(); 
	}
	@Override
	public boolean equals(Object obj) {
		if (obj==null) return false; 
		if (!(obj instanceof CombinedPopulationProduct)) return false;
		CombinedPopulationProduct asFCM = (CombinedPopulationProduct) obj; 
		return nakedProduct.equals(asFCM.getNakedProduct()) && accumulatedProducts.equals(asFCM.getAccumulatedProducts());
	}
	
	
	/**
	 * Returns the representation of a mean population.
	 * @param s
	 * @return
	 */
	public static CombinedPopulationProduct getMeanPopulation(State s){
		PopulationProduct nakedProduct = PopulationProduct.getMeanProduct(s);
		return new CombinedPopulationProduct(nakedProduct);
	}
	
	/**
	 * Returns the representation of a mean accumulated population.
	 * @param s
	 * @return
	 */
	public static CombinedPopulationProduct getMeanAccumulatedPopulation(State s){
		PopulationProduct accProduct = PopulationProduct.getMeanProduct(s);
		Multiset<PopulationProduct> accProducts = HashMultiset.<PopulationProduct>create(); 
		accProducts.add(accProduct);
		return new CombinedPopulationProduct(null,accProducts);
	}
	
	int order=-1; 
	public int getOrder(){
		if (order!=-1) return order; 
		order = nakedProduct!=null?nakedProduct.getOrder():0;
		for (PopulationProduct product:accumulatedProducts){
			order+=product.getOrder();
		}
		return order; 
	}
	
	@Override
	public String toString() {
		String ret = "";
		if (nakedProduct!=null) ret+=nakedProduct.toString() + (accumulatedProducts.isEmpty()?"":" ");
		boolean first = true;
		for (Multiset.Entry<PopulationProduct> e : accumulatedProducts.entrySet()) {
			if (e.getCount() > 0) {
				if (first)
					first = false;
				else
					ret += " ";
				ret += "acc("+e.getElement().toString()+")";
				if (e.getCount() > 1)
					ret += "^" + e.getCount();
			}
		}
		return ret;
	}
	
	/**
	 * Returns the representation of a product of two combined products.
	 * @param a
	 * @param b
	 * @return
	 */
	public static CombinedPopulationProduct getProductOf(CombinedPopulationProduct a, CombinedPopulationProduct b){
		if (a==null) return b; 
		if (b==null) return a; 
		PopulationProduct newNakedProduct = PopulationProduct.getProduct(a.getNakedProduct(), b.getNakedProduct());
		Multiset<PopulationProduct> newAccumulatedProducts = HashMultiset.<PopulationProduct>create(); 
		for (Multiset.Entry<PopulationProduct> e:a.getAccumulatedProducts().entrySet()){
			newAccumulatedProducts.add(e.getElement(), e.getCount());
		}
		for (Multiset.Entry<PopulationProduct> e:b.getAccumulatedProducts().entrySet()){
			newAccumulatedProducts.add(e.getElement(), e.getCount());
		}
		return new CombinedPopulationProduct(newNakedProduct,newAccumulatedProducts);
	}
	
	/**
	 * Returns the representation of this product raised to a given power.
	 * @param p
	 * @return
	 */
	public CombinedPopulationProduct getPower(int p){
		PopulationProduct newNakedProduct = nakedProduct.toThePower(p);
		Multiset<PopulationProduct> newAccumulatedProducts = HashMultiset.<PopulationProduct>create(); 
		for (Multiset.Entry<PopulationProduct> e:accumulatedProducts.entrySet()){
			newAccumulatedProducts.add(e.getElement(),e.getCount()*p);
		}
		return new CombinedPopulationProduct(newNakedProduct,newAccumulatedProducts); 
	}
	
}
