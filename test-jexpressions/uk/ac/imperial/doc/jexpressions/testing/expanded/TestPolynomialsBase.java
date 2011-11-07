package uk.ac.imperial.doc.jexpressions.testing.expanded;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expanded.DoubleNormaliser;
import uk.ac.imperial.doc.jexpressions.expanded.Polynomial;
import uk.ac.imperial.doc.jexpressions.expanded.UnexpandableExpression;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class TestPolynomialsBase {
	
	protected static Polynomial pApB; // a + b
	protected static Polynomial pA2p2ABpB2; // a^2 + 2ab + b^2
	protected static Polynomial p1; // 1.0
	protected static Polynomial p2; // 2.0
	protected static Polynomial pA2p2Ap3AB; // a^2 + 2*a + 3*ab
	protected static Polynomial pAp2p3B; // a + 2 + 3*b
	protected static Polynomial p2t1; // 2.0*1.0
	
	protected static Multiset<ExpandedExpression> t0; // 0.0
    protected static Multiset<ExpandedExpression> t1; // 1.0
    protected static Multiset<ExpandedExpression> t2; // 2.0
    
    protected static Multiset<ExpandedExpression> t3; // 2.0^2
    protected static Multiset<ExpandedExpression> t4; // 4.0
	
    protected static Multiset<ExpandedExpression> a;
	protected static Multiset<ExpandedExpression> b;
    protected static Multiset<ExpandedExpression> c;
	protected static Multiset<ExpandedExpression> d;
	
	protected static Multiset<ExpandedExpression> ab;
	protected static Multiset<ExpandedExpression> ad;
	protected static Multiset<ExpandedExpression> bc;
	protected static Multiset<ExpandedExpression> bd;
	
	
	@BeforeClass
	public static void createPolynomialObjects(){
		a = HashMultiset.<ExpandedExpression>create();
		a.add(new UnexpandableExpression(new ConstantExpression("a"), new DoubleNormaliser()));
		b = HashMultiset.<ExpandedExpression>create();
		b.add(new UnexpandableExpression(new ConstantExpression("b"), new DoubleNormaliser()));
		Map<Multiset<ExpandedExpression>, AbstractExpression> p1m = new HashMap<Multiset<ExpandedExpression>, AbstractExpression>();
		p1m.put(a, DoubleExpression.ONE);
		p1m.put(b, DoubleExpression.ONE);
		pApB = new Polynomial(p1m, new DoubleNormaliser());
		
		Multiset<ExpandedExpression> a2 = HashMultiset.<ExpandedExpression>create();
		a2.add(new UnexpandableExpression(new ConstantExpression("a"), new DoubleNormaliser()),2);
		Multiset<ExpandedExpression> b2 = HashMultiset.<ExpandedExpression>create();
		b2.add(new UnexpandableExpression(new ConstantExpression("b"),new DoubleNormaliser()),2);
		
		ab = HashMultiset.<ExpandedExpression>create();
		ab.add(new UnexpandableExpression(new ConstantExpression("a"), new DoubleNormaliser()),1);
		ab.add(new UnexpandableExpression(new ConstantExpression("b"),new DoubleNormaliser()),1);
		Map<Multiset<ExpandedExpression>, AbstractExpression> p2m = new HashMap<Multiset<ExpandedExpression>, AbstractExpression>();
		p2m.put(a2,DoubleExpression.ONE);
		p2m.put(ab,new DoubleExpression(2.0));
		p2m.put(b2,DoubleExpression.ONE);
		pA2p2ABpB2 = new Polynomial(p2m, new DoubleNormaliser());
		
		t0 = HashMultiset.<ExpandedExpression>create();
		t0.add(new UnexpandableExpression(new DoubleExpression(0.0), new DoubleNormaliser()),1);
		
		t1 = HashMultiset.<ExpandedExpression>create();
		t1.add(new UnexpandableExpression(new DoubleExpression(1.0),new DoubleNormaliser()),1);
		Map<Multiset<ExpandedExpression>, AbstractExpression> p3m = new HashMap<Multiset<ExpandedExpression>, AbstractExpression>();
		p3m.put(t1, DoubleExpression.ONE);
		p1 = new Polynomial(p3m, new DoubleNormaliser());
		
		t2 = HashMultiset.<ExpandedExpression>create();
		t2.add(new UnexpandableExpression(new DoubleExpression(2.0), new DoubleNormaliser()),1);
		
		t3 = HashMultiset.<ExpandedExpression>create();
		t3.add(new UnexpandableExpression(new DoubleExpression(2.0),new DoubleNormaliser()),2);
		
		t4 = HashMultiset.<ExpandedExpression>create();
		t4.add(new UnexpandableExpression(new DoubleExpression(4.0),new DoubleNormaliser()),1);
		
		Map<Multiset<ExpandedExpression>, AbstractExpression> p4m = new HashMap<Multiset<ExpandedExpression>, AbstractExpression>();
		p4m.put(t2, DoubleExpression.ONE);
		p2 = new Polynomial(p4m, new DoubleNormaliser());
		
		Map<Multiset<ExpandedExpression>, AbstractExpression> p5m = new HashMap<Multiset<ExpandedExpression>, AbstractExpression>();
		p5m.put(a2, DoubleExpression.ONE);
		p5m.put(a, new DoubleExpression(2.0));
		p5m.put(ab, new DoubleExpression(3.0));
		pA2p2Ap3AB = new Polynomial(p5m, new DoubleNormaliser());
		
		Map<Multiset<ExpandedExpression>, AbstractExpression> p6m = new HashMap<Multiset<ExpandedExpression>, AbstractExpression>();
		p6m.put(a, DoubleExpression.ONE);
		p6m.put(t1, new DoubleExpression(2.0));
		p6m.put(b, new DoubleExpression(3.0));
		pAp2p3B = new Polynomial(p6m, new DoubleNormaliser());
		
		c = HashMultiset.<ExpandedExpression>create();
		c.add(new UnexpandableExpression(new ConstantExpression("c"), new DoubleNormaliser()));
		
		d = HashMultiset.<ExpandedExpression>create();
		d.add(new UnexpandableExpression(new ConstantExpression("d"), new DoubleNormaliser()));
		
		ad = HashMultiset.<ExpandedExpression>create();
		ad.add(new UnexpandableExpression(new ConstantExpression("a"), new DoubleNormaliser()));
		ad.add(new UnexpandableExpression(new ConstantExpression("d"), new DoubleNormaliser()));
		
		bc = HashMultiset.<ExpandedExpression>create();
		bc.add(new UnexpandableExpression(new ConstantExpression("b"),new DoubleNormaliser()));
		bc.add(new UnexpandableExpression(new ConstantExpression("c"), new DoubleNormaliser()));
		
		bd = HashMultiset.<ExpandedExpression>create();
		bd.add(new UnexpandableExpression(new ConstantExpression("b"), new DoubleNormaliser()));
		bd.add(new UnexpandableExpression(new ConstantExpression("d"), new DoubleNormaliser()));
		
		Map<Multiset<ExpandedExpression>, AbstractExpression> p2t1m = new HashMap<Multiset<ExpandedExpression>, AbstractExpression>();
		p2t1m.put(t1, new DoubleExpression(2.0));
		p2t1 = new Polynomial(p2t1m, new DoubleNormaliser());
	}
	

}
