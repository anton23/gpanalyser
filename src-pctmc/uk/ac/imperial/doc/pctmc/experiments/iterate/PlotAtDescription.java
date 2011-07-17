package uk.ac.imperial.doc.pctmc.experiments.iterate;


 

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.ExpressionVariableSetterPCTMC;



public class PlotAtDescription {
	private AbstractExpression expression; 
	private double time;	
	private String filename;
	private List<PlotConstraint> constraints;
	
	private List<AbstractExpression> plotExpressions; 
	private double[] atTimes; 
	
	private AbstractExpressionEvaluator evaluator; 
	
	
	public double[] getAtTimes() {
		return atTimes;
	}

	public AbstractExpressionEvaluator getEvaluator() {
		return evaluator;
	}

	public void setEvaluator(AbstractExpressionEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public List<AbstractExpression> getPlotExpressions() {
		return plotExpressions;
	}

	public void unfoldExpressions(Map<ExpressionVariable,AbstractExpression> unfoldedVariables){
		plotExpressions = new ArrayList<AbstractExpression>(constraints.size()+1);
		List<AbstractExpression> pAExpressions = new LinkedList<AbstractExpression>(); 
		pAExpressions.add(getExpression()); 
		for (PlotConstraint pc:getConstraints()){
			pAExpressions.add(pc.getExpression());
		}
		for (AbstractExpression e:pAExpressions){
			ExpressionVariableSetterPCTMC setter = new ExpressionVariableSetterPCTMC(unfoldedVariables);
			e.accept(setter); 								
			plotExpressions.add(e); 
		}
	}
	
	public PlotAtDescription(AbstractExpression expression, double time,List<PlotConstraint> constraints,
			String filename) {
		super();
		this.expression = expression;
		this.time = time;
		this.filename = filename;
		this.constraints = new ArrayList<PlotConstraint>(constraints);
		atTimes = new double[constraints.size()+1];
		atTimes[0] = time;
		int i = 1; 
		for (PlotConstraint c:constraints){
			atTimes[i++] = c.getAtTime(); 
		}
	}
	
	
	
	public List<PlotConstraint> getConstraints() {
		return constraints;
	}



	public AbstractExpression getExpression() {
		return expression;
	}
	public double getTime() {
		return time;
	}
	public String getFilename() {
		return filename;
	} 
	
	@Override
	public String toString() {
		String constraintString = "";
		if (!constraints.isEmpty()){
			constraintString = " when " + ToStringUtils.iterableToSSV(constraints, " and ");
		}
		
		return expression.toString() + " at " + time + constraintString + ((filename==null||filename.isEmpty())?"":(" ->"+ filename)); 
	}

}
