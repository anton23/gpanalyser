tree grammar GPACompiler;
 
options{   
  language = Java; 
  tokenVocab = GPAParser; 
  ASTLabelType = CommonTree;  
}

import PCTMCCompilerPrototype;

@header{


	package uk.ac.imperial.doc.gpa.syntax;


	import java.util.LinkedList;
	import java.util.Map;
	import java.util.HashMap;
	import java.util.Set;
	import java.util.HashSet;
	import java.util.LinkedHashMap;
	import java.util.Collection;

	import com.google.common.collect.Multimap;
	import com.google.common.collect.HashMultimap;
	import com.google.common.collect.LinkedHashMultimap;
	import com.google.common.collect.HashMultiset;
	import com.google.common.collect.Multiset;

	import uk.ac.imperial.doc.jexpressions.expressions.*;
	import uk.ac.imperial.doc.jexpressions.conditions.*;
	import uk.ac.imperial.doc.jexpressions.constants.*;
	import uk.ac.imperial.doc.jexpressions.variables.*;
	import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;

	import uk.ac.imperial.doc.pctmc.analysis.*;

	import uk.ac.imperial.doc.pctmc.odeanalysis.*; 
	import uk.ac.imperial.doc.pctmc.simulation.*;
	import uk.ac.imperial.doc.pctmc.compare.*;
	import uk.ac.imperial.doc.pctmc.expressions.*;
	import uk.ac.imperial.doc.pctmc.expressions.patterns.*;
	import uk.ac.imperial.doc.pctmc.plain.*;
	import uk.ac.imperial.doc.pctmc.representation.State;
	import uk.ac.imperial.doc.pctmc.representation.*; 
	import uk.ac.imperial.doc.pctmc.experiments.iterate.*; 
	import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.*; 
	import uk.ac.imperial.doc.pctmc.postprocessors.numerical.*;
	import uk.ac.imperial.doc.pctmc.syntax.CustomRecognitionException;
    import uk.ac.imperial.doc.pctmc.syntax.ErrorReporter;

	import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
	import uk.ac.imperial.doc.gpepa.representation.components.*;
	import uk.ac.imperial.doc.igpepa.representation.components.*;
	import uk.ac.imperial.doc.gpepa.representation.group.*;
	import uk.ac.imperial.doc.gpepa.representation.model.*;
	import uk.ac.imperial.doc.igpepa.representation.model.*;
	import uk.ac.imperial.doc.gpepa.representation.*;
	import uk.ac.imperial.doc.gpepa.states.*;
	import uk.ac.imperial.doc.gpa.patterns.*; 
	import uk.ac.imperial.doc.gpa.pctmc.*;
	import uk.ac.imperial.doc.gpa.syntax.CompilerError;
	
	import uk.ac.imperial.doc.pctmc.interpreter.IExtension;

	// Probes

	import java.util.ArrayList;
	import java.util.Arrays;
	import java.io.ByteArrayOutputStream;
	import java.io.PrintStream;

	import com.rits.cloning.Cloner;

	import uk.ac.imperial.doc.gpa.fsm.*;
	import uk.ac.imperial.doc.gpa.probes.*;
	import uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions.*;
	import uk.ac.imperial.doc.pctmc.utils.FileUtils;
}

@members {

	protected Stack<String> hint = new Stack<String>();

	protected ErrorReporter errorReporter;

	public void setErrorReporter(ErrorReporter errorReporter) {
		this.errorReporter = errorReporter;
	}

	public String getErrorHeader(RecognitionException e) {
		return "line "+e.line+":"+e.charPositionInLine;
	}

	public void displayRecognitionError(String[] tokenNames,
										RecognitionException e) {
		String hdr = getErrorHeader(e);
		String msg = getErrorMessage(e, tokenNames);

		if (errorReporter != null) {
			errorReporter.addError("["+hdr + "] " + msg);
		}
	}


	public String getErrorMessage(RecognitionException e,
									String[] tokenNames) {
		String ret;
		if (!hint.isEmpty()) {
			ret = hint.peek();
		} else{
			ret =  super.getErrorMessage(e, tokenNames);
		}
		return ret;
	}

	// Probes

	private String t = "temp";
	private GroupedModel mainModel;
	private Constants mainConstants;
	private Map<ExpressionVariable,AbstractExpression> mainUnfoldedVariables;
	private iPEPAComponentDefinitions mainDefinitions;
	private Map<String, PEPAComponent> components;
	private List<ITransition> excluded = new LinkedList<ITransition> ();
	Cloner deepCloner = new Cloner ();

	private void initExcluded ()
	{
		excluded.add (new StartTransition ());
		excluded.add (new StopTransition ());
	}



	
}

@rulecatch {
  catch (RecognitionException re) {
   reportError(re);  
   recover(input, re);
  }
  catch (AssertionError e) {
   reportError(new CustomRecognitionException(input, e.getMessage()));
   recover(input, new CustomRecognitionException(input, e.getMessage()));
  }
  catch (Exception e) {
   e.printStackTrace ();
  }
}


start:;

modelDefinition[Map<ExpressionVariable,AbstractExpression> unfoldedVariables,Constants constants] returns [PCTMC pctmc]
@init{
  Set<String> actions = new HashSet<String>(); 
  Set<String> cooperationActions = new HashSet<String>();
  mainConstants = constants;
  mainUnfoldedVariables = unfoldedVariables;
}:
  cd = componentDefinitions
  {
    components = $cd.componentDefinitions;
    mainDefinitions = new iPEPAComponentDefinitions (components);
  }
  m=model
  {
    mainModel = $m.model;
  }
  (ca=countActions {cooperationActions=$countActions.cooperationActions;})?
  {
  	mainDefinitions = mainDefinitions.removeVanishingStates($m.model.getInitialComponents());
	$m.model.unfoldImplicitCooperations(mainDefinitions);
    $pctmc  = GPEPAToPCTMC.getPCTMC(mainDefinitions ,$m.model,cooperationActions, $unfoldedVariables);
  }
;

countActions returns[Set<String> cooperationActions]
@init{
  $cooperationActions = new HashSet<String>();
}:
  ^(COUNTACTIONS (a=LOWERCASENAME {$cooperationActions.add($a.text);} )+)
;

componentDefinitions returns [Map<String,PEPAComponent> componentDefinitions]
@init{
  componentDefinitions = new HashMap<String,PEPAComponent>();
}:
  (^(COMPONENT id=UPPERCASENAME s=component){
    String seqId = $id.text; 
    PEPAComponent seq = $s.c; 
    componentDefinitions.put(seqId,seq);
  })+
;

model returns [GroupedModel model]:
  ^(COOP cooperationActions=coop  l=model r=model){
    model = new iPEPAGroupCooperation($l.model,$r.model,$cooperationActions.cooperationActions);
  }
 | ^(COOP TIMES l=model r=model){
       model = new iPEPAGroupCooperation($l.model,$r.model);
   }
 | ^(LABELLEDGROUP label=UPPERCASENAME g=group){
    $model = new iPEPALabelledComponentGroup($label.text,$g.group);
   }  
;

group returns [Group group]
@init{  
  Multimap counts = LinkedHashMultimap.<PEPAComponent,AbstractExpression>create(); 
}
@after{
  $group = new Group(counts); 
}:

  (^(MULT s=component n=expression){
    counts.put($s.c,$n.e);
  }  
  )+
;

state returns [State t]:
  gp = groupComponentPair {$t = new GPEPAState($gp.gp);}
 |a = actionCount {$t = new GPEPAActionCount($a.a);}
;

actionCount returns [String a]:
 ^(ACOUNT n=LOWERCASENAME) {$a = $n.text;}
;

groupComponentPair returns [GroupComponentPair gp]:
  ^(PAIR g=UPPERCASENAME c=component)
  {$gp = new GroupComponentPair($g.text,$c.c);}
;



component returns [PEPAComponent c]:
    co = coopComponent {$c = $co.c;}
   | ch=choice {$c=$ch.c;}
     
   |p=primaryComponent {
    $c= $p.c;
   }
   ;

coopComponent returns [PEPAComponent c]:
  ^(COOPCOMP a=coop l=component DUMMY r=component){
        $c = new iCooperationComponent($l.c,$r.c,$a.cooperationActions);
    }
  | ^(COOPCOMP TIMES l=component DUMMY r=component){
        $c = new iCooperationComponent($l.c,$r.c);
    }
;
  
choice returns [PEPAComponent c]
@init{
  List<AbstractPrefix> choices = new LinkedList<AbstractPrefix>();
}
@after{
  $c=new iChoice(choices);
}:
  (p=prefix{
      choices.add($p.c); 
  })+
;

prefix returns [AbstractPrefix c]:
  ^(PREFIX r=expression a=LOWERCASENAME s=component){
      AbstractPrefix prefix = new iPrefix($a.text,$r.e,null,$s.c,
      	new LinkedList<ImmediatePrefix>());
      $c=prefix; 
   }
   | ^(PREFIX PASSIVE w=expression? a=LOWERCASENAME s=component){
      AbstractExpression tw = (w == null) ? DoubleExpression.ONE : $w.e;
      AbstractPrefix prefix = new iPassivePrefix($a.text,null,tw,$s.c,
      	new LinkedList<ImmediatePrefix>());
      $c=prefix; 
   }
   | ^(PREFIX a=LOWERCASENAME s=component){
      AbstractPrefix prefix
        = new ImmediatePrefix($a.text,DoubleExpression.ONE,$s.c);
	  $c=prefix;
   }
; 

primaryComponent returns [PEPAComponent c]:
  n=UPPERCASENAME{
    $c=new ComponentId($n.text);
  }
  | ANY {$c=new AnyComponent();}
  |STOP {
    $c = new Stop(); 
  } 
;
  
coop returns [Set<String> cooperationActions]
@init{
  cooperationActions = new HashSet<String>();
}:
  (a=LOWERCASENAME{
  if (!$a.text.isEmpty()){
    cooperationActions.add($a.text);
    } 
   })+
;

extensions returns [List<IExtension> extensions]
@init
{
	initExcluded ();
	$extensions = new LinkedList<IExtension>();
}
	:	^(PROBES (d=probe_def {$extensions.add($d.probeExecutable);})*) ;

// Local_And_Global_Probe

// Local

probel [String name, Set<ITransition> allActions, Set<String> alphabet,
		boolean steady]
	returns [Map<String, PEPAComponent> probeComponents,
			 Map<String, PEPAComponent> altProbeComponents,
			 Set<ComponentId> acceptingComponents]
scope
{
	List<NFAUtils.Signaller> signallers;
}
@init
{
	$acceptingComponents = new HashSet<ComponentId> ();
	$probel::signallers = new ArrayList<NFAUtils.Signaller> ();
}
	:	^(PROBEL (rl_signal [$allActions, $alphabet])+ rp=REPETITION?)
			{
				NFAState starting_state = NFAUtils.mergeSignallers
					($probel::signallers);
				if (steady)
				{
					NFAState nonrepeating = deepCloner.deepClone
						(starting_state);
					$altProbeComponents = new HashMap<String, PEPAComponent> ();
					ProbeSpec.generateProbeComponent
						($name, nonrepeating, false, allActions,
							$altProbeComponents, $acceptingComponents);
				}
				$probeComponents = new HashMap<String, PEPAComponent> ();
				Set<ComponentId> thisAccepting = new HashSet<ComponentId>();
				ProbeSpec.generateProbeComponent
					($name, starting_state, rp != null, allActions,
						$probeComponents, thisAccepting);
				if (!steady)
				{
					$acceptingComponents = thisAccepting;
				}
			} ;

rl_signal [Set<ITransition> allActions, Set<String> alphabet]
@init
{
	NFAState starting_state = new NFAState (t);
}
	:	^(RLS rl_single [starting_state, $allActions, $alphabet] sig=signal)
			{
				SignalTransition signal = new SignalTransition ($sig.name);
				starting_state = NFAtoDFA.convertToDFA (starting_state, t);
				$probel::signallers.add (new NFAUtils.Signaller
						(starting_state, signal));
			} ;

rl_single [NFAState current_state, Set<ITransition> allActions,
		Set<String> alphabet]
	returns [NFAState reached_state]
@init
{
	NFAState starting_state = new NFAState (t);
}
	:	^(RL_SINGLE (^(RL_SINGLE_BRACKETED
			rs=rl_bracketed [starting_state, $allActions, $alphabet]
			(op=rl_un_operators
				[starting_state, $rs.reached_state, $allActions])?)))
			{
				if (op != null)
				{
					$current_state.addTransition (new EmptyTransition (),
						$op.starting_state);
					$reached_state = $op.reached_state;
				}
				else
				{
					$current_state.addTransition (new EmptyTransition (),
						starting_state);
					$reached_state = $rs.reached_state;
				}
				$current_state.setAccepting (false);
			}
		| ^(RL_SINGLE (^(ACTION rs=immediateActions
						[starting_state, $allActions, $alphabet]
						(op=rl_un_operators
							[starting_state, $rs.reached_state,
							$allActions])?)))
			{
				if (op != null)
				{
					$current_state.addTransition (new EmptyTransition (),
						$op.starting_state);
					$reached_state = $op.reached_state;
				}
				else
				{
					$current_state.addTransition (new EmptyTransition (),
						starting_state);
					$reached_state = $rs.reached_state;
				}
				$current_state.setAccepting (false);
			} ;

rl_bracketed  [NFAState current_state, Set<ITransition> allActions,
		Set<String> alphabet]
	returns [NFAState reached_state]
	:	^(RL_BRACKETED rl [$current_state, $allActions, $alphabet])
			{
				$reached_state = $rl.reached_state;
			} ;

rl [NFAState current_state, Set<ITransition> allActions, Set<String> alphabet]
	returns [NFAState reached_state]
@init
{
	NFAState new_starting_state1 = new NFAState (t);
	NFAState new_starting_state2 = new NFAState (t);
}
	:	^(RL rl_single [new_starting_state1, $allActions, $alphabet]
		(rl2=rl [new_starting_state2, $allActions, $alphabet]
		 op=rl_bin_operators [new_starting_state1, $rl_single.reached_state,
				new_starting_state2, $rl2.reached_state, $allActions])?)
			{
				if (op != null)
				{
					$current_state.addTransition (new EmptyTransition (),
						$op.starting_state);
					$reached_state = $op.reached_state;
				}
				else
				{
					$current_state.addTransition (new EmptyTransition (),
						new_starting_state1);
					$reached_state = $rl_single.reached_state;
				}
				$current_state.setAccepting (false);
				$reached_state.setAccepting (true);
			} ;

rl_bin_operators [NFAState starting_state1,
	NFAState current_state1, NFAState starting_state2,
	NFAState current_state2, Set<ITransition> allActions]
	returns [NFAState starting_state, NFAState reached_state]
	:	^(BINARY_OP COMMA)
			{
				$starting_state = new NFAState (t);
				$starting_state.addTransition (new EmptyTransition (),
					$starting_state1);
				current_state1.addTransition (new EmptyTransition (),
					starting_state2);
				$current_state1.setAccepting (false);
				$reached_state = $current_state2;
			}
		| ^(BINARY_OP PAR)
			{
				$starting_state = new NFAState (t);
				$current_state1.setAccepting (false);
				$current_state2.setAccepting (false);
				$starting_state.addTransition (new EmptyTransition (),
					$starting_state1);
				$starting_state.addTransition (new EmptyTransition (),
					starting_state2);
				$reached_state = new NFAState (t);
				$reached_state.setAccepting (true);
				$current_state1.addTransition (new EmptyTransition (),
					$reached_state);
				$current_state2.addTransition (new EmptyTransition (),
					$reached_state);
			}
		| ^(BINARY_OP SEMI)
			{
				NFAState dfa1 = NFAtoDFA.convertToDFA (starting_state1, t);
				NFAState dfa2 = NFAtoDFA.convertToDFA (starting_state2, t);

				List<CartesianUtils.CartesianState> list
					= CartesianUtils.getCompleteCartesianDFA (dfa1, dfa2);
				CartesianUtils.CartesianState comb_starting_state
					= CartesianUtils.getCartesianState (dfa1, dfa2, list);
				$starting_state = NFAUtils.getBothCombination
					(comb_starting_state, list, t);
				$reached_state = new NFAState (t);

				NFAUtils.unifyAcceptingStates ($starting_state, $reached_state);
			}
		| ^(BINARY_OP DIVIDE)
			{
				NFAState dfa1 = NFAtoDFA.convertToDFA (starting_state1, t);
				NFAState dfa2 = NFAtoDFA.convertToDFA (starting_state2, t);

				List<CartesianUtils.CartesianState> list
					= CartesianUtils.getCompleteCartesianDFA (dfa1, dfa2);
				CartesianUtils.CartesianState comb_starting_state
					= CartesianUtils.getCartesianState (dfa1, dfa2, list);
				$starting_state = NFAUtils.getResetCombination
					(comb_starting_state, list, t);
				$reached_state = new NFAState (t);

				NFAUtils.unifyAcceptingStates ($starting_state, $reached_state);
			}
		| ^(BINARY_OP AT)
			{
				NFAState dfa1 = NFAtoDFA.convertToDFA (starting_state1, t);
				NFAState dfa2 = NFAtoDFA.convertToDFA (starting_state2, t);

				List<CartesianUtils.CartesianState> list
					= CartesianUtils.getCompleteCartesianDFA (dfa1, dfa2);
				CartesianUtils.CartesianState comb_starting_state
					= CartesianUtils.getCartesianState (dfa1, dfa2, list);
				$starting_state = NFAUtils.getFailCombination
					(comb_starting_state, list, t, $allActions);
				$reached_state = new NFAState (t);
				NFAUtils.unifyAcceptingStates ($starting_state, $reached_state);
			} ;

rl_un_operators [NFAState sub_starting_state,
	NFAState sub_current_state, Set<ITransition> allActions]
	returns [NFAState starting_state, NFAState reached_state]
@init
{
	$starting_state = new NFAState (t);
	$reached_state = new NFAState (t);
	$reached_state.setAccepting (true);
}
	:	^(UNARY_OP e1=expression (COMMA e2=expression)?)
			{
				ExpressionEvaluatorWithConstants e1eval
					= new ExpressionEvaluatorWithConstants (mainConstants);
				$e1.e.accept (e1eval);
				int e1int = (int) e1eval.getResult ();

				$sub_starting_state
					= NFAtoDFA.convertToDFA ($sub_starting_state, t);
				NFAUtils.unifyAcceptingStates
					($sub_starting_state, $reached_state);
				NFAState currentLast = $starting_state;
				int i = 0;
				for (; i < e1int; i++)
				{
					NFAState sub = deepCloner.deepClone ($sub_starting_state);
					currentLast.addTransition (new EmptyTransition (), sub);
					currentLast = NFADetectors.detectSingleAcceptingState (sub);
					currentLast.setAccepting (false);
				}
				if (e2 != null)
				{
					ExpressionEvaluatorWithConstants e2eval
							= new ExpressionEvaluatorWithConstants
								(mainConstants);
					$e2.e.accept (e2eval);
					int e2int = (int) e2eval.getResult ();

					for (; i < e2int; i++)
					{
						NFAState sub = deepCloner.deepClone
							($sub_starting_state);
						currentLast.addTransition (new EmptyTransition (), sub);
						currentLast.setAccepting (true);
						currentLast
							= NFADetectors.detectSingleAcceptingState (sub);
					}
					NFAUtils.unifyAcceptingStates
						($starting_state, $reached_state);
				}
				else
				{
					currentLast.addTransition
						(new EmptyTransition (), $reached_state);
				}
			}
		| ^(UNARY_OP ZERO_ONE)
			{
				$starting_state.addTransition (new EmptyTransition (),
					$sub_starting_state);
				$starting_state.addTransition (new EmptyTransition (),
					$reached_state);
				$sub_current_state.addTransition (new EmptyTransition (),
					$reached_state);
				$sub_current_state.setAccepting (false);
			}
		| ^(UNARY_OP PLUS)
			{
				$starting_state.addTransition (new EmptyTransition (),
					$sub_starting_state);
				$sub_current_state.addTransition (new EmptyTransition (),
					$starting_state);
				$sub_current_state.addTransition (new EmptyTransition (),
					$reached_state);
				$sub_current_state.setAccepting (false);
			}
		| times [$sub_starting_state, $sub_current_state]
			{
				$starting_state = $times.starting_state;
				$reached_state = $times.reached_state;
			}
		| ^(UNARY_OP NEGATION_OP)
			{
				$starting_state
					= NFAtoDFA.convertToDFA ($sub_starting_state, t);
				NFAState failure = NFAUtils.getNewFailureState ($allActions);
				NFAUtils.unifyAcceptingStates ($starting_state, failure);
				$starting_state = NFAtoDFA.convertToDFA ($starting_state, t);
				NFAUtils.invertAcceptingStates ($starting_state);
				NFAUtils.unifyAcceptingStates ($starting_state, $reached_state);
			} ;

times [NFAState sub_starting_state, NFAState sub_current_state]
	returns [NFAState starting_state, NFAState reached_state]
@init
{
	$starting_state = new NFAState (t);
	$reached_state = new NFAState (t);
	$reached_state.setAccepting (true);
}
	:	^(UNARY_OP TIMES)
			{
				$starting_state.addTransition (new EmptyTransition (),
					$reached_state);
				$reached_state.addTransition (new EmptyTransition (),
					$sub_starting_state);
				$sub_current_state.addTransition (new EmptyTransition (),
					$starting_state);
				$sub_current_state.setAccepting (false);
			} ;

signal returns [String name]
	:	^(SIGNAL signal_name=LOWERCASENAME)
			{
				$name = $signal_name.text;
			} ;

immediateActions [NFAState current_state, Set<ITransition> allActions,
		Set<String> alphabet]
	returns [NFAState reached_state]
	:	eventual_specific_action [$current_state, $allActions, $alphabet]
			{
				$reached_state = $eventual_specific_action.reached_state;
			}
		| subsequent_specific_action  [$current_state, $allActions, $alphabet]
			{
				$reached_state = $subsequent_specific_action.reached_state;
			}
		| any_action [$current_state, $allActions]
			{
				$reached_state = $any_action.reached_state;
			}
		| empty_action [$current_state]
			{
				$reached_state = $empty_action.reached_state;
			} ;

eventual_specific_action [NFAState current_state, Set<ITransition> allActions,
		Set<String> alphabet]
	returns [NFAState reached_state, GPEPAActionCount action]
@init
{
	NFAState new_starting_state1 = new NFAState (t);
	NFAState new_starting_state2 = new NFAState (t);
}
	:	^(EVENTUAL dot1=any_action [new_starting_state1, $allActions]
			t1=times [new_starting_state1, $dot1.reached_state]
			action_name=LOWERCASENAME dot2=any_action
			[new_starting_state2, $allActions]
			t2=times [new_starting_state2, $dot2.reached_state])
			{
				$current_state.addTransition (new EmptyTransition (),
					$t1.starting_state);
				$t1.reached_state.addTransition(new Transition
						($action_name.text), $t2.starting_state);
				$t1.reached_state.setAccepting (false);
				$reached_state = $t2.reached_state;
				$reached_state.setAccepting (true);
				$action = new GPEPAActionCount ($action_name.text);
				$alphabet.add ($action_name.text);
			} ;

subsequent_specific_action [NFAState current_state, Set<ITransition> allActions,
		Set<String> alphabet]
	returns [NFAState reached_state, String action]
	:	^(SPECIFIC name=LOWERCASENAME)
			{
				$reached_state = new NFAState (t);
				$reached_state.setAccepting (true);
				$action = $name.text;
				$current_state.addTransition
					(new Transition ($action), $reached_state);
				$current_state.setAccepting (false);
				NFAState failure = NFAUtils.getNewFailureState (allActions);
				for (ITransition transition : $allActions)
				{
					$current_state.addTransitionIfNotExisting
						(transition.getSimpleTransition (), failure);
				}
				$alphabet.add ($name.text);
			} ;

any_action [NFAState current_state, Set<ITransition> allActions]
	returns [NFAState reached_state]
	:	DOT
			{
				$reached_state = new NFAState (t);
				$reached_state.setAccepting (true);
				// $current_state.addTransition (new AnyTransition (),
				// 	$reached_state);
				for (ITransition transition : $allActions)
				{
					$current_state.addTransitionIfNotExisting
						(transition.getSimpleTransition (), $reached_state);
				}
				$current_state.setAccepting (false);
			} ;

empty_action [NFAState current_state]
	returns [NFAState reached_state, AbstractUExpression U]
	:	EMPTY
			{
				$reached_state = new NFAState (t);
				$reached_state.setAccepting (true);
				$current_state.addTransition (new EmptyTransition (),
					$reached_state);
				$current_state.setAccepting (false);
			} ;

// Global

probeg [boolean simulate, GlobalProbe gprobe, Set<ITransition> allActions,
		Set<String> alphabet]
@init
{
	NFAState starting_state1 = new NFAState (t);
	NFAState starting_state2 = new NFAState (t);
}
	:	^(PROBEG start_actions=rg [starting_state1, $allActions, $alphabet]
			stop_actions=rg [starting_state2, $allActions, $alphabet]
			rp=REPETITION?)
			{
				if ($simulate)
				{
					starting_state1 = NFAtoDFA.convertToDFA
						(starting_state1, t);
					starting_state2 = NFAtoDFA.convertToDFA
						(starting_state2, t);
					Set<NFAState> accepting
						= NFADetectors.detectAllAcceptingStates
							(starting_state1);
					for (NFAState acc_state : accepting)
					{
						acc_state.replaceTransition
							(new StartTransition (), starting_state2);
						acc_state.setAccepting (false);
					}
					NFAState final_acc_state = new NFAState (t);
					final_acc_state.setAccepting (true);
					if (rp != null)
					{
						final_acc_state = starting_state1;
					}
					accepting = NFADetectors.detectAllAcceptingStates
							(starting_state2);
					for (NFAState acc_state : accepting)
					{
						acc_state.replaceTransition
							(new StopTransition (), final_acc_state);
						acc_state.setAccepting (false);
					}

					$gprobe.setStartingState (starting_state1);
				}
				else
				{
					$gprobe.setU (new SequenceUExpression
							($start_actions.U, $stop_actions.U));
				}
			} ;

rg [NFAState current_state, Set<ITransition> allActions, Set<String> alphabet]
	returns [NFAState reached_state, AbstractUExpression U]
	:   // for fluid flow, we need no state machine
		rg_single [$allActions, $alphabet]
			{
				$U = $rg_single.U;
			}

		// for simulation, predicates are disabled
		| rl_single [$current_state, $allActions, $alphabet]
			{
				$reached_state = $rl_single.reached_state;
			} ;

rg_single [Set<ITransition> allActions, Set<String> alphabet]
	returns [AbstractUExpression U]
	:	rga_all [$allActions, $alphabet]
			{
				$U = $rga_all.U;
			}
		| ^(RG predicate=main_pred? op1=rg_single [$allActions, $alphabet]
		   		(op2=rg_single [$allActions, $alphabet]
		   		 op=rg_op [$op1.U, $op2.U])?)
			{
				if (op != null)
				{
					$U = $op.U;
				}
				else
				{
					$U = $op1.U;
				}
				if (predicate != null)
				{
					$U = new PredUExpression ($U, $predicate.predicate);
				}
			} ;

rg_op [AbstractUExpression U1, AbstractUExpression U2]
	returns [AbstractUExpression U]
	:
		^(BINARY_OP COMMA)
			{
				$U = new SequenceUExpression ($U1, $U2);
			}
		| ^(BINARY_OP PAR)
			{
				$U = new EitherUExpression ($U1, $U2);
			}
		| ^(BINARY_OP SEMI)
			{
				$U = new BothUExpression ($U1, $U2);
			} ;

rga_all [Set<ITransition> allActions, Set<String> alphabet]
	returns [AbstractUExpression U]
	:	^(RGA_ALL rga [$allActions, $alphabet] expr=expression?)
			{
				int times = 1;
				if (expr != null)
				{
					ExpressionEvaluatorWithConstants eval
						= new ExpressionEvaluatorWithConstants (mainConstants);
					$expr.e.accept (eval);
					times = (int) eval.getResult ();
				}
				$U = new ActionsUExpression ($rga.U, times);
			}
		| ^(EMPTY EMPTY)
			{
				$U = new ActionsUExpression
					(new UPrimeExpression
						(new HashSet<GPEPAActionCount> ()), 1);
			} ;

rga [Set<ITransition> allActions, Set<String> alphabet]
	returns [UPrimeExpression U]
@init
{
	Set<GPEPAActionCount> actions = new HashSet<GPEPAActionCount> ();
}
	:	^(RGA (a = eventual_specific_action
			[new NFAState (t), $allActions, $alphabet]
			{actions.add ($a.action);})+)
		{
			actions.remove (null);
			$U = new UPrimeExpression (actions);
		} ;

// Predicates for global
main_pred returns [Predicate predicate]
    :   p=pred
        {
            String predicateString = "(" + $p.predicate + ")";
            $predicate = Predicate.create (predicateString);
        } ;

pred returns [String predicate]
	:	pred1=pred_single (op=logical_op pred2=pred)?
	        {
                $predicate = "(" + $pred1.predicate + ")";
                if (op != null)
                {
                    $predicate += $op.operator + $pred2.predicate;
                }
	        } ;

logical_op returns [String operator]
    :   op=LOGICAL_OR
            {
                $operator = " " + $op.text + " ";
            } ;

pred_single returns [String predicate]
    :   bool=logical_pred
            {
                $predicate = $bool.predicate;
            }
        | bool=negation
            {
                $predicate = $bool.predicate;
            } ;

negation returns [String predicate]
	:	^(LOGICAL_NEGATION bool=logical_pred)
	        {
                $predicate = "(!(" + $bool.predicate + "))";
	        } ;

logical_pred returns [String predicate]
	:   TRUE
	        {
	            $predicate = "true";
	        }
	    | FALSE
	        {
                $predicate = "false";
            }
	    | bool=b_expr
	        {
	            $predicate = $bool.predicate;
	        } ;

b_expr returns [String predicate]
	:	^(cmp=comparison expr1=r_expr expr2=r_expr)
	        {
	            $predicate = $expr1.predicate
	                + $cmp.operator + $expr2.predicate;
	        } ;

comparison returns [String operator]
    :   cmp=COMPARISON
            {
                $operator = " " + $cmp.text + " ";
            }
        | cmp=LANGLE
            {
                $operator = " " + $cmp.text + " ";
            }
        | cmp=RANGLE
            {
                $operator = " " + $cmp.text + " ";
            } ;

r_expr returns [String predicate]
	:	^(EXPRESSION expr=concrete_r_expr (op=binary_op expr2=r_expr)?)
	        {
                $predicate = $expr.predicate;
                if (op != null)
                {
                    $predicate += $op.operator + $expr2.predicate;
                }
	        } ;

binary_op returns [String operator]
	:	op=PLUS
	        {
	            $operator = " " + $op.text + " ";
	        }
	    | op=MINUS
	        {
	            $operator = " " + $op.text + " ";
	        }
	    | op=TIMES
	        {
	            $operator = " " + $op.text + " ";
	        }
	    | op=DIVIDE
	        {
	            $operator = " " + $op.text + " ";
	        }
	    ;

concrete_r_expr returns [String predicate]
	:	gp=groupComponentPair
	        {
	            GPEPAState gps = new GPEPAState ($gp.gp);
                $predicate = "data [mapping.get (\"" + gps.toString () + "\")]";
	        }
	    | expr=expression
	        {
	        	ExpressionEvaluatorWithConstants eval
					= new ExpressionEvaluatorWithConstants (mainConstants);
				$expr.e.accept (eval);
	            $predicate = "(" + eval.getResult () + ")";
	        } ;

// Probe_spec

probe_def returns [ProbeSpec probeExecutable]
scope
{
	GroupedModel model;

	boolean steady;
}
@init
{
	$probe_def::model = deepCloner.deepClone (mainModel);
}
	:	^(PROBE_DEF	o=out? a=analysis[null, mainConstants, null]
			md=mode mt=probe_spec [$a.analysis, $a.postprocessor, $md.chosenMode, $md.par])
			{
				$probeExecutable = $mt.probeExecutable;
				if (o != null)
				{
				  $probeExecutable.setOutput($o.name);
				}
			}
		;

mode returns [int chosenMode, double par]
	:	^(STEADY limitTime=expression)
			{
				$probe_def::steady = true;
				ExpressionEvaluatorWithConstants parEval
					= new ExpressionEvaluatorWithConstants (mainConstants);
				$limitTime.e.accept (parEval);
				$par = parEval.getResult ();
				$chosenMode = 1;
			}
		| ^(TRANSIENT limitTime=expression)
			{
				$probe_def::steady = false;
				ExpressionEvaluatorWithConstants parEval
					= new ExpressionEvaluatorWithConstants (mainConstants);
				$limitTime.e.accept (parEval);
				$par = parEval.getResult ();
				$chosenMode = 2;
			}
		| ^(GLOBAL GLOBAL)
			{
				$probe_def::steady = false;
				$par = 0;
				$chosenMode = 3;
			} ;

out returns [String name]
	:	^(FILE FILENAME)
		{
			$name = $FILENAME.text.replace ("\"", "");
		} ;

probe_spec [AbstractPCTMCAnalysis analysis, NumericalPostprocessor postprocessor, int mode, double modePar]
	returns [ProbeSpec probeExecutable]
scope
{
     ProbeSpec spec;
     Set<PEPAComponent> initialStates;         
}
@init
{
  $probe_spec::spec = new ProbeSpec(mainDefinitions.getActions(),
  $probe_def::model, $analysis, $postprocessor, $mode, $modePar);
  $probe_spec::initialStates = new HashSet<PEPAComponent>();
}
	:	^(DEF signalNames=SIGNALS
				{
					String signalsString = $signalNames.text;
					String[] signals = signalsString.split (";");
					for (String signal : signals)
					{
							$probe_spec::spec.addToAllActions(signal);
					}
				}
			{
			  Map<String, PEPAComponent> newComp = new HashMap<String, PEPAComponent>();
			  Map<String, PEPAComponent> altComp = new HashMap<String, PEPAComponent>();
			}
			globalProbeName=UPPERCASENAME (local_probes [newComp, altComp])?
			{
				$probe_spec::spec.processGlobal(components, $probe_spec::initialStates, newComp, altComp);
				
			}
			(locations [$probe_spec::spec.newMainDef])?
			probeg [analysis instanceof PCTMCSimulation, $probe_spec::spec.gprobe, $probe_spec::spec.allActions,
					$probe_spec::spec.alphabet])
			{
			 $probe_spec::spec.afterProbeg($globalProbeName.text,
			   $probe_def::steady,
			   excluded,
			   mainConstants,
			   mainUnfoldedVariables,
			   mainDefinitions);
  

            } 
     {$probeExecutable = $probe_spec::spec;}       
            ;

local_probes [Map<String, PEPAComponent> newComp,
	Map<String, PEPAComponent> altComp]
	:	^(LPROBES_DEF (local_probe_ass [$newComp, $altComp])+) ;

local_probe_ass [Map<String, PEPAComponent> newComp,
	Map<String, PEPAComponent> altComp]
	:	^(DEF name=UPPERCASENAME probe=probel
			[$name.text, $probe_spec::spec.allActions, $probe_spec::spec.alphabet,
			 $probe_def::steady])
			 {
			 	$newComp.putAll ($probe.probeComponents);
			 	if ($probe_def::steady)
			 	{
			 		$altComp.putAll ($probe.altProbeComponents);
			 	}
			 	$probe_spec::spec.localAcceptingStates
			 		= ($probe.acceptingComponents);
			 	$probe_spec::initialStates.add (new ComponentId ($name.text));
			 };

locations [PEPAComponentDefinitions newDefinitions]
	:	^(LOCATIONS location+)
			{
				$probe_def::model.unfoldImplicitCooperations ($newDefinitions);
			};

location
	:	^(SUBSTITUTE m1=model m2=model)
			{
				Map<GroupedModel, GroupedModel> owners
					= new HashMap<GroupedModel, GroupedModel> ();
				$probe_def::model.enumerateGroupedModelParents (owners, null);
				GroupedModel ownerOfTheSought = owners.get ($m1.model);

				if (ownerOfTheSought == null)
				{
					throw new Error ("substitution unsuccessful: no "
					    + "appropriate model to substitute found");
				}
				if (ownerOfTheSought instanceof LabelledComponentGroup)
				{
					$probe_def::model = $m2.model;
				}
				else if (ownerOfTheSought instanceof GroupCooperation)
				{
					GroupCooperation gc = (GroupCooperation) ownerOfTheSought;
					List<GroupCooperation> parentsUpdate
						= gc.getParentsList (owners);
					if (gc.getLeft ().equals ($m1.model))
					{
						gc.setLeft ($m2.model);
					}
					else
					{
						gc.setRight ($m2.model);
					}
					for (GroupCooperation p : parentsUpdate)
					{
						p.refreshComponentGroups();
					}
				}
				else
				{
					throw new Error ("substitution unsuccessful: unsupported"
					    + " kind of model to substitute");
				}
			} ;
