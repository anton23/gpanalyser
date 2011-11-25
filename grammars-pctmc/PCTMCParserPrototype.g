parser grammar PCTMCParserPrototype;

options {
  language = Java;
  output = AST; 
  ASTLabelType = CommonTree;  
  backtrack = true;
}

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
  EXPODE;
  ODETEST;
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

 
//stops the parser on errors instead of recovering
/*
@rulecatch {
  catch (RecognitionException re) {
   reportError(re);  
   recover(input, re);
  }
}*/

system:
  {requireDefinitions = true;}
  constantDefinition* varDefinition* 
  {hint.push("incomplete model definition");} modelDefinition {hint.pop();}
  {hint.push("allowed analyses are 'ODEs', 'Simulation', 'Compare' and experiments 'Iterate' and 'Minimise'");}
  analysis* experiment*
  EOF {hint.pop();};

modelDefinition:
  eventDefinition*
  initDef*
;


analysis:
  odeAnalysis
 |simulation
 |compare
;

compare:
  COMPARE LPAR
    analysis COMMA
    analysis
  RPAR LBRACE
      plotDescription*
  RBRACE -> ^(COMPARE analysis analysis plotDescription*)
;

experiment
// Allows addition of new constants in range specifications
@init{
  Set<String> tmp = constants;
  constants = new HashSet<String>(constants);
}
@after{
  constants = tmp;
}
:
    ir = iterateSpec
        min = minimiseSpec?
    (WHERE
      constantReEvaluation+)?
    analysis PLOT LBRACE
      plots=plotAtSpecifications
    RBRACE
  -> ^(ITERATE $ir $min? (WHERE constantReEvaluation+)? analysis $plots)
 | min = minimiseSpec
    (WHERE
      constantReEvaluation+)?
    analysis PLOT LBRACE
      plots=plotAtSpecifications
    RBRACE
  -> ^(ITERATE $min (WHERE constantReEvaluation+)? analysis $plots)
 
;

iterateSpec:
 (ITERATE 
    ir=rangeSpecifications) -> rangeSpecifications
;



minimiseSpec:
  (MINIMISE minSpec=plotAtSpecificationNoFile mr=rangeSpecifications)
  -> MINIMISE  ^(PLOT $minSpec) $mr
;

rangeSpecifications:
rangeSpecification+
;

constantReEvaluation:
  id=LOWERCASENAME DEF rhs=expression SEMI ->  $id $rhs
;

rangeSpecification:
  constant=LOWERCASENAME FROMVALUE from=REALNUMBER TOVALUE to=REALNUMBER steps=stepSpecification 
 {constants.add($constant.text);}
  -> ^(RANGE $constant $from $to $steps)
;

stepSpecification:
    IN INTEGER STEPS 
  | STEP REALNUMBER 
;

plotAtSpecifications:
  plotAtSpecification+
;

plotAtSpecification:
 plotAtSpecificationNoFile (TO FILENAME)? SEMI -> 
  ^(PLOT plotAtSpecificationNoFile FILENAME?)
;

plotAtSpecificationNoFile:
  plotAt (WHEN constraints)?
;

constraints:
  constraint (AND constraint)* -> constraint* 
;

constraint:
 plotAt GEQ REALNUMBER -> plotAt GEQ REALNUMBER
;

plotAt:
  expression ATTIME REALNUMBER -> expression REALNUMBER
;


odeAnalysis:
  ODES 
  odeParameters?
  {hint.push("ODE analysis has to be of the form ODEs(stopTime=<number>, stepSize=<number>, density=<integer>){}'");}
      LPAR
  STOPTIME DEF stopTime = REALNUMBER COMMA
  STEPSIZE DEF stepSize = REALNUMBER COMMA
  DENSITY DEF density=INTEGER   
  RPAR {hint.pop();}LBRACE
    plotDescription*
  RBRACE
  -> ^(ODES odeParameters? $stopTime $stepSize $density LBRACE plotDescription* RBRACE )
;

odeParameters:
  LBRACK
     {hint.push("ODE analysis parameters have to be of the form [name1=value1, ..., nameK=valueK]");}
   parameter (COMMA parameter)* 
   {hint.pop();}
  RBRACK
;

parameter:
   LOWERCASENAME DEF (UPPERCASENAME|REALNUMBER|INTEGER) ;
 
simulation:
  SIMULATION LPAR
  STOPTIME DEF stopTime = REALNUMBER COMMA
  STEPSIZE DEF stepSize = REALNUMBER COMMA
  REPLICATIONS DEF replications=INTEGER   
  RPAR LBRACE
    plotDescription*
  RBRACE
  -> ^(SIMULATION $stopTime $stepSize $replications LBRACE plotDescription* RBRACE )
;

plotDescription:
 {hint.push("each plot description has to be of the 'e1,...,en (optional ->\"filename\");" +
            " where e1,...,en are expectation based expressions");}
 {requiresExpectation = true;}
  (expressionList (TO {hint.push("filename description has to be of the form '-> \"filename\"'");} FILENAME {hint.pop();})? SEMI) {hint.pop();}
 {requiresExpectation = false;}
;

analysesSpec:
  STOPTIME DEF REALNUMBER SEMI
  STEPSIZE DEF REALNUMBER SEMI
  DENSITY DEF INTEGER SEMI
  REPLICATIONS DEF INTEGER SEMI
;

var: VAR LOWERCASENAME -> ^(VAR LOWERCASENAME); 


state: UPPERCASENAME; 

//-----Rules for definitions----- 

constantDefinition:
  id=LOWERCASENAME {hint.push("constant definition has to be of the form <constant> = <number> ;");}  
   DEF  
    (rate=REALNUMBER|rate=INTEGER) SEMI {hint.pop();} 
     {constants.add($id.text);}
     -> ^(CONSTANT $id $rate);
  
varDefinition:
  var DEF expression SEMI -> ^(VARIABLE var expression);
  
//-----Rules for events

eventDefinition:
dec=stateSum? TO inc=stateSum? AT expression SEMI -> ^(EVENT $dec? TO $inc? AT expression)
;

stateSum:
 state (PLUS state)* -> state+
;
     
     
//-----Initial values

initDef:state DEF expression SEMI -> ^(INIT state DEF expression);      
    
//-----Rules for expressions-----    

     
expression:   
  multExpression ((PLUS|MINUS) multExpression)*;    

multExpression
  : 
  powerExpression ((TIMES|DIVIDE) powerExpression)*;
  
powerExpression
  :
  signExpression (POWER signExpression)*;
  
signExpression
  : 
  (MINUS)? primaryExpression;
  


primaryExpression:    
      p=combinedPowerProduct {if (requiresExpectation && !insideExpectation) reportError(new CustomRecognitionException(input, "population " + $p.text + " has to be inside an expectation"));}
     | var 
     |REALNUMBER
     |INTEGER
     | LPAR expression RPAR 
     | MIN LPAR expression COMMA expression RPAR -> ^(MIN expression COMMA expression)
     | LOWERCASENAME LPAR expressionList RPAR -> ^(FUN LOWERCASENAME expressionList)
     | TIME 
     | c = LOWERCASENAME {if (requireDefinitions && !constants.contains($c.text)) 
          reportError(new CustomRecognitionException(input, "constant '" + $c.text + "' unknown"));}
     | mean 
     | generalExpectation
     | central
     | scentral
     | PATTERN state -> ^(PATTERN state)
;


generalExpectation:
  GENEXPECTATION LBRACK {insideExpectation = true;}
     expression 
   RBRACK 
   {insideExpectation = false;}
   -> ^(GENEXPECTATION expression)
;

mean
:
  MEAN LBRACK {insideExpectation = true;}
    e=expression 
       RBRACK 
       {insideExpectation = false;} -> ^(MEAN expression)
;

central:
   CENTRAL LBRACK {insideExpectation = true;}
      expression COMMA INTEGER 
     RBRACK
     {insideExpectation = false;}
      -> ^(CENTRAL expression INTEGER)
  |VARIANCE LBRACK {insideExpectation = true;}
  expression 
  RBRACK 
  {insideExpectation = false;}
  -> ^(CENTRAL expression INTEGER["2"])
;

scentral:
   SCENTRAL LBRACK {insideExpectation = true;}
      expression COMMA INTEGER RBRACK
   {insideExpectation = false;} -> ^(SCENTRAL expression INTEGER)
;


combinedPowerProduct:
   powerProduct accPower*  ->
    ^(COMBINEDPRODUCT powerProduct accPower*)
 |accPower+  ->
    ^(COMBINEDPRODUCT accPower+);  

powerProduct:
  power+ -> ^(PRODUCT power+);
  
power:
  state (POWER INTEGER)? -> state INTEGER?;
  
acc:
 ACC LPAR powerProduct RPAR -> ^(ACC powerProduct); 
 
accPower:
  acc (POWER INTEGER)? -> acc INTEGER?; 

expressionList:
    expression (COMMA expression)*; 
    
//-----Extra rules for tests

odeTest:
 combinedPowerProduct (COMMA combinedPowerProduct)* SEMI expectedODE+ -> ^(ODETEST combinedPowerProduct+ expectedODE+);

expectedODE: 
DER MEAN LBRACK combinedPowerProduct RBRACK DIVIDE DT DEF expression SEMI -> ^(EXPODE combinedPowerProduct expression);   
   
