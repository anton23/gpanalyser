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


