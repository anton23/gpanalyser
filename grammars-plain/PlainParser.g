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
   
   import uk.ac.imperial.doc.pctmc.syntax.CustomRecognitionException;
   import uk.ac.imperial.doc.pctmc.syntax.ErrorReporter;  
   import java.util.LinkedList;

}

//This is a hack until the composite grammars are implemented in a better way
@members{
  protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException{   
    throw new MissingTokenException(ttype, input, null);
}   

public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException {
    throw e;
}

protected void mismatch(IntStream input, int ttype, BitSet follow) throws RecognitionException {
    throw new MismatchedTokenException(ttype, input);
} 

protected List<String> errors = new LinkedList<String>();

public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        errors.add(hdr + " " + msg);
    }
    public List<String> getErrors() {
        return errors;
    }
}

@rulecatch {
  catch (RecognitionException re) {
    reportError(re);
    //throw re;
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
