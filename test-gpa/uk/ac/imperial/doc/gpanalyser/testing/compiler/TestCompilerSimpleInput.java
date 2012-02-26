package uk.ac.imperial.doc.gpanalyser.testing.compiler;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Test;

import uk.ac.imperial.doc.gpepa.representation.components.*;
import uk.ac.imperial.doc.gpepa.representation.group.Group;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.representation.model.LabelledComponentGroup;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TestCompilerSimpleInput extends BaseCompilerTest {
	
	public TestCompilerSimpleInput() throws ParseException{
		super("test-gpa-inputs/simpleModel/model.gpepa");
	}
	
	
	@Test
	public void testConstants() {
		Constants constants = representation.getConstants();		
		Map<String, Double> constantsMap = constants.getConstantsMap();
		Map<String, Double> constantsMapExpected = new HashMap<String, Double>();
		constantsMapExpected.put("ra", 1.0);
		constantsMapExpected.put("rb", 0.1);
		constantsMapExpected.put("n", 10.0);
		assertEquals(constantsMapExpected, constantsMap);
	}
		
	@Test
	public void testComponentDefinitions() {
		Map<String, PEPAComponent> definitions = pctmc.getComponentDefinitions().getDefinitions();
		Map<String, PEPAComponent> definitionsExpected = new HashMap<String, PEPAComponent>();
		definitionsExpected.put("A", new Choice(Lists.newArrayList((AbstractPrefix) new Prefix("a", new ConstantExpression("ra"), null, new ComponentId("B"), new LinkedList<ImmediatePrefix>()))));
		definitionsExpected.put("B", new Choice(Lists.newArrayList((AbstractPrefix) new Prefix("b", new ConstantExpression("rb"), null, new ComponentId("A"), new LinkedList<ImmediatePrefix>()))));
		assertEquals(definitionsExpected, definitions);
	}
	
	@Test
	public void testModel() {		
		GroupedModel model = pctmc.getModel();
		Map<PEPAComponent, AbstractExpression> initCounts= new HashMap<PEPAComponent, AbstractExpression>();
		initCounts.put(new ComponentId("A"), new ConstantExpression("n"));
		GroupedModel modelExpected = new LabelledComponentGroup("As", new Group(initCounts));
		assertEquals(modelExpected, model);		
	}
	
	@Test
	public void testPCTMC() {
		PCTMC tmp = pctmc;
		State sAsA = new GPEPAState(new GroupComponentPair("As", new ComponentId("A")));
		State sAsB = new GPEPAState(new GroupComponentPair("As", new ComponentId("B")));
		assertEquals(Sets.newHashSet(sAsA,
				                     sAsB)
				, tmp.getStateIndex().keySet());
		Collection<EvolutionEvent> evolutionEvents = tmp.getEvolutionEvents();
		Collection<EvolutionEvent> evolutionEventsExpected = new LinkedList<EvolutionEvent>();
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(sAsA), Lists.newArrayList(sAsB),
				ProductExpression.create(new PopulationExpression(sAsA), new ConstantExpression("ra"))));
		
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(sAsB), Lists.newArrayList(sAsA),
				ProductExpression.create(new PopulationExpression(sAsB), new ConstantExpression("rb"))));

		assertEquals(evolutionEventsExpected, evolutionEvents);
	}
}