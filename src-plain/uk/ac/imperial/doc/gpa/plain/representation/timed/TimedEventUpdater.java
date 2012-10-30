package uk.ac.imperial.doc.gpa.plain.representation.timed;

import java.util.Collection;

import uk.ac.imperial.doc.jexpressions.constants.Constants;

public abstract class TimedEventUpdater
{
	abstract public Double getValue(double time);
	abstract public Collection<Double> getAllEventTimes();
	abstract public void update(Constants constants, double[] popVector, double time);
}
