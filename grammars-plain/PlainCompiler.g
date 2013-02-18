tree grammar PlainCompiler; 
 
options{
  language = Java; 
  tokenVocab = PlainParser; 
  ASTLabelType = CommonTree; 
 
}
import PCTMCCompilerPrototype;

@header{
  
  package uk.ac.imperial.doc.gpa.plain.syntax;
 
  
  import java.util.LinkedList;
  import java.util.Map;
  import java.util.HashMap;
  import java.util.Set;
  import java.util.HashSet;
  import java.util.LinkedHashMap;
  import java.util.Collection;
  
  import uk.ac.imperial.doc.jexpressions.expressions.*;
  import uk.ac.imperial.doc.jexpressions.conditions.*;
  import uk.ac.imperial.doc.jexpressions.constants.*;
  import uk.ac.imperial.doc.jexpressions.variables.*;
  
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
  
  import uk.ac.imperial.doc.gpa.plain.expressions.*; 
  import uk.ac.imperial.doc.gpa.plain.representation.*;
   
  import com.google.common.collect.Multimap;
  import com.google.common.collect.LinkedHashMultimap;
  
  import com.google.common.collect.HashMultiset;
  import com.google.common.collect.Multiset;
  
  
    import uk.ac.imperial.doc.gpa.syntax.CompilerError;
  import uk.ac.imperial.doc.pctmc.syntax.CustomRecognitionException;
  import uk.ac.imperial.doc.pctmc.syntax.ErrorReporter;
  
  import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
 
 import uk.ac.imperial.doc.pctmc.interpreter.IExtension;
  import uk.ac.imperial.doc.pctmc.experiments.distribution.DistributionSimulation;
  import uk.ac.imperial.doc.pctmc.experiments.distribution.GroupOfDistributions;
  import uk.ac.imperial.doc.pctmc.experiments.distribution.DistributionsAtAllTimes;
  import uk.ac.imperial.doc.pctmc.experiments.distribution.DistributionsAtTimes;
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


state returns [State t]
@init{
  List<String> components = new LinkedList<String>(); 
}
:
  ^(TRANSACTION (id=UPPERCASENAME{components.add($id.text);})+) {
  $t = new Transaction(components);
  }
| ^(COUNT c=UPPERCASENAME) {$t = new CountingState($c.text);}
;


