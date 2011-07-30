parser grammar GPAParser;
options {
  language = Java;
  output = AST; 
  ASTLabelType = CommonTree; 
  tokenVocab = GPALexer; 
  backtrack = true;
}

import PCTMCParserPrototype;


tokens{
  COMPONENT;
  LABELLEDGROUP;
  COOP;
  PREFIX;
  MULT;
  COOPCOMP;
  PAIR;
}

@header{
  package uk.ac.imperial.doc.gpa.syntax; 
  
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

start:;

modelDefinition: componentDefinition+ model countActions? ;


countActions:
  COUNTACTIONS (LOWERCASENAME)+ SEMI -> ^(COUNTACTIONS LOWERCASENAME+)
;

componentDefinition:
  id=UPPERCASENAME DEF s=component SEMI-> ^(COMPONENT $id $s);

component:   
    l=choice a=cooperationSet r=choice -> ^(COOPCOMP $l $a $r)
    |choice;


//allows only choice between prefixes (not constants)
choice:
  prefix (PLUS prefix)* -> prefix+
   ;
      
prefix: 
  LPAR a=LOWERCASENAME COMMA r=expression RPAR DOT s=prefix -> ^(PREFIX $a $r $s)
  | primaryComponent;
  
primaryComponent:
   UPPERCASENAME
 | STOP
 | ANY
 | LPAR c=component RPAR -> $c;

model: 
   l=labelledGroup actions=cooperationSet r=labelledGroup -> ^(COOP $l $actions $r)
  |  labelledGroup 
;
   
labelledGroup:
   label=UPPERCASENAME LBRACE g=group RBRACE -> ^(LABELLEDGROUP $label $g)
  | LPAR model RPAR -> model;
 
cooperationSet:
   LANGLE LOWERCASENAME (COMMA LOWERCASENAME)* RANGLE -> LOWERCASENAME+
  |LANGLE RANGLE -> LOWERCASENAME[""]; 
   
group:
  groupComponent (PAR groupComponent)* -> groupComponent+;
 
groupComponent:
  s=component (LBRACK n=expression RBRACK) -> ^(MULT $s $n)
 |s=component -> ^(MULT $s REALNUMBER["1.0"]);
 

//states:

state: 
  groupComponentPair
 |actionCount; 

actionCount:
ACOUNT LOWERCASENAME -> ^(ACOUNT LOWERCASENAME)
;

groupComponentPair:
     UPPERCASENAME INGROUP component   -> ^(PAIR UPPERCASENAME component)
;
 
 

