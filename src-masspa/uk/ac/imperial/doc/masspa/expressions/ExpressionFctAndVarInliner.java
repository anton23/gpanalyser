package uk.ac.imperial.doc.masspa.expressions;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.IConstantExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionTransformer;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.jexpressions.variables.IExpressionVariableVisitor;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.representation.components.ConstComponent;
import uk.ac.imperial.doc.masspa.representation.model.Location;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAActionCount;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAAgentPop;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAModel;
import uk.ac.imperial.doc.masspa.representation.model.VarLocation;
import uk.ac.imperial.doc.masspa.representation.model.util.LocationHelper;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IPopulationProductVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IPopulationVisitor;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProductExpression;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

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
			// Local variable
			Double value = m_constants.getConstantValue(localConstName);
			if (value != null) {result = new ConstantExpression(localConstName); return;}
			
			// Global variable
			String varname = localConstName.replace(l.toString(), VarLocation.getInstance().toString());
			value = m_constants.getConstantValue(varname);
			if (value != null) {result = new ConstantExpression(varname); return;}
			
			// Variable doesn't exist
			throw new AssertionError(String.format(Messages.s_COMPILER_CONST_UNDEFINED, localConstName));
		}
	}
	
	@Override
	public void visit(CombinedProductExpression e)
	{
		Multiset<State> localisedProduct = HashMultiset.create();
		for (Multiset.Entry<State> entry : e.getProduct().getPopulationProduct().getRepresentation().entrySet())
		{
			if (entry.getElement() instanceof MASSPAActionCount)
			{
				MASSPAActionCount count = (MASSPAActionCount)entry.getElement();
				Location l = LocationHelper.getLocalisedLocation(count.getLocation().toString(), m_loc);
				MASSPAActionCount localCount = m_model.getActionCount(new MASSPAActionCount(((MASSPAActionCount)entry.getElement()).getName(),l));
				if (localCount == null)
				{
					throw new AssertionError(String.format(Messages.s_COMPILER_ACTIONCOUNT_INVALID, entry.getElement()));
				}
				localisedProduct.add(localCount,entry.getCount());
			}
			else if (entry.getElement() instanceof MASSPAAgentPop)
			{
				MASSPAAgentPop pop = (MASSPAAgentPop)entry.getElement();
				Location l = LocationHelper.getLocalisedLocation(pop.getLocation().toString(), m_loc);
				MASSPAAgentPop localPop = m_model.getAgentPop(new MASSPAAgentPop(new ConstComponent(pop.getComponentName()),l));
				if (localPop == null)
				{
					throw new AssertionError(String.format(Messages.s_COMPILER_AGENTPOP_INVALID, entry.getElement()));
				}
				localisedProduct.add(localPop,entry.getCount());
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
				throw new AssertionError(String.format(Messages.s_COMPILER_ACTIONCOUNT_INVALID, s));
			}
		}
		else if (s instanceof MASSPAAgentPop)
		{
			MASSPAAgentPop pop = (MASSPAAgentPop)s;
			Location l = LocationHelper.getLocalisedLocation(pop.getLocation().toString(), m_loc);
			s = m_model.getAgentPop(new MASSPAAgentPop(new ConstComponent(pop.getComponentName()),l));
			if (s == null)
			{
				throw new AssertionError(String.format(Messages.s_COMPILER_AGENTPOP_INVALID, s));
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
			throw new AssertionError(String.format(Messages.s_COMPILER_VAR_UNDEFINED, e));
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
			throw new AssertionError(String.format(Messages.s_COMPILER_FUN_UNDEFINED, e));
		}

		// Now create new arg mapping
		int argNo=0;
		HashMap<ConstantExpression, AbstractExpression> boundArgs = new HashMap<ConstantExpression, AbstractExpression>();
		for (AbstractExpression ae : funDef.getArguments())
		{
			if (!(ae instanceof ConstantExpression))
			{
				throw new AssertionError(String.format(Messages.s_COMPILER_FUN_INVALID_ARG, ae, funDef));
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
