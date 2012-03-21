package uk.ac.imperial.doc.pctmc.testing;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.odeanalysis.closures.NormalClosureVisitorUniversal;
import uk.ac.imperial.doc.pctmc.odeanalysis.closures.NormalMomentClosure;
import uk.ac.imperial.doc.pctmc.plain.PlainState;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TestNormalMomentClosure {
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	public void testPartitions()
	{
		Set<Set<List<Integer>>> expected = (Set)Sets.newHashSet(
			(Set)Sets.newHashSet((List)Lists.newArrayList(1,2),(List)Lists.newArrayList(3,4)),
			(Set)Sets.newHashSet((List)Lists.newArrayList(1,3),(List)Lists.newArrayList(2,4)),		
			(Set)Sets.newHashSet((List)Lists.newArrayList(1,4),(List)Lists.newArrayList(2,3))		
		);
		assertEquals(expected, NormalClosureVisitorUniversal.getAllPartitionsIntoPairs(Lists.newArrayList(1,2,3,4)));
		
		Set<Set<List<Integer>>> expected2 = (Set)Sets.newHashSet(
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,2),(List)Lists.newArrayList(3,4),(List)Lists.newArrayList(5,6)),
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,3),(List)Lists.newArrayList(2,4),(List)Lists.newArrayList(5,6)),		
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,4),(List)Lists.newArrayList(2,3),(List)Lists.newArrayList(5,6)),
				
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,2),(List)Lists.newArrayList(3,5),(List)Lists.newArrayList(4,6)),
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,3),(List)Lists.newArrayList(2,5),(List)Lists.newArrayList(4,6)),		
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,5),(List)Lists.newArrayList(2,3),(List)Lists.newArrayList(4,6)),
				
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,2),(List)Lists.newArrayList(4,5),(List)Lists.newArrayList(3,6)),
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,4),(List)Lists.newArrayList(2,5),(List)Lists.newArrayList(3,6)),		
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,5),(List)Lists.newArrayList(2,4),(List)Lists.newArrayList(3,6)),
				
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,3),(List)Lists.newArrayList(4,5),(List)Lists.newArrayList(2,6)),
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,4),(List)Lists.newArrayList(3,5),(List)Lists.newArrayList(2,6)),		
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,5),(List)Lists.newArrayList(3,4),(List)Lists.newArrayList(2,6)),
				
				(Set)Sets.newHashSet((List)Lists.newArrayList(2,3),(List)Lists.newArrayList(4,5),(List)Lists.newArrayList(1,6)),
				(Set)Sets.newHashSet((List)Lists.newArrayList(2,4),(List)Lists.newArrayList(3,5),(List)Lists.newArrayList(1,6)),		
				(Set)Sets.newHashSet((List)Lists.newArrayList(2,5),(List)Lists.newArrayList(3,4),(List)Lists.newArrayList(1,6))
			);
		assertEquals(expected2, NormalClosureVisitorUniversal.getAllPartitionsIntoPairs(Lists.newArrayList(1,2,3,4,5,6)));
	}

	@Test
	public void testClosures()
	{
		PopulationProduct a = PopulationProduct.getMeanProduct(new PlainState("A"));
		PopulationProduct b = PopulationProduct.getMeanProduct(new PlainState("B"));
		PopulationProduct c = PopulationProduct.getMeanProduct(new PlainState("C"));
		PopulationProduct d = PopulationProduct.getMeanProduct(new PlainState("D"));
		PopulationProduct e = PopulationProduct.getMeanProduct(new PlainState("E"));
		PopulationProduct ab = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("A")),PopulationProduct.getMeanProduct(new PlainState("B"))); 
		PopulationProduct ac = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("A")),PopulationProduct.getMeanProduct(new PlainState("C"))); 
		PopulationProduct ad = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("A")),PopulationProduct.getMeanProduct(new PlainState("D"))); 
		PopulationProduct ae = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("A")),PopulationProduct.getMeanProduct(new PlainState("E"))); 
		PopulationProduct bc = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("B")),PopulationProduct.getMeanProduct(new PlainState("C")));
		PopulationProduct bd = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("B")),PopulationProduct.getMeanProduct(new PlainState("D")));
		PopulationProduct be = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("B")),PopulationProduct.getMeanProduct(new PlainState("E")));
		PopulationProduct cd = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("C")),PopulationProduct.getMeanProduct(new PlainState("D")));
		PopulationProduct ce = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("C")),PopulationProduct.getMeanProduct(new PlainState("E")));
		PopulationProduct de = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("D")),PopulationProduct.getMeanProduct(new PlainState("E")));
		PopulationProduct abc = PopulationProduct.getProduct(ab,PopulationProduct.getMeanProduct(new PlainState("C"))); 
		PopulationProduct abd = PopulationProduct.getProduct(ab,PopulationProduct.getMeanProduct(new PlainState("D"))); 
		PopulationProduct abe = PopulationProduct.getProduct(ab,PopulationProduct.getMeanProduct(new PlainState("E"))); 
		PopulationProduct acd = PopulationProduct.getProduct(ac,PopulationProduct.getMeanProduct(new PlainState("D"))); 
		PopulationProduct ace = PopulationProduct.getProduct(ac,PopulationProduct.getMeanProduct(new PlainState("E")));
		PopulationProduct ade = PopulationProduct.getProduct(ad,PopulationProduct.getMeanProduct(new PlainState("E")));
		PopulationProduct bcd = PopulationProduct.getProduct(bc,PopulationProduct.getMeanProduct(new PlainState("D")));
		PopulationProduct bce = PopulationProduct.getProduct(bc,PopulationProduct.getMeanProduct(new PlainState("E")));
		PopulationProduct bde = PopulationProduct.getProduct(bd,PopulationProduct.getMeanProduct(new PlainState("E")));
		PopulationProduct cde = PopulationProduct.getProduct(cd,PopulationProduct.getMeanProduct(new PlainState("E")));

		AbstractExpression aEx =  CombinedProductExpression.create(new CombinedPopulationProduct(a));
		AbstractExpression bEx =  CombinedProductExpression.create(new CombinedPopulationProduct(b));
		AbstractExpression cEx = CombinedProductExpression.create(new CombinedPopulationProduct(c));
		AbstractExpression dEx = CombinedProductExpression.create(new CombinedPopulationProduct(d));
		AbstractExpression eEx = CombinedProductExpression.create(new CombinedPopulationProduct(e));
		AbstractExpression abEx = CombinedProductExpression.create(new CombinedPopulationProduct(ab));
		AbstractExpression acEx = CombinedProductExpression.create(new CombinedPopulationProduct(ac));
		AbstractExpression adEx = CombinedProductExpression.create(new CombinedPopulationProduct(ad));
		AbstractExpression aeEx = CombinedProductExpression.create(new CombinedPopulationProduct(ae));
		AbstractExpression bcEx = CombinedProductExpression.create(new CombinedPopulationProduct(bc));
		AbstractExpression bdEx = CombinedProductExpression.create(new CombinedPopulationProduct(bd));
		AbstractExpression beEx = CombinedProductExpression.create(new CombinedPopulationProduct(be));
		AbstractExpression cdEx = CombinedProductExpression.create(new CombinedPopulationProduct(cd));
		AbstractExpression ceEx = CombinedProductExpression.create(new CombinedPopulationProduct(ce));
		AbstractExpression deEx = CombinedProductExpression.create(new CombinedPopulationProduct(de));
		AbstractExpression abcEx = CombinedProductExpression.create(new CombinedPopulationProduct(abc));
		AbstractExpression abdEx = CombinedProductExpression.create(new CombinedPopulationProduct(abd));
		AbstractExpression abeEx = CombinedProductExpression.create(new CombinedPopulationProduct(abe));
		AbstractExpression acdEx = CombinedProductExpression.create(new CombinedPopulationProduct(acd));
		AbstractExpression aceEx = CombinedProductExpression.create(new CombinedPopulationProduct(ace));
		AbstractExpression adeEx = CombinedProductExpression.create(new CombinedPopulationProduct(ade));
		AbstractExpression bcdEx = CombinedProductExpression.create(new CombinedPopulationProduct(bcd));
		AbstractExpression bceEx = CombinedProductExpression.create(new CombinedPopulationProduct(bce));
		AbstractExpression bdeEx = CombinedProductExpression.create(new CombinedPopulationProduct(bde));
		AbstractExpression cdeEx = CombinedProductExpression.create(new CombinedPopulationProduct(cde));
		
		SumExpression closedExpr;
		Set<AbstractExpression> expSummands,genSummands;
		NormalMomentClosure nmc;
		
		// (2,3) normal closure
		nmc = new NormalMomentClosure(2);
		closedExpr = (SumExpression)nmc.insertProductIntoRate(bcEx, a);
		genSummands = new HashSet<AbstractExpression>(closedExpr.getSummands());
		expSummands = new HashSet<AbstractExpression>();
		expSummands.add(genSummand(1.0,new AbstractExpression[]{aEx,bcEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{bEx,acEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{cEx,abEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{aEx,bEx,cEx}));
		assertEquals(expSummands,genSummands);
		
		// (2,4) normal closure
		nmc = new NormalMomentClosure(2);
		closedExpr = (SumExpression)nmc.insertProductIntoRate(cdEx, ab);
		genSummands = new HashSet<AbstractExpression>(closedExpr.getSummands());
		expSummands = new HashSet<AbstractExpression>();
		expSummands.add(genSummand(1.0,new AbstractExpression[]{abEx,cdEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{acEx,bdEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{adEx,bcEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{aEx,bEx,cEx,dEx}));
		assertEquals(expSummands,genSummands);
		
		// (2,5) normal closure TODO(Check by hand again)
		nmc = new NormalMomentClosure(2);
		closedExpr = (SumExpression)nmc.insertProductIntoRate(cdeEx, ab);
		genSummands = new HashSet<AbstractExpression>(closedExpr.getSummands());
		expSummands = new HashSet<AbstractExpression>();
		expSummands.add(genSummand(1.0,new AbstractExpression[]{abEx,cdEx,eEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{abEx,ceEx,dEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{abEx,deEx,cEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{acEx,bdEx,eEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{acEx,beEx,dEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{acEx,deEx,bEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{adEx,bcEx,eEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{adEx,beEx,cEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{adEx,ceEx,bEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{aeEx,bcEx,dEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{aeEx,bdEx,cEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{aeEx,cdEx,bEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{bcEx,deEx,aEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{bdEx,ceEx,aEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{beEx,cdEx,aEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{abEx,cEx,dEx,eEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{acEx,bEx,dEx,eEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{adEx,bEx,cEx,eEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{aeEx,bEx,cEx,dEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{bcEx,aEx,dEx,eEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{bdEx,aEx,cEx,eEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{beEx,aEx,cEx,dEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{cdEx,aEx,bEx,eEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{ceEx,aEx,bEx,dEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{deEx,aEx,bEx,cEx}));
		expSummands.add(genSummand(6.0,new AbstractExpression[]{aEx,bEx,cEx,dEx,eEx}));
		assertEquals(expSummands,genSummands);
		
		// (3,4) normal closure
		nmc = new NormalMomentClosure(3);
		closedExpr = (SumExpression)nmc.insertProductIntoRate(cdEx, ab);
		genSummands = new HashSet<AbstractExpression>(closedExpr.getSummands());
		expSummands = new HashSet<AbstractExpression>();
		expSummands.add(genSummand(1.0,new AbstractExpression[]{abcEx,dEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{abdEx,cEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{acdEx,bEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{bcdEx,aEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{abEx,cdEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{acEx,bdEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{adEx,bcEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{abEx,cEx,dEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{acEx,bEx,dEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{adEx,bEx,cEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{bcEx,aEx,dEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{bdEx,aEx,cEx}));
		expSummands.add(genSummand(-2.0,new AbstractExpression[]{cdEx,aEx,bEx}));
		expSummands.add(genSummand(6.0,new AbstractExpression[]{aEx,bEx,cEx,dEx}));
		assertEquals(expSummands,genSummands);
		
		// (3,5) normal closure
		nmc = new NormalMomentClosure(3);
		closedExpr = (SumExpression)nmc.insertProductIntoRate(cdeEx, ab);
		genSummands = new HashSet<AbstractExpression>(closedExpr.getSummands());
		expSummands = new HashSet<AbstractExpression>();
		expSummands.add(genSummand(1.0,new AbstractExpression[]{abcEx,dEx,eEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{abdEx,cEx,eEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{abeEx,cEx,dEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{acdEx,bEx,eEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{aceEx,bEx,dEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{adeEx,bEx,cEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{bcdEx,aEx,eEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{bceEx,aEx,dEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{bdeEx,aEx,cEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{cdeEx,aEx,bEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{abEx,cdEx,eEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{abEx,ceEx,dEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{abEx,deEx,cEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{acEx,bdEx,eEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{acEx,beEx,dEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{acEx,deEx,bEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{adEx,bcEx,eEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{adEx,beEx,cEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{adEx,ceEx,bEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{aeEx,bcEx,dEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{aeEx,bdEx,cEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{aeEx,cdEx,bEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{bcEx,deEx,aEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{bdEx,ceEx,aEx}));
		expSummands.add(genSummand(1.0,new AbstractExpression[]{beEx,cdEx,aEx}));
		expSummands.add(genSummand(-5.0,new AbstractExpression[]{abEx,cEx,dEx,eEx}));
		expSummands.add(genSummand(-5.0,new AbstractExpression[]{acEx,bEx,dEx,eEx}));
		expSummands.add(genSummand(-5.0,new AbstractExpression[]{adEx,bEx,cEx,eEx}));
		expSummands.add(genSummand(-5.0,new AbstractExpression[]{aeEx,bEx,cEx,dEx}));
		expSummands.add(genSummand(-5.0,new AbstractExpression[]{bcEx,aEx,dEx,eEx}));
		expSummands.add(genSummand(-5.0,new AbstractExpression[]{bdEx,aEx,cEx,eEx}));
		expSummands.add(genSummand(-5.0,new AbstractExpression[]{beEx,aEx,cEx,dEx}));
		expSummands.add(genSummand(-5.0,new AbstractExpression[]{cdEx,aEx,bEx,eEx}));
		expSummands.add(genSummand(-5.0,new AbstractExpression[]{ceEx,aEx,bEx,dEx}));
		expSummands.add(genSummand(-5.0,new AbstractExpression[]{deEx,aEx,bEx,cEx}));
		expSummands.add(genSummand(26.0,new AbstractExpression[]{aEx,bEx,cEx,dEx,eEx}));
		assertEquals(expSummands,genSummands);
	}
	
	private AbstractExpression genSummand(double _mult, AbstractExpression ... _abstex)
	{
		return ProductExpression.create(new DoubleExpression(_mult),ProductExpression.createOrdered(Arrays.asList(_abstex)));
	}
}
