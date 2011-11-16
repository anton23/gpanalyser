package uk.ac.imperial.doc.gpanalyser.testing.odes;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.gpanalyser.testing.compiler.TestCompilerClientServer;
import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.odeanalysis.NewODEGenerator;
public class TestODEGenerator extends NewODEGenerator{
	
	
	
	public TestODEGenerator() throws ParseException {
		super(new TestCompilerClientServer().getRepresentation().getPctmc());
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
		AbstractExpression rhs = generateODE(eClient2);
		
		ExpandedExpression expandedExpected = TestODEGeneratorExpectedODEs.expandExpression(expected);
		ExpandedExpression expandedRhs = TestODEGeneratorExpectedODEs.expandExpression(rhs);
		assertEquals(expandedExpected, expandedRhs);
	}

}
