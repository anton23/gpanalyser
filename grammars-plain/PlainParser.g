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
   
   import java.util.LinkedList;

}

//This is a hack until the composite grammars are implemented in a better way
@members{
  protected List<String> errors = new LinkedList<String>();

public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        errors.add(hdr + " " + msg);
    }
    public List<String> getErrors() {
    	LinkedList<String> allErrors = new LinkedList<String>(errors); 
    	allErrors.addAll(gPCTMCParserPrototype.getErrors());
        return errors;
    }
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
