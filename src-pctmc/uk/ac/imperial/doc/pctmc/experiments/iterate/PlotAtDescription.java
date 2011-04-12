package uk.ac.imperial.doc.pctmc.experiments.iterate;


 

import java.util.List;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;



public class PlotAtDescription {
	private AbstractExpression expression; 
	private double time;	
	private String filename;
	private List<PlotConstraint> constraints; 
	
	
	public PlotAtDescription(AbstractExpression expression, double time,List<PlotConstraint> constraints,
			String filename) {
		super();
		this.expression = expression;
		this.time = time;
		this.filename = filename;
		this.constraints = constraints; 
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
