package uk.ac.imperial.doc.gpanalyser.testing.compiler;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Test;

import uk.ac.imperial.doc.gpepa.representation.components.AbstractPrefix;
import uk.ac.imperial.doc.gpepa.representation.components.Choice;
import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.Prefix;
import uk.ac.imperial.doc.gpepa.representation.group.Group;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.igpepa.representation.model.iPEPAGroupCooperation;
import uk.ac.imperial.doc.igpepa.representation.model.iPEPALabelledComponentGroup;
import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


public class TestCompilerClientServer extends BaseCompilerTest {

	public TestCompilerClientServer() throws ParseException {
		super("test-gpa-inputs/clientServer/model.gpepa");
	}
	
	@Test
	public void testConstants() {
		Constants constants = representation.getConstants();		
		Map<String, Double> constantsMap = constants.getConstantsMap();
		Map<String, Double> constantsMapExpected = new HashMap<String, Double>();
		constantsMapExpected.put("rr", 2.0);
		constantsMapExpected.put("rt", 0.27);
		constantsMapExpected.put("rb", 0.1);
		constantsMapExpected.put("rd", 1.0);
		constantsMapExpected.put("rrst", 1.0);
		constantsMapExpected.put("n", 100.0);
		constantsMapExpected.put("m", 60.0);
		assertEquals(constantsMapExpected, constantsMap);
	}
	
	@Test
	public void testComponentDefinitions() {
		Map<String, PEPAComponent> definitions = pctmc.getComponentDefinitions().getDefinitions();
		Map<String, PEPAComponent> definitionsExpected = new HashMap<String, PEPAComponent>();
		definitionsExpected.put("Client", new Choice(Lists.newArrayList(
                (AbstractPrefix) new Prefix("request", new ConstantExpression("rr"), null, new ComponentId("Client_waiting")))));
		definitionsExpected.put("Client_waiting", new Choice(Lists.newArrayList(
                (AbstractPrefix) new Prefix("data", new ConstantExpression("rd"), null, new ComponentId("Client_think")))));
		definitionsExpected.put("Client_think", new Choice(Lists.newArrayList(
                (AbstractPrefix) new Prefix("think", new ConstantExpression("rt"), null, new ComponentId("Client")))));
		
		definitionsExpected.put("Server", new Choice(Lists.newArrayList(
                (AbstractPrefix) new Prefix("request", new ConstantExpression("rr"), null, new ComponentId("Server_get")),
                (AbstractPrefix) new Prefix("break", new ConstantExpression("rb"), null, new ComponentId("Server_broken")))));
		definitionsExpected.put("Server_get", new Choice(Lists.newArrayList(
                (AbstractPrefix) new Prefix("data", new ConstantExpression("rd"), null, new ComponentId("Server")))));
		definitionsExpected.put("Server_broken", new Choice(Lists.newArrayList(
                (AbstractPrefix) new Prefix("reset", new ConstantExpression("rrst"), null, new ComponentId("Server")))));

		assertEquals(definitionsExpected, definitions);
	}
	
	@Test
	public void testModel() {		
		GroupedModel model = pctmc.getModel();
		Map<PEPAComponent, AbstractExpression> initCountsClient= new HashMap<PEPAComponent, AbstractExpression>();
		initCountsClient.put(new ComponentId("Client"), new ConstantExpression("n"));
		
		Map<PEPAComponent, AbstractExpression> initCountsServer= new HashMap<PEPAComponent, AbstractExpression>();
		initCountsServer.put(new ComponentId("Server"), new ConstantExpression("m"));
		GroupedModel modelExpected = new iPEPAGroupCooperation(
				new iPEPALabelledComponentGroup("Clients", new Group(initCountsClient)), 
				new iPEPALabelledComponentGroup("Servers", new Group(initCountsServer)), 
				Sets.newHashSet("request", "data"));
		
		assertEquals(modelExpected, model);		
	}
	
	@Test
	public void testPCTMC() {
		PCTMC tmp = pctmc;
		State sClient = new GPEPAState(new GroupComponentPair("Clients", new ComponentId("Client")));
		State sClientWaiting = new GPEPAState(new GroupComponentPair("Clients", new ComponentId("Client_waiting")));
		State sClientThink = new GPEPAState(new GroupComponentPair("Clients", new ComponentId("Client_think")));
		
		State sServer = new GPEPAState(new GroupComponentPair("Servers", new ComponentId("Server")));
		State sServerGet = new GPEPAState(new GroupComponentPair("Servers", new ComponentId("Server_get")));
		State sServerBroken = new GPEPAState(new GroupComponentPair("Servers", new ComponentId("Server_broken")));
		
		AbstractExpression rr = new ConstantExpression("rr");
		AbstractExpression rd = new ConstantExpression("rd");
		
		assertEquals(Sets.newHashSet(sClient, sClientWaiting, sClientThink, sServer, sServerGet, sServerBroken)
				, tmp.getStateIndex().keySet());
		
		Collection<EvolutionEvent> evolutionEvents = tmp.getEvolutionEvents();
		Collection<EvolutionEvent> evolutionEventsExpected = new LinkedList<EvolutionEvent>();

		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(sClient, sServer), Lists.newArrayList(sClientWaiting, sServerGet),
				MinExpression.create(ProductExpression.create(CombinedProductExpression.createMeanExpression(sClient), rr),
					                 ProductExpression.create(CombinedProductExpression.createMeanExpression(sServer), rr))
						                 ));
		
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(sClientWaiting, sServerGet), Lists.newArrayList(sClientThink, sServer),
				MinExpression.create(ProductExpression.create(CombinedProductExpression.createMeanExpression(sClientWaiting), rd),
						             ProductExpression.create(CombinedProductExpression.createMeanExpression(sServerGet), rd))
			    ));
		
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(sClientThink), Lists.newArrayList(sClient),
				ProductExpression.create(CombinedProductExpression.createMeanExpression(sClientThink), new ConstantExpression("rt"))));
		
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(sServer), Lists.newArrayList(sServerBroken),
				ProductExpression.create(CombinedProductExpression.createMeanExpression(sServer), new ConstantExpression("rb"))));
		
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(sServerBroken), Lists.newArrayList(sServer),
				ProductExpression.create(CombinedProductExpression.createMeanExpression(sServerBroken), new ConstantExpression("rrst"))));
		assertEquals(new HashSet<EvolutionEvent>(evolutionEventsExpected), new HashSet<EvolutionEvent>(evolutionEvents));
	}
	

}
