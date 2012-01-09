lexer grammar GPALexer;

options
{
	backtrack = true;
}

import PCTMCLexerPrototype;

@header
{
	package uk.ac.imperial.doc.gpa.syntax; 
}

LANGLE: '<';
RANGLE: '>';
PAR: '|';
DOT: '.';
MIN: 'min';
INGROUP: ':';
STOP: 'stop';
ACOUNT:'#';
COUNTACTIONS:'Count';
ANY:'_';
TIME:'t';

//Local_and_Global_Probe_lexer

//Local

PASSIVE
	:	'T' ;

REPETITION
	:	'<-' ;

ZERO_ONE
	:	'?' ;

NEGATION_OP
	:	'!' ;

EMPTY
	:	'eE' ;

CCA
	:	'~' ;

//Global

START
	:	'start' ;

LOGICAL_NEGATION
	:	'nN' ;

LOGICAL_OR
	:	'||' ;

LOGICAL_AND
	:	'&&' ;

TRUE
	:	'tT' ;

FALSE
	:	'fF' ;

COMPARISON
	:	'==' | '>=' | '<=' ;

//Local - low priority

//Probe_spec

fragment WS
	:	( '\t' | ' ' | '\r' | '\n'| '\u000C' ) ;

WHERE
	:	WS 'where' WS;

OBSERVES
	:	WS 'observes' WS ;

PROBE_DEF
	:	'Probe';

SUBSTITUTE
	:	'=>' ;

WHITESPACE
	:	( '\t' | ' ' | '\r' | '\n'| '\u000C' )	{ $channel = HIDDEN; } ;
