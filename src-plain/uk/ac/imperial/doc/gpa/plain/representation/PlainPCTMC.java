package uk.ac.imperial.doc.gpa.plain.representation;

import java.util.Collection;
import java.util.Map;

import uk.ac.imperial.doc.gpa.plain.representation.timed.TimedEvents;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

/**
 * This particular PCTMC allows users
 * to specify time-inhomogenous PCTMCs.
 * 
 * @author Chris Guenther
 */
public class PlainPCTMC extends PCTMC {

	protected TimedEvents mTimedEvents;
	
	public PlainPCTMC(Map<State, AbstractExpression> initMap,
			Collection<EvolutionEvent> evolutionEvents,
			TimedEvents timedEvents)
	{
		super(initMap, evolutionEvents);
		setTimedEvents(timedEvents);
	}

	/**
	 * @param timedEvents contains information about all
	 * 		  time-inhomogenous events of the PCTMC
	 */
	public void setTimedEvents(TimedEvents timedEvents) {
		mTimedEvents = timedEvents;
	}
	
	/**
	 * @return information about all time-inhomogenous
	 * 		   events of the PCTMC
	 */
	public TimedEvents getTimedEvents() {
		return mTimedEvents;
	}
}
