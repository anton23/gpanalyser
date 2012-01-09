tree grammar GPACompiler;
 
options{   
  language = Java; 
  tokenVocab = GPAParser; 
  ASTLabelType = CommonTree;  
}
import PCTMCCompilerPrototype;

@header{
  
package uk.ac.imperial.doc.gpa.syntax;
   
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Collection;
  
import com.google.common.collect.Multimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
  
import uk.ac.imperial.doc.jexpressions.expressions.*;
import uk.ac.imperial.doc.jexpressions.conditions.*;
import uk.ac.imperial.doc.jexpressions.constants.*;
import uk.ac.imperial.doc.jexpressions.variables.*;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
    
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
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.*;
import uk.ac.imperial.doc.pctmc.syntax.CustomRecognitionException;
import uk.ac.imperial.doc.pctmc.syntax.ErrorReporter;
      
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.representation.components.*;
import uk.ac.imperial.doc.gpepa.representation.group.*; 
import uk.ac.imperial.doc.gpepa.representation.model.*; 
import uk.ac.imperial.doc.gpepa.representation.*;
import uk.ac.imperial.doc.gpepa.states.*;
import uk.ac.imperial.doc.gpa.patterns.*; 
import uk.ac.imperial.doc.gpa.pctmc.*;
import uk.ac.imperial.doc.gpa.syntax.CompilerError;
}

@members {

     protected Stack<String> hint = new Stack<String>();
     
     protected ErrorReporter errorReporter;
     
     public void setErrorReporter(ErrorReporter errorReporter) {
          this.errorReporter = errorReporter;
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
        String ret;
        if (!hint.isEmpty()) {
          ret = hint.peek();
        } else{
          ret =  super.getErrorMessage(e, tokenNames);
        }
        return ret;
      }
}

@rulecatch {
  catch (RecognitionException re) {
   reportError(re);  
   recover(input, re);
  }
  catch (AssertionError e) {
   reportError(new CustomRecognitionException(input, e.getMessage()));
   recover(input, new CustomRecognitionException(input, e.getMessage()));
  }
}

start:; 

modelDefinition[Map<ExpressionVariable,AbstractExpression> unfoldedVariables,Constants constants] returns [PCTMC pctmc]
@init{
  Set<String> actions = new HashSet<String>(); 
}:
  cd = componentDefinitions
  m=model
  (ca=countActions {actions=$countActions.actions;})?  
  {   
    $pctmc  = GPEPAToPCTMC.getPCTMC(new PEPAComponentDefinitions($cd.componentDefinitions),$m.model,actions, $unfoldedVariables);
  }
;

countActions returns[Set<String> actions]
@init{
  $actions = new HashSet<String>(); 
}:
  ^(COUNTACTIONS (a=LOWERCASENAME {$actions.add($a.text);} )+) 
;

componentDefinitions returns [Map<String,PEPAComponent> componentDefinitions]
@init{
  componentDefinitions = new HashMap<String,PEPAComponent>();
}:
  (^(COMPONENT id=UPPERCASENAME s=component){
    String seqId = $id.text; 
    PEPAComponent seq = $s.c; 
    componentDefinitions.put(seqId,seq);
  })+
;
model returns [GroupedModel model]:
  ^(COOP l=model actions=coop r=model){
    model = new GroupCooperation($l.model,$r.model,$actions.actions); 
  }
 | ^(LABELLEDGROUP label=UPPERCASENAME g=group){
    $model = new LabelledComponentGroup($label.text,$g.group);
   }  
;

group returns [Group group]
@init{  
  Multimap counts = LinkedHashMultimap.<PEPAComponent,AbstractExpression>create(); 
}
@after{
  $group = new Group(counts); 
}:

  (^(MULT s=component n=expression){
    counts.put($s.c,$n.e);  

  }  
  )+
;

state returns [State t]:
  gp = groupComponentPair {$t = new GPEPAState($gp.gp);}
 |a = actionCount {$t = new GPEPAActionCount($a.a);}
;

actionCount returns [String a]:
 ^(ACOUNT n=LOWERCASENAME) {$a = $n.text;}
;

groupComponentPair returns [GroupComponentPair gp]:
  ^(PAIR g=UPPERCASENAME c=component)
  {$gp = new GroupComponentPair($g.text,$c.c);}
;



component returns [PEPAComponent c]:
    co = coopComponent {$c = $co.c;}
   | ch=choice {$c=$ch.c;}
     
   |p=primaryComponent {
    $c= $p.c;
   }
   ;

coopComponent returns [PEPAComponent c]:
  ^(COOPCOMP l=component  a=coop r=component{
        $c = new CooperationComponent($l.c,$r.c,$a.actions);
    
    }  )  ;
  
choice returns [PEPAComponent c]
@init{
  List<Prefix> choices = new LinkedList<Prefix>(); 
}
@after{
  $c=new Choice(choices); 
}:
  (p=prefix{
      choices.add($p.c); 
  })+
;

prefix returns [Prefix c]:
  ^(PREFIX a=LOWERCASENAME r=expression s=component){
      Prefix prefix = new Prefix($a.text,$r.e,$s.c); 
      $c=prefix; 
   }
; 

primaryComponent returns [PEPAComponent c]:
  n=UPPERCASENAME{
    $c=new ComponentId($n.text);
  }
  | ANY {$c=new AnyComponent();}
  |STOP {
    $c = new Stop(); 
  } 
;
  
coop returns [Set<String> actions]
@init{
  actions = new HashSet<String>();
}:
  (a=LOWERCASENAME{
  if (!$a.text.isEmpty()){
    actions.add($a.text);
    } 
   })+
;

