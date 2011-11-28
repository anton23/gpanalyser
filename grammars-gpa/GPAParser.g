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
  
  import uk.ac.imperial.doc.pctmc.syntax.ErrorReporter;  
  import uk.ac.imperial.doc.pctmc.syntax.CustomRecognitionException;
  import uk.ac.imperial.doc.pctmc.syntax.ParsingData;
  import uk.ac.imperial.doc.pctmc.syntax.GPAParsingData;
  import uk.ac.imperial.doc.pctmc.syntax.PlainParsingData;
  
  import java.util.LinkedList;
  import java.util.Set;
  import java.util.HashSet;

}

//This is a hack until the composite grammars are implemented in a better way
@members{

  protected Set<String> tmpGroupNames = new HashSet<String>();
  protected Set<String> groupNames = new HashSet<String>();
  protected Set<String> tmpComponentNames = new HashSet<String>();
  protected Set<String> componentNames = new HashSet<String>();
    
  protected Stack<String> hint;
  
  protected ErrorReporter errorReporter;
  
  public void setErrorReporter(ErrorReporter errorReporter) {    
    this.errorReporter = errorReporter;
    gPCTMCParserPrototype.setErrorReporter(errorReporter);
    hint = gPCTMCParserPrototype.hint;
  }
  
  public ParsingData getParsingData() {
       return new GPAParsingData(tmpComponentNames, tmpGroupNames);
  } 
  
  public void setParsingData(ParsingData parsingData) {
       if (parsingData instanceof GPAParsingData) {
            componentNames = ((GPAParsingData)parsingData).getComponentNames();
            groupNames = ((GPAParsingData)parsingData).getGroupNames();
       }
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
        String ret = "";
        
          ret += gPCTMCParserPrototype.getModifiedErrorMessage(e, tokenNames);
                
        if (!hint.isEmpty()) {
          ret += " (" + hint.peek() + ")";
        } else 
        if (!gPCTMCParserPrototype.hint.isEmpty()) { 
          ret += " (" + gPCTMCParserPrototype.hint.peek() + ")";
        } 
        
        return ret;
      }
}



start:;

modelDefinition: 
  {hint.push("at least one PEPA component definition required");} componentDefinition+ {hint.pop();}
  {hint.push("missing system equation");} model {hint.pop();}
  countActions? ;


countActions:
  COUNTACTIONS (LOWERCASENAME)+ SEMI -> ^(COUNTACTIONS LOWERCASENAME+)
;

componentDefinition:
  id=UPPERCASENAME DEF {hint.push("invalid definition of component '" + $id.text+"'");} s=component {hint.pop();}
      {hint.push("definition of '" + $id.text + "' must end with a semicolon");} SEMI {hint.pop();}
  {tmpComponentNames.add($id.text);} 
  -> ^(COMPONENT $id $s);

component:   
    choice cooperationSet choice -> ^(COOPCOMP choice cooperationSet choice)
   |choice;


//allows only choice between prefixes (not constants)
choice:
  prefix (PLUS {hint.push("summation operator must be followed by a proper prefix, e.g. (a, ra).C");} properPrefix {hint.pop();})* -> prefix properPrefix*
   ;
      
prefix: 
    properPrefix
  | primaryComponent;
  
properPrefix:
  LPAR LOWERCASENAME COMMA expression RPAR DOT prefix -> ^(PREFIX LOWERCASENAME expression prefix) ;  
  
primaryComponent:
   n = UPPERCASENAME {if (gPCTMCParserPrototype.requireDefinitions && !componentNames.contains($n.text)){
      displayRecognitionError(getTokenNames(), new CustomRecognitionException(input, "unknown component '" + $n.text+"'"));}}
 | STOP
 | ANY
 | LPAR component RPAR -> component;
 
model: 
   labelledGroup cooperationSet labelledGroup -> ^(COOP labelledGroup cooperationSet labelledGroup)
  |  labelledGroup 
;
   
labelledGroup:
  l=UPPERCASENAME {tmpGroupNames.add($l.text);}
   {hint.push("group components must be enclosed inside '{' and '}'");} LBRACE {hint.pop();}
     group RBRACE -> ^(LABELLEDGROUP UPPERCASENAME group)
  | LPAR model RPAR -> model;
 
cooperationSet:
   LANGLE {hint.push("expecting a (possibly empty) list of action names");} 
      LOWERCASENAME 
       (COMMA {hint.push("cooperation set has to be of the form <a1, a2, ..., >");} LOWERCASENAME {hint.pop();})* 
       {hint.pop();}
      {hint.push("closing bracket '>' missing");}RANGLE {hint.pop();} -> LOWERCASENAME+
  |LANGLE RANGLE -> LOWERCASENAME[""]; 
   
group:
  groupComponent (PAR {hint.push("group definition has to be of the form G{A[n]|B[m]|...|Z[k]}");} groupComponent {hint.pop();})* -> groupComponent+;
 
groupComponent:
  component (LBRACK expression RBRACK) -> ^(MULT component expression)
 |component -> ^(MULT component REALNUMBER["1.0"]);
 


//states:

state: 
  groupComponentPair
 |actionCount; 

actionCount:
ACOUNT LOWERCASENAME -> ^(ACOUNT LOWERCASENAME)
;

groupComponentPair:
     {hint.push("populations have to be of the form 'Group:Component'");}
     n=UPPERCASENAME      
     INGROUP {if (gPCTMCParserPrototype.requireDefinitions && !groupNames.contains($n.text)) {
          reportError(new CustomRecognitionException(input, "invalid group label " + $n.text));
     }} component  
     {hint.pop();} -> ^(PAIR UPPERCASENAME component)
;