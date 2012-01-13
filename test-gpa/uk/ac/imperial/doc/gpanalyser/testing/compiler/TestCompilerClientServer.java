package uk.ac.imperial.doc.gpanalyser.testing.compiler;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import uk.ac.imperial.doc.gpepa.representation.components.Choice;
import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.Prefix;
import uk.ac.imperial.doc.gpepa.representation.group.Group;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupCooperation;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.representation.model.LabelledComponentGroup;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
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
		definitionsExpected.put("Client", new Choice((List<Prefix>)Lists.newArrayList(
				new Prefix("request", new ConstantExpression("rr"), new ComponentId("Client_waiting")))));
		definitionsExpected.put("Client_waiting", new Choice((List<Prefix>)Lists.newArrayList(
				new Prefix("data", new ConstantExpression("rd"), new ComponentId("Client_think")))));
		definitionsExpected.put("Client_think", new Choice((List<Prefix>)Lists.newArrayList(
				new Prefix("think", new ConstantExpression("rt"), new ComponentId("Client")))));
		
		definitionsExpected.put("Server", new Choice((List<Prefix>)Lists.newArrayList(
				new Prefix("request", new ConstantExpression("rr"), new ComponentId("Server_get")),
				new Prefix("break", new ConstantExpression("rb"), new ComponentId("Server_broken")))));
		definitionsExpected.put("Server_get", new Choice((List<Prefix>)Lists.newArrayList(
				new Prefix("data", new ConstantExpression("rd"), new ComponentId("Server")))));
		definitionsExpected.put("Server_broken", new Choice((List<Prefix>)Lists.newArrayList(
				new Prefix("reset", new ConstantExpression("rrst"), new ComponentId("Server")))));

		assertEquals(definitionsExpected, definitions);
	}
	
	@Test
	public void testModel() {		
		GroupedModel model = pctmc.getModel();
		Map<PEPAComponent, AbstractExpression> initCountsClient= new HashMap<PEPAComponent, AbstractExpression>();
		initCountsClient.put(new ComponentId("Client"), new ConstantExpression("n"));
		
		Map<PEPAComponent, AbstractExpression> initCountsServer= new HashMap<PEPAComponent, AbstractExpression>();
		initCountsServer.put(new ComponentId("Server"), new ConstantExpression("m"));
		GroupedModel modelExpected = new GroupCooperation(
				new LabelledComponentGroup("Clients", new Group(initCountsClient)), 
				new LabelledComponentGroup("Servers", new Group(initCountsServer)), 
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
				MinExpression.create(ProductExpression.create(new PopulationExpression(sClient), rr),
					                 ProductExpression.create(new PopulationExpression(sServer), rr))
						                 ));
		
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(sClientWaiting, sServerGet), Lists.newArrayList(sClientThink, sServer),
				MinExpression.create(ProductExpression.create(new PopulationExpression(sClientWaiting), rd),
						             ProductExpression.create(new PopulationExpression(sServerGet), rd))
			    ));
		
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(sClientThink), Lists.newArrayList(sClient),
				ProductExpression.create(new PopulationExpression(sClientThink), new ConstantExpression("rt"))));
		
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(sServer), Lists.newArrayList(sServerBroken),
				ProductExpression.create(new PopulationExpression(sServer), new ConstantExpression("rb"))));
		
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(sServerBroken), Lists.newArrayList(sServer),
				ProductExpression.create(new PopulationExpression(sServerBroken), new ConstantExpression("rrst"))));
		assertEquals(new HashSet<EvolutionEvent>(evolutionEventsExpected), new HashSet<EvolutionEvent>(evolutionEvents));
	}
	

}
