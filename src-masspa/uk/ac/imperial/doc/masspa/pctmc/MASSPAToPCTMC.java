package uk.ac.imperial.doc.masspa.pctmc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MaxExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.UMinusExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.masspa.expressions.ExpressionFctAndVarInliner;
import uk.ac.imperial.doc.masspa.expressions.ExpressionPopProductCreator;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.representation.components.ChoiceComponent;
import uk.ac.imperial.doc.masspa.representation.components.ConstComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.components.Prefix;
import uk.ac.imperial.doc.masspa.representation.components.ReceivePrefix;
import uk.ac.imperial.doc.masspa.representation.components.SendPrefix;
import uk.ac.imperial.doc.masspa.representation.model.AllLocation;
import uk.ac.imperial.doc.masspa.representation.model.Location;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAActionCount;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAAgentPop;
import uk.ac.imperial.doc.masspa.representation.model.MASSPABirth;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAChannel;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAModel;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAMovement;
import uk.ac.imperial.doc.masspa.representation.model.VarLocation;
import uk.ac.imperial.doc.masspa.representation.model.util.LocationHelper;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

/***
 * This class converts a MASSPA model into a PCTMC
 * (Population CTMC).
 * 
 * @author Chris Guenther
 */
public class MASSPAToPCTMC
{
	public static PCTMC getPCTMC(MASSPAModel _model, Map<ExpressionVariable, AbstractExpression> _variables, Constants _constants)
	{	
		// Create initial populations
		Set<MASSPAAgentPop> agentPops = _model.getAllAgentPopulations();
		Set<MASSPAActionCount> actionCounts = _model.getAllActionCounts();
		Map<State, AbstractExpression> initCounts = createInitialCounts(agentPops, actionCounts, _model);

		// Create evolutions
		localiseConstants(_model, _constants);
		inlineVariables(_model, _variables, _constants);
		List<EvolutionEvent> events = createEvolutionEvents(agentPops, actionCounts, initCounts, _variables, _constants, _model);	

		
		/*
		for (Entry<String, Double> e : _constants.getConstantsMap().entrySet())
		{
			System.out.println (e.getKey() + "=" + e.getValue());
		}
		
		for (Entry<ExpressionVariable, AbstractExpression> e : _variables.entrySet())
		{
			System.out.println (e.getKey() + "=" + e.getValue());
		}

		for (MASSPAAgentPop pop : agentPops)
		{
			System.out.println(pop.getNameAndInitPop());
		}
		for (EvolutionEvent evo : events)
		{
			if (evo.toString().startsWith("Route"))
			System.out.println(evo);
		}*/
		
		// Build MASSPAPCTMC
		return new MASSPAPCTMC(initCounts, events, _model);
	}

	@SuppressWarnings("unchecked")
	public static Map<State,AbstractExpression> createInitialCounts(final Set<MASSPAAgentPop> _agentPops, final Set<MASSPAActionCount> _actionCounts, final MASSPAModel _model)
	{
		Map<State,AbstractExpression> initCounts = new HashMap<State,AbstractExpression>();
		
		for (MASSPAAgentPop pop : _agentPops)
		{
			if (!pop.getLocation().equals(VarLocation.getInstance()))
			{
				// Check if there is at least one non-zero population for any
				// derivative state of the population we want to add otherwise
				// there is no point investigating the population as it will
				// always be 0.
				boolean zeroAgent = true;
				for (MASSPAAgentPop p : (Set<MASSPAAgentPop>)_model.getPredecessorPopulations(pop))
				{
					if (!p.getInitialPopulation().equals(new IntegerExpression(0)) &&
						!p.getInitialPopulation().equals(new DoubleExpression(0.0)))
					{
						zeroAgent = false;
						break;
					}
				}
	 
				// At least one derivative state has a non-zero population
				if (!zeroAgent)
				{
					initCounts.put(pop, pop.getInitialPopulation());
				}
			}
		}
		for (MASSPAActionCount count : _actionCounts)
		{
			if (!count.getLocation().equals(VarLocation.getInstance()))
			{
				initCounts.put(count, count.getInitVal());
			}
		}
		
		return initCounts;
	}
	
	protected static AbstractExpression inlineFctsAndVars(AbstractExpression _rate, Location _loc, MASSPAModel _model, Map<FunctionCallExpression, AbstractExpression> _functions, Map<ExpressionVariable, AbstractExpression> _variables, Constants _constants)
	{	
		ExpressionFctAndVarInliner inliner = new ExpressionFctAndVarInliner(_loc, _model, _functions, _variables, _constants);
		_rate.accept(inliner);
		return inliner.getResult();
	}

	protected static void localiseConstants(MASSPAModel _model, Constants _constants)
	{
		Constants cnstscopy = _constants.getCopyOf();
		for (String cnst : cnstscopy.getConstantsMap().keySet())
		{
			String[] cnstNameSplit = cnst.split("@");
			
			// This constant may have placeholders in its expression
			if (!VarLocation.getInstance().toString().endsWith(cnstNameSplit[1]))
			{
				Location loc = LocationHelper.getLocalisedLocation(cnst, null);
				if (!_model.getAllLocations().contains(loc))
				{
					throw new AssertionError(String.format(Messages.s_COMPILER_CONST_INVALID_LOCATION, cnst, loc));
				}
			}
		}
		_constants.recalculateConstantIndex();
	}
	
	protected static void inlineVariables(MASSPAModel _model,	Map<ExpressionVariable, AbstractExpression> _variables,	Constants _constants)
	{
		Map<ExpressionVariable, AbstractExpression> vars = new HashMap<ExpressionVariable, AbstractExpression>(_variables);
		for (Entry<ExpressionVariable, AbstractExpression> var : vars.entrySet())
		{
			String varName = var.getKey().getName();
			String[] varNameSplit = varName.split("@");

			// This variable may have placeholders in its expression
			if (VarLocation.getInstance().toString().endsWith(varNameSplit[1]))
			{
				AbstractExpression varExpr = var.getValue();
				for (Location loc : _model.getAllLocations())
				{
					ExpressionVariable tempVar = new ExpressionVariable(varNameSplit[0]+loc.toString());
					// We need to ensure that we don't overwrite localised variables
					if (_variables.get(tempVar) == null)
					{
						AbstractExpression inlinedFctAndVars = inlineFctsAndVars(varExpr, loc, _model, null, _variables, _constants);	
						//tempVar.setUnfolded(inlinedFctAndVars);
						_variables.put(tempVar, inlinedFctAndVars);
					}
				}
			}
			else
			{
				Location loc = LocationHelper.getLocalisedLocation(varName, null);
				if (!_model.getAllLocations().contains(loc))
				{
					throw new AssertionError(String.format(Messages.s_COMPILER_VAR_INVALID_LOCATION, varName, loc));
				}
				AbstractExpression varExpr = var.getValue();
				AbstractExpression inlinedFctAndVars = inlineFctsAndVars(varExpr, loc, _model, null, _variables, _constants);	
				//var.getKey().setUnfolded(inlinedFctAndVars);
				_variables.put(var.getKey(), inlinedFctAndVars);
			}
		}
	}

	protected static void addCountActions(final List<State> _increasing, final List<State> _decreasing, final String _countAction, final Location _l, final Set<MASSPAActionCount> _actionCounts)
	{
		if (_countAction.isEmpty()) {return;}
		MASSPAActionCount local = new MASSPAActionCount(_countAction.replace("dec_",""), _l);
		MASSPAActionCount global = new MASSPAActionCount(_countAction.replace("dec_",""), AllLocation.getInstance());
		
		if (_countAction.contains("dec_"))
		{
			_decreasing.add(local);
			_decreasing.add(global);
		}
		else
		{
			_increasing.add(local);
			_increasing.add(global);
		}
	}
	
	private static AbstractExpression createPopulationProductRate(AbstractExpression _rate, MASSPAAgentPop _pop)
	{
		// Ensure that we will always have E[_pop Y] and not E[_pop]*E[Y] before applying moment closures
		// This is done by distributively pushing E[_pop] into the expression and merging it with other
		// Population expression that it is multiplied with
		ExpressionPopProductCreator eCreator = new ExpressionPopProductCreator(_pop);
		_rate.accept(eCreator);
		if (!eCreator.hasPushedThrough())
		{
			// In case no other E[...] exists in _rate
			return ProductExpression.create(eCreator.getResult(), new PopulationExpression(_pop));
		}
		return eCreator.getResult();
	}
	
	protected static List<EvolutionEvent> createEvolutionEvents(
			Set<MASSPAAgentPop> _agentPops, Set<MASSPAActionCount> _actionCounts,
			Map<State, AbstractExpression> _initCounts,
			Map<ExpressionVariable, AbstractExpression> _variables,
			Constants _constants, MASSPAModel _model)
	{
		List<EvolutionEvent> events = new LinkedList<EvolutionEvent>();
		
		for (MASSPAAgentPop pop : _agentPops)
		{
			if (!_initCounts.containsKey(pop))
			{
				// Unreachable population
				continue;
			}
			
			// Event originating from local transitions
			// Derive events from agent definitions and message channels
			MASSPAComponent def = pop.getComponent().getDefinition();
			if (def instanceof ChoiceComponent)
			{
				for (Prefix p : ((ChoiceComponent)def).getChoices())
				{
					MASSPAAgentPop contPop = _model.getAgentPop(new MASSPAAgentPop(p.getContinuation(), pop.getLocation()));
					if(!_initCounts.containsKey(contPop)) {continue;}

					// Local event (i.e. non-message induced event)
					if (!(p instanceof ReceivePrefix))
					{		
						List<State> increasing = new LinkedList<State>();			
						List<State> decreasing = new LinkedList<State>();
						decreasing.add(pop);
						increasing.add(contPop);
						
						// Simplify rate expression
						AbstractExpression inlinedFctAndVars = inlineFctsAndVars(p.getRate(), pop.getLocation(), _model, null, _variables, _constants);
						AbstractExpression evoRate = createPopulationProductRate(inlinedFctAndVars, pop);

						// Add any countActions to increasing set
						addCountActions(increasing,decreasing,p.getAction(),pop.getLocation(),_actionCounts);
						
						// Check if there is an asynchronous channel for send prefix
						if (p instanceof SendPrefix)
						{
							boolean allSynchChannel=true;
							for (MASSPAChannel chan : _model.getAllChannelsSender(pop, ((SendPrefix)p).getMsg()))
							{
								if (chan.getRateType() != MASSPAChannel.RateType.MULTISERVER_SYNC)
								{
									allSynchChannel=false;
									break;
								}
							}
							
							// At least one channel is asynchronous
							if (!allSynchChannel)
							{
								// Create and add event
								events.add(new EvolutionEvent(decreasing, increasing, evoRate));	
							}	
						}
						else
						{
							// Create and add event
							events.add(new EvolutionEvent(decreasing, increasing, evoRate));	
						}
					}
					// External event (i.e. message induced event)
					if (p instanceof ReceivePrefix)
					{					
						// Find all channels that can induce the transition and
						// create a new MASSPAEvolutionEvent for each one of them
						ReceivePrefix rp = (ReceivePrefix) p;
						Set<MASSPAChannel> channels = _model.getAllChannels(pop, rp.getMsg());
						for (MASSPAChannel chan : channels)
						{
							MASSPAComponent defSender = ((ConstComponent)chan.getSender().getComponent()).getDefinition();
							if (defSender instanceof ChoiceComponent)
							{
								for (Prefix p2 : ((ChoiceComponent)defSender).getChoices())
								{
									if (p2 instanceof SendPrefix)
									{
										SendPrefix sp = (SendPrefix)p2;
										if (sp.getMsg().equals(chan.getMsg()))
										{											
											List<State> increasing = new LinkedList<State>();			
											List<State> decreasing = new LinkedList<State>();

											AbstractExpression msgEmissionRateExpr = ProductExpression.create(sp.getRate(),sp.getNofMsgsSent());
											msgEmissionRateExpr = inlineFctsAndVars(msgEmissionRateExpr, chan.getSender().getLocation(), _model, null, _variables, _constants);
											AbstractExpression intensity = (AbstractExpression)chan.getIntensity();
											
											// Simplify acc prob * Intensity * msg emission rate expression
											AbstractExpression rateExpr = ProductExpression.create(msgEmissionRateExpr,rp.getAcceptanceProbability());

											// Add any countActions to increasing set
											addCountActions(increasing,decreasing,rp.getAction(),contPop.getLocation(),_actionCounts);
											
											// Combined rate: rate = rate * E[nofSender nofReceiver]
											AbstractExpression ae=null;
											Map<State, Integer> map = new HashMap<State, Integer>();
											Map<State, Integer> map2 = new HashMap<State, Integer>();
											if (chan.getRateType() == MASSPAChannel.RateType.MULTISERVER_SYNC)
											{
												// Add any countActions to increasing set
												addCountActions(increasing,decreasing,sp.getAction(),chan.getSender().getLocation(),_actionCounts);
												
												// Synchronous
												decreasing.add(pop);
												decreasing.add(chan.getSender());
												increasing.add(contPop);
												increasing.add(new MASSPAAgentPop(sp.getContinuation(),chan.getSender().getLocation()));
																								
												// min(E[nofSender], E[nofReceiver])
												map.put(pop, 1);
												map2.put(chan.getSender(), 1);
												// Special case when intensity is a population
												if (intensity instanceof CombinedProductExpression)
												{
													
													map.put(((CombinedProductExpression)intensity).getProduct().getNakedProduct().asMultiset().elementSet().iterator().next(),1);
													map2.put(((CombinedProductExpression)intensity).getProduct().getNakedProduct().asMultiset().elementSet().iterator().next(),1);
													ae = ProductExpression.create(new UMinusExpression(MaxExpression.create(new UMinusExpression(CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(map)))),new UMinusExpression(CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(map2)))))),rateExpr);
													
													//ae = ProductExpression.create(ProductExpression.create(rateExpr,MinExpression.create(CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(map))),CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(map2))))),intensity);
													//ae = ProductExpression.create(ProductExpression.create(MinExpression.create(CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(map))),CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(map2))))),rateExpr,intensity);
					
												}
												else
												{
													rateExpr = ProductExpression.create(intensity,rateExpr);
													ae = ProductExpression.create(rateExpr,MinExpression.create(CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(map))),CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(map2)))));
												}
											}
											else if (chan.getRateType() == MASSPAChannel.RateType.MASSACTION_ASYNC)
											{
												// Asynchronous
												decreasing.add(pop);
												increasing.add(contPop);
												
												// E[nofSender nofReceiver]
												map.put(chan.getSender(), 1);
												map.put(pop, 1);
												// Special case when intensity is a population
												//if (intensity instanceof CombinedProductExpression)
//												{
//													map.put(((CombinedProductExpression)intensity).getProduct().getNakedProduct().asMultiset().elementSet().iterator().next(),1);
//													ae = ProductExpression.create(new DoubleExpression(rate),CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(map))));
//												}
//												else
//												{
//													rateExpr = ProductExpression.create(intensity,rateExpr);
//													ae = ProductExpression.create(rateExpr,CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(map))));
//												}
											}
											events.add(new EvolutionEvent(decreasing, increasing, ae));
										}
									}
								}
							}
						}
					}
				}
			}
			
			// Movement events
			for (MASSPAMovement move : _model.getAllMovements(pop))
			{
				List<State> increasing = new LinkedList<State>();			
				List<State> decreasing = new LinkedList<State>();
				decreasing.add(pop);
				increasing.add(move.getTo());
				
				// Simplify rate expression
				AbstractExpression inlinedFctAndVars = inlineFctsAndVars(move.getRate(), pop.getLocation(), _model, null, _variables, _constants);
				AbstractExpression evoRate = createPopulationProductRate(inlinedFctAndVars, pop);
				
				// Add any countActions to increasing set
				addCountActions(increasing,decreasing,move.getLeaveAction(),pop.getLocation(),_actionCounts);
				addCountActions(increasing,decreasing,move.getEnterAction(),move.getTo().getLocation(),_actionCounts);
				
				// Create and add event
				events.add(new EvolutionEvent(decreasing, increasing, evoRate));
			}
			
			// Birth events
			if (_model.getBirth(pop) != null)
			{
				MASSPABirth birth = _model.getBirth(pop);
				List<State> increasing = new LinkedList<State>();			
				List<State> decreasing = new LinkedList<State>();
				increasing.add(pop);

				// Simplify rate expression
				AbstractExpression inlinedFctAndVars = inlineFctsAndVars(birth.getRate(), pop.getLocation(), _model, null, _variables , _constants);
								
				// Add any countActions to increasing set
				addCountActions(increasing,decreasing,birth.getAction(),pop.getLocation(),_actionCounts);
				
				// Create and add event
				events.add(new EvolutionEvent(decreasing, increasing, inlinedFctAndVars));
			}
		}
		return events;
	}
}
