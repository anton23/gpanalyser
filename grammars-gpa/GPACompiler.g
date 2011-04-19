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
  
  import uk.ac.imperial.doc.jexpressions.expressions.*;
  import uk.ac.imperial.doc.jexpressions.constants.*;
  import uk.ac.imperial.doc.jexpressions.variables.*;
  
  import uk.ac.imperial.doc.pctmc.analysis.*;
  
  import uk.ac.imperial.doc.pctmc.odeanalysis.*; 
  import uk.ac.imperial.doc.pctmc.simulation.*;
  import uk.ac.imperial.doc.pctmc.compare.*;
  
  import uk.ac.imperial.doc.pctmc.expressions.*;
  import uk.ac.imperial.doc.pctmc.plain.*;
  import uk.ac.imperial.doc.pctmc.representation.State;
  import uk.ac.imperial.doc.pctmc.representation.*; 
  import uk.ac.imperial.doc.pctmc.experiments.iterate.*; 
  import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.*; 
  
  
  import com.google.common.collect.Multimap;
  import com.google.common.collect.LinkedHashMultimap;
  
  import com.google.common.collect.HashMultiset;
  import com.google.common.collect.Multiset;
  
  
  import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
  import uk.ac.imperial.doc.gpepa.representation.components.*;
  import uk.ac.imperial.doc.pctmc.expressions.patterns.*;
  import uk.ac.imperial.doc.gpepa.representation.group.*; 
  import uk.ac.imperial.doc.gpepa.representation.model.*; 
  import uk.ac.imperial.doc.gpepa.representation.*;
  import uk.ac.imperial.doc.gpepa.states.*;
  import uk.ac.imperial.doc.gpa.patterns.*; 
  import uk.ac.imperial.doc.gpa.pctmc.*;
  
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
    $pctmc  = GPEPAToPCTMC.getPCTMC(new PEPAComponentDefinitions($cd.componentDefinitions),$m.model,actions);
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
    $c=new Constant($n.text);
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

