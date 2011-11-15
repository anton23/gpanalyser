package uk.ac.imperial.doc.gpanalyser.testing.odes;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.ANTLRFileStream;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.jexpressions.expanded.DoubleConstantCoefficients;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandingExpressionTransformerWithMoments;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinusExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.odeanalysis.ODEGenerator;

public class TestODEGeneratorBase {
	
	protected static PCTMCInterpreter interpreter;
	protected PCTMCFileRepresentation representation;
	
	public TestODEGeneratorBase(PCTMCFileRepresentation representation) {
		this.representation = representation;
		interpreter = GPAPMain.createGPEPAInterpreter();
	}
	
	@SuppressWarnings("unchecked")
	protected void checkExpectedODEs(String file) throws ParseException, IOException, IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		Object compilerReturn = interpreter.parseGenericRule(new ANTLRFileStream(file), "odeTest", false);
		List<CombinedPopulationProduct> moments = 
			(List<CombinedPopulationProduct>) compilerReturn.getClass().getField("moments").get(compilerReturn);
		Map<CombinedPopulationProduct, AbstractExpression> expectedODEs = (Map<CombinedPopulationProduct, AbstractExpression>) 
			compilerReturn.getClass().getField("odes").get(compilerReturn);
		
		int order = 0;
		for (CombinedPopulationProduct m:moments) {
			order = Math.max(order, m.getOrder());
		}
		ODEGenerator generator = new ODEGenerator(representation.getPctmc());
		generator.getODEMethodWithCombinedMoments(order, moments);
		
		//assertEquals(expectedODEs.keySet(), generator.getMomentIndex().keySet());
		for (Map.Entry<CombinedPopulationProduct, AbstractExpression> e:expectedODEs.entrySet()) {
			ExpandedExpression expectedExpanded = expandExpression(e.getValue());
			ExpandedExpression actualExpanded = expandExpression((generator.getRHS(e.getKey())));
			assertEquals("ODE for moment " + e.getKey() + ", difference:\n"
					+ expandExpression(new MinusExpression(e.getValue(), generator.getRHS(e.getKey()))).toAbstractExpression()+"\n",
					expectedExpanded, actualExpanded);
			
		}
	}
	
	protected CombinedPopulationProduct parseCombinedProduct(String string) throws ParseException{
		return (CombinedPopulationProduct) interpreter.parseGenericRule(string, "combinedPowerProduct", false);
	}
	
	protected Set<CombinedPopulationProduct> parseCombinedProducts(String... string) throws ParseException{
		Set<CombinedPopulationProduct> ret = new HashSet<CombinedPopulationProduct>();
		for  (String s:string) {
			ret.add((CombinedPopulationProduct) interpreter.parseGenericRule(s, "combinedPowerProduct", false));
		}
		return ret;
	}
	
	
	protected ExpandedExpression parseExpression(String s) throws ParseException {
		return expandExpression(interpreter.parseExpressionList(s).get(0));		
	}
	
	protected ExpandedExpression expandExpression(AbstractExpression e) {
		ExpandingExpressionTransformerWithMoments t = new ExpandingExpressionTransformerWithMoments(new DoubleConstantCoefficients());
		e.accept(t);
		return t.getResult();
	}
}
