package uk.ac.imperial.doc.gpa.probes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.gpa.fsm.ITransition;
import uk.ac.imperial.doc.gpa.fsm.NFADetectors;
import uk.ac.imperial.doc.gpa.fsm.NFAState;
import uk.ac.imperial.doc.gpa.fsm.NFAStateToPEPA;
import uk.ac.imperial.doc.gpa.fsm.NFAUtils;
import uk.ac.imperial.doc.gpa.fsm.SignalTransition;
import uk.ac.imperial.doc.gpa.fsm.Transition;
import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.group.Group;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.representation.model.LabelledComponentGroup;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.igpepa.representation.components.iPEPAComponentDefinitions;
import uk.ac.imperial.doc.igpepa.representation.model.GlobalProbeSimGroupCooperation;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.interpreter.IExtension;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.rits.cloning.Cloner;

public class ProbeSpec implements IExtension{

	public GlobalProbe gprobe;
	public Set<String> alphabet;
	public Set<ITransition> allActions;
	
	Map<String, PEPAComponent> origComponents;
	public Set<ComponentId> localAcceptingStates;

	private Set<PEPAComponent> initialStates;
	
	private AbstractPCTMCAnalysis analysis;
	private NumericalPostprocessor postprocessor;
	
	private AbstractProbeRunner runner;
	
	private String output;
	
	
	public Map<String, PEPAComponent> newComp;
	public Map<String, PEPAComponent> altComp;

	public PEPAComponentDefinitions newMainDef;

	GroupedModel model;

	int mode;
	double modePar;

	public CDF measured_times;

	public ProbeSpec(Set<String> actions,
			 GroupedModel model,
			 AbstractPCTMCAnalysis analysis,
			 NumericalPostprocessor postprocessor,	
			 int mode, double modePar) {
		this.analysis = analysis;
		this.postprocessor = postprocessor;
	
		this.alphabet = new HashSet<String> ();
		this.gprobe = new GlobalProbe();
		this.allActions = new HashSet<ITransition> ();
		for (String action : actions) {
			allActions.add(new Transition(action));
		}
		
		this.localAcceptingStates = new HashSet<ComponentId>();

		this.model = model;
		this.mode = mode;
		this.modePar = modePar;
	}

	public void addToAllActions(String signal) {
		allActions.add(new SignalTransition(signal));
	}

	public void processGlobal(Map<String, PEPAComponent> components, Set<PEPAComponent> initialStates,
			Map<String, PEPAComponent> newComp,  Map<String, PEPAComponent> altComp		
	) {
		this.newComp = newComp;
		this.altComp = altComp;
		this.origComponents = new Cloner().deepClone(components);
		this.initialStates = initialStates;
		Map<String, PEPAComponent> newDef = new HashMap<String, PEPAComponent>(
				newComp);
		newDef.putAll(origComponents);
		newMainDef = new iPEPAComponentDefinitions(newDef)
				.removeVanishingStates(initialStates);
	}
	
	public void collectNewComp() {
	}

	private Map<PEPAComponentDefinitions, Set<ComponentId>> defMap;
	private Set<GPEPAState> stateObservers;
	private PEPAComponentDefinitions altDef;
	
	
	public void afterProbeg(String globalProbeName, 				
			boolean steady, List<ITransition> excluded,
			Constants mainConstants,
			Map<ExpressionVariable,AbstractExpression> mainUnfoldedVariables,
			iPEPAComponentDefinitions mainDefinitions
	) throws Exception {
		gprobe.setName(globalProbeName);

		stateObservers = new HashSet<GPEPAState>();
		Set<GroupComponentPair> pairs = model
				.getGroupComponentPairs(newMainDef);
		for (GroupComponentPair g : pairs) {
			stateObservers.add(new GPEPAState(g));
		}

		defMap = new HashMap<PEPAComponentDefinitions, Set<ComponentId>>();
		Set<ComponentId> newComps = new HashSet<ComponentId>();
		for (String name : newComp.keySet()) {
			newComps.add(new ComponentId(name));
		}
		defMap.put(newMainDef, newComps);
		altDef = null;
		if (steady) {
			Set<ComponentId> altComps = new HashSet<ComponentId>();
			for (String name : altComp.keySet()) {
				altComps.add(new ComponentId(name));
			}
			// we can reuse altComp now
			altComp.putAll(origComponents);
			altDef = new iPEPAComponentDefinitions(altComp)
					.removeVanishingStates(initialStates);
			defMap.put(altDef, altComps);
		}
		if (analysis instanceof PCTMCSimulation) {
			if (mode == 3) {
				Set<ITransition> monitoring = NFADetectors.detectAlphabet(
						gprobe.getStartingState(), true, excluded);
				Map<String, PEPAComponent> globalComponents = new HashMap<String, PEPAComponent>();
				Set<ComponentId> accepting = new HashSet<ComponentId>();
				generateProbeComponent(gprobe.getName(), gprobe
						.getStartingState(), false, monitoring,
						globalComponents, accepting);

				Multimap<PEPAComponent, AbstractExpression> counts = HashMultimap
						.create();
				counts.put(new ComponentId(gprobe.getName()),
						DoubleExpression.ONE);
				Group group = new Group(counts);
				String label = "GlobalProbe";
				GroupedModel globalModel = new GlobalProbeSimGroupCooperation(
						model, new LabelledComponentGroup(label, group),
						alphabet);

				globalComponents.putAll(origComponents);
				globalComponents.putAll(newComp);
				initialStates.add(new ComponentId(gprobe.getName()));
				PEPAComponentDefinitions globalDef = new iPEPAComponentDefinitions(
						globalComponents).removeVanishingStates(initialStates);

				stateObservers.clear();
				pairs = globalModel.getGroupComponentPairs(globalDef);
				for (GroupComponentPair g : pairs) {
					stateObservers.add(new GPEPAState(g));
				}
				runner = new SimProbeRunner(mainConstants,
						mainUnfoldedVariables);
				
				model = globalModel;
				newMainDef = globalDef;
				altDef = null;
				defMap = null;
				localAcceptingStates = accepting;
			} else {
				runner = new SimProbeRunner(mainConstants,
						mainUnfoldedVariables);
			}
		} else {
			runner = new ODEProbeRunner(mainConstants,
					mainUnfoldedVariables);
		}
	}
	
	public void execute() {
		measured_times = runner.executeProbedModel(gprobe,
		model, stateObservers, newMainDef, altDef, defMap,
		localAcceptingStates, analysis, postprocessor, alphabet,
		mode, modePar);
		
		if (output != null)
		{
			FileUtils.writeGeneralFile
				(measured_times.toString (), output);
		}
	}

	public static void generateProbeComponent(String name, NFAState startingState,
			boolean repeating, Set<ITransition> allActions,
			Map<String, PEPAComponent> probeComponents,
			Set<ComponentId> accepting) throws Exception {
		Set<NFAState> acceptingStates = NFADetectors
				.detectAllAcceptingStates(startingState);

		if (repeating) {
			Set<NFAState> allStates = NFADetectors
					.detectAllStates(startingState);
			for (NFAState state : allStates) {
				Multimap<ITransition, NFAState> transitions = state
						.getTransitions();
				ITransition[] trArr = transitions.keySet().toArray(
						new ITransition[1]);
				for (int i = 0; i < trArr.length; i++) {
					Collection<NFAState> reachables = transitions.get(trArr[i]);
					for (NFAState reachable : reachables) {
						if (acceptingStates.contains(reachable)) {
							state.replaceTransition(trArr[i], startingState);
							break;
						}
					}
				}
			}
		} else {
			for (NFAState accState : acceptingStates) {
				for (ITransition transition : allActions) {
					accState.addTransitionIfNotExisting(transition
							.getSimpleTransition(), accState);
				}
			}
		}


		NFAUtils.removeSurplusSelfLoops(startingState);

		Map<String, PEPAComponent> newComponents = NFAStateToPEPA
				.HybridDFAtoPEPA(startingState, name);
		for (NFAState state : acceptingStates) {
			accepting.add(new ComponentId(state.getName()));
		}
		probeComponents.putAll(newComponents);
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}
	
	

}
