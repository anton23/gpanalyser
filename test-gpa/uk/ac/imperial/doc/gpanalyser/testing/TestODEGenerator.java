package uk.ac.imperial.doc.gpanalyser.testing;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.jexpressions.expanded.DoubleConstantCoefficients;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandingExpressionTransformerWithMoments;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.odeanalysis.ODEGenerator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TestODEGenerator {
	
	private static PCTMCInterpreter interpreter;
	
	@BeforeClass
	public static void prepareInterpreter() {
		interpreter = GPAPMain.createGPEPAInterpreter();
	}
	
	@Test
	public void testSimpleInputFirstMoments() throws ParseException {
		PCTMCFileRepresentation representation = TestCompilerSimpleInput.getRepresentation();

		CombinedPopulationProduct eA = parseCombinedProduct("As:A");
		CombinedPopulationProduct eB = parseCombinedProduct("As:B");
		
		ODEGenerator generator = new ODEGenerator(representation.getPctmc());
		generator.getODEMethodWithCombinedMoments(1, Lists.newArrayList(eA, eB));

		assertEquals(Sets.newHashSet(eA, eB), generator.getMomentIndex().keySet());
		assertEquals(parseExpression("- As:A * ra + As:B * rb"), expandExpression(generator.getRHS(eA)));
		assertEquals(parseExpression("As:A * ra - As:B * rb"), expandExpression(generator.getRHS(eB)));
		
	}
	
	@Test
	public void testSimpleInputSecondMoments() throws ParseException {
		PCTMCFileRepresentation representation = TestCompilerSimpleInput.getRepresentation();
		ODEGenerator generator = new ODEGenerator(representation.getPctmc());
		
		generator.getODEMethodWithCombinedMoments(2, new LinkedList<CombinedPopulationProduct>());
		assertEquals(parseCombinedProducts("As:A", "As:B", "As:A^2", "As:B^2", "As:A As:B"), generator.getMomentIndex().keySet());
		
		assertEquals(parseExpression("-2*As:A^2 * ra + As:A * ra + 2*As:A As:B * rb + As:B * rb"),
				expandExpression(generator.getRHS(parseCombinedProduct("As:A^2"))));	
		assertEquals(parseExpression("-2*As:B^2 * rb + As:B * rb + 2*As:A As:B * ra + As:A * ra"),
				expandExpression(generator.getRHS(parseCombinedProduct("As:B^2"))));		
		assertEquals(parseExpression("As:A^2 * ra - As:A As:B * ra - As:A * ra + As:B^2 * rb - As:A As:B * rb - As:B * rb"),
				expandExpression(generator.getRHS(parseCombinedProduct("As:A As:B"))));
	}
	
	
	@Test
	public void testSimpleFirstAccumulatedMoments() throws ParseException {
		PCTMCFileRepresentation representation = TestCompilerSimpleInput.getRepresentation();
		ODEGenerator generator = new ODEGenerator(representation.getPctmc());
		
		CombinedPopulationProduct accA = parseCombinedProduct("acc(As:A)");
		CombinedPopulationProduct accB = parseCombinedProduct("acc(As:B)");
		
		generator.getODEMethodWithCombinedMoments(2, Lists.newArrayList(accA, accB));
		assertEquals(parseCombinedProducts("As:A", "As:B", "As:A^2", "As:B^2", "As:A As:B", "acc(As:A)", "acc(As:B)"), generator.getMomentIndex().keySet());
		
		assertEquals(parseExpression("As:A"), expandExpression(generator.getRHS(accA)));
		assertEquals(parseExpression("As:B"), expandExpression(generator.getRHS(accB)));
	}
	
	@Test
	public void testSimpleSecondAccumulatedMoments() throws ParseException {
		PCTMCFileRepresentation representation = TestCompilerSimpleInput.getRepresentation();
		ODEGenerator generator = new ODEGenerator(representation.getPctmc());
		
		CombinedPopulationProduct accA2 = parseCombinedProduct("acc(As:A)^2");
		CombinedPopulationProduct accB2 = parseCombinedProduct("acc(As:B)^2");
		CombinedPopulationProduct accAs = parseCombinedProduct("acc(As:A^2)");
		
		generator.getODEMethodWithCombinedMoments(2, Lists.newArrayList(accA2, accB2, accAs));
		Set<CombinedPopulationProduct> expected = parseCombinedProducts("As:A", "As:B", "As:A^2", "As:B^2", "As:A As:B",
				                            "acc(As:A^2)", "As:A acc(As:A)", "As:A acc(As:B)",
				                           "As:B acc(As:A)", "As:B acc(As:B)", "acc(As:A)^2", "acc(As:B)^2");
		assertEquals(expected, generator.getMomentIndex().keySet());

		
		assertEquals(parseExpression("2* As:A acc(As:A)"), expandExpression(generator.getRHS(accA2)));
		assertEquals(parseExpression("2* As:B acc(As:B)"), expandExpression(generator.getRHS(accB2)));
		
		assertEquals(parseExpression("- As:A acc(As:A)* ra + As:B acc(As:A) * rb + As:A^2"),
				expandExpression(generator.getRHS(parseCombinedProduct("As:A acc(As:A)"))));
		assertEquals(parseExpression("  As:A acc(As:B)* ra - As:B acc(As:B) * rb + As:B^2"),
				expandExpression(generator.getRHS(parseCombinedProduct("As:B acc(As:B)"))));
		assertEquals(parseExpression("  As:A^2"),
				expandExpression(generator.getRHS(accAs)));
	}

	
	private CombinedPopulationProduct parseCombinedProduct(String string) throws ParseException{
		return (CombinedPopulationProduct) interpreter.parseGenericRule(string, "combinedPowerProduct", false);
	}
	
	private Set<CombinedPopulationProduct> parseCombinedProducts(String... string) throws ParseException{
		Set<CombinedPopulationProduct> ret = new HashSet<CombinedPopulationProduct>();
		for  (String s:string) {
			ret.add((CombinedPopulationProduct) interpreter.parseGenericRule(s, "combinedPowerProduct", false));
		}
		return ret;
	}
	
	
	private ExpandedExpression parseExpression(String s) throws ParseException {
		return expandExpression(interpreter.parseExpressionList(s).get(0));		
	}
	
	private ExpandedExpression expandExpression(AbstractExpression e) {
		ExpandingExpressionTransformerWithMoments t = new ExpandingExpressionTransformerWithMoments(new DoubleConstantCoefficients());
		e.accept(t);
		return t.getResult();
	}
}
