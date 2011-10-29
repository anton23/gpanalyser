package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.statements.AbstractStatement;
import uk.ac.imperial.doc.jexpressions.statements.Assignment;
import uk.ac.imperial.doc.jexpressions.statements.Increment;
import uk.ac.imperial.doc.jexpressions.statements.SkipStatement;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CollectUsedMomentsVisitor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;
import uk.ac.imperial.doc.pctmc.utils.Binomial;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

public class ODEGenerator {
	
	protected Map<CombinedPopulationProduct, AbstractExpression> rhs;
	protected State[] states;
	protected BiMap<State, Integer> stateIndex;
	protected List<CombinedPopulationProduct>[][] memMoments;
	protected PCTMC pctmc;
	protected BiMap<CombinedPopulationProduct, Integer> momentIndex;
	
	public ODEGenerator(PCTMC pctmc) {
		super();
		this.stateIndex = pctmc.getStateIndex();
		states = new State[stateIndex.size()];
		for (Map.Entry<State, Integer> e : stateIndex.entrySet()) {
			states[e.getValue()] = e.getKey();
		}
		this.pctmc = pctmc;
	}

	public BiMap<CombinedPopulationProduct, Integer> getMomentIndex() {
		return momentIndex;
	}

	public AbstractExpression getRHS(CombinedPopulationProduct p) {
		return rhs.get(p);
	}

	private AbstractExpression getDerivative(
			CombinedPopulationProduct combinedProduct) {

		List<AbstractExpression> sum = new LinkedList<AbstractExpression>();

		for (Multiset.Entry<PopulationProduct> accumulatedMoment : combinedProduct
				.getAccumulatedProducts().entrySet()) {
			DoubleExpression coefficient = new DoubleExpression(
					(double) accumulatedMoment.getCount());
			Multiset<PopulationProduct> newAccumulatedMoments = HashMultiset
					.<PopulationProduct> create();
			for (Multiset.Entry<PopulationProduct> e : combinedProduct
					.getAccumulatedProducts().entrySet()) {
				newAccumulatedMoments.add(e.getElement(), e.getCount());
			}
			newAccumulatedMoments.remove(accumulatedMoment.getElement(), 1);
			PopulationProduct newNakedMoment = PopulationProduct.getProduct(
					combinedProduct.getNakedProduct(), accumulatedMoment
							.getElement());
			CombinedPopulationProduct tmp = new CombinedPopulationProduct(
					newNakedMoment, newAccumulatedMoments);
			AbstractExpression diff = ProductExpression.create(coefficient,
					CombinedProductExpression.create(tmp));
			sum.add(diff);
		}
		if (combinedProduct.getNakedProduct().getOrder() > 0) {
			AbstractExpression diffNakedMoment = getRHS(new CombinedPopulationProduct(
					combinedProduct.getNakedProduct()));
			// need to insert the accumulated moments into the RHS
			IntegralInsterterVisitor visitor = new IntegralInsterterVisitor(
					new CombinedPopulationProduct(null, combinedProduct
							.getAccumulatedProducts()));
			diffNakedMoment.accept(visitor);
			sum.add(visitor.getResult());
		}
		return SumExpression.create(sum);
	}

	// also calculates the combined moment index
	public ODEMethod getODEMethodWithCombinedMoments(int order,
			Collection<CombinedPopulationProduct> combinedMoments) {

		List<AbstractStatement> lines = getODEMethodBody(order);
		int nmoments = momentIndex.size();
		if (combinedMoments != null) {
			Set<CombinedPopulationProduct> seen = new HashSet<CombinedPopulationProduct>();
			Queue<CombinedPopulationProduct> neededMoments = new LinkedList<CombinedPopulationProduct>();
			for (CombinedPopulationProduct c : combinedMoments) {
				neededMoments.add(c);
				seen.add(c);
			}

			while (!neededMoments.isEmpty()) {
				CombinedPopulationProduct cm = neededMoments.remove();
				if (!cm.getAccumulatedProducts().isEmpty()) {
					AbstractExpression derivative = getDerivative(cm);
					lines.add(new Increment(CombinedProductExpression
							.create(cm), derivative));
					rhs.put(cm, derivative);
					// need to add all the new combined moments
					// GetCombinedProductsVisitor visitor = new
					// GetCombinedProductsVisitor();
					CollectUsedMomentsVisitor visitor = new CollectUsedMomentsVisitor();
					derivative.accept(visitor);
					for (CombinedPopulationProduct usedMoment : visitor
							.getUsedCombinedMoments()) {
						if (!seen.contains(usedMoment)
								&& !usedMoment.getAccumulatedProducts()
										.isEmpty()) {
							seen.add(usedMoment);
							neededMoments.add(usedMoment);
						}
					}
				}
			}

			int i = momentIndex.size();

			for (CombinedPopulationProduct c : seen) {
				if (!momentIndex.containsKey(c))
					momentIndex.put(c, i++);
			}

		}
		AbstractStatement[] ret = lines.toArray(new AbstractStatement[0]);
		PCTMCLogging.info("Number of combined moments: "
				+ (momentIndex.size() - nmoments));
		return new ODEMethod(ret);
	}

	@SuppressWarnings("unchecked")
	public BiMap<CombinedPopulationProduct, Integer> calculateProductIndex(
			int maxOrder) {
		int n = states.length;
		memMoments = (List<CombinedPopulationProduct>[][]) new List[maxOrder + 1][n];
		int i = 0;
		BiMap<CombinedPopulationProduct, Integer> ret = HashBiMap
				.<CombinedPopulationProduct, Integer> create();
		for (int o = 1; o <= maxOrder; o++) {
			List<CombinedPopulationProduct> products = getMoments(o, n);
			for (CombinedPopulationProduct p : products) {
				ret.put(p, i++);
			}
		}
		return ret;
	}

	protected List<CombinedPopulationProduct> getMoments(int order,
			int components) {
		if (memMoments[order][components - 1] != null) {
			return memMoments[order][components - 1];
		}
		List<CombinedPopulationProduct> ret = new LinkedList<CombinedPopulationProduct>();
		if (components == 1) {
			Map<State, Integer> map = new HashMap<State, Integer>();
			map.put(states[0], order);
			CombinedPopulationProduct product = new CombinedPopulationProduct(
					new PopulationProduct(map));
			ret.add(product);
		} else if (order == 0) {
			CombinedPopulationProduct product = new CombinedPopulationProduct(
					new PopulationProduct(new HashMap<State, Integer>()));
			ret.add(product);
		} else {
			for (int i = 0; i <= order; i++) {
				List<CombinedPopulationProduct> tmp = getMoments(order - i,
						components - 1);
				for (CombinedPopulationProduct p : tmp) {
					Map<State, Integer> map = new HashMap<State, Integer>(p
							.getNakedProduct().getProduct());
					map.put(states[components - 1], i);
					ret.add(new CombinedPopulationProduct(
							new PopulationProduct(map)));
				}
			}
		}
		memMoments[order][components - 1] = ret;
		return ret;
	}

	protected int maxCounter;

	protected List<AbstractStatement> getODEMethodBody(int order) {
		List<AbstractStatement> lines = new LinkedList<AbstractStatement>();
		rhs = new HashMap<CombinedPopulationProduct, AbstractExpression>();

		Multimap<PopulationProduct, AbstractExpression> incrementMap = LinkedListMultimap
				.<PopulationProduct, AbstractExpression> create();

		momentIndex = calculateProductIndex(order);
		PCTMCLogging.info("Number of moments: " + momentIndex.size());

		Set<CombinedPopulationProduct> allMoments = momentIndex.keySet();
		Set<CombinedPopulationProduct> possibleKsTmp = new HashSet<CombinedPopulationProduct>();
		for (CombinedPopulationProduct e : allMoments) {
			if (e.getOrder() < order) {
				possibleKsTmp.add(e);
			}
		}
		possibleKsTmp.add(new CombinedPopulationProduct(new PopulationProduct(
				new HashMap<State, Integer>())));
		List<Map<State, Integer>> possibleKs = new ArrayList<Map<State, Integer>>(
				possibleKsTmp.size());

		for (CombinedPopulationProduct k : possibleKsTmp) {
			Map<State, Integer> newK = new HashMap<State, Integer>();
			for (int i = 0; i < states.length; i++) {
				newK.put(states[i], k.getNakedProduct().getPowerOf(states[i]));
			}
			possibleKs.add(newK);
		}

		maxCounter = 0;
		int products = 0;

		Collection<EvolutionEvent> evolutionEvents = pctmc.getEvolutionEvents();
		for (EvolutionEvent e : evolutionEvents) {

			AbstractExpression jointRateFunction = e.getRate();
			List<State> jminus = e.getDecreasing();
			List<State> jplus = e.getIncreasing();

			Multiset<State> jminusMset = HashMultiset.<State> create(jminus);
			Multiset<State> jplusMset = HashMultiset.<State> create(jplus);
			for (State s : jminus) {
				jplusMset.remove(s);
			}
			for (State s : jplus) {
				jminusMset.remove(s);
			}
			// the new jminus/jplus multi sets are disjoint now
			List<State> jminusNew = new LinkedList<State>(jminusMset);
			List<State> jplusNew = new LinkedList<State>(jplusMset);

			for (Map<State, Integer> k : possibleKs) {

				List<PopulationProduct> ms = getM(jminusNew, jplusNew,
						new PopulationProduct(k), order);
				GetVVersionVisitor visitor = new GetVVersionVisitorMomentClosure(
						new PopulationProduct(k), order);
				jointRateFunction.accept(visitor);
				AbstractExpression jointRate = visitor.getResult();

				for (PopulationProduct moment : ms) {

					Integer binom = 1;
					for (State b : jminusMset.elementSet()) {
						binom *= Binomial
								.choose(moment.getPowerOf(b), k.get(b));
						binom *= (int) Math.pow(-jminusMset.count(b), moment
								.getPowerOf(b)
								- k.get(b));
					}
					for (State b : jplusMset.elementSet()) {
						binom *= Binomial
								.choose(moment.getPowerOf(b), k.get(b));
						binom *= (int) Math.pow(jplusMset.count(b), moment
								.getPowerOf(b)
								- k.get(b));
					}

					AbstractExpression binomExpression = new DoubleExpression(
							(double) binom);
					AbstractExpression term = ProductExpression.create(
							binomExpression, jointRate);
					/*
					 * Increment statement = new Increment(
					 * CombinedProductExpression.create(new
					 * CombinedPopulationProduct(moment)),term);
					 * lines.add(statement);
					 */

					incrementMap.put(moment, term);

					products++;
				}

			}

		}
		for (PopulationProduct p : incrementMap.keySet()) {
			AbstractExpression pRhs = SumExpression.create(incrementMap.get(p));
			rhs.put(new CombinedPopulationProduct(p), pRhs);
			Assignment statement = new Assignment(CombinedProductExpression
					.create(new CombinedPopulationProduct(p)), pRhs);
			lines.add(statement);
		}

		PCTMCLogging.info("Number of products in RHS: " + products);

		if (lines.isEmpty())
			lines.add(new SkipStatement());

		return lines;
	}

	protected List<PopulationProduct> getM(List<State> jminus,
			List<State> jplus, PopulationProduct k, int maxOrder) {
		List<Integer> jminusNum = new LinkedList<Integer>();
		List<Integer> jplusNum = new LinkedList<Integer>();
		int[] kNum = new int[states.length];
		for (State p : jminus) {
			jminusNum.add(stateIndex.get(p));
		}
		for (State p : jplus) {
			jplusNum.add(stateIndex.get(p));
		}
		for (Map.Entry<State, Integer> e : k.getProduct().entrySet()) {
			kNum[stateIndex.get(e.getKey())] = e.getValue();
		}
		List<int[]> flatMs = getM(jminusNum, jplusNum, kNum, maxOrder);
		List<PopulationProduct> ret = new LinkedList<PopulationProduct>();
		for (int[] flatM : flatMs) {
			Map<State, Integer> m = new HashMap<State, Integer>();
			for (int i = 0; i < flatM.length; i++) {
				m.put(states[i], flatM[i]);
			}
			ret.add(new PopulationProduct(m));
		}
		return ret;
	}

	protected List<int[]> getM(List<Integer> jminus, List<Integer> jplus,
			int[] k, int maxOrder) {
		List<Integer> intersection = new LinkedList<Integer>(jminus);
		intersection.retainAll(jplus);

		List<Integer> j = new LinkedList<Integer>(jminus);
		j.addAll(jplus);
		j.removeAll(intersection);
		if (j.isEmpty()) {
			return new LinkedList<int[]>();
		}

		int components = k.length;
		int order = 0;
		for (int i = 0; i < components; i++)
			order += k[i];

		List<int[]> ret = new LinkedList<int[]>();
		for (int i = 1; i <= maxOrder - order; i++) {
			ret.addAll(getMrec(j, components, i, k));
		}

		return ret;
	}

	protected List<int[]> getMrec(List<Integer> j, int components, int toAdd,
			int[] k) {
		List<int[]> moments = new LinkedList<int[]>();
		if (toAdd == 0) {
			moments.add(Arrays.copyOf(k, k.length));
			return moments;
		}
		if (components == 1) {
			int k0 = k[0];
			if (!j.contains(0)) {
				return moments;
			} else {
				int[] moment = new int[k.length];
				moment[0] = k0 + toAdd;
				moments.add(moment);
				return moments;
			}
		}
		if (!j.contains(components - 1)) {
			int kcurrent = k[components - 1];
			List<int[]> tmpMoments = getMrec(j, components - 1, toAdd, k);
			for (int[] moment : tmpMoments) {
				moment[components - 1] = kcurrent;
				moments.add(moment);
			}
			return moments;
		} else {
			int kcurrent = k[components - 1];
			for (int add = 0; add <= toAdd; add++) {
				List<int[]> tmpMoments = getMrec(j, components - 1, add, k);
				for (int[] moment : tmpMoments) {
					moment[components - 1] = kcurrent + toAdd - add;
					moments.add(moment);
				}
			}
			return moments;
		}
	}

}
