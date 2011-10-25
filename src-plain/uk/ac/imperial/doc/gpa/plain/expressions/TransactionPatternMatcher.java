package uk.ac.imperial.doc.gpa.plain.expressions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.gpa.plain.representation.Transaction;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternMatcher;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

public class TransactionPatternMatcher extends PatternMatcher {
	public TransactionPatternMatcher(PCTMC pctmc) {
		super(pctmc);
	}

	@Override
	public AbstractExpression getMatchingExpression(State s) {
		if (!(s instanceof CountingState)){
			throw new AssertionError("Pattern is not a counting state!"); 
		}
		CountingState t = (CountingState)s;
		String countOf = t.getComponent(); 
		List<AbstractExpression> summands = new LinkedList<AbstractExpression>(); 
		for (State p:pctmc.getStateIndex().keySet()){
			if ((p instanceof Transaction)){
				Transaction pt = (Transaction) p;
				List<String> ptComponents = pt.getComponents(); 
				Multiset<String> tmp = HashMultiset.<String>create(ptComponents);
				ArrayList<AbstractExpression> terms = Lists.newArrayList(new DoubleExpression((double)tmp.count(countOf)),CombinedProductExpression.createMeanExpression(pt));
				summands.add(ProductExpression.create(terms));
			}
		}
		return SumExpression.create(summands);
	}

}
