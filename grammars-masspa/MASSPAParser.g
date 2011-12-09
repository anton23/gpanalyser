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

@members{

    protected Stack<String> hint = new Stack<String>();

    public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        if (errorReporter != null) {
          errorReporter.addError("["+hdr + "] " + msg);
        }
    }
    
    protected ErrorReporter errorReporter;
    
    public boolean requireDefinitions = false;
    
    public void setErrorReporter(ErrorReporter errorReporter) {
       this.errorReporter = errorReporter;
    }
    
  public boolean define = false;
  
  public ParsingData getParsingData() { 
    return null;  
  } 
  
  public void setParsingData(ParsingData parsingData) {
      
  }
    
     
  public String getErrorHeader(RecognitionException e) {
    return "line "+e.line+":"+e.charPositionInLine;
  }
    
	   public String getErrorMessage(RecognitionException e,
                              String[] tokenNames) {
        String ret = "";
        
          ret += getModifiedErrorMessage(e, tokenNames);
                
        if (!hint.isEmpty()) {
          ret += " (" + hint.peek() + ")";
        } 
        
        return ret;
      }
	  
	   public String getTokenErrorDisplay(Token t) {
    String s = t.getText();
    if (s.equals("<EOF>")) {
       return "the end of file";
    }
    if ( s==null ) {
      if ( t.getType()==Token.EOF ) {
        return "the end of file";
      }
      else {
        s = "<"+t.getType()+">";
      }
    }
    s = s.replaceAll("\n","\\\\n");
    s = s.replaceAll("\r","\\\\r");
    s = s.replaceAll("\t","\\\\t");
    return "'"+s+"'";
  }
	  
	  
	  private String[] customTokenNames(String[] tokenNames) {
	     String[] ret = new String[tokenNames.length];
	     Map<String, String> map = new HashMap<String, String>();
	     map.put("SEMI", "';'");
	     map.put("LBRACE", "'{'");
	     map.put("EOF", "the end of file");
	     map.put("STOPTIME","'stopTime'");
	     map.put("STEPSIZE","'stepSize'");
	     map.put("DENSITY","'density'");
	     map.put("REPLICATIONS","'replications'");
	     map.put("INTEGER","an integer");
	     map.put("LPAR","(");
	     map.put("RPAR",")");
	     map.put("FILENAME","filename of the form \"filename\"");
	     for (int i = 0; i<tokenNames.length; i++) {
	         ret[i] = tokenNames[i]; 
	         if (map.containsKey(ret[i])) {
	           ret[i] = map.get(ret[i]);
	         }
	     }
	     return ret;
	  }
	  
	  public String getModifiedErrorMessage(RecognitionException e, String[] tokenNames) {
	  tokenNames = customTokenNames(tokenNames);
    String msg = e.getMessage();
    if ( e instanceof UnwantedTokenException ) {
      UnwantedTokenException ute = (UnwantedTokenException)e;
      String tokenName="<unknown>";
      if ( ute.expecting== Token.EOF ) {
        tokenName = "EOF";
      }
      else {
        tokenName = tokenNames[ute.expecting];
      }
      msg = "extraneous input "+getTokenErrorDisplay(ute.getUnexpectedToken())+
        " expecting "+tokenName;
    }
    else if ( e instanceof MissingTokenException ) {
      MissingTokenException mte = (MissingTokenException)e;
      String tokenName="<unknown>";
      if ( mte.expecting== Token.EOF ) {
        tokenName = "EOF";
        msg = "unknown command " + getTokenErrorDisplay(e.token);
      }
      else {
        tokenName = tokenNames[mte.expecting];
        msg = "missing "+tokenName+" at "+getTokenErrorDisplay(e.token);
      }
    }
    else if ( e instanceof MismatchedTokenException ) {
      MismatchedTokenException mte = (MismatchedTokenException)e;
      String tokenName="<unknown>";
      if ( mte.expecting== Token.EOF ) {
        tokenName = "EOF";
      }
      else {
        tokenName = tokenNames[mte.expecting];
      }  
      
      msg = "mismatched input "+getTokenErrorDisplay(e.token)+
        " expecting "+tokenName;
    }
    else if ( e instanceof MismatchedTreeNodeException ) {
      MismatchedTreeNodeException mtne = (MismatchedTreeNodeException)e;
      String tokenName="<unknown>";
      if ( mtne.expecting==Token.EOF ) {
        tokenName = "EOF";
      }
      else {
        tokenName = tokenNames[mtne.expecting];
      } 

      msg = "mismatched tree node: "+mtne.node+
        " expecting "+tokenName;
    }
    else if ( e instanceof NoViableAltException ) {
      //NoViableAltException nvae = (NoViableAltException)e;
      // for development, can add "decision=<<"+nvae.grammarDecisionDescription+">>"
      // and "(decision="+nvae.decisionNumber+") and
      // "state "+nvae.stateNumber
      msg = "no viable alternative at "+getTokenErrorDisplay(e.token);
    }
    else if ( e instanceof EarlyExitException ) {
      //EarlyExitException eee = (EarlyExitException)e;
      // for development, can add "(decision="+eee.decisionNumber+")"
      msg = "required list did not match anything at input "+
        getTokenErrorDisplay(e.token);
    }
    else if ( e instanceof MismatchedSetException ) {
      MismatchedSetException mse = (MismatchedSetException)e;
      msg = "mismatched input "+getTokenErrorDisplay(e.token)+
        " expecting set "+mse.expecting;
    }
    else if ( e instanceof MismatchedNotSetException ) {
      MismatchedNotSetException mse = (MismatchedNotSetException)e;
      msg = "mismatched input "+getTokenErrorDisplay(e.token)+
        " expecting set "+mse.expecting;
    }
    else if ( e instanceof FailedPredicateException ) {
      FailedPredicateException fpe = (FailedPredicateException)e;
      msg = "rule "+fpe.ruleName+" failed predicate: {"+
        fpe.predicateText+"}?";
    } else if ( e instanceof CustomRecognitionException ) {
      CustomRecognitionException mse = (CustomRecognitionException)e;
      msg = mse.getMessage();
    }
    return msg;
  }
  
  boolean requiresExpectation = false;
  boolean insideExpectation = false;
  
  Set<String> constants = new HashSet<String>();
}

@header
{
package uk.ac.imperial.doc.masspa.syntax;

import uk.ac.imperial.doc.masspa.util.MASSPALogging;
import uk.ac.imperial.doc.pctmc.syntax.ErrorReporter;  
import uk.ac.imperial.doc.pctmc.syntax.CustomRecognitionException;
import uk.ac.imperial.doc.pctmc.syntax.ParsingData;
  
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
}

// ****************************************************
// *    			PCTMC Overwrites			      *
// ****************************************************
constant:
  id=LOWERCASENAME AT location -> ^(CONSTANT LOWERCASENAME location)
;

variable:
  VAR id=LOWERCASENAME AT location -> ^(VARIABLE LOWERCASENAME location)
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
  LPAR r=expression RPAR DOT s=primaryComponent -> ^(PREFIX TAU $r $s)
| LPAR action=LOWERCASENAME COMMA r=expression RPAR DOT s=primaryComponent -> ^(PREFIX $action $r $s)
| SENDMSG LPAR r=expression COMMA msg=UPPERCASENAME COMMA nofmsg=expression RPAR DOT s=primaryComponent -> ^(SEND TAU $r $msg $nofmsg $s)
| SENDMSG LPAR action=LOWERCASENAME COMMA r=expression COMMA msg=UPPERCASENAME COMMA nofmsg=expression RPAR DOT s=primaryComponent -> ^(SEND $action $r $msg $nofmsg $s)
| RECVMSG LPAR msg=UPPERCASENAME COMMA msgaccprob=expression RPAR DOT s=primaryComponent -> ^(RECV TAU $msg $msgaccprob $s) 
| RECVMSG LPAR action=LOWERCASENAME COMMA msg=UPPERCASENAME COMMA msgaccprob=expression RPAR DOT s=primaryComponent -> ^(RECV $action $msg $msgaccprob $s) 
| primaryComponent
;

primaryComponent:
  UPPERCASENAME
| STOP
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
| LPAR LOC_VAR RPAR -> ^(LOCATION LOC_VAR)
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
