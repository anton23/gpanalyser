package uk.ac.imperial.doc.jexpressions.testing.expanded;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.expanded.DoubleCoefficients;
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
	
	protected static Multiset<UnexpandableExpression> t0; // 0.0
    protected static Multiset<UnexpandableExpression> t1; // 1.0
    protected static Multiset<UnexpandableExpression> t2; // 2.0
    
    protected static Multiset<UnexpandableExpression> t3; // 2.0^2
    protected static Multiset<UnexpandableExpression> t4; // 4.0
	
    protected static Multiset<UnexpandableExpression> a;
	protected static Multiset<UnexpandableExpression> b;
    protected static Multiset<UnexpandableExpression> c;
	protected static Multiset<UnexpandableExpression> d;
	
	protected static Multiset<UnexpandableExpression> ab;
	protected static Multiset<UnexpandableExpression> ad;
	protected static Multiset<UnexpandableExpression> bc;
	protected static Multiset<UnexpandableExpression> bd;
	
	
	@BeforeClass
	public static void createPolynomialObjects(){
		a = HashMultiset.<UnexpandableExpression>create();
		a.add(new UnexpandableExpression(new ConstantExpression("a"), new DoubleCoefficients()));
		b = HashMultiset.<UnexpandableExpression>create();
		b.add(new UnexpandableExpression(new ConstantExpression("b"), new DoubleCoefficients()));
		Map<Multiset<UnexpandableExpression>, AbstractExpression> p1m = new HashMap<Multiset<UnexpandableExpression>, AbstractExpression>();
		p1m.put(a, DoubleExpression.ONE);
		p1m.put(b, DoubleExpression.ONE);
		pApB = new Polynomial(p1m, new DoubleCoefficients());
		
		Multiset<UnexpandableExpression> a2 = HashMultiset.<UnexpandableExpression>create();
		a2.add(new UnexpandableExpression(new ConstantExpression("a"), new DoubleCoefficients()),2);
		Multiset<UnexpandableExpression> b2 = HashMultiset.<UnexpandableExpression>create();
		b2.add(new UnexpandableExpression(new ConstantExpression("b"),new DoubleCoefficients()),2);
		
		ab = HashMultiset.<UnexpandableExpression>create();
		ab.add(new UnexpandableExpression(new ConstantExpression("a"), new DoubleCoefficients()),1);
		ab.add(new UnexpandableExpression(new ConstantExpression("b"),new DoubleCoefficients()),1);
		Map<Multiset<UnexpandableExpression>, AbstractExpression> p2m = new HashMap<Multiset<UnexpandableExpression>, AbstractExpression>();
		p2m.put(a2,DoubleExpression.ONE);
		p2m.put(ab,new DoubleExpression(2.0));
		p2m.put(b2,DoubleExpression.ONE);
		pA2p2ABpB2 = new Polynomial(p2m, new DoubleCoefficients());
		
		t0 = HashMultiset.<UnexpandableExpression>create();
		t0.add(new UnexpandableExpression(new DoubleExpression(0.0), new DoubleCoefficients()),1);
		
		t1 = HashMultiset.<UnexpandableExpression>create();
		t1.add(new UnexpandableExpression(new DoubleExpression(1.0),new DoubleCoefficients()),1);
		Map<Multiset<UnexpandableExpression>, AbstractExpression> p3m = new HashMap<Multiset<UnexpandableExpression>, AbstractExpression>();
		p3m.put(t1, DoubleExpression.ONE);
		p1 = new Polynomial(p3m, new DoubleCoefficients());
		
		t2 = HashMultiset.<UnexpandableExpression>create();
		t2.add(new UnexpandableExpression(new DoubleExpression(2.0), new DoubleCoefficients()),1);
		
		t3 = HashMultiset.<UnexpandableExpression>create();
		t3.add(new UnexpandableExpression(new DoubleExpression(2.0),new DoubleCoefficients()),2);
		
		t4 = HashMultiset.<UnexpandableExpression>create();
		t4.add(new UnexpandableExpression(new DoubleExpression(4.0),new DoubleCoefficients()),1);
		
		Map<Multiset<UnexpandableExpression>, AbstractExpression> p4m = new HashMap<Multiset<UnexpandableExpression>, AbstractExpression>();
		p4m.put(t2, DoubleExpression.ONE);
		p2 = new Polynomial(p4m, new DoubleCoefficients());
		
		Map<Multiset<UnexpandableExpression>, AbstractExpression> p5m = new HashMap<Multiset<UnexpandableExpression>, AbstractExpression>();
		p5m.put(a2, DoubleExpression.ONE);
		p5m.put(a, new DoubleExpression(2.0));
		p5m.put(ab, new DoubleExpression(3.0));
		pA2p2Ap3AB = new Polynomial(p5m, new DoubleCoefficients());
		
		Map<Multiset<UnexpandableExpression>, AbstractExpression> p6m = new HashMap<Multiset<UnexpandableExpression>, AbstractExpression>();
		p6m.put(a, DoubleExpression.ONE);
		p6m.put(t1, new DoubleExpression(2.0));
		p6m.put(b, new DoubleExpression(3.0));
		pAp2p3B = new Polynomial(p6m, new DoubleCoefficients());
		
		c = HashMultiset.<UnexpandableExpression>create();
		c.add(new UnexpandableExpression(new ConstantExpression("c"), new DoubleCoefficients()));
		
		d = HashMultiset.<UnexpandableExpression>create();
		d.add(new UnexpandableExpression(new ConstantExpression("d"), new DoubleCoefficients()));
		
		ad = HashMultiset.<UnexpandableExpression>create();
		ad.add(new UnexpandableExpression(new ConstantExpression("a"), new DoubleCoefficients()));
		ad.add(new UnexpandableExpression(new ConstantExpression("d"), new DoubleCoefficients()));
		
		bc = HashMultiset.<UnexpandableExpression>create();
		bc.add(new UnexpandableExpression(new ConstantExpression("b"),new DoubleCoefficients()));
		bc.add(new UnexpandableExpression(new ConstantExpression("c"), new DoubleCoefficients()));
		
		bd = HashMultiset.<UnexpandableExpression>create();
		bd.add(new UnexpandableExpression(new ConstantExpression("b"), new DoubleCoefficients()));
		bd.add(new UnexpandableExpression(new ConstantExpression("d"), new DoubleCoefficients()));
		
		Map<Multiset<UnexpandableExpression>, AbstractExpression> p2t1m = new HashMap<Multiset<UnexpandableExpression>, AbstractExpression>();
		p2t1m.put(t1, new DoubleExpression(2.0));
		p2t1 = new Polynomial(p2t1m, new DoubleCoefficients());
	}
	

}
