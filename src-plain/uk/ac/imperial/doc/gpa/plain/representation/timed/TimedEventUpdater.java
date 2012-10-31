package uk.ac.imperial.doc.gpa.plain.representation.timed;

import java.util.Collection;

import uk.ac.imperial.doc.jexpressions.constants.Constants;

/**
 * Modify a constant or a population
 * according to a deterministic event
 * that occurs at a known time
 * 
 * @author Chris Guenther
 */
public abstract class TimedEventUpdater
{
	/**
	 * @param time
	 * @return the value of the event at time {@code time}
	 * 		   if no event occurs at {@code time} then null
	 * 		   is returned
	 */
	abstract public Double getValue(double time);

	/**
	 * @return times at which an event occurs
	 */
	abstract public Collection<Double> getAllEventTimes();

	/**
	 * Update {@code constants} or {@code popVector} according
	 * to the event at {@code time}. {@code time} must be a time
	 * for which an event is defined, i.e. getValue({@code time})
	 * should not return null
	 * 
	 * @param constants
	 * @param popVector
	 * @param time
	 */
	abstract public void update(Constants constants, double[] popVector, double time);
}
