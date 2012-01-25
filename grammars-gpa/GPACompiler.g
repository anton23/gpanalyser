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
	import uk.ac.imperial.doc.gpepa.representation.group.*;
	import uk.ac.imperial.doc.gpepa.representation.model.*; 
	import uk.ac.imperial.doc.gpepa.representation.*;
	import uk.ac.imperial.doc.gpepa.states.*;
	import uk.ac.imperial.doc.gpa.patterns.*; 
	import uk.ac.imperial.doc.gpa.pctmc.*;
	import uk.ac.imperial.doc.gpa.syntax.CompilerError;

	// Probes

	import java.util.ArrayList;
	import java.util.Arrays;
	import java.io.ByteArrayOutputStream;
	import java.io.PrintStream;

	import com.rits.cloning.Cloner;

	import uk.ac.imperial.doc.gpa.fsm.*;
	import uk.ac.imperial.doc.gpa.probes.*;
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
	private Map<ExpressionVariable, AbstractExpression> mainUnfoldedVariables;
	private Constants mainConstants;
	private PEPAComponentDefinitions mainDefinitions;
	private Map<String, PEPAComponent> components;
	private List<ITransition> excluded = new LinkedList<ITransition> ();
	Cloner deepCloner = new Cloner ();

	private void initExcluded ()
	{
		excluded.add (new SignalTransition ("start"));
		excluded.add (new SignalTransition ("stop"));
	}

	private Map<String, PEPAComponent>
		loadProbe (String probeComp, GPAParser parser) throws Exception
	{
		GPALexer lex = new GPALexer (new ANTLRStringStream (probeComp));
		CommonTokenStream tokens = new CommonTokenStream (lex);
		parser.setTokenStream (tokens);

		GPAParser.componentDefinitions_return probe
			= parser.componentDefinitions ();
		CommonTreeNodeStream nodes
			= new CommonTreeNodeStream ((Tree) probe.tree);
		TreeNodeStream current = this.getTreeNodeStream ();
		this.setTreeNodeStream (nodes);
		Map<String,PEPAComponent> newComponents = this.componentDefinitions ();
		this.setTreeNodeStream (current);
		return newComponents;
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
  Set<String> cooperationActions = new HashSet<String>();
  mainConstants = constants;
  mainUnfoldedVariables = unfoldedVariables;
}:
  cd = componentDefinitions
  {
    components = $cd.componentDefinitions;
    mainDefinitions = new PEPAComponentDefinitions (components);
  }
  m=model
  {
    mainModel = $m.model;
  }
  (ca=countActions {cooperationActions=$countActions.cooperationActions;})?
  {
    $pctmc  = GPEPAToPCTMC.getPCTMC(new PEPAComponentDefinitions($cd.componentDefinitions),$m.model,cooperationActions);
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
  ^(COOP l=model cooperationActions=coop r=model){
    model = new GroupCooperation($l.model,$r.model,$cooperationActions.cooperationActions);
  }
 | ^(LABELLEDGROUP label=UPPERCASENAME g=group){
    $model = new LabelledComponentGroup($label.text,$g.group);
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
  ^(COOPCOMP l=component  a=coop r=component{
        $c = new CooperationComponent($l.c,$r.c,$a.cooperationActions);
    
    }  )  ;
  
choice returns [PEPAComponent c]
@init{
  List<AbstractPrefix> choices = new LinkedList<AbstractPrefix>();
}
@after{
  $c=new Choice(choices); 
}:
  (p=prefix{
      choices.add($p.c); 
  })+
;

prefix returns [AbstractPrefix c]:
  ^(PREFIX r=expression a=LOWERCASENAME s=component){
      AbstractPrefix prefix = new Prefix($a.text,$r.e,$s.c,
      	new LinkedList<ImmediatePrefix>());
      $c=prefix; 
   }
   | ^(PREFIX PASSIVE a=LOWERCASENAME s=component){
      AbstractPrefix prefix = new PassivePrefix($a.text,$s.c,
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

extensions
@init
{
	initExcluded ();
}
	:	^(PROBES probe_def*) ;

// Local_And_Global_Probe

// Local

probel [String name]
	:	^(PROBEL proberl=rl_signal rp=REPETITION?)
			{
				NFAState starting_state = NFAtoDFA.convertToDFA
					($proberl.starting_state, t);
				NFAState accepting = NFADetectors.detectSingleAcceptingState
					(starting_state);
				if (rp == null)
				{
					for (ITransition transition : $probe_spec::allActions)
					{
						accepting.addTransitionIfNotExisting
							(transition.getSimpleTransition (), accepting);
					}
				}
				else
				{
                	accepting.addTransition
                		(new EmptyTransition (), starting_state);
				}
				starting_state = NFAtoDFA.convertToDFA (starting_state, t);
				NFAUtils.removeAnyTransitions
					($probe_spec::allActions, starting_state);
			    $probe_spec::alphabet.addAll
			    	(NFADetectors.detectAlphabet
			    		(starting_state, true, excluded));
				NFAUtils.extendStatesWithSelfLoops
					($probe_spec::allActions, starting_state);

				ByteArrayOutputStream stream = new ByteArrayOutputStream ();
				NFAStateToPEPA.HybridDFAtoPEPA
					(starting_state, $name, 0, new PrintStream (stream));
				$probe_spec::newComponents.putAll
					(loadProbe (stream.toString (), $probe_def::parser));
			} ;

rl_signal returns [NFAState starting_state]
@init
{
	$starting_state = new NFAState (t);
}
	:	^(RLS rl_single [$starting_state] sig=signal next_rl=rl_signal?)
			{
				ITransition signal = new SignalTransition ($sig.name);
				$starting_state = NFAtoDFA.convertToDFA ($starting_state, t);
				NFAState acc_state = new NFAState (t);
				NFAUtils.unifyAcceptingStates ($starting_state, acc_state);
				NFAState new_acc_state = new NFAState (t);
				new_acc_state.setAccepting (next_rl == null);
				acc_state.addTransition (signal, new_acc_state);
				acc_state.setAccepting (false);
				if (next_rl != null)
				{
					new_acc_state.addTransition
						(new EmptyTransition (), $next_rl.starting_state);
				}
			} ;

rl_single [NFAState current_state]
	returns [NFAState reached_state]
@init
{
	NFAState starting_state = new NFAState (t);
}
	:	^(RL_SINGLE (^(RL_SINGLE_BRACKETED rs=rl_bracketed [starting_state]
						(op=rl_un_operators
							[starting_state, $rs.reached_state])?)))
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
		| ^(RL_SINGLE (^(ACTION rs=immediateActions [starting_state]
						(op=rl_un_operators
							[starting_state, $rs.reached_state])?)))
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

rl [NFAState current_state]
	returns [NFAState reached_state]
@init
{
	NFAState new_starting_state1 = new NFAState (t);
	NFAState new_starting_state2 = new NFAState (t);
}
	:	^(RL rl_single [new_starting_state1]
		(rl2=rl [new_starting_state2] op=rl_bin_operators
				[new_starting_state1, $rl_single.reached_state,
				new_starting_state2, $rl2.reached_state])?)
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
	NFAState current_state2]
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
					(comb_starting_state, list, t, $probe_spec::allActions);
				$reached_state = new NFAState (t);
				NFAUtils.unifyAcceptingStates ($starting_state, $reached_state);
			} ;

rl_un_operators [NFAState sub_starting_state,
	NFAState sub_current_state]
	returns [NFAState starting_state, NFAState reached_state]
@init
{
	$starting_state = new NFAState (t);
	$reached_state = new NFAState (t);
	$reached_state.setAccepting (true);
}
	:	^(UNARY_OP e1=expression (COMMA e2=expression)?)
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
				Set<NFAState> accepting
					= NFADetectors.detectAllAcceptingStates ($starting_state);
				for (NFAState state : accepting)
				{
					for (ITransition transition : $probe_spec::allActions)
					{
						state.addTransitionIfNotExisting
							(transition.getSimpleTransition (), state);
					}
				}
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

rl_bracketed  [NFAState current_state]
	returns [NFAState reached_state]
	:	^(RL_BRACKETED rl [$current_state])
			{
				$reached_state = $rl.reached_state;
			} ;

signal returns [String name]
	:	^(SIGNAL signal_name=LOWERCASENAME)
			{
				$name = $signal_name.text;
			} ;

immediateActions [NFAState current_state]
	returns [NFAState reached_state]
	:	eventual_specific_action [$current_state]
			{
				$reached_state = $eventual_specific_action.reached_state;
			}
		| subsequent_specific_action  [$current_state]
			{
				$reached_state = $subsequent_specific_action.reached_state;
			}
		| any_action [$current_state]
			{
				$reached_state = $any_action.reached_state;
			}
		| empty_action [$current_state]
			{
				$reached_state = $empty_action.reached_state;
			} ;

eventual_specific_action [NFAState current_state]
	returns [NFAState reached_state]
@init
{
	NFAState new_starting_state1 = new NFAState (t);
	new_starting_state1.setAccepting (false);
	NFAState new_starting_state2 = new NFAState (t);
	new_starting_state2.setAccepting (false);
	NFAState new_starting_state3 = new NFAState (t);
	new_starting_state3.setAccepting (false);
}
	:	^(CCA dot1=any_action [new_starting_state1]
				t1=times [new_starting_state1, $dot1.reached_state]
			specific_action=subsequent_specific_action [new_starting_state2]
			dot2=any_action [new_starting_state3]
				t2=times [new_starting_state3, $dot2.reached_state])
			{
				$current_state.addTransition (new EmptyTransition (),
					$t1.starting_state);
				$t1.reached_state.addTransition (new EmptyTransition (),
					new_starting_state2);
				$t1.reached_state.setAccepting (false);
				$specific_action.reached_state.addTransition
					(new EmptyTransition (), $t2.starting_state);
				$specific_action.reached_state.setAccepting (false);
				$reached_state = $t2.reached_state;
				$reached_state.setAccepting (true);
			} ;

subsequent_specific_action [NFAState current_state]
	returns [NFAState reached_state]
	:	^(ACTION_NAME name=LOWERCASENAME)
			{
				$reached_state = new NFAState (t);
				$reached_state.setAccepting (true);
				$current_state.addTransition (new Transition ($name.text),
					$reached_state);
				$current_state.setAccepting (false);
			} ;

any_action [NFAState current_state]
	returns [NFAState reached_state]
	:	DOT
			{
				$reached_state = new NFAState (t);
				$reached_state.setAccepting (true);
				$current_state.addTransition (new AnyTransition (),
					$reached_state);
				$current_state.setAccepting (false);
			} ;

empty_action [NFAState current_state]
	returns [NFAState reached_state]
	:	EMPTY
			{
				$reached_state = new NFAState (t);
				$reached_state.setAccepting (true);
				$current_state.addTransition (new EmptyTransition (),
					$reached_state);
				$current_state.setAccepting (false);
			} ;

// Global

probeg
@init
{
	NFAState starting_state1 = new NFAState (t);
	NFAState starting_state2 = new NFAState (t);
}
	:	^(PROBEG start_actions=rg [starting_state1]
			stop_actions=rg [starting_state2] rp=REPETITION?)
			{
				NFAState acc_state = new NFAState (t);
				NFAUtils.unifyAcceptingStates (starting_state1, acc_state);
				acc_state.addTransition
					(new SignalTransition ("start"), starting_state2);
				acc_state.setAccepting (false);
				acc_state = new NFAState (t);
                NFAUtils.unifyAcceptingStates (starting_state2, acc_state);
                acc_state.setAccepting (false);
                NFAState final_acc_state = new NFAState (t);
                final_acc_state.setAccepting (true);
                acc_state.addTransition
                    (new SignalTransition ("stop"), final_acc_state);
                starting_state1 = NFAtoDFA.convertToDFA (starting_state1, t);
                acc_state = NFADetectors.detectSingleAcceptingState
                	(starting_state1);
                Set<ITransition> alphabet = NFADetectors.detectAlphabet
					(starting_state1, false, new LinkedList<ITransition> ());
				if (rp != null)
				{
					acc_state.addTransition
						(new EmptyTransition (), starting_state1);
					starting_state1 = NFAtoDFA.convertToDFA (starting_state1,
						$probe_spec::probe.getName ());
				}
				NFAUtils.removeAnyTransitions
					($probe_spec::allActions, starting_state1);
				$probe_spec::probe.setStartingState (starting_state1);
			} ;

rg [NFAState current_state] returns [NFAState reached_state]
@init
{
	NFAState new_starting_state1 = new NFAState (t);
	NFAState new_starting_state2 = new NFAState (t);
}
	:	^(RG rl_single [new_starting_state1] pred1=main_pred?
	        {
	            if (pred1 != null)
	            {
	                new_starting_state1.setPredicate ($pred1.predicate);
	            }
	        }
		(rg2=rg [new_starting_state2] op=rl_bin_operators
				[new_starting_state1, $rl_single.reached_state,
				new_starting_state2, $rg2.reached_state])?)
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
			} ;

// Predicates for global
main_pred returns [NFAPredicate predicate]
    :   p=pred
        {
            String predicateString = "(" + $p.predicate + ")";
            $predicate = NFAPredicate.create (predicateString);
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
            }
        | op=LOGICAL_AND
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
	            $probe_def::stateObservers.add (gps);
                $predicate = "data [statesCountExpressions.indexOf"
                    + "(mapping.get (\"" + gps.toString () + "\"))]";
	        }
	    | expr=expression
	        {
	            $predicate = "(" + $expr.e.toString () + ")";
	        } ;

// Probe_spec

probe_def
scope
{
	GPAParser parser;
	GroupedModel model;
	List<GPEPAState> stateObservers;
	AbstractExpression stop_time;
	AbstractExpression step_size;
	int density;
}
@init
{
	$probe_def::parser = new GPAParser (null);
	$probe_def::parser.setErrorReporter (new ErrorReporter ());
	$probe_def::model = deepCloner.deepClone (mainModel);
	$probe_def::stateObservers = new LinkedList<GPEPAState> ();
}
	:	^(PROBE_DEF
			settings=odeSettings
				{
					$probe_def::stop_time = $settings.stopTime;
					$probe_def::step_size = $settings.stepSize;
					$probe_def::density = $settings.density;
				}
			probe_spec) ;

probe_spec
scope
{
    Set<ITransition> alphabet;
    Set<ITransition> allActions;
    IProbe probe;
    Map<String, PEPAComponent> newComponents;
}
@init
{
	GlobalProbe gprobe = new GlobalProbe ();
	$probe_spec::probe = gprobe;
	$probe_spec::alphabet = new HashSet<ITransition> ();
	$probe_spec::allActions = new HashSet<ITransition> ();
	Set<String> actions = mainDefinitions.getActions ();
	for (String action : actions)
	{
		$probe_spec::allActions.add (new Transition (action));
	}
	$probe_spec::newComponents = deepCloner.deepClone (components);
}
	:	^(DEF signalNames=SIGNALS
						{
							String signalsString = $signalNames.text;
							String[] signals = signalsString.split (";");
							for (String signal : signals)
							{
								$probe_spec::allActions.add
									(new SignalTransition (signal));
							}
						}
			globalProbeName=UPPERCASENAME (local_probes locations)? probeg)
			{
				gprobe.setName ($globalProbeName.text);
				ProbeRunner.executeProbedModel (gprobe, $probe_def::model,
					$probe_def::stateObservers, $probe_spec::newComponents,
					mainConstants, $probe_def::stop_time,
					$probe_def::step_size, $probe_def::density,
					$probe_spec::alphabet, excluded);
            } ;

local_probes
	:	local_probe_ass (COMMA local_probe_ass)* ;

local_probe_ass
	:	^(DEF name=UPPERCASENAME probel [$name.text]) ;

locations
	:	location (COMMA location)* ;

location
	:	^(SUBSTITUTE m1=model m2=model)
			{
				$probe_def::model = deepCloner.deepClone ($probe_def::model);
				Map<GroupedModel, GroupedModel> gmodels
					= new HashMap<GroupedModel, GroupedModel> ();
				$probe_def::model.enumerateGroupedModelParents (gmodels, null);
				GroupedModel ownerOfTheSought = gmodels.get ($m1.model);

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
					if (gc.getLeft ().equals ($m1.model))
					{
						gc.setLeft ($m2.model);
					}
					else
					{
						gc.setRight ($m2.model);
					}
				}
				else
				{
					throw new Error ("substitution unsuccessful: unsupported"
					    + " kind of model to substitute");
				}
			} ;
