package uk.ac.imperial.doc.jexpressions.testing.expanded;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expanded.Polynomial;
import uk.ac.imperial.doc.jexpressions.expanded.UnexpandableExpression;
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
		a.add(new UnexpandableExpression(new ConstantExpression("a")));
		b = HashMultiset.<ExpandedExpression>create();
		b.add(new UnexpandableExpression(new ConstantExpression("b")));
		Map<Multiset<ExpandedExpression>, Double> p1m = new HashMap<Multiset<ExpandedExpression>, Double>();
		p1m.put(a, 1.0);
		p1m.put(b, 1.0);
		pApB = new Polynomial(p1m);
		
		Multiset<ExpandedExpression> a2 = HashMultiset.<ExpandedExpression>create();
		a2.add(new UnexpandableExpression(new ConstantExpression("a")),2);
		Multiset<ExpandedExpression> b2 = HashMultiset.<ExpandedExpression>create();
		b2.add(new UnexpandableExpression(new ConstantExpression("b")),2);
		
		ab = HashMultiset.<ExpandedExpression>create();
		ab.add(new UnexpandableExpression(new ConstantExpression("a")),1);
		ab.add(new UnexpandableExpression(new ConstantExpression("b")),1);
		Map<Multiset<ExpandedExpression>, Double> p2m = new HashMap<Multiset<ExpandedExpression>, Double>();
		p2m.put(a2,1.0);
		p2m.put(ab,2.0);
		p2m.put(b2,1.0);
		pA2p2ABpB2 = new Polynomial(p2m);
		
		t0 = HashMultiset.<ExpandedExpression>create();
		t0.add(new UnexpandableExpression(new DoubleExpression(0.0)),1);
		
		t1 = HashMultiset.<ExpandedExpression>create();
		t1.add(new UnexpandableExpression(new DoubleExpression(1.0)),1);
		Map<Multiset<ExpandedExpression>, Double> p3m = new HashMap<Multiset<ExpandedExpression>, Double>();
		p3m.put(t1, 1.0);
		p1 = new Polynomial(p3m);
		
		t2 = HashMultiset.<ExpandedExpression>create();
		t2.add(new UnexpandableExpression(new DoubleExpression(2.0)),1);
		
		t3 = HashMultiset.<ExpandedExpression>create();
		t3.add(new UnexpandableExpression(new DoubleExpression(2.0)),2);
		
		t4 = HashMultiset.<ExpandedExpression>create();
		t4.add(new UnexpandableExpression(new DoubleExpression(4.0)),1);
		
		Map<Multiset<ExpandedExpression>, Double> p4m = new HashMap<Multiset<ExpandedExpression>, Double>();
		p4m.put(t2, 1.0);
		p2 = new Polynomial(p4m);
		
		Map<Multiset<ExpandedExpression>, Double> p5m = new HashMap<Multiset<ExpandedExpression>, Double>();
		p5m.put(a2, 1.0);
		p5m.put(a, 2.0);
		p5m.put(ab, 3.0);
		pA2p2Ap3AB = new Polynomial(p5m);
		
		Map<Multiset<ExpandedExpression>, Double> p6m = new HashMap<Multiset<ExpandedExpression>, Double>();
		p6m.put(a, 1.0);
		p6m.put(t1, 2.0);
		p6m.put(b, 3.0);
		pAp2p3B = new Polynomial(p6m);
		
		c = HashMultiset.<ExpandedExpression>create();
		c.add(new UnexpandableExpression(new ConstantExpression("c")));
		
		d = HashMultiset.<ExpandedExpression>create();
		d.add(new UnexpandableExpression(new ConstantExpression("d")));
		
		ad = HashMultiset.<ExpandedExpression>create();
		ad.add(new UnexpandableExpression(new ConstantExpression("a")));
		ad.add(new UnexpandableExpression(new ConstantExpression("d")));
		
		bc = HashMultiset.<ExpandedExpression>create();
		bc.add(new UnexpandableExpression(new ConstantExpression("b")));
		bc.add(new UnexpandableExpression(new ConstantExpression("c")));
		
		bd = HashMultiset.<ExpandedExpression>create();
		bd.add(new UnexpandableExpression(new ConstantExpression("b")));
		bd.add(new UnexpandableExpression(new ConstantExpression("d")));
		
		Map<Multiset<ExpandedExpression>, Double> p2t1m = new HashMap<Multiset<ExpandedExpression>, Double>();
		p2t1m.put(t1, 2.0);
		p2t1 = new Polynomial(p2t1m);
	}
	

}
