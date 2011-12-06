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
  
import uk.ac.imperial.doc.jexpressions.expressions.*;
import uk.ac.imperial.doc.jexpressions.constants.*;
import uk.ac.imperial.doc.jexpressions.variables.*;
  
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

import com.google.common.collect.Multimap;
import com.google.common.collect.LinkedHashMultimap;	  
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import uk.ac.imperial.doc.masspa.representation.components.*;
import uk.ac.imperial.doc.masspa.representation.model.*; 
import uk.ac.imperial.doc.masspa.representation.*;
import uk.ac.imperial.doc.masspa.pctmc.*;
import uk.ac.imperial.doc.masspa.util.*;
}

@members
{
  MASSPAModel m_model=null;
}

// ***********************************
// *    Compiler for entire PCTMC    *
// *        MASSPA  OVERRIDES        *
// ***********************************
start:;

modelDefinition[Map<ExpressionVariable,AbstractExpression> variables, Constants constants] returns [PCTMC pctmc]
@init{Set<ActionCountState> actions = new HashSet<ActionCountState>();}:
  agents=agentDefinitions
  model[$agents.agents]
  {$pctmc = MASSPAToPCTMC.getPCTMC(m_model,variables,constants);}
;

state returns [State t]:
  ^(STATE agentPopulation) {$t = agentPopulation.agentPop;}
| ^(STATE actionCount) {$t = actionCount.aCount;}
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
  msg=UPPERCASENAME {m = agents.getMessage($msg.text);}
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
| ^(PREFIX a=LOWERCASENAME r=expression s=primaryComponent[agents, scopeName]){$c = new Prefix($a.text,$r.e,$s.c);}
| ^(SEND TAU r=expression msg=message[agents] nofmsg=expression s=primaryComponent[agents, scopeName]){$c = new SendPrefix("",$r.e,$msg.m,$nofmsg.e,$s.c);}
| ^(SEND a=LOWERCASENAME r=expression msg=message[agents] nofmsg=expression s=primaryComponent[agents, scopeName]){$c = new SendPrefix($a.text,$r.e,$msg.m,$nofmsg.e,$s.c);}
| ^(RECV TAU msg=message[agents] msgaccprob=expression s=primaryComponent[agents, scopeName]){$c = new ReceivePrefix("",$msg.m,$msgaccprob.e,$s.c);}
| ^(RECV a=LOWERCASENAME msg=message[agents] msgaccprob=expression s=primaryComponent[agents, scopeName]){$c = new ReceivePrefix($a.text,$msg.m,$msgaccprob.e,$s.c);}
; 

primaryComponent[MASSPAAgents agents, String scopeName] returns [MASSPAComponent c]:
  id=UPPERCASENAME {$c = agents.getConstComponent(scopeName, $id.text, $UPPERCASENAME.getLine());}
| ANY  {$c = agents.getAnyComponent();}
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
@after{l = (coords.size() > 0) ? new Location(coords) : new AnyLocation();}:
  ^(LOCATION (i=INTEGER {coords.add(Integer.parseInt($i.text));})+)
| ^(LOCATION ANY)
;

locationDef:
  ^(LOCATIONS (l=location {m_model.addLocation($l.l,$LOCATIONS.getLine());})+)
;

agentPopulation returns [MASSPAAgentPop agentPop]:
  ^(AGENTPOP type=UPPERCASENAME l=location) {$agentPop=m_model.getAgentPop($type.text,$l.l,$AGENTPOP.getLine());}
;

actionCount returns [MASSPAActionCount aCount]:
  ^(ACOUNT LOWERCASENAME) {$aCount = new MASSPAActionCount(LOWERCASENAME.text);}
| ^(ACOUNT LOWERCASENAME location) {$aCount = new MASSPAActionCount(LOWERCASENAME.text+location.l.toString());}
;

initVal:
  ^(INITVAL ap=agentPopulation nofAgents=expression) {m_model.addInitialAgentPopulation($ap.agentPop,$nofAgents.e,$INITVAL.getLine());}
| ^(INITVAL ac=actionCount count=expression) {m_model.addInitialActionCount($ac.aCount,$count.e,$INITVAL.getLine());}
;

channel:
  ^(CHANNEL sender=agentPopulation receiver=agentPopulation msg=message[m_model.getMASSPAAgents()] intensity=expression) {m_model.addChannel($sender.agentPop,$receiver.agentPop,$msg.m,$intensity.e,$CHANNEL.getLine());}
;