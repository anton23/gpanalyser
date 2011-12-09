package uk.ac.imperial.doc.masspa.expressions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.IConstantExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionTransformer;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.jexpressions.variables.IExpressionVariableVisitor;
import uk.ac.imperial.doc.masspa.representation.components.ConstComponent;
import uk.ac.imperial.doc.masspa.representation.model.Location;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAActionCount;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAAgentPop;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAModel;
import uk.ac.imperial.doc.masspa.representation.model.VarLocation;
import uk.ac.imperial.doc.masspa.util.LocationHelper;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IPopulationProductVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IPopulationVisitor;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProductExpression;
import uk.ac.imperial.doc.pctmc.representation.State;

/**
 * This class inlines both functions and variables
 * 
 * @author Chris Guenther
 * @param <d>
 */
public class ExpressionFctAndVarInliner extends ExpressionTransformer  implements IConstantExpressionVisitor, IPopulationVisitor, IPopulationProductVisitor, ICombinedProductExpressionVisitor, IExpressionVariableVisitor
{
	private Location m_loc;
	private MASSPAModel m_model;
	private Map<FunctionCallExpression, AbstractExpression> m_functions;
	private Map<ExpressionVariable, AbstractExpression> m_variables;
	private Constants m_constants;
	private Map<ConstantExpression,AbstractExpression> m_boundArgs = new HashMap<ConstantExpression,AbstractExpression>();
	
	public ExpressionFctAndVarInliner(Location _loc, MASSPAModel _model, Map<FunctionCallExpression, AbstractExpression> _functions, Map<ExpressionVariable, AbstractExpression> _variables, Constants _constants)
	{
		m_loc = _loc;
		m_model = _model;
		m_functions = _functions;
		m_variables = _variables;
		m_constants = _constants;
	}
	
	public ExpressionFctAndVarInliner(Location _loc, MASSPAModel _model, Map<FunctionCallExpression, AbstractExpression> _functions, Map<ExpressionVariable, AbstractExpression> _variables, Constants _constants, Map<ConstantExpression, AbstractExpression> _boundArgs)
	{
		this(_loc, _model, _functions, _variables, _constants);
		m_boundArgs = _boundArgs;
	}

	@Override
	public void visit(ConstantExpression e)
	{
		// First localise constant
		Location l = LocationHelper.getLocalisedLocation(e.getConstant(), m_loc);
		String localConstName = (l != null) ? (e.getConstant().replace(VarLocation.getInstance().toString(), l.toString())) : e.getConstant();
		
		// Bound variable substitution
		ConstantExpression ce = new ConstantExpression(localConstName);
		result = (m_boundArgs.containsKey(ce)) ? m_boundArgs.get(ce) : ce;
		// Bound variable substitution failed
		if (result == ce)
		{
			// Find global variable
			Double value = m_constants.getConstantValue(localConstName);
			value = (value == null) ? m_constants.getConstantValue(localConstName.replace(l.toString(), VarLocation.getInstance().toString())) : value;
			if (value == null)
			{
				throw new AssertionError("Constant " + localConstName + " is undefined.");
			}
			result = new DoubleExpression(value);			
		}
	}
	
	@Override
	public void visit(CombinedProductExpression e)
	{
		Map<State,Integer> localisedProduct = new HashMap<State,Integer>();
		for (Entry<State, Integer> entry : e.getProduct().getNakedProduct().getRepresentation().entrySet())
		{
			if (entry.getKey() instanceof MASSPAActionCount)
			{
				MASSPAActionCount count = (MASSPAActionCount)entry.getKey();
				Location l = LocationHelper.getLocalisedLocation(count.getLocation().toString(), m_loc);
				MASSPAActionCount localCount = m_model.getActionCount(new MASSPAActionCount(((MASSPAActionCount)entry.getKey()).getName(),l));
				if (localCount == null)
				{
					throw new AssertionError("Action count " + entry.getKey() + " refers to an undefined action or location.");
				}
				localisedProduct.put(localCount,entry.getValue());
			}
			else if (entry.getKey() instanceof MASSPAAgentPop)
			{
				MASSPAAgentPop pop = (MASSPAAgentPop)entry.getKey();
				Location l = LocationHelper.getLocalisedLocation(pop.getLocation().toString(), m_loc);
				MASSPAAgentPop localPop = m_model.getAgentPop(new MASSPAAgentPop(new ConstComponent(pop.getComponentName()),l));
				if (localPop == null)
				{
					throw new AssertionError("Agent population " + entry.getKey() + " refers to an undefined agent state or location.");
				}
				localisedProduct.put(localPop,entry.getValue());
			}
		}
		result = CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(localisedProduct)));
	}

	@Override
	public void visit(PopulationExpression e)
	{
		State s = e.getState();
		if (s instanceof MASSPAActionCount)
		{
			MASSPAActionCount count = (MASSPAActionCount)s;
			Location l = LocationHelper.getLocalisedLocation(count.getLocation().toString(), m_loc);
			s = m_model.getActionCount(new MASSPAActionCount(((MASSPAActionCount) s).getName(), l));
			if (s == null)
			{
				throw new AssertionError("Action count " + s + " refers to an undefined action or location.");
			}
		}
		else if (s instanceof MASSPAAgentPop)
		{
			MASSPAAgentPop pop = (MASSPAAgentPop)s;
			Location l = LocationHelper.getLocalisedLocation(pop.getLocation().toString(), m_loc);
			s = m_model.getAgentPop(new MASSPAAgentPop(new ConstComponent(pop.getComponentName()),l));
			if (s == null)
			{
				throw new AssertionError("Agent population " + s + " refers to an undefined agent state or location.");
			}
		}
		result = new PopulationExpression(s);
	}

	@Override
	public void visit(PopulationProductExpression e)
	{
		throw new AssertionError("Not handled yet");
	}
	
	@Override
	public void visit(ExpressionVariable e)
	{
		Location l = LocationHelper.getLocalisedLocation(e.getName(), m_loc);
		String localVarName = (l != null) ? (e.getName().replace(VarLocation.getInstance().toString(), l.toString())) : e.getName();
		String genericVarName = e.getName()+VarLocation.getInstance().toString();
		
		ExpressionVariable localVar = new ExpressionVariable(localVarName);
		ExpressionVariable genericVar =  new ExpressionVariable(genericVarName);
		AbstractExpression def = (m_variables.containsKey(localVar)) ? m_variables.get(localVar) : m_variables.get(genericVar);
		
		if (def == null)
		{
			throw new AssertionError("Unable to find definition for variable " + e);
		}
		
		ExpressionFctAndVarInliner inliner = new ExpressionFctAndVarInliner(l, m_model, m_functions, m_variables, m_constants);
		def.accept(inliner);
		result = inliner.getResult();
	}
	
	@Override
	public void visit(FunctionCallExpression e)
	{
		Location l = LocationHelper.getLocalisedLocation(e.getName(), m_loc);

		String localFunName = (l != null) ? (e.getName().replace(VarLocation.getInstance().toString(), l.toString())) : e.getName();
		String genericFunName = e.getName().replace(l.toString(), VarLocation.getInstance().toString());
		FunctionCallExpression funDef = null;		
		for (FunctionCallExpression fun : m_functions.keySet())
		{
			// Try to find local function definition
			if (fun.getName().equals(localFunName) && fun.getArguments().size() == e.getArguments().size())
			{
				funDef = fun;
				break;
			}
			// In case no local definition exists use generic location definition
			if (fun.getName().equals(genericFunName) && fun.getArguments().size() == e.getArguments().size())
			{
				funDef = fun;
			}
		}
		
		if (funDef == null)
		{
			throw new AssertionError("Unable to find definition for function " + e + " or function " + localFunName);
		}

		// Now create new arg mapping
		int argNo=0;
		HashMap<ConstantExpression, AbstractExpression> boundArgs = new HashMap<ConstantExpression, AbstractExpression>();
		for (AbstractExpression ae : funDef.getArguments())
		{
			if (!(ae instanceof ConstantExpression))
			{
				throw new AssertionError(ae + " in function definition of " + funDef + " must be constant Expression");
			}
			
			// Substitute if necessary
			AbstractExpression arg = e.getArguments().get(argNo++);
			arg.accept(this);
			boundArgs.put((ConstantExpression)ae, result);
		}
		
		ExpressionFctAndVarInliner inliner = new ExpressionFctAndVarInliner(l, m_model, m_functions, m_variables, m_constants, boundArgs);
		AbstractExpression def = m_functions.get(funDef);
		def.accept(inliner);
		result = inliner.getResult();
	}
}
