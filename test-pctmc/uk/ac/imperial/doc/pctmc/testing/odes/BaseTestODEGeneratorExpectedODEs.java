package uk.ac.imperial.doc.pctmc.testing.odes;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.ANTLRFileStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.common.collect.Sets;

import uk.ac.imperial.doc.jexpressions.expanded.DoubleConstantCoefficients;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandedExpression;
import uk.ac.imperial.doc.jexpressions.expanded.ExpandingExpressionTransformerWithMoments;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinusExpression;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CollectUsedMomentsVisitor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.ExpressionVariableSetterPCTMC;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.odeanalysis.NewODEGenerator;
import uk.ac.imperial.doc.pctmc.odeanalysis.closures.NormalMomentClosure;


@RunWith(Parameterized.class)
public abstract class BaseTestODEGeneratorExpectedODEs {
	

	protected PCTMCInterpreter interpreter;
	protected PCTMCFileRepresentation representation;
	protected String file;
	
	protected abstract PCTMCInterpreter initialiseInterpreter();
	protected abstract String getPath();

	public BaseTestODEGeneratorExpectedODEs(String file) throws ParseException {
		this.interpreter  = initialiseInterpreter();
		this.representation = interpreter.parsePCTMCFile(getPath() + file + "/model.gpepa");		
		this.file = file;
	}
	
	@Test
	public void testFirstMoments() throws ParseException, IllegalArgumentException, SecurityException, IOException, IllegalAccessException, NoSuchFieldException {
		checkExpectedODEs(getPath() + file + "/firstOrder");
	}
	
	@Test
	public void testSecondMoments() throws ParseException, IllegalArgumentException, SecurityException, IOException, IllegalAccessException, NoSuchFieldException {
		checkExpectedODEs(getPath() + file + "/secondOrder");		
	}
	
	
	@Test
	public void testFirstAccumulatedMoments() throws ParseException, IllegalArgumentException, SecurityException, IOException, IllegalAccessException, NoSuchFieldException {
		checkExpectedODEs(getPath() + file + "/accFirstOrder");
	}
	
	@Test
	public void testSecondAccumulatedMoments() throws ParseException, IllegalArgumentException, SecurityException, IOException, IllegalAccessException, NoSuchFieldException {
		checkExpectedODEs(getPath() + file + "/accSecondOrder");
	}
	

	@SuppressWarnings("unchecked")
	protected void checkExpectedODEs(String file) throws ParseException, IOException, IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		Object compilerReturn = interpreter.parseGenericRule(new ANTLRFileStream(file), "odeTest", false);
		List<AbstractExpression> expressions = 
			(List<AbstractExpression>) compilerReturn.getClass().getField("moments").get(compilerReturn);
		Map<CombinedPopulationProduct, AbstractExpression> expectedODEs = (Map<CombinedPopulationProduct, AbstractExpression>) 
			compilerReturn.getClass().getField("odes").get(compilerReturn);
		
		for (AbstractExpression rhs:expectedODEs.values()) {
			ExpressionVariableSetterPCTMC setter = new ExpressionVariableSetterPCTMC(
					representation.getUnfoldedVariables());
			rhs.accept(setter);
		}		Set<CombinedPopulationProduct> moments = new HashSet<CombinedPopulationProduct>();
		for (AbstractExpression e:expressions) {
			CollectUsedMomentsVisitor visitor = new CollectUsedMomentsVisitor();
			e.accept(visitor);
			moments.addAll(visitor.getUsedCombinedMoments());	
		}
		
		int order = 0;
		for (CombinedPopulationProduct m:moments) {
			order = Math.max(order, m.getOrder());
		}
		NewODEGenerator generator = new NewODEGenerator(representation.getPctmc(), new NormalMomentClosure(order));
		generator.getODEMethodWithCombinedMoments(moments);		
		for (Map.Entry<CombinedPopulationProduct, AbstractExpression> e:expectedODEs.entrySet()) {
			ExpandedExpression expectedExpanded = expandExpression(e.getValue());
			ExpandedExpression actualExpanded = expandExpression((generator.getRHS(e.getKey())));
			assertEquals("ODE for moment " + e.getKey() + ", difference:\n"
					+ expandExpression(new MinusExpression(e.getValue(), generator.getRHS(e.getKey()))).toAbstractExpression()+"\n",
					expectedExpanded, actualExpanded);
			
		}
		assertEquals("The system of ODEs has some additional equations " + Sets.difference(generator.getMomentIndex().keySet(), expectedODEs.keySet()), expectedODEs.keySet(), generator.getMomentIndex().keySet());
	}
	
	protected CombinedPopulationProduct parseCombinedProduct(String string) throws ParseException{
		return (CombinedPopulationProduct) interpreter.parseGenericRule(string, "combinedPowerProduct", false);
	}
	
	public static Set<CombinedPopulationProduct> parseCombinedProducts(PCTMCInterpreter interpreter, String... string) throws ParseException{
		Set<CombinedPopulationProduct> ret = new HashSet<CombinedPopulationProduct>();
		for  (String s:string) {
			ret.add((CombinedPopulationProduct) interpreter.parseGenericRule(s, "combinedPowerProduct", false));
		}
		return ret;
	}
	
	
	protected ExpandedExpression parseExpression(String s) throws ParseException {
		return expandExpression(interpreter.parseExpressionList(s).get(0));		
	}
	
	public static ExpandedExpression expandExpression(AbstractExpression e) {
		ExpandingExpressionTransformerWithMoments t = new ExpandingExpressionTransformerWithMoments(new DoubleConstantCoefficients());
		e.accept(t);
		ExpandedExpression ret = t.getResult();
		return ret;
	}
}
