package uk.ac.imperial.doc.masspa.pctmc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.masspa.expressions.ExpressionFctAndVarInliner;
import uk.ac.imperial.doc.masspa.representation.components.ChoiceComponent;
import uk.ac.imperial.doc.masspa.representation.components.ConstComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.components.Prefix;
import uk.ac.imperial.doc.masspa.representation.components.ReceivePrefix;
import uk.ac.imperial.doc.masspa.representation.components.SendPrefix;
import uk.ac.imperial.doc.masspa.representation.model.Location;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAActionCount;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAAgentPop;
import uk.ac.imperial.doc.masspa.representation.model.MASSPABirth;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAChannel;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAModel;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAMovement;
import uk.ac.imperial.doc.masspa.representation.model.VarLocation;
import uk.ac.imperial.doc.masspa.util.LocationHelper;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

/***
 * This class converts a MASSPA model into a SPCTMC
 * (Spatial Parameterised CTMC).
 * 
 * @author Chris Guenther
 */
public class MASSPAToPCTMC
{
	public static PCTMC getPCTMC(MASSPAModel _model, Map<ExpressionVariable, AbstractExpression> _variables, Constants _constants)
	{	
		// Find populations
		Set<MASSPAAgentPop> agentPops = _model.getAllAgentPopulations();
		Set<MASSPAActionCount> actionCounts = _model.getAllActionCounts();
		Map<State, AbstractExpression> initMap = new HashMap<State, AbstractExpression>();
		for (MASSPAAgentPop pop : agentPops)
		{
			if (!pop.getLocation().equals(VarLocation.getInstance()))
			{
				initMap.put(pop, pop.getInitialPopulation());
			}
		}
		for (MASSPAActionCount count : actionCounts)
		{
			if (!count.getLocation().equals(VarLocation.getInstance()))
			{
				initMap.put(count, count.getInitVal());
			}
		}
		
		// Expand variables
		inlineVariables(_model, _variables, _constants);
		
		// Create evolutions
		List<EvolutionEvent> events = null;// = createEvolutionEvents(pops, initCounts, actionCounts, _variables, _constants, _model);
				
		// Build MASSPAPCTMC
		return new MASSPAPCTMC(initMap, events, _model);
		
		/*
		System.out.println("\nInitialPopulations:");
		for (Entry<State, AbstractExpression> initCount : initCounts.entrySet())
		{
			System.out.println(initCount.getKey() + " = " + initCount.getValue());
		}
	
		System.out.println("\nEvolutions:");
		for (EvolutionEvent evo : events)
		{
			if (evo.toString().startsWith("WSN"))
			System.out.println(evo);
		}*/
	}
	
	private static AbstractExpression inlineFctsAndVars(AbstractExpression _rate, Location _loc, MASSPAModel _model, Map<FunctionCallExpression, AbstractExpression> _functions, Map<ExpressionVariable, AbstractExpression> _variables, Constants _constants)
	{	
		ExpressionFctAndVarInliner inliner = new ExpressionFctAndVarInliner(_loc, _model, _functions, _variables, _constants);
		_rate.accept(inliner);
		return inliner.getResult();
	}
	
	private static void inlineVariables(MASSPAModel _model,	Map<ExpressionVariable, AbstractExpression> _variables,	Constants _constants)
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
					throw new AssertionError("Variable " + varName + " has an invalid location");
				}
				AbstractExpression varExpr = var.getValue();
				AbstractExpression inlinedFctAndVars = inlineFctsAndVars(varExpr, loc, _model, null, _variables, _constants);	
				//var.getKey().setUnfolded(inlinedFctAndVars);
				_variables.put(var.getKey(), inlinedFctAndVars);
			}
		}
	}


/*
	public static Map<String,MASSPAActionCount> getActionCounts(MASSPAModel _model, Map<FunctionCallExpression,AbstractExpression> _functions, Map<ExpressionVariable, AbstractExpression> _unfoldedVariables, Set<ActionCountState> _countActions)
	{
		Map<String,MASSPAActionCount> actionCounts = new HashMap<String,MASSPAActionCount>();
		
		// Add action counts that are referenced in functions and variables
		AnyLocation anyLoc = new AnyLocation();
		Set<Location> locs = _model.getAllLocations();
		List<AbstractExpression> definitions = new LinkedList<AbstractExpression>();
		definitions.addAll(_functions.values());
		definitions.addAll(_unfoldedVariables.values());
		for (AbstractExpression ae : definitions)
		{
			// Find all count actions in ae
			ExpressionActionCountFinder acfind = new ExpressionActionCountFinder();
			ae.accept(acfind);
			for (ActionCountState a : acfind.getActionCounts())
			{
				// Need to add this action count for all locations
				if (a.toString().contains(anyLoc.toString()))
				{
					for (Location loc : locs)
					{
						String countName = a.toString().replace(anyLoc.toString(), loc.toString());
						actionCounts.put(countName, new MASSPAActionCount(countName));
					}
				}
				else
				{
					actionCounts.put(a.toString(), new MASSPAActionCount(a.toString()));
				}
			}
		}
		
		// Add all action counts that are explicitly requested
		for (ActionCountState a : _countActions)
		{
			actionCounts.put(a.toString(), new MASSPAActionCount(a.toString(),a.getInitVal()));
		}
		
		return actionCounts;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<State,AbstractExpression> createInitialCounts(final Set<MASSPAAgentPop> _pops, final Map<String, MASSPAActionCount> _actionCounts, final MASSPAModel _model)
	{
		Map<State,AbstractExpression> initCounts = new HashMap<State,AbstractExpression>();
		
		for (MASSPAAgentPop pop : _pops)
		{
			// Check if there is at least one non-zero population for any
			// derivative state of the population we want to add otherwise
			// there is no point investigating the population as it will
			// always be 0.
			boolean zeroAgent = true;
			for (MASSPAAgentPop p : (Set<MASSPAAgentPop>)_model.getPredecessorPopulations(pop))
			{
				if (!p.getInitialPopulation().equals(new IntegerExpression(0)))
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
		for (MASSPAActionCount a : _actionCounts.values())
		{
			initCounts.put(a, (a.getInitVal()==null) ? new ZeroExpression() : a.getInitVal());
		}
		
		return initCounts;
	}

	public static List<EvolutionEvent> createEvolutionEvents(final Set<MASSPAAgentPop> _pops, Map<State,AbstractExpression> _initCounts, Map<String,MASSPAActionCount> _actionCounts,  Map<FunctionCallExpression, AbstractExpression> _functions, Map<ExpressionVariable, AbstractExpression> , Constants _constants, MASSPAModel _model)
	{
		List<EvolutionEvent> events = new LinkedList<EvolutionEvent>();
		
		for (MASSPAAgentPop pop : _pops)
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
					if (!(p instanceof ReceivePrefix) && !(p instanceof SendPrefix))
					{
						List<State> increasing = new LinkedList<State>();			
						List<State> decreasing = new LinkedList<State>();
						decreasing.add(pop);
						increasing.add(contPop);
						
						// Simplify rate expression
						AbstractExpression inlinedFctAndVars = inlineFctsAndVars(p.getRate(), pop.getLocation(), _functions, , _constants);
						AbstractExpression evoRate = createPopulationProductRate(inlinedFctAndVars, pop);

						// Add any countActions to increasing set
						addCountActions(increasing,decreasing,p.getAction(),pop.getLocation(),_actionCounts);
						
						// Create and add event
						events.add(new EvolutionEvent(decreasing, increasing, evoRate));
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
											decreasing.add(pop);
											decreasing.add(chan.getSender());
											increasing.add(contPop);
											increasing.add(new MASSPAAgentPop(sp.getContinuation(),chan.getSender().getLocation()));
											
											AbstractExpression msgEmissionRateExpr = ProductExpression.create(sp.getRate(),sp.getNofMsgsSent());
											double msgEmissionRate = simplifyRateUsingLocationConsts(msgEmissionRateExpr,_constants,chan.getSender().getLocation());
											double msgAccProb = simplifyRateUsingLocationConsts(rp.getAcceptanceProbability(),_constants,pop.getLocation());
											CombinedProductExpression intensity = (CombinedProductExpression)chan.getIntensity();
											
											// Simplify acc prob * Intensity * msg emission rate expression
											AbstractExpression rateExpr = ProductExpression.create(new DoubleExpression(msgAccProb),new DoubleExpression(msgEmissionRate));
											double rate = simplifyRate(rateExpr,_constants);
											if (rate == 0) {continue;}
				
											// E[nofSender nofReceiver]
											Map<State, Integer> map = new HashMap<State, Integer>();
											//map.put(chan.getSender(), 1);
											map.put(pop, 1);
											map.put(intensity.getProduct().getNakedProduct().asMultiset().elementSet().iterator().next(),1);
						
											Map<State, Integer> map2 = new HashMap<State, Integer>();
											map2.put(chan.getSender(), 1);
											map2.put(intensity.getProduct().getNakedProduct().asMultiset().elementSet().iterator().next(),1);
											
											// Add any countActions to increasing set
											addCountActions(increasing,decreasing,p.getAction(),pop.getLocation(),_actionCounts);
											addCountActions(increasing,decreasing,rp.getAction(),contPop.getLocation(),_actionCounts);
											
											// Combined rate: rate = rate * E[nofSender nofReceiver]
											AbstractExpression ae = ProductExpression.create(new DoubleExpression(rate),MinExpression.create(CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(map))),CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(map2)))));								
											//AbstractExpression ae = ProductExpression.create(new DoubleExpression(rate),CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(map))));								
											events.add(new MASSPAEvolutionEvent(decreasing, increasing, ae, chan.getSender()));
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
				AbstractExpression inlinedFctAndVars = inlineFctsAndVars(move.getRate(), pop.getLocation(), _functions, , _constants);
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
				AbstractExpression inlinedFctAndVars = inlineFctsAndVars(birth.getRate(), pop.getLocation(), _functions, , _constants);
								
				// Add any countActions to increasing set
				addCountActions(increasing,decreasing,birth.getAction(),pop.getLocation(),_actionCounts);
				
				// Create and add event
				events.add(new EvolutionEvent(decreasing, increasing, inlinedFctAndVars));
			}
		}
		return events;
	}

	private static AbstractExpression inlineFctsAndVars(AbstractExpression _rate, Location _loc,	Map<FunctionCallExpression, AbstractExpression> _functions, Map<ExpressionVariable, AbstractExpression> , Constants _constants)
	{	
		ExpressionFctAndVarInliner inliner = new ExpressionFctAndVarInliner(_loc, _functions, , _constants);
		_rate.accept(inliner);
		return inliner.getResult();
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

		//return ProductExpression.create(_rate, new PopulationExpression(_pop));
	}
	
	public static double simplifyRate(AbstractExpression _rate, Constants _constants)
	{
		ExpressionEvaluatorWithConstants eval = new ExpressionEvaluatorWithConstants(_constants);
		_rate.accept(eval);
		return eval.getResult();
	}

	@SuppressWarnings("unchecked")
	public static AbstractExpression expandChannelIntensity(MASSPAAgentPop _pop, MASSPAChannel _chan, MASSPAModel _model)
	{
		AbstractExpression recvAgentPopSub = null;
		for (MASSPAAgentPop dsp :  (Set<MASSPAAgentPop>)_model.getAgentStatePopulations(_pop))
		{
			recvAgentPopSub = (recvAgentPopSub == null) ? dsp.getInitialPopulation() : SumExpression.create(recvAgentPopSub,dsp.getInitialPopulation());
		}

		ChannelIntensityExpressionVisitor ciev = new ChannelIntensityExpressionVisitor(recvAgentPopSub);
		AbstractExpression intensity = _chan.getIntensity();
		intensity.accept(ciev);
		return ciev.getResult();
	}

	protected static double simplifyRateUsingLocationConsts(AbstractExpression _rate, Constants _constants, Location _loc)
	{
		ExpressionEvaluatorWithConstants eval = new ExpressionEvaluatorWithLocationConstants(_constants,_loc);
		_rate.accept(eval);
		System.out.print(eval.getResult());
		return eval.getResult();
	}

	protected static void addCountActions(final List<State> _increasing, final List<State> _decreasing, final String _countAction, final Location _l, final Map<String, MASSPAActionCount> _actionCounts)
	{
		String countActionA = (new ActionCountState(_countAction + _l.toString())).toString();

		for (Entry<String, MASSPAActionCount> ca : _actionCounts.entrySet())
		{
			if (countActionA.startsWith(ca.getKey()) || countActionA.replace("#dec_", "#").startsWith(ca.getKey()))
			{
				if (!countActionA.startsWith("#dec_"))
				{
					_increasing.add(ca.getValue());
				}
				else
				{
					_decreasing.add(ca.getValue());
				}
			}
		}
	}*/
}
