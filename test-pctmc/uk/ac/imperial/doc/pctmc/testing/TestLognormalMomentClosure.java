package uk.ac.imperial.doc.pctmc.testing;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.odeanalysis.closures.LognormalMomentClosure;
import uk.ac.imperial.doc.pctmc.plain.PlainState;

public class TestLognormalMomentClosure {

	@Test
	public void testClosures()
	{
		PopulationProduct a = PopulationProduct.getMeanProduct(new PlainState("A"));
		PopulationProduct b = PopulationProduct.getMeanProduct(new PlainState("B"));
		PopulationProduct c = PopulationProduct.getMeanProduct(new PlainState("C"));
		PopulationProduct d = PopulationProduct.getMeanProduct(new PlainState("D"));
		PopulationProduct aa = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("A")),PopulationProduct.getMeanProduct(new PlainState("A"))); 
		PopulationProduct ab = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("A")),PopulationProduct.getMeanProduct(new PlainState("B"))); 
		PopulationProduct ac = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("A")),PopulationProduct.getMeanProduct(new PlainState("C"))); 
		PopulationProduct ad = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("A")),PopulationProduct.getMeanProduct(new PlainState("D")));  
		PopulationProduct bb = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("B")),PopulationProduct.getMeanProduct(new PlainState("B")));
		PopulationProduct bc = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("B")),PopulationProduct.getMeanProduct(new PlainState("C")));
		PopulationProduct bd = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("B")),PopulationProduct.getMeanProduct(new PlainState("D")));
		PopulationProduct cd = PopulationProduct.getProduct(PopulationProduct.getMeanProduct(new PlainState("C")),PopulationProduct.getMeanProduct(new PlainState("D")));
		PopulationProduct abc = PopulationProduct.getProduct(ab,PopulationProduct.getMeanProduct(new PlainState("C"))); 
		PopulationProduct abd = PopulationProduct.getProduct(ab,PopulationProduct.getMeanProduct(new PlainState("D"))); 
		PopulationProduct acd = PopulationProduct.getProduct(ac,PopulationProduct.getMeanProduct(new PlainState("D"))); 
		PopulationProduct bcd = PopulationProduct.getProduct(bc,PopulationProduct.getMeanProduct(new PlainState("D")));

		AbstractExpression aEx =  CombinedProductExpression.create(new CombinedPopulationProduct(a));
		AbstractExpression bEx =  CombinedProductExpression.create(new CombinedPopulationProduct(b));
		AbstractExpression cEx = CombinedProductExpression.create(new CombinedPopulationProduct(c));
		AbstractExpression dEx = CombinedProductExpression.create(new CombinedPopulationProduct(d));
		AbstractExpression aaEx = CombinedProductExpression.create(new CombinedPopulationProduct(aa));
		AbstractExpression abEx = CombinedProductExpression.create(new CombinedPopulationProduct(ab));
		AbstractExpression acEx = CombinedProductExpression.create(new CombinedPopulationProduct(ac));
		AbstractExpression adEx = CombinedProductExpression.create(new CombinedPopulationProduct(ad));
		AbstractExpression bbEx = CombinedProductExpression.create(new CombinedPopulationProduct(bb));
		AbstractExpression bcEx = CombinedProductExpression.create(new CombinedPopulationProduct(bc));
		AbstractExpression bdEx = CombinedProductExpression.create(new CombinedPopulationProduct(bd));
		AbstractExpression cdEx = CombinedProductExpression.create(new CombinedPopulationProduct(cd));
		AbstractExpression abcEx = CombinedProductExpression.create(new CombinedPopulationProduct(abc));
		AbstractExpression abdEx = CombinedProductExpression.create(new CombinedPopulationProduct(abd));
		AbstractExpression acdEx = CombinedProductExpression.create(new CombinedPopulationProduct(acd));
		AbstractExpression bcdEx = CombinedProductExpression.create(new CombinedPopulationProduct(bcd));
		
		LognormalMomentClosure lnmc;
		PEPADivExpression closedExprDiv;
		MinExpression genMinEx;
		Set<AbstractExpression> expNumFacts,expDenFacts,genNumFacts,genDenFacts;

		// (2,3) lognormal closure - no mean field stabilisation
		lnmc = new LognormalMomentClosure(2, 0);
		closedExprDiv = (PEPADivExpression)lnmc.insertProductIntoRate(bcEx, a);
		expNumFacts = new HashSet<AbstractExpression>();
		expDenFacts = new HashSet<AbstractExpression>();
		expNumFacts.add(abEx);
		expNumFacts.add(bcEx);
		expNumFacts.add(acEx);
		expDenFacts.add(aEx);
		expDenFacts.add(bEx);
		expDenFacts.add(cEx);
		genNumFacts = new HashSet<AbstractExpression>(((ProductExpression)closedExprDiv.getNumerator()).getTerms());
		genDenFacts = new HashSet<AbstractExpression>(((ProductExpression)closedExprDiv.getDenominator()).getTerms());
		assertEquals(expNumFacts,genNumFacts);
		assertEquals(expDenFacts,genDenFacts);
		// Check mean field stabilisation
		lnmc = new LognormalMomentClosure(2, 10);
		genMinEx = (MinExpression)lnmc.insertProductIntoRate(bcEx, a);
		assertEquals(closedExprDiv,genMinEx.getA());
		assertEquals(ProductExpression.createOrdered(Arrays.asList(new DoubleExpression(10.0),aEx,bEx,cEx)),genMinEx.getB());
		
		// (2,3) lognormal closure - with 1 duplicate
		lnmc = new LognormalMomentClosure(2, 0);
		closedExprDiv = (PEPADivExpression)lnmc.insertProductIntoRate(aaEx, b);
		expNumFacts = new HashSet<AbstractExpression>();
		expDenFacts = new HashSet<AbstractExpression>();
		expNumFacts.add(PowerExpression.create(abEx,new IntegerExpression(2)));
		expNumFacts.add(aaEx);
		expDenFacts.add(PowerExpression.create(aEx,new IntegerExpression(2)));
		expDenFacts.add(bEx);
		genNumFacts = new HashSet<AbstractExpression>(((ProductExpression)closedExprDiv.getNumerator()).getTerms());
		genDenFacts = new HashSet<AbstractExpression>(((ProductExpression)closedExprDiv.getDenominator()).getTerms());
		assertEquals(expNumFacts,genNumFacts);
		assertEquals(expDenFacts,genDenFacts);
		// Check mean field stabilisation
		lnmc = new LognormalMomentClosure(2, 10);
		genMinEx = (MinExpression)lnmc.insertProductIntoRate(aaEx, b);
		assertEquals(closedExprDiv,genMinEx.getA());
		assertEquals(ProductExpression.createOrdered(Arrays.asList(new DoubleExpression(10.0),aEx,aEx,bEx)),genMinEx.getB());
		
		// (2,3) lognormal closure - with 1 triplet
		lnmc = new LognormalMomentClosure(2, 0);
		closedExprDiv = (PEPADivExpression)lnmc.insertProductIntoRate(aaEx, a);
		assertEquals(PowerExpression.create(aaEx,new IntegerExpression(3)),closedExprDiv.getNumerator());
		assertEquals(PowerExpression.create(aEx,new IntegerExpression(3)),closedExprDiv.getDenominator());
		// Check mean field stabilisation
		lnmc = new LognormalMomentClosure(2, 10);
		genMinEx = (MinExpression)lnmc.insertProductIntoRate(aaEx, a);
		assertEquals(closedExprDiv,genMinEx.getA());
		assertEquals(ProductExpression.createOrdered(Arrays.asList(new DoubleExpression(10.0),aEx,aEx,aEx)),genMinEx.getB());
	
		// (2,4) lognormal closure
		lnmc = new LognormalMomentClosure(2, 0);
		closedExprDiv = (PEPADivExpression)lnmc.insertProductIntoRate(cdEx, ab);
		expNumFacts = new HashSet<AbstractExpression>();
		expDenFacts = new HashSet<AbstractExpression>();
		expNumFacts.add(abEx);
		expNumFacts.add(acEx);
		expNumFacts.add(adEx);
		expNumFacts.add(bcEx);
		expNumFacts.add(bdEx);
		expNumFacts.add(cdEx);
		expDenFacts.add(PowerExpression.create(aEx,new IntegerExpression(2)));
		expDenFacts.add(PowerExpression.create(bEx,new IntegerExpression(2)));
		expDenFacts.add(PowerExpression.create(cEx,new IntegerExpression(2)));
		expDenFacts.add(PowerExpression.create(dEx,new IntegerExpression(2)));
		genNumFacts = new HashSet<AbstractExpression>(((ProductExpression)closedExprDiv.getNumerator()).getTerms());
		genDenFacts = new HashSet<AbstractExpression>(((ProductExpression)closedExprDiv.getDenominator()).getTerms());
		assertEquals(expNumFacts,genNumFacts);
		assertEquals(expDenFacts,genDenFacts);
		// Check mean field stabilisation
		lnmc = new LognormalMomentClosure(2, 10);
		genMinEx = (MinExpression)lnmc.insertProductIntoRate(cdEx, ab);
		assertEquals(closedExprDiv,genMinEx.getA());
		assertEquals(ProductExpression.createOrdered(Arrays.asList(new DoubleExpression(10.0),aEx,bEx,cEx,dEx)),genMinEx.getB());
		
		// (2,4) lognormal closure - 1 duplicate
		lnmc = new LognormalMomentClosure(2, 0);
		closedExprDiv = (PEPADivExpression)lnmc.insertProductIntoRate(bcEx, aa);
		expNumFacts = new HashSet<AbstractExpression>();
		expDenFacts = new HashSet<AbstractExpression>();
		expNumFacts.add(aaEx);
		expNumFacts.add(PowerExpression.create(abEx,new IntegerExpression(2)));
		expNumFacts.add(PowerExpression.create(acEx,new IntegerExpression(2)));
		expNumFacts.add(bcEx);
		expDenFacts.add(PowerExpression.create(aEx,new IntegerExpression(4)));
		expDenFacts.add(PowerExpression.create(bEx,new IntegerExpression(2)));
		expDenFacts.add(PowerExpression.create(cEx,new IntegerExpression(2)));
		genNumFacts = new HashSet<AbstractExpression>(((ProductExpression)closedExprDiv.getNumerator()).getTerms());
		genDenFacts = new HashSet<AbstractExpression>(((ProductExpression)closedExprDiv.getDenominator()).getTerms());
		assertEquals(expNumFacts,genNumFacts);
		assertEquals(expDenFacts,genDenFacts);
		// Check mean field stabilisation
		lnmc = new LognormalMomentClosure(2, 10);
		genMinEx = (MinExpression)lnmc.insertProductIntoRate(bcEx, aa);
		assertEquals(closedExprDiv,genMinEx.getA());
		assertEquals(ProductExpression.createOrdered(Arrays.asList(new DoubleExpression(10.0),aEx,aEx,bEx,cEx)),genMinEx.getB());
		
		// (2,4) lognormal closure - 2 duplicates
		lnmc = new LognormalMomentClosure(2, 0);
		closedExprDiv = (PEPADivExpression)lnmc.insertProductIntoRate(bbEx, aa);
		expNumFacts = new HashSet<AbstractExpression>();
		expDenFacts = new HashSet<AbstractExpression>();
		expNumFacts.add(aaEx);
		expNumFacts.add(PowerExpression.create(abEx,new IntegerExpression(4)));
		expNumFacts.add(bbEx);
		expDenFacts.add(PowerExpression.create(aEx,new IntegerExpression(4)));
		expDenFacts.add(PowerExpression.create(bEx,new IntegerExpression(4)));
		genNumFacts = new HashSet<AbstractExpression>(((ProductExpression)closedExprDiv.getNumerator()).getTerms());
		genDenFacts = new HashSet<AbstractExpression>(((ProductExpression)closedExprDiv.getDenominator()).getTerms());
		assertEquals(expNumFacts,genNumFacts);
		assertEquals(expDenFacts,genDenFacts);
		// Check mean field stabilisation
		lnmc = new LognormalMomentClosure(2, 10);
		genMinEx = (MinExpression)lnmc.insertProductIntoRate(bbEx, aa);
		assertEquals(closedExprDiv,genMinEx.getA());
		assertEquals(ProductExpression.createOrdered(Arrays.asList(new DoubleExpression(10.0),aEx,aEx,bEx,bEx)),genMinEx.getB());
		
		// (2,4) lognormal closure - 1 triplet
		lnmc = new LognormalMomentClosure(2, 0);
		closedExprDiv = (PEPADivExpression)lnmc.insertProductIntoRate(abEx, aa);
		expNumFacts = new HashSet<AbstractExpression>();
		expDenFacts = new HashSet<AbstractExpression>();
		expNumFacts.add(PowerExpression.create(aaEx,new IntegerExpression(3)));
		expNumFacts.add(PowerExpression.create(abEx,new IntegerExpression(3)));
		expDenFacts.add(PowerExpression.create(aEx,new IntegerExpression(6)));
		expDenFacts.add(PowerExpression.create(bEx,new IntegerExpression(2)));
		genNumFacts = new HashSet<AbstractExpression>(((ProductExpression)closedExprDiv.getNumerator()).getTerms());
		genDenFacts = new HashSet<AbstractExpression>(((ProductExpression)closedExprDiv.getDenominator()).getTerms());
		assertEquals(expNumFacts,genNumFacts);
		assertEquals(expDenFacts,genDenFacts);
		// Check mean field stabilisation
		lnmc = new LognormalMomentClosure(2, 10);
		genMinEx = (MinExpression)lnmc.insertProductIntoRate(abEx, aa);
		assertEquals(closedExprDiv,genMinEx.getA());
		assertEquals(ProductExpression.createOrdered(Arrays.asList(new DoubleExpression(10.0),aEx,aEx,aEx,bEx)),genMinEx.getB());
		
		// (2,4) lognormal closure - 1 quad
		lnmc = new LognormalMomentClosure(2, 0);
		closedExprDiv = (PEPADivExpression)lnmc.insertProductIntoRate(aaEx, aa);
		expNumFacts = new HashSet<AbstractExpression>();
		expDenFacts = new HashSet<AbstractExpression>();
		expNumFacts.add(PowerExpression.create(aaEx,new IntegerExpression(6)));
		expDenFacts.add(PowerExpression.create(aEx,new IntegerExpression(8)));
		assertEquals(PowerExpression.create(aaEx,new IntegerExpression(6)),closedExprDiv.getNumerator());
		assertEquals(PowerExpression.create(aEx,new IntegerExpression(8)),closedExprDiv.getDenominator());
		// Check mean field stabilisation
		lnmc = new LognormalMomentClosure(2, 10);
		genMinEx = (MinExpression)lnmc.insertProductIntoRate(aaEx, aa);
		assertEquals(closedExprDiv,genMinEx.getA());
		assertEquals(ProductExpression.createOrdered(Arrays.asList(new DoubleExpression(10.0),aEx,aEx,aEx,aEx)),genMinEx.getB());
		
		// (3,4) lognormal closure
		lnmc = new LognormalMomentClosure(3, 0);
		closedExprDiv = (PEPADivExpression)lnmc.insertProductIntoRate(cdEx, ab);
		expNumFacts = new HashSet<AbstractExpression>();
		expDenFacts = new HashSet<AbstractExpression>();
		expNumFacts.add(abcEx);
		expNumFacts.add(abdEx);
		expNumFacts.add(acdEx);
		expNumFacts.add(bcdEx);
		expNumFacts.add(aEx);
		expNumFacts.add(bEx);
		expNumFacts.add(cEx);
		expNumFacts.add(dEx);
		expDenFacts.add(abEx);
		expDenFacts.add(acEx);
		expDenFacts.add(adEx);
		expDenFacts.add(bcEx);
		expDenFacts.add(bdEx);
		expDenFacts.add(cdEx);
		genNumFacts = new HashSet<AbstractExpression>(((ProductExpression)closedExprDiv.getNumerator()).getTerms());
		genDenFacts = new HashSet<AbstractExpression>(((ProductExpression)closedExprDiv.getDenominator()).getTerms());
		assertEquals(expNumFacts,genNumFacts);
		assertEquals(expDenFacts,genDenFacts);
		// Check mean field stabilisation
		lnmc = new LognormalMomentClosure(3, 10);
		genMinEx = (MinExpression)lnmc.insertProductIntoRate(cdEx, ab);
		assertEquals(closedExprDiv,genMinEx.getA());
		assertEquals(ProductExpression.createOrdered(Arrays.asList(new DoubleExpression(10.0),aEx,bEx,cEx,dEx)),genMinEx.getB());
	}
	
}
