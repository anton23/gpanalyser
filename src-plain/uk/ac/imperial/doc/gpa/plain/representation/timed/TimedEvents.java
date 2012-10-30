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
 * @author mcg05
 */
public class TimedEvents {

	Map<String, String> mRateFiles = new HashMap<String,String>();
	Map<State, String> mJumpFiles = new HashMap<State,String>();
	Map<State, String> mResetFiles = new HashMap<State,String>();

	public void addRateFile(String rateName, String fileName) {
		mRateFiles.put(rateName,fileName);
	}

	public void addJumpFile(State s, String fileName) {
		mJumpFiles.put(s, fileName);
	}
	
	public void addResetFile(State s, String fileName) {
		mResetFiles.put(s, fileName);
	}

	/**
	 * TODO: make it work for moments up to any order - not just order 2
	 * @param momentIndicies
	 * @return given the {@code momentIndicies} create and compile a class that modifies the population vector used
	 * 		   by the numerical post processor class when a population jump occurs
	 */
	public Map<State, ITimedEventPopUpdateFct> getJumpUpdateFcts(Map<CombinedPopulationProduct, Integer> momentIndicies) {
		Map<State, ITimedEventPopUpdateFct> updaters =
				new HashMap<State, ITimedEventPopUpdateFct>();
		int cnt=0;
		for (State s : mJumpFiles.keySet()) {			
			// Find important indices
			Map<State,Integer> firstOrderIndicides = new HashMap<State,Integer>();
			Set<Integer> secondOrderIndices = new HashSet<Integer>();
			Map<Integer,State> secondOrderOtherMomentIndex = new HashMap<Integer,State>();
			int firstOrderIndex = findIndices(s, momentIndicies, firstOrderIndicides, secondOrderIndices, secondOrderOtherMomentIndex);
			
			// Generate the code
			String className = "PopUpdaterFctJump"+(cnt++);
			String code = "import uk.ac.imperial.doc.gpa.plain.representation.timed.ITimedEventPopUpdateFct;\n" +
						  "public class "+className+" implements ITimedEventPopUpdateFct {\n" +
					      "public void update(double[] popVector, double value){\n";
			
			for (int i : secondOrderIndices) {
				int otherMomentIndex = firstOrderIndicides.get(secondOrderOtherMomentIndex.get(i));
				// Dealing with X^2
				if (otherMomentIndex == firstOrderIndex) {
					code += "popVector["+i+"] += Math.pow(popVector["+firstOrderIndex+"]+value,2)"+
							"-Math.pow(popVector["+firstOrderIndex+"],2);\n";
				}
				// Dealing with XY
				else
				{
					code += "popVector["+i+"] += popVector["+otherMomentIndex+"]*value;\n";
				}
			}
			code += "popVector["+firstOrderIndex+"] += value;\n";
			code += "}\n}";
			ITimedEventPopUpdateFct popUpdFct = (ITimedEventPopUpdateFct)ClassCompiler.getInstance(code, className);
			updaters.put(s, popUpdFct);
		}
		
		return updaters;
	}
	
	/**
	 * TODO: make it work for moments up to any order - not just order 2
	 * @param momentIndicies
	 * @return given the {@code momentIndicies} create and compile a class that modifies the population vector used
	 * 		   by the numerical post processor class when a population reset occurs
	 */
	public Map<State, ITimedEventPopUpdateFct> getResetUpdateFcts(Map<CombinedPopulationProduct, Integer> momentIndicies) {
		Map<State, ITimedEventPopUpdateFct> updaters =
				new HashMap<State, ITimedEventPopUpdateFct>();
		int cnt=0;
		for (State s : mJumpFiles.keySet()) {			
			// Find important indices
			Map<State,Integer> firstOrderIndicides = new HashMap<State,Integer>();
			Set<Integer> secondOrderIndices = new HashSet<Integer>();
			Map<Integer,State> secondOrderOtherMomentIndex = new HashMap<Integer,State>();
			int firstOrderIndex = findIndices(s, momentIndicies, firstOrderIndicides, secondOrderIndices, secondOrderOtherMomentIndex);
			
			// Generate the code
			String className = "PopUpdaterFctReset"+(cnt++);
			String code = "import uk.ac.imperial.doc.gpa.plain.representation.timed.ITimedEventPopUpdateFct;\n" +
						  "public class "+className+" implements ITimedEventPopUpdateFct {\n" +
					      "public void update(double[] popVector, double value){\n";
			
			for (int i : secondOrderIndices) {
				int otherMomentIndex = firstOrderIndicides.get(secondOrderOtherMomentIndex.get(i));
				// Dealing with X^2
				if (otherMomentIndex == firstOrderIndex) {
					code += "popVector["+i+"] = value*value;\n";
				}
				// Dealing with XY
				else
				{
					code += "popVector["+i+"] = value*popVector["+otherMomentIndex+"];\n";
				}
			}
			code += "popVector["+firstOrderIndex+"] = value;\n";
			code += "}\n}";
			ITimedEventPopUpdateFct popUpdFct = (ITimedEventPopUpdateFct)ClassCompiler.getInstance(code, className);
			updaters.put(s, popUpdFct);
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
	private int findIndices(State s, Map<CombinedPopulationProduct, Integer> momentIndicies,
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
	 * @return TimedEventUpdater objects for all time dependent rates
	 */
	private Map<String, TimedEventUpdater> getRateUpdaters() {
		Map<String, TimedEventUpdater> rateUpdaters = 
				new HashMap<String,TimedEventUpdater>();
		
		Map<String,double[][]> allSeries = JExpressionsJavaUtils.loadTimeSeriesFromFile(mRateFiles);
		for (Entry<String, double[][]> e : allSeries.entrySet()) {
			String rateName = e.getKey();
			double[][] rates = e.getValue();
			rateUpdaters.put(rateName,new DiscreteRateTimedEventUpdater(rates, rateName));
		}

		return rateUpdaters;
	}

	/**
	 * @param popFiles
	 * @param popUpdateFcts
	 * @return TimedEventUpdater objects for all populations in {@code popFiles}
	 * 		   where the update type, i.e. jump or reset is defined in {@code popUpdateFcts}
	 */
	private Map<State, TimedEventUpdater> getPopUpdaters(
			Map<State, String> popFiles,
			Map<State, ITimedEventPopUpdateFct> popUpdateFcts) {
		Map<State, TimedEventUpdater> popUpdaters = 
				new HashMap<State,TimedEventUpdater>();

		Map<State,double[][]> allSeries = JExpressionsJavaUtils.loadTimeSeriesFromFile(popFiles);
		for (Entry<State, double[][]> e : allSeries.entrySet()) {
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
		Map<State,TimedEventUpdater> jumpUpdaters = getPopUpdaters(mJumpFiles,jumpUpdateFcts);
		Map<State,TimedEventUpdater> resetUpdaters = getPopUpdaters(mResetFiles,resetUpdateFcts);
		
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
