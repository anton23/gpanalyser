tree grammar MASSPACompiler;
 
options
{   
  language = Java; 
  tokenVocab = MASSPAParser; 
  ASTLabelType = CommonTree;  
}

import PCTMCCompilerPrototype;

@header
{
package uk.ac.imperial.doc.masspa.syntax;

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

import uk.ac.imperial.doc.pctmc.interpreter.IExtension;  
  
  
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
import uk.ac.imperial.doc.pctmc.representation.accumulations.*; 
import uk.ac.imperial.doc.pctmc.experiments.iterate.*; 
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.*; 
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.*;
import uk.ac.imperial.doc.pctmc.syntax.CustomRecognitionException;
import uk.ac.imperial.doc.pctmc.syntax.ErrorReporter;

import uk.ac.imperial.doc.masspa.representation.components.*;
import uk.ac.imperial.doc.masspa.representation.model.*; 
import uk.ac.imperial.doc.masspa.representation.*;
 

import uk.ac.imperial.doc.masspa.pctmc.*;
import uk.ac.imperial.doc.masspa.util.*;
import uk.ac.imperial.doc.pctmc.experiments.distribution.DistributionSimulation;
import uk.ac.imperial.doc.pctmc.experiments.distribution.GroupOfDistributions;
import uk.ac.imperial.doc.pctmc.experiments.distribution.DistributionsAtAllTimes;
  import uk.ac.imperial.doc.pctmc.experiments.distribution.DistributionsAtTimes;
}

@members
{
     MASSPAModel m_model=null;

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
}

// ***********************************
// *    Compiler for entire PCTMC    *
// *        MASSPA  OVERRIDES        *
// ***********************************
start:;

modelDefinition[Map<ExpressionVariable,AbstractExpression> variables, Constants constants] returns [PCTMC pctmc]
@init{Set<MASSPAActionCount> actions = new HashSet<MASSPAActionCount>();}:
  agents=agentDefinitions
  model[$agents.agents]
  {$pctmc = MASSPAToPCTMC.getPCTMC(m_model,variables,constants);}
;

state returns [State t]:
  ^(STATE ^(AGENTPOP id=UPPERCASENAME l=location)) {$t = (State)new MASSPAAgentPop(new ConstComponent($id.text),$l.l);}
| ^(STATE ^(ACOUNT id=LOWERCASENAME l=location)) {$t = (State)new MASSPAActionCount($id.text,$l.l);}
;

constant returns [String text]:
  ^(CONSTANT id=LOWERCASENAME l=location) {$text = $id.text+$l.l.toString();}
;

variable returns [String text]:
  ^(VARIABLE id=LOWERCASENAME l=location) {$text = $id.text+$l.l.toString();}
;

// ****************************************************
// *    Compiler for sequential agents definitions    *
// ****************************************************
agentDefinitions returns [MASSPAAgents agents]
@init{agents = new MASSPAAgents();}
@after{agents.validateComponentDefinitions();}:
(
	^(AGENT scopeName=UPPERCASENAME componentDefinitions[agents,$scopeName.text])
)+
;

componentDefinitions[MASSPAAgents agents, String scopeName]:
(
  ^(COMPONENT id=UPPERCASENAME {ConstComponent cnst = agents.getConstComponent(scopeName, $id.text, $COMPONENT.getLine());}
  s=component[agents, scopeName] {cnst.define($s.c, $COMPONENT.getLine());})
)+
;

message[MASSPAAgents agents] returns [MASSPAMessage m]:
  msg=UPPERCASENAME {$m = agents.getMessage($msg.text); $m = ($m == null) ? new MASSPAMessage($msg.text) : $m;}
;

component[MASSPAAgents agents, String scopeName] returns [MASSPAComponent c]:
  ch=choice[agents, scopeName] {$c=$ch.c;}
| p=primaryComponent[agents, scopeName] {$c=$p.c;}  
;
  
choice[MASSPAAgents agents, String scopeName] returns [MASSPAComponent c]
@init{List<Prefix> choices = new LinkedList<Prefix>();}
@after{$c=agents.getChoiceComponent(choices);}:
  (p=prefix[agents, scopeName] {choices.add($p.c);})+
;

prefix[MASSPAAgents agents, String scopeName] returns [Prefix c]:
  ^(PREFIX TAU r=expression s=primaryComponent[agents, scopeName]){$c = new Prefix("",$r.e,$s.c);}
| ^(PREFIX a=LOWERCASENAME r=expression s=primaryComponent[agents, scopeName]){agents.addAction($a.text); $c = new Prefix($a.text,$r.e,$s.c);}
| ^(SEND TAU r=expression msg=message[agents] nofmsg=expression s=primaryComponent[agents, scopeName]){agents.addMessage($msg.m); $c = new SendPrefix("",$r.e,$msg.m,$nofmsg.e,$s.c);}
| ^(SEND a=LOWERCASENAME r=expression msg=message[agents] nofmsg=expression s=primaryComponent[agents, scopeName]){agents.addAction($a.text); agents.addMessage($msg.m); $c = new SendPrefix($a.text,$r.e,$msg.m,$nofmsg.e,$s.c);}
| ^(RECV TAU msg=message[agents] msgaccprob=expression s=primaryComponent[agents, scopeName]){agents.addMessage($msg.m); $c = new ReceivePrefix("",$msg.m,$msgaccprob.e,$s.c);}
| ^(RECV a=LOWERCASENAME msg=message[agents] msgaccprob=expression s=primaryComponent[agents, scopeName]){agents.addAction($a.text); agents.addMessage($msg.m); $c = new ReceivePrefix($a.text,$msg.m,$msgaccprob.e,$s.c);}
; 

primaryComponent[MASSPAAgents agents, String scopeName] returns [MASSPAComponent c]:
  id=UPPERCASENAME {$c = agents.getConstComponent(scopeName, $id.text, $UPPERCASENAME.getLine());}
| STOP {$c = agents.getStopComponent();}
;

// ****************************************
// *    Compiler for model definitions    *
// ****************************************
model[MASSPAAgents componentDefinitions] returns [MASSPAModel model]:
  {m_model = new MASSPAModel(componentDefinitions);}
  locationDef initVal* channel*
;

location returns [Location l]
@init{List<Integer> coords = new LinkedList<Integer>();}
@after{$l = (coords.size() > 0) ? new Location(coords) : $l;}:
  ^(LOCATION (i=INTEGER {coords.add(Integer.parseInt($i.text));})+)
| ^(LOCATION LOC_ALL) {$l = AllLocation.getInstance();}
| ^(LOCATION LOC_VAR) {$l = VarLocation.getInstance();}
;

locationDef:
  ^(LOCATIONS (l=location {m_model.addLocation($l.l,$LOCATIONS.getLine());})+)
;

agentPopulation returns [MASSPAAgentPop agentPop]:
  ^(AGENTPOP id=UPPERCASENAME l=location) {$agentPop=m_model.getAgentPop($id.text,$l.l,$AGENTPOP.getLine());}
;

actionCount returns [MASSPAActionCount aCount]:
  ^(ACOUNT id=LOWERCASENAME l=location) {$aCount = m_model.getActionCount($id.text, $l.l, $ACOUNT.getLine());}
;

initVal:
  ^(INITVAL ap=agentPopulation nofAgents=expression) {m_model.addInitialAgentPopulation($ap.agentPop,$nofAgents.e,$INITVAL.getLine());}
| ^(INITVAL ac=actionCount count=expression) {m_model.addInitialActionCount($ac.aCount,$count.e,$INITVAL.getLine());}
;

channel:
  ^(CHANNELTYPE type=LOWERCASENAME) {m_model.setChannelType($type.text,$CHANNELTYPE.getLine());}
| ^(CHANNEL sender=agentPopulation receiver=agentPopulation msg=message[m_model.getMASSPAAgents()] intensity=expression) {m_model.addChannel($sender.agentPop,$receiver.agentPop,$msg.m,$intensity.e,$CHANNEL.getLine());}
;