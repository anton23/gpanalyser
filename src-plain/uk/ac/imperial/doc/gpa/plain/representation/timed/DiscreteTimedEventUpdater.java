package uk.ac.imperial.doc.gpa.plain.representation.timed;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class DiscreteTimedEventUpdater extends TimedEventUpdater
{
	protected Map<Double,Double> mValues;
	DiscreteTimedEventUpdater(double[][] values) {
		mValues = new HashMap<Double,Double>();
		for (double[] value : values) {
			mValues.put(value[0], value[1]);
		}
	}

	@Override
	public Collection<Double> getAllEventTimes() {
		return Collections.unmodifiableSet(mValues.keySet());
	}
	
	public Double getValue(double time) {
		return mValues.get(time);
	}
}
