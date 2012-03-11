package uk.ac.imperial.doc.gpanalyser.testing.odes;


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.antlr.runtime.ANTLRFileStream;
import org.junit.Test;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.gpanalyser.testing.compiler.TestCompilerClientServer;
import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinusExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.odeanalysis.NewODEGenerator;
import uk.ac.imperial.doc.pctmc.odeanalysis.closures.NormalMomentClosure;
import uk.ac.imperial.doc.pctmc.testing.odes.BaseTestODEGeneratorExpectedODEs;
public class TestODEGenerator extends NewODEGenerator{
	
	protected PCTMCInterpreter interpreter;
	
	public TestODEGenerator() throws ParseException {
		super(new TestCompilerClientServer().getRepresentation().getPctmc(), new NormalMomentClosure(2));
		interpreter = GPAPMain.createGPEPAInterpreter();
	}

	@Test
	public void testGenerateODE() throws ParseException {
		PopulationProduct eClient = PopulationProduct.getMeanProduct(new GPEPAState(new GroupComponentPair("Clients", new ComponentId("Client"))));
		PopulationProduct eClient2 = PopulationProduct.getProduct(eClient, eClient);
		
		PCTMCInterpreter interpreter = GPAPMain.createGPEPAInterpreter();
		AbstractExpression expected = interpreter
				.parseExpressionList(
						"-2*min(Clients:Client^2 * rr, Clients:Client Servers:Server * rr) + min(Clients:Client * rr, Servers:Server * rr) + 2* Clients:Client Clients:Client_think*rt + Clients:Client_think*rt")
				.get(0);
		AbstractExpression rhs = getDerivativeOfMoment(eClient2);
		
		ExpandedExpression expandedExpected = TestGPEPAExpectedODEs.expandExpression(expected);
		ExpandedExpression expandedRhs = TestGPEPAExpectedODEs.expandExpression(rhs);
		assertEquals(expandedExpected, expandedRhs);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGenerateSystem() throws ParseException, IOException, IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		generateODESystem(BaseTestODEGeneratorExpectedODEs.parseCombinedProducts(interpreter, "acc(Clients:Client)"));
		Object compilerReturn = interpreter.parseGenericRule(new ANTLRFileStream("test-gpa-inputs/clientServer/accFirstOrder"), "odeTest", false);
		
		Map<CombinedPopulationProduct, AbstractExpression> expectedODEs = (Map<CombinedPopulationProduct, AbstractExpression>) 
			compilerReturn.getClass().getField("odes").get(compilerReturn);

		for (Map.Entry<CombinedPopulationProduct, AbstractExpression> e:expectedODEs.entrySet()) {
			ExpandedExpression expectedExpanded = TestGPEPAExpectedODEs.expandExpression(e.getValue());
			ExpandedExpression actualExpanded = TestGPEPAExpectedODEs.expandExpression((m_rhs.get(e.getKey())));
			assertEquals("ODE for moment " + e.getKey() + ", difference:\n"
					+ TestGPEPAExpectedODEs.expandExpression(new MinusExpression(e.getValue(), m_rhs.get(e.getKey()))).toAbstractExpression()+"\n",
					expectedExpanded, actualExpanded);			
		}
	}
}