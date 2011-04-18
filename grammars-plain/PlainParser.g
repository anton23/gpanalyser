parser grammar PlainParser;

options {
  language = Java;
  output = AST; 
  ASTLabelType = CommonTree; 
  tokenVocab = PlainLexer; 
  backtrack = true;
}

import PCTMCParserPrototype;

tokens{
  COMPONENT; 
  CONSTANT;
  VARIABLE; 
  TRANSACTION; 
  TLIST; 
  FUN;
  EVENT; 
  INIT;
  PRODUCT; 
  COMBINEDPRODUCT; 
  RANGE;
}

@header{
   package uk.ac.imperial.doc.gpa.plain.syntax;

}




state: 
  transaction
 |countPattern
 ;
 
countPattern:
 COUNT UPPERCASENAME -> ^(COUNT UPPERCASENAME)
;

transaction: LBRACE componentList RBRACE -> ^(TRANSACTION componentList);

componentList:
UPPERCASENAME (COMMA UPPERCASENAME)* -> UPPERCASENAME+ 
;
