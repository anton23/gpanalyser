package uk.ac.imperial.doc.masspa.tests.integration.compiler;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinusExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.masspa.representation.components.ChoiceComponent;
import uk.ac.imperial.doc.masspa.representation.components.ConstComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAMessage;
import uk.ac.imperial.doc.masspa.representation.components.Prefix;
import uk.ac.imperial.doc.masspa.representation.components.ReceivePrefix;
import uk.ac.imperial.doc.masspa.representation.components.SendPrefix;
import uk.ac.imperial.doc.masspa.representation.model.AllLocation;
import uk.ac.imperial.doc.masspa.representation.model.Location;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAAgentPop;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAChannel;
import uk.ac.imperial.doc.masspa.representation.model.VarLocation;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.Lists;

public class MASSPACompilerWSNASynchTest extends BaseCompilerTest
{
	public MASSPACompilerWSNASynchTest() throws ParseException
	{
		super("test-masspa-inputs/wsn/wsn_async.masspa");
	}
	
	private State getPop(final String _name, final String _loc)
	{
		if (_name.startsWith("#"))
		{
			String name = _name.replaceFirst("#", "");
			if (_loc.equals("x"))
			{
				return pctmc.getModel().getActionCount(name, VarLocation.getInstance(), 0);
			}
			else if (_loc.equals("A"))
			{
				return pctmc.getModel().getActionCount(name, AllLocation.getInstance(), 0);
			}
			else
			{
				return pctmc.getModel().getActionCount(name, new Location(Lists.newArrayList(Integer.parseInt(_loc))), 0);
			}	
		}
		else
		{
			if (_loc.equals("x"))
			{
				return pctmc.getModel().getAgentPop(_name, VarLocation.getInstance(), 0);
			}
			else if (_loc.equals("A"))
			{
				return pctmc.getModel().getAgentPop(_name, AllLocation.getInstance(), 0);
			}
			else
			{
				return pctmc.getModel().getAgentPop(_name, new Location(Lists.newArrayList(Integer.parseInt(_loc))), 0);
			}
		}
	}
	
	@Test
	public void testSpatialConstants()
	{
		Constants constants = representation.getConstants();		
		Map<String, Double> constantsMap = constants.getConstantsMap();
		Map<String, Double> constantsMapExpected = new HashMap<String, Double>();
		constantsMapExpected.put("mu@(x)", 0.1);
		constantsMapExpected.put("mu@(0)", 0.1);
		constantsMapExpected.put("mu@(1)", 0.15);
		constantsMapExpected.put("mu@(2)", 0.1);
		constantsMapExpected.put("pop@(x)", 300.0);
		constantsMapExpected.put("pop@(0)", 300.0);
		constantsMapExpected.put("pop@(1)", 300.0);
		constantsMapExpected.put("pop@(2)", 450.0);
		assertEquals(constantsMapExpected, constantsMap);
	}

	@Test
	public void testSpatialVariables()
	{
		Map<ExpressionVariable, AbstractExpression> varMap = representation.getUnfoldedVariables();		
		Map<ExpressionVariable, AbstractExpression> varMapExpected = new HashMap<ExpressionVariable, AbstractExpression>();
		Map<State, Integer> rateAtX = new HashMap<State, Integer>();
		rateAtX.put(getPop("Off","x"),1); rateAtX.put(getPop("On","x"),1);
		rateAtX.put(pctmc.getModel().getAgentPop("On", VarLocation.getInstance(), 0),1);
		varMapExpected.put(new ExpressionVariable("myRate@(x)"), SumExpression.create(ProductExpression.create(new DoubleExpression(2.0),CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(rateAtX)))), new DoubleExpression(10.0)));
		Map<State, Integer> rateAt0 = new HashMap<State, Integer>();
		rateAt0.put(getPop("Off","0"),1); rateAt0.put(getPop("On","0"),1);
		varMapExpected.put(new ExpressionVariable("myRate@(0)"), SumExpression.create(ProductExpression.create(new DoubleExpression(2.0),CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(rateAt0)))), new DoubleExpression(10.0)));
		Map<State, Integer> rateAt1 = new HashMap<State, Integer>();
		rateAt1.put(getPop("#turn_on", "1"),1);
		varMapExpected.put(new ExpressionVariable("myRate@(1)"), new MinusExpression(ProductExpression.create(new DoubleExpression(2.0),CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(rateAt1)))), new DoubleExpression(10.0)));
		Map<State, Integer> rateAt2 = new HashMap<State, Integer>();
		rateAt2.put(getPop("Off","1"),1); rateAt2.put(getPop("On","2"),1);
		varMapExpected.put(new ExpressionVariable("myRate@(2)"), SumExpression.create(ProductExpression.create(new DoubleExpression(4.0),CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(rateAt2)))), new DoubleExpression(20.0)));
		assertEquals(varMap, varMapExpected);
	}
	
	@Test
	public void testAgentDefinitions()
	{
		Map<MASSPAComponent, MASSPAComponent> agentStateDefs = new HashMap<MASSPAComponent, MASSPAComponent>();
		for (MASSPAComponent c : pctmc.getModel().getMASSPAAgents().getComponents())
		{
			if (c instanceof ConstComponent)
			{
				agentStateDefs.put(c, c.getDefinition());
			}
		}
		Map<MASSPAComponent, MASSPAComponent> agentStateDefsExpected = new HashMap<MASSPAComponent, MASSPAComponent>();
		ConstComponent on = new ConstComponent("On");
		ConstComponent off = new ConstComponent("Off");
		on.define(new ChoiceComponent(Lists.newArrayList((Prefix)new SendPrefix("turn_off",new ConstantExpression("mu@(x)"),new MASSPAMessage("M"),new DoubleExpression(1.0),off))), 0);
		off.define(new ChoiceComponent(Lists.newArrayList((Prefix)new ReceivePrefix("turn_on",new MASSPAMessage("M"),new DoubleExpression(1.0),on), new Prefix("",new DoubleExpression(0.00001),on))), 0);
		agentStateDefsExpected.put(on,on.getDefinition());
		agentStateDefsExpected.put(off,off.getDefinition());
		assertEquals(agentStateDefsExpected, agentStateDefs);
	}
	
	@Test
	public void testInitAgentPopulations()
	{
		Map<State,AbstractExpression> initPops = pctmc.getInitMap();
		Map<State,AbstractExpression> initPopsExpected = new HashMap<State,AbstractExpression>();
		
		initPopsExpected.put(getPop("On","0"), new ConstantExpression("pop@(0)"));
		initPopsExpected.put(getPop("On","1"), new DoubleExpression(0.0));
		initPopsExpected.put(getPop("On","2"), new DoubleExpression(0.0));
		initPopsExpected.put(getPop("Off","0"), new DoubleExpression(0.0));
		initPopsExpected.put(getPop("Off","1"), new ConstantExpression("pop@(1)"));
		initPopsExpected.put(getPop("Off","2"), new ConstantExpression("pop@(2)"));
		initPopsExpected.put(getPop("#turn_on","0"), new DoubleExpression(0.0));
		initPopsExpected.put(getPop("#turn_on","1"), new DoubleExpression(10.0));
		initPopsExpected.put(getPop("#turn_on","2"), new DoubleExpression(0.0));
		initPopsExpected.put(getPop("#turn_on","A"), new DoubleExpression(10.0));
		initPopsExpected.put(getPop("#turn_off","0"), new DoubleExpression(10.0));
		initPopsExpected.put(getPop("#turn_off","1"), new DoubleExpression(0.0));
		initPopsExpected.put(getPop("#turn_off","2"), new DoubleExpression(0.0));
		initPopsExpected.put(getPop("#turn_off","A"), new DoubleExpression(10.0));	
		
		assertEquals(initPopsExpected, initPops);
	}
	
	@Test
	public void testChannels()
	{
		Set<MASSPAChannel> chans1 = pctmc.getModel().getAllChannels((MASSPAAgentPop)getPop("Off","0"), new MASSPAMessage("M"));
		Set<MASSPAChannel> chans1Expected = new HashSet<MASSPAChannel>();
		chans1Expected.add(new MASSPAChannel((MASSPAAgentPop)getPop("On","1"),(MASSPAAgentPop)getPop("Off","0"),new MASSPAMessage("M"),new DoubleExpression(1.0/300.0),MASSPAChannel.RateType.MASSACTION_ASYNC));
		assertEquals(chans1Expected, chans1);
		
		Set<MASSPAChannel> chans2=pctmc.getModel().getAllChannels((MASSPAAgentPop)getPop("Off","1"), new MASSPAMessage("M"));
		Set<MASSPAChannel> chans2Expected = new HashSet<MASSPAChannel>();
		chans2Expected.add(new MASSPAChannel((MASSPAAgentPop)getPop("On","0"),(MASSPAAgentPop)getPop("Off","1"),new MASSPAMessage("M"),new DoubleExpression(1.0/300.0),MASSPAChannel.RateType.MASSACTION_ASYNC));
		chans2Expected.add(new MASSPAChannel((MASSPAAgentPop)getPop("On","2"),(MASSPAAgentPop)getPop("Off","1"),new MASSPAMessage("M"),new DoubleExpression(1.0/450.0),MASSPAChannel.RateType.MASSACTION_ASYNC));
		assertEquals(chans2Expected, chans2);
		
		Set<MASSPAChannel> chans3=pctmc.getModel().getAllChannels((MASSPAAgentPop)getPop("Off","2"), new MASSPAMessage("M"));
		Set<MASSPAChannel> chans3Expected = new HashSet<MASSPAChannel>();
		chans3Expected.add(new MASSPAChannel((MASSPAAgentPop)getPop("On","1"),(MASSPAAgentPop)getPop("Off","2"),new MASSPAMessage("M"),new DoubleExpression(1.0/300.0),MASSPAChannel.RateType.MASSACTION_ASYNC));
		assertEquals(chans3Expected, chans3);
	}
	
	@Test
	public void testPCTMC()
	{
		Collection<EvolutionEvent> evolutionEvents = pctmc.getEvolutionEvents();
		Collection<EvolutionEvent> evolutionEventsExpected = new LinkedList<EvolutionEvent>();
		
		// Evo's @(0)
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(getPop("On","0")), Lists.newArrayList(getPop("Off","0"),getPop("#turn_off","0"),getPop("#turn_off","A")),
				ProductExpression.create(new PopulationExpression(getPop("On","0")), new DoubleExpression(0.1))));
		Map<State, Integer> rateAt0 = new HashMap<State, Integer>();
		rateAt0.put(getPop("Off","0"),1); rateAt0.put(getPop("On","1"),1);
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(getPop("Off","0")), Lists.newArrayList(getPop("#turn_on","0"),getPop("#turn_on","A"),getPop("On","0")),
				ProductExpression.create(ProductExpression.create(CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(rateAt0))),new DoubleExpression(0.15/300.0)))));
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(getPop("Off","0")), Lists.newArrayList(getPop("On","0")),
				ProductExpression.create(new PopulationExpression(getPop("Off","0")), new DoubleExpression(0.00001))));
		
		
		// Evo's @(2)
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(getPop("On","2")), Lists.newArrayList(getPop("Off","2"),getPop("#turn_off","2"),getPop("#turn_off","A")),
				ProductExpression.create(new PopulationExpression(getPop("On","2")), new DoubleExpression(0.1))));
		Map<State, Integer> rateAt2 = new HashMap<State, Integer>();
		rateAt2.put(getPop("Off","2"),1); rateAt2.put(getPop("On","1"),1);
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(getPop("Off","2")), Lists.newArrayList(getPop("#turn_on","2"),getPop("#turn_on","A"),getPop("On","2")),
				ProductExpression.create(ProductExpression.create(CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(rateAt2))),new DoubleExpression(0.15/450.0)))));
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(getPop("Off","2")), Lists.newArrayList(getPop("On","2")),
				ProductExpression.create(new PopulationExpression(getPop("Off","2")), new DoubleExpression(0.00001))));
		
		// Evo's @(1)
		Map<State, Integer> rateAt11 = new HashMap<State, Integer>();
		rateAt11.put(getPop("Off","1"),1); rateAt11.put(getPop("On","0"),1);
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(getPop("Off","1")), Lists.newArrayList(getPop("#turn_on","1"),getPop("#turn_on","A"),getPop("On","1")),
				ProductExpression.create(ProductExpression.create(CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(rateAt11))),new DoubleExpression(0.1/300.0)))));
		Map<State, Integer> rateAt12 = new HashMap<State, Integer>();
		rateAt12.put(getPop("Off","1"),1); rateAt12.put(getPop("On","2"),1);
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(getPop("Off","1")), Lists.newArrayList(getPop("#turn_on","1"),getPop("#turn_on","A"),getPop("On","1")),
				ProductExpression.create(ProductExpression.create(CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(rateAt12))),new DoubleExpression(0.1/300.0)))));
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(getPop("Off","1")), Lists.newArrayList(getPop("On","1")),
				ProductExpression.create(new PopulationExpression(getPop("Off","1")), new DoubleExpression(0.00001))));
		evolutionEventsExpected.add(new EvolutionEvent(Lists.newArrayList(getPop("On","1")), Lists.newArrayList(getPop("Off","1"),getPop("#turn_off","1"),getPop("#turn_off","A")),
				ProductExpression.create(new PopulationExpression(getPop("On","1")), new DoubleExpression(0.15))));
		
		Iterator<EvolutionEvent> iter = evolutionEvents.iterator();
		Iterator<EvolutionEvent> iterExp = evolutionEventsExpected.iterator();
		
		while (iter.hasNext() && iterExp.hasNext())
		{
			assertEquals(iterExp.next(),iter.next());
		}
		
		assertEquals(evolutionEventsExpected, evolutionEvents);
	}
}