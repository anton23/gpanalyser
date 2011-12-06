parser grammar MASSPAParser;

options {
  language = Java;
  output = AST; 
  ASTLabelType = CommonTree; 
  tokenVocab = MASSPALexer; 
  backtrack = true;
}

import PCTMCParserPrototype;

tokens
{
	// Tokens for sequential operators
	COMPONENT;
	PREFIX;
	SEND;
	RECV;
	TAU;

	// Tokens for parallel composition
	LOCATION;
	LOCATIONS;
	AGENTPOP;
	INITVAL;
	COUNTACTIONS;
}

@header
{
package uk.ac.imperial.doc.masspa.syntax;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;
}

@members
{
}

// ****************************************************
// *    			PCTMC Overwrites			      *
// ****************************************************
constant:
  id=LOWERCASENAME (AT location)? -> ^(CONSTANT LOWERCASENAME location?)
;

variable:
  VAR id=LOWERCASENAME (AT location)? -> ^(VARIABLE LOWERCASENAME location?)
; 

state: 
  agentPopulation -> ^(STATE agentPopulation)
| actionCount -> ^(STATE actionCount)
;

// ****************************************************
// *    Syntax for definition of sequential agents    *
// ****************************************************
start:;

modelDefinition:
  agentDefinition+
  model
;

agentDefinition:
  AGENT scopeName=UPPERCASENAME LBRACE componentDefinition+ RBRACE SEMI -> ^(AGENT $scopeName componentDefinition+)
;

componentDefinition:
  UPPERCASENAME DEF component SEMI -> ^(COMPONENT UPPERCASENAME component)
;

component:
  choice
;

choice:
  prefix (PLUS prefix)* -> prefix+
;

prefix:
  LPAR r=expression RPAR DOT s=prefix -> ^(PREFIX TAU $r $s)
| LPAR action=LOWERCASENAME COMMA r=expression RPAR DOT s=prefix -> ^(PREFIX $action $r $s)
| SENDMSG LPAR r=expression COMMA msg=UPPERCASENAME COMMA nofmsg=expression RPAR DOT s=prefix -> ^(SEND TAU $r $msg $nofmsg $s)
| SENDMSG LPAR action=LOWERCASENAME COMMA r=expression COMMA msg=UPPERCASENAME COMMA nofmsg=expression RPAR DOT s=prefix -> ^(SEND $action $r $msg $nofmsg $s)
| RECVMSG LPAR msg=UPPERCASENAME COMMA msgaccprob=expression RPAR DOT s=prefix -> ^(RECV TAU $msg $msgaccprob $s) 
| RECVMSG LPAR action=LOWERCASENAME COMMA msg=UPPERCASENAME COMMA msgaccprob=expression RPAR DOT s=prefix -> ^(RECV $action $msg $msgaccprob $s) 
| primaryComponent
;

primaryComponent:
  UPPERCASENAME
| STOP
| ANY
| LPAR component RPAR -> component
;

// ****************************************************
// *      Syntax for definition of spatial model      *
// ****************************************************
model:
  locationDef
  initVal*
  channel*
;

location: 
  LPAR INTEGER (COMMA INTEGER)* RPAR -> ^(LOCATION INTEGER+)
| LPAR LOC_ANY RPAR -> ^(LOCATION LOC_ANY)  
;

locationDef:
  LOCS DEF LBRACE location (COMMA location)* RBRACE SEMI -> ^(LOCATIONS location+)
;

agentPopulation:
  UPPERCASENAME AT location -> ^(AGENTPOP UPPERCASENAME location)
;

actionCount:
  ACOUNT LOWERCASENAME (AT location)? -> ^(ACOUNT LOWERCASENAME location?)
;

initVal:
  agentPopulation DEF expression SEMI -> ^(INITVAL agentPopulation expression)
| actionCount DEF expression SEMI -> ^(INITVAL actionCount expression)
;

channel:
  CHANNEL LPAR sender=agentPopulation COMMA receiver=agentPopulation COMMA msg=UPPERCASENAME RPAR DEF intensity=expression SEMI -> ^(CHANNEL $sender $receiver $msg $intensity) 
;
