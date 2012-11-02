package uk.ac.imperial.doc.gpa.plain.representation.timed;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;

import uk.ac.imperial.doc.jexpressions.javaoutput.utils.JExpressionsJavaUtils;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.javaoutput.utils.ClassCompiler;
import uk.ac.imperial.doc.pctmc.representation.State;

/**
 * Class contains information required to evaluate
 * PCTMC with time dependent rates, population jumps
 * and resets
 * 
 * @author Chris Guenther
 */
public class TimedEvents {
	
	public static String sDUMMY_FILENAME = "dummy.dat";
	Map<String, double[][]> mRateEvents = new HashMap<String, double[][]>();
	Map<State, double[][]> mJumpEvents = new HashMap<State, double[][]>();
	Map<State, double[][]> mResetEvents = new HashMap<State, double[][]>();
	
	public void addRateEventsFromFile(String rateName, String fileName) {
		mRateEvents.put(rateName, loadEventsFromFile(fileName));
	}

	public void addJumpEventsFromFile(State jumpState, String fileName) {
		mJumpEvents.put(jumpState, loadEventsFromFile(fileName));
	}
	
	public void addResetEventsFromFile(State resetState, String fileName) {
		mResetEvents.put(resetState, loadEventsFromFile(fileName));
	}

	/**
	 * Events will be automatically loaded from files but
	 * if needed they can also be set manually
	 * 
	 * @param rateEvents
	 * @param jumpEvents
	 * @param resetEvents
	 */
	public void setEvents(Map<String, double[][]> rateEvents,
						   Map<State, double[][]> jumpEvents,
						   Map<State, double[][]> resetEvents)
	{
		mRateEvents = rateEvents;
		mJumpEvents = jumpEvents;
		mResetEvents = resetEvents;
	}
	
	/**
	 * @param fileName
	 * @return events found in {@code fileName} unless
	 * 			{@code fileName} is sDUMMY_FILENAME in which case null
	 * 			is returned
	 */
	protected <T> double[][] loadEventsFromFile(String fileName) {
		if (fileName.equals(sDUMMY_FILENAME)) {return null;}
		return JExpressionsJavaUtils.loadTimeSeriesFromFile(fileName);
	}

	public static String sTemplateITimedEventPopUpdateFct = 
								"import uk.ac.imperial.doc.gpa.plain.representation.timed.ITimedEventPopUpdateFct;\n" +
			  					"public class %s implements ITimedEventPopUpdateFct {\n" +
			  					"public void update(double[] popVector, double value){\n" +
			  					"%s\n}\n}";
	
	/**
	 * Compile {@code className} with {@code functionBody} and create an instance of the class
	 * 
	 * @param className
	 * @param functionBody
	 * @return instance of ITimedEventPopUpdateFct with {@code className} and {@code functionBody}
	 */
	protected ITimedEventPopUpdateFct compileUpdateFcts(String className, String functionBody)
	{
		// Compile and instantiate the class
		Object[] codeArgs = {className, functionBody};
		String code = String.format(sTemplateITimedEventPopUpdateFct, codeArgs);
		return  (ITimedEventPopUpdateFct)ClassCompiler.getInstance(code, className);
	}
	
	/**
	 * @param countIndices
	 * @return for each population that is subject to jumps we create and compile a custom class
	 * 			that modifies the population count vector used by the numerical post processor
	 * 			class when the jump occurs
	 */
	public Map<State, ITimedEventPopUpdateFct> getJumpUpdateCountsFcts(Map<State, Integer> countIndices) {
		Map<State, ITimedEventPopUpdateFct> updaters =	new HashMap<State, ITimedEventPopUpdateFct>();
		int cnt=0;
		for (State s : mJumpEvents.keySet()) {
			String className = "PopUpdaterFctCountJump"+(cnt++);
			String functionBody = "popVector["+countIndices.get(s)+"] += value;";
			updaters.put(s, compileUpdateFcts(className,functionBody));
		}
		return updaters;
	}
	
	/**
	 * @param countIndices
	 * @return for each population that is subject to resets we create and compile a custom class
	 * 			that modifies the population count vector used by the numerical post processor
	 * 			class when the reset occurs
	 */
	public Map<State, ITimedEventPopUpdateFct> getResetUpdateCountsFcts(Map<State, Integer> countIndices) {
		Map<State, ITimedEventPopUpdateFct> updaters =	new HashMap<State, ITimedEventPopUpdateFct>();
		int cnt=0;
		for (State s : mResetEvents.keySet()) {
			String className = "PopUpdaterFctCountReset"+(cnt++);
			String functionBody = "popVector["+countIndices.get(s)+"] = value;";
			updaters.put(s, compileUpdateFcts(className,functionBody));
		}
		return updaters;
	}
	
	/**
	 * TODO: make it work for moments up to any order - not just order 2
	 * @param momentIndicies
	 * @return for each population that is subject to jumps we create and compile a custom class
	 * 			that modifies the population moment vector used by the numerical post processor
	 * 			class when the jump occurs
	 */
	public Map<State, ITimedEventPopUpdateFct> getJumpUpdateMomentsFcts(Map<CombinedPopulationProduct, Integer> momentIndicies) {
		Map<State, ITimedEventPopUpdateFct> updaters =
				new HashMap<State, ITimedEventPopUpdateFct>();
		int cnt=0;
		for (State s : mJumpEvents.keySet()) {			
			// Find important indices
			Map<State,Integer> firstOrderIndicides = new HashMap<State,Integer>();
			Set<Integer> secondOrderIndices = new HashSet<Integer>();
			Map<Integer,State> secondOrderOtherMomentIndex = new HashMap<Integer,State>();
			int firstOrderIndex = findMomentIndices(s, momentIndicies, firstOrderIndicides, secondOrderIndices, secondOrderOtherMomentIndex);
			
			// Generate the code
			String className = "PopUpdaterFctMomentJump"+(cnt++);
			String functionBody = "";
			
			for (int i : secondOrderIndices) {
				int otherMomentIndex = firstOrderIndicides.get(secondOrderOtherMomentIndex.get(i));
				// Dealing with X^2
				if (otherMomentIndex == firstOrderIndex) {
					functionBody += "popVector["+i+"] += Math.pow(popVector["+firstOrderIndex+"]+value,2)"+
									"-Math.pow(popVector["+firstOrderIndex+"],2);\n";
				}
				// Dealing with XY
				else
				{
					functionBody += "popVector["+i+"] += popVector["+otherMomentIndex+"]*value;\n";
				}
			}
			functionBody += "popVector["+firstOrderIndex+"] += value;\n";
			updaters.put(s, compileUpdateFcts(className,functionBody));
		}
		
		return updaters;
	}
	
	/**
	 * TODO: make it work for moments up to any order - not just order 2
	 * @param momentIndicies
	 * @return for each population that is subject to resets we create and compile a custom class
	 * 			that modifies the population moment vector used by the numerical post processor
	 * 			class when the reset occurs
	 */
	public Map<State, ITimedEventPopUpdateFct> getResetUpdateMomentsFcts(Map<CombinedPopulationProduct, Integer> momentIndicies) {
		Map<State, ITimedEventPopUpdateFct> updaters =
				new HashMap<State, ITimedEventPopUpdateFct>();
		int cnt=0;
		for (State s : mResetEvents.keySet()) {	
			// Find important indices
			Map<State,Integer> firstOrderIndicides = new HashMap<State,Integer>();
			Set<Integer> secondOrderIndices = new HashSet<Integer>();
			Map<Integer,State> secondOrderOtherMomentIndex = new HashMap<Integer,State>();
			int firstOrderIndex = findMomentIndices(s, momentIndicies, firstOrderIndicides, secondOrderIndices, secondOrderOtherMomentIndex);
			
			// Generate the code
			String className = "PopUpdaterFctMomentReset"+(cnt++);
			String functionBody = "";
			
			for (int i : secondOrderIndices) {
				int otherMomentIndex = firstOrderIndicides.get(secondOrderOtherMomentIndex.get(i));
				// Dealing with X^2
				if (otherMomentIndex == firstOrderIndex) {
					functionBody += "popVector["+i+"] = value*value;\n";
				}
				// Dealing with XY
				else
				{
					functionBody += "popVector["+i+"] = value*popVector["+otherMomentIndex+"];\n";
				}
			}
			functionBody += "popVector["+firstOrderIndex+"] = value;\n";
			updaters.put(s, compileUpdateFcts(className,functionBody));
		}
		
		return updaters;
	}

	/**
	 * Find array indices of all first and second order moments
	 * that are affected by a deterministic population change
	 * in {@code s}.
	 * 
	 * TODO: Extend for moments higher than order 2
	 * 
	 * @param s
	 * @param momentIndicies
	 * @param firstOrderIndicides
	 * @param secondOrderIndices
	 * @param secondOrderOtherMomentIndex
	 * @return index of population s in array
	 */
	private int findMomentIndices(State s, Map<CombinedPopulationProduct, Integer> momentIndicies,
							Map<State, Integer> firstOrderIndicides, Set<Integer> secondOrderIndices,
							Map<Integer, State> secondOrderOtherMomentIndex) {
		int firstOrderIndex = -1;
				
		for (Entry<CombinedPopulationProduct, Integer> e : momentIndicies.entrySet()) {
			CombinedPopulationProduct cpp = e.getKey();
			if (cpp.getOrder() == 1) {
				State s1 = cpp.getNakedProduct().asMultiset().iterator().next();
				if(s1.equals(s)) {
					firstOrderIndex = e.getValue();
				}
				firstOrderIndicides.put(s1,e.getValue());
			}
			else if (cpp.getOrder() == 2) {
				Iterator<State> states = cpp.getNakedProduct().asMultiset().iterator();
				State s1 = states.next();
				State s2 = states.next();
				if (s1.equals(s)) {secondOrderIndices.add(e.getValue());
								   secondOrderOtherMomentIndex.put(e.getValue(),s2);
								   continue;}
				if (s2.equals(s)) {secondOrderIndices.add(e.getValue());
				 				   secondOrderOtherMomentIndex.put(e.getValue(),s1);}
			}
			else {
				throw new AssertionError("Inhomogeneous jumps currently not support for moments higher than order 2");
			}
		}
		
		return firstOrderIndex;
	}
	
	/**
	 * @return TimedEventUpdater objects for all known time dependent rates
	 */
	protected Map<String, TimedEventUpdater> getRateUpdaters() {
		Map<String, TimedEventUpdater> rateUpdaters = 
				new HashMap<String,TimedEventUpdater>();

		for (Entry<String, double[][]> e : mRateEvents.entrySet()) {
			String rateName = e.getKey();
			double[][] rates = e.getValue();
			rateUpdaters.put(rateName,new DiscreteRateTimedEventUpdater(rates, rateName));
		}

		return rateUpdaters;
	}

	/**
	 * @param events
	 * @param popUpdateFcts must be defined for all these populations in {@code events}
	 * @return TimedEventUpdater objects for all populations in {@code events}
	 */
	protected Map<State, TimedEventUpdater> getPopUpdaters(
			Map<State, double[][]> events,
			Map<State, ITimedEventPopUpdateFct> popUpdateFcts) {
		Map<State, TimedEventUpdater> popUpdaters = 
				new HashMap<State,TimedEventUpdater>();

		for (Entry<State, double[][]> e : events.entrySet()) {
			State pop = e.getKey();
			double[][] rates = e.getValue();
			popUpdaters.put(pop,new DiscretePopTimedEventUpdater(rates, popUpdateFcts.get(pop)));
		}

		return popUpdaters;
	}
	
	/**
	 * @param jumpUpdateFcts the custom compiled objects that modify population arrays for jumps
	 * @param resetUpdateFcts the custom compiled objects that modify population arrays for resets
	 * @return ordered schedule of rate and population updates
	 */
	public Map<Double, Collection<TimedEventUpdater>> genTimedEventUpdates(
			Map<State, ITimedEventPopUpdateFct> jumpUpdateFcts,
			Map<State, ITimedEventPopUpdateFct> resetUpdateFcts)
	{
		Map<Double, Collection<TimedEventUpdater>> updates =
				new TreeMap<Double, Collection<TimedEventUpdater>>();
		Map<String,TimedEventUpdater> rateUpdaters = getRateUpdaters();
		Map<State,TimedEventUpdater> jumpUpdaters = getPopUpdaters(mJumpEvents,jumpUpdateFcts);
		Map<State,TimedEventUpdater> resetUpdaters = getPopUpdaters(mResetEvents,resetUpdateFcts);
		
		// The rate updates
		for (TimedEventUpdater teu : rateUpdaters.values()) {
			for (double time : teu.getAllEventTimes()) {
				Collection<TimedEventUpdater> events = updates.get(time);
				if (events == null) {
					events = new LinkedList<TimedEventUpdater>();
					updates.put(time, events);
				}
				events.add(teu);
			}
		}
		
		// The jump updates
		for (Entry<State, TimedEventUpdater> e : jumpUpdaters.entrySet()) {
			TimedEventUpdater teu = e.getValue();
			TimedEventUpdater teuReset = resetUpdaters.get(e.getKey());
			for (double time : teu.getAllEventTimes()) {
				Collection<TimedEventUpdater> events = updates.get(time);
				if (events == null) {
					events = new LinkedList<TimedEventUpdater>();
					updates.put(time, events);
				}
				// Ensure that we only add the jump event if no
				// reset event occurs for the same population at
				// the same time
				if (teuReset == null || teuReset.getValue(time) == null) {
					events.add(teu);
				}
			}
		}
		
		// The reset updates
		for (TimedEventUpdater teu : resetUpdaters.values()) {
			for (double time : teu.getAllEventTimes()) {
				Collection<TimedEventUpdater> events = updates.get(time);
				if (events == null) {
					events = new LinkedList<TimedEventUpdater>();
					updates.put(time, events);
				}
				events.add(teu);
			}
		}
		
		return updates;
	}
}
