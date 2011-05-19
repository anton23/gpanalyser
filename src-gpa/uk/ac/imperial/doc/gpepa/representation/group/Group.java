package uk.ac.imperial.doc.gpepa.representation.group;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ZeroExpression;

import com.google.common.collect.Multimap;

/**
 * Class representing a component group.
 * 
 * @author Anton Stefanek
 */
public class Group {

	public Group getShorthandVersion(PEPAComponentDefinitions definitions) {
		//TODO fix me
		Map<PEPAComponent,AbstractExpression> newComponents = new HashMap<PEPAComponent,AbstractExpression>(); 
		for (Map.Entry<PEPAComponent, AbstractExpression> e:counts.entrySet()){
			newComponents.put(definitions.getShorthand(e.getKey()), e.getValue());
		}
		return new Group(newComponents); 
	}

	public Group(Multimap<PEPAComponent, AbstractExpression> countMultimap){
		counts = new HashMap<PEPAComponent,AbstractExpression>();  
		for (PEPAComponent c:countMultimap.keySet()){
			AbstractExpression summation = SumExpression.create(countMultimap.get(c));
			counts.put(c, summation); 
		}
	}
	
	
	private Map<PEPAComponent,AbstractExpression> counts; 
	
	public AbstractExpression getCountExpression(PEPAComponent component){
		if (!counts.containsKey(component)) return new ZeroExpression(); 
		return counts.get(component); 
	}
	
	public void setCountExpression(PEPAComponent component,AbstractExpression value){
		counts.put(component, value);
	}


	

	public Set<String> getActions(PEPAComponentDefinitions definitions) {
		Set<String> ret = new HashSet<String>();
		for (PEPAComponent c : counts.keySet()) {
			ret.addAll(c.getActions(definitions));
		}
		return ret; 
	}

	public String toString() {
		String ret = ""; 
		int i = 0;
		for (Map.Entry<PEPAComponent, AbstractExpression> e : counts.entrySet()) {
			if (i++ > 0) {
				ret += "|";
			}
			if (e.getValue() instanceof DoubleExpression && ((DoubleExpression)e.getValue()).getValue() == 1.0 ){
				ret += e.getKey().toString();
			} else {
				ret += e.getKey().toString() + "[" + e.getValue() + "]";
			}
		}
		return ret; 
	}

	/**
	 * Returns the derivative states of a given component.
	 * 
	 * @param definitions
	 * @return
	 */
	public Set<PEPAComponent> getComponentDerivatives(
			PEPAComponentDefinitions definitions) {

		Set<PEPAComponent> ret = new HashSet<PEPAComponent>();

		for (PEPAComponent c : counts.keySet()) {
			Set<PEPAComponent> states = c.getDerivativeStates(
					new HashSet<PEPAComponent>(ret), definitions);
			ret.addAll(states);
		}
		return ret; 
	}

	public Group(Map<PEPAComponent, AbstractExpression> counts) {
		super();
		this.counts = counts;
	}
}
