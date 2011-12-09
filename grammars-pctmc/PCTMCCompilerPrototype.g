tree grammar PCTMCCompilerPrototype; 
 
options{
  language = Java; 
  //tokenVocab = PCTMCLexer; 
  ASTLabelType = CommonTree; 
}
/*
@header{
  
  package pctmc.syntax;
 
  
  import java.util.LinkedList;
  import java.util.Map;
  import java.util.HashMap;
  import java.util.Set;
  import java.util.HashSet;
  import java.util.LinkedHashMap;
  import java.util.Collection;
  
  import jexpressions.expressions.*;
  import jexpressions.constants.*;
  import jexpressions.variables.*;
  
  import pctmc.analysis.*;
  
  import pctmc.odeanalysis.*; 
  import pctmc.simulation.*;
  import pctmc.compare.*;
  
  import pctmc.expressions.patterns.*;
  import pctmc.expressions.*;
  import pctmc.plain.*;
  import pctmc.representation.State;
  import pctmc.representation.*; 
  import pctmc.experiments.iterate.*; 
  import pctmc.analysis.plotexpressions.*;
  import pctmc.expressions.patterns.*;
  import pctmc.postprocessors.numerical.*; 
   
  import com.google.common.collect.Multimap;
  import com.google.common.collect.LinkedHashMultimap;
  
  import com.google.common.collect.HashMultiset;
  import com.google.common.collect.Multiset;
  
  
}
*/

@members{
  Map<ExpressionVariable,AbstractExpression> vars; 
}

system returns[Constants constants, 
               Map<ExpressionVariable,AbstractExpression> unfoldedVariables,
               PCTMC pctmc,                
               Multimap<AbstractPCTMCAnalysis,PlotDescription> plots,
               List<PCTMCExperiment> experiments
    ]
@init{       
  Map<String,Double> constantMap = new LinkedHashMap<String,Double>();      
  vars = new LinkedHashMap<ExpressionVariable,AbstractExpression>();
  $plots = LinkedHashMultimap.<AbstractPCTMCAnalysis,PlotDescription>create();
  $experiments = new LinkedList<PCTMCExperiment>();    
}

: constantDefinition[constantMap]* {$constants = new Constants(constantMap);}
   varDefinition* {$unfoldedVariables = new ExpressionVariableUnfolderPCTMC(vars).unfoldVariables();
                    vars = $unfoldedVariables; }
   m=modelDefinition[$unfoldedVariables,$constants] {$pctmc = $m.pctmc;}
   
   analysis[$pctmc, $constants, $plots]*
   
   (e=experiment[$pctmc, $constants, $unfoldedVariables] {$experiments.add($e.iterate);})* 
;

modelDefinition[Map<ExpressionVariable,AbstractExpression> unfoldedVariables,Constants constants] returns [PCTMC pctmc]
@init{
  Map<State,AbstractExpression> initCounts = new LinkedHashMap<State,AbstractExpression>();
  List<EventSpecification> eventSpecifications = new LinkedList<EventSpecification>();
}:

   (d=eventDefinition {eventSpecifications.add($d.e);})*
   initDefinition[initCounts]*
   
   {      
      Set<State> states = new HashSet<State>(); 
      List<EvolutionEvent> events = new LinkedList<EvolutionEvent>();
      for (EventSpecification es:eventSpecifications){
        List<State> decreasing = new LinkedList<State>(); 
        for (State t:es.getDecreasing()){
          states.add(t); 
          decreasing.add(t); 
        }
        List<State> increasing = new LinkedList<State>();
        for (State t:es.getIncreasing()){
          states.add(t);  
          increasing.add(t); 
        }
        ExpressionVariableSetterPCTMC setter = new ExpressionVariableSetterPCTMC($unfoldedVariables); 
        es.getRate().accept(setter); 
        EvolutionEvent event = new EvolutionEvent(decreasing, increasing, es.getRate());
        events.add(event);
      }
      Map<State,AbstractExpression> initMap = new HashMap<State, AbstractExpression>(); 
      for (State t:states){
        if (!initCounts.containsKey(t)){
          initMap.put(t, DoubleExpression.ZERO); 
        } else {
          ExpressionVariableSetterPCTMC setter = new ExpressionVariableSetterPCTMC($unfoldedVariables);
          initCounts.get(t).accept(setter); 
          initMap.put(t, initCounts.get(t)); 
        }
      }
      
      $pctmc = new PCTMC(initMap, events);
};


experiment[PCTMC pctmc, Constants constants, Map<ExpressionVariable,AbstractExpression> unfoldedVariables] returns [PCTMCExperiment iterate]
@init{  
  List<RangeSpecification> ranges = new LinkedList<RangeSpecification>(); 
  List<PlotAtDescription> plots = new LinkedList<PlotAtDescription>(); 
  Map<String,AbstractExpression> reEvaluation = new HashMap<String,AbstractExpression>(); 
  List<RangeSpecification> minRanges = new LinkedList<RangeSpecification>();;
  PlotAtDescription minSpecification=null; 
}
:
  ^(ITERATE (r=rangeSpecification {ranges.add($r.range);})*
    (MINIMISE m=plotAtSpecification[$constants]  {minSpecification = $m.p;}(mr=rangeSpecification {minRanges.add($mr.range);})+)?
   (WHERE 
       ((c=LOWERCASENAME rhs=expression) {reEvaluation.put($c.text,$rhs.e); })+ )?
  
    a=analysis[$pctmc,$constants, null]   
    (p=plotAtSpecification[$constants] {plots.add($p.p);})*
   )
  {$iterate = new PCTMCIterate(ranges,minSpecification,minRanges,reEvaluation,$a.analysis,$a.postprocessor,plots,$unfoldedVariables);}
| ^(TRANSIENT_ITERATE (r=rangeSpecification {ranges.add($r.range);})+
   (WHERE 
       ((c=LOWERCASENAME rhs=expression) {reEvaluation.put($c.text,$rhs.e); })+ )?
      a=analysis[$pctmc,$constants, null]
      ps=plotDescriptions
  {$iterate = new PCTMCTransientIterate(ranges,reEvaluation,$a.analysis, $a.postprocessor,$ps.p, $unfoldedVariables);}
  )
;

rangeSpecification returns[RangeSpecification range]:
  ^(RANGE c=constant from=realnumber to=realnumber 
    ((IN steps=integer STEPS) {$range = new RangeSpecification($c.text,$from.value,$to.value,$steps.value); } 
    | (STEP step=realnumber) {$range = new RangeSpecification($c.text,$from.value,$to.value,$step.value);}
    ))
  
;

plotAtSpecification[Constants constants] returns [PlotAtDescription p]
@init{
  String f=""; 
  List<PlotConstraint> constraints = new LinkedList<PlotConstraint>(); 
}: 
  ^(PLOT pl=plotAt[$constants]
      ( WHEN 
        (pa=plotAt[$constants] GEQ prob=realnumber {
             constraints.add(new PlotConstraint($pa.e,$pa.t,$prob.value));})+
      )?      
       (file=FILENAME {f=$file.text.replace("\"","");})?)
  {
    $p = new PlotAtDescription($pl.e,$pl.t,constraints,f); 
  }
;

plotAt[Constants constants] returns [AbstractExpression e, double t]:
   exp=expression ATTIME
   time=expression 
  
   {$e = $exp.e;
    ExpressionEvaluatorWithConstants timeEval = new ExpressionEvaluatorWithConstants($constants);
    $time.e.accept(timeEval);
    $t = timeEval.getResult();} 
;

analysis[PCTMC pctmc, Constants constants, Multimap<AbstractPCTMCAnalysis,PlotDescription> plots] 
returns [AbstractPCTMCAnalysis analysis, NumericalPostprocessor postprocessor]
:
   o=odeAnalysis[pctmc,constants, plots] {$analysis=$o.analysis; $postprocessor=$o.postprocessor;}
 | s=simulation[pctmc,constants, plots] {$analysis=$s.analysis; $postprocessor=$s.postprocessor;}
 | c=compare[pctmc, constants, plots] {$analysis=$c.analysis; $postprocessor=$c.postprocessor;}
;

compare[PCTMC pctmc, Constants constants, Multimap<AbstractPCTMCAnalysis,PlotDescription> plots] 
returns [AbstractPCTMCAnalysis analysis, NumericalPostprocessor postprocessor]:
^(COMPARE a1=analysis[pctmc, constants, plots] a2=analysis[pctmc, constants, plots] ps=plotDescriptions)
{
  $analysis = new PCTMCCompareAnalysis($a1.analysis,$a2.analysis); 
  $postprocessor = new CompareAnalysisNumericalPostprocessor($a1.postprocessor,$a2.postprocessor);
  $analysis.addPostprocessor($postprocessor); 
  if ($plots!=null) $plots.putAll($analysis,$ps.p);   
}
;
odeAnalysis[PCTMC pctmc, Constants constants, Multimap<AbstractPCTMCAnalysis,PlotDescription> plots] 
returns [PCTMCODEAnalysis analysis, NumericalPostprocessor postprocessor]
@init{
  Map<String, Object> parameters = new HashMap<String, Object>();
}:
  ^(ODES  
         (LBRACK
             p1=parameter {parameters.put($p1.name, $p1.value);} 
             (COMMA p=parameter 
                          {parameters.put($p.name, $p.value);})* 
          RBRACK)?
         stop=expression COMMA step=expression COMMA den=integer LBRACE 
         ps=plotDescriptions 
    RBRACE    
   ){
      $analysis = new PCTMCODEAnalysis($pctmc, parameters);
      ExpressionEvaluatorWithConstants stopEval = new ExpressionEvaluatorWithConstants($constants);
      $stop.e.accept(stopEval);
      ExpressionEvaluatorWithConstants stepEval = new ExpressionEvaluatorWithConstants($constants);
      $step.e.accept(stepEval);
      $postprocessor = new ODEAnalysisNumericalPostprocessor(stopEval.getResult(),
          stepEval.getResult(),$den.value);
      $analysis.addPostprocessor($postprocessor);
      if ($plots!=null) $plots.putAll($analysis,$ps.p); 
   }
  
;

parameter returns [String name, Object value]:
  p=LOWERCASENAME DEF (n=UPPERCASENAME{$name = $p.text; $value = $n.text;}
                      |r=realnumber  {$name = $p.text; $value = $r.value;}
                      |i=integer  {$name = $p.text; $value = $i.value;})
;

simulation[PCTMC pctmc, Constants constants, Multimap<AbstractPCTMCAnalysis,PlotDescription> plots] 
returns [PCTMCSimulation analysis, NumericalPostprocessor postprocessor]:
  ^(SIMULATION stop=expression COMMA step=expression COMMA replications=integer LBRACE 
         ps=plotDescriptions 
    RBRACE    
   ){
      $analysis = new PCTMCSimulation($pctmc);
      
      ExpressionEvaluatorWithConstants stopEval = new ExpressionEvaluatorWithConstants($constants);
      $stop.e.accept(stopEval);
      ExpressionEvaluatorWithConstants stepEval = new ExpressionEvaluatorWithConstants($constants);
      $step.e.accept(stepEval);
      
      $postprocessor = new SimulationAnalysisNumericalPostprocessor(stopEval.getResult(),stepEval.getResult(),$replications.value);
      $analysis.addPostprocessor($postprocessor);
      if ($plots!=null) $plots.putAll($analysis,$ps.p); 
   }
  
;

plotDescriptions returns [List<PlotDescription> p]
@init{
  String file; 
  $p = new LinkedList<PlotDescription>();
}:
  ({file="";}l=expressionList (TO s=FILENAME {file=$s.text.replace("\"","");})?{
      $p.add(new PlotDescription($l.e,file)); 
  } SEMI )*
;

expressionList returns [List<AbstractExpression> e]
@init{
  $e = new LinkedList<AbstractExpression>(); 
}:
  exp = expression {$e.add($exp.e);} (COMMA exp2 = expression {$e.add($exp2.e); })*
;

constant returns [String text]:
  ^(CONSTANT id=LOWERCASENAME) {$text = $id.text;}
;

constantDefinition[Map<String,Double> map]:
  ^(CONSTDEF id=constant (value=realnumber {$map.put($id.text,$value.value);}
                         |valueI=integer   {$map.put($id.text,new Double($valueI.value));})
);

variable returns [String text]:
  ^(VARIABLE id=LOWERCASENAME) {$text = $id.text;}
;

varDefinition:
  ^(VARDEF var=variable exp=expression {vars.put(new ExpressionVariable($var.text),$exp.e);})
;

eventDefinition returns [EventSpecification e]:
  ^(EVENT dec=stateSum  TO  inc=stateSum AT rate=expression) {$e=new EventSpecification($dec.t,$inc.t,$rate.e);}
;

initDefinition[Map<State,AbstractExpression> map]:
  ^(INIT t=state DEF e=expression {$map.put($t.t,$e.e);})
; 

state returns [State t]:
  ^(STATE n=UPPERCASENAME) {$t = new PlainState($n.text);}
;

stateSum returns [List<State> t]
@init{$t = new LinkedList<State>();}:
  (tr=state{$t.add($tr.t);})*
;

primary_expression returns[AbstractExpression e]:

   id=variable {$e = new ExpressionVariable($id.text);}
 | LPAR ne = expression RPAR {$e = $ne.e;}
 | r = realnumber {$e = new DoubleExpression($r.value);}
 | i = integer {$e = new DoubleExpression((double)$i.value);}
 | TIME {$e = new TimeExpression();}
 | c=constant {$e = new ConstantExpression($c.text);}
 | cp=combinedPowerProduct {$e = CombinedProductExpression.create($cp.c);}
 | m=mean {$e = $m.m;} 
 | eg = generalExpectation {$e = $eg.e;}
 | cm=central {$e = $cm.c;}
 | scm=scentral {$e = $scm.c;}
 | ^(MIN exp1=expression COMMA exp2=expression) {$e = MinExpression.create($exp1.e,$exp2.e); }
 | ^(MAX exp1=expression COMMA exp2=expression) {$e = MaxExpression.create($exp1.e,$exp2.e); }
 |  {List<AbstractExpression> args = new LinkedList<AbstractExpression>(); }        
   ^(FUN name=LOWERCASENAME firstArg=expression {args.add($firstArg.e);} (COMMA arg=expression {args.add($arg.e);})*) {$e = FunctionCallExpression.create($name.text,args);}
 | ^(PATTERN s=state) {$e = new PatternPopulationExpression($s.t);}  
;

generalExpectation returns [GeneralExpectationExpression e]:
  ^(GENEXPECTATION exp = expression)
  {$e = new GeneralExpectationExpression($exp.e);}
;

mean returns [MeanOfLinearCombinationExpression m]:
  ^(MEAN e=expression) {$m = new MeanOfLinearCombinationExpression($e.e,vars);}
;

central returns [CentralMomentOfLinearCombinationExpression c]:
  ^(CENTRAL e=expression n=integer) {$c = new CentralMomentOfLinearCombinationExpression($e.e,$n.value,vars);}
;

scentral returns [StandardisedCentralMomentOfLinearCombinationExpression c]:
  ^(SCENTRAL e=expression n=integer) {$c = new StandardisedCentralMomentOfLinearCombinationExpression($e.e,$n.value,vars);}
;


combinedPowerProduct returns [CombinedPopulationProduct c]
@init{
  PopulationProduct nakedProduct = null; 
}:
 ^(COMBINEDPRODUCT (n=product {nakedProduct = $n.p;})?
  ps=accPowers {c=new CombinedPopulationProduct(nakedProduct,$ps.a);} )
;

accPowers returns [Multiset<PopulationProduct> a]
@init{
  $a = HashMultiset.<PopulationProduct>create(); 
}:
  ( ^(ACC p=product) 
          (n=integer {$a.add($p.p,$n.value-1);})? 
      {$a.add($p.p,1);}
  )*
;

product returns [PopulationProduct p]
@init{ 
  Multiset<State> tmp = HashMultiset.<State>create(); 
}
@after{ 
  $p = new PopulationProduct(tmp); 
}:
  ^(PRODUCT (s=state 
         (n=integer {tmp.add($s.t,$n.value-1);})? 
        {tmp.add($s.t,1);}
  )+ )
;

expression returns [AbstractExpression e]
@init{
  List<AbstractExpression> positiveTerms = new LinkedList<AbstractExpression>(); 
  List<AbstractExpression> negativeTerms = new LinkedList<AbstractExpression>(); 
}
@after{
  if (negativeTerms.isEmpty()){
    $e = SumExpression.create(positiveTerms);
  } else if (positiveTerms.isEmpty()){
    $e = new UMinusExpression(SumExpression.create(negativeTerms)); 
  } else {
     $e = new MinusExpression(SumExpression.create(positiveTerms),SumExpression.create(negativeTerms)); 
  }
}:
  f=mult_expression {positiveTerms.add($f.e);} ( (PLUS p=mult_expression {positiveTerms.add($p.e);})
                   |(MINUS n=mult_expression {negativeTerms.add($n.e);}))*;    

mult_expression returns [AbstractExpression e]
@init{
  List<AbstractExpression> numTerms = new LinkedList<AbstractExpression>(); 
  List<AbstractExpression> denTerms = new LinkedList<AbstractExpression>(); 
}
@after{
  if (denTerms.isEmpty()){
    $e = ProductExpression.create(numTerms);
  } else {
     $e = DivExpression.create(ProductExpression.create(numTerms),ProductExpression.create(denTerms)); 
  }
}: 
  f=power_expression {numTerms.add($f.e);} ((TIMES n=power_expression {numTerms.add($n.e);}) 
                    |(DIVIDE d=power_expression {denTerms.add($d.e);})
                    )*; 

power_expression returns [AbstractExpression e]:
  e1 = sign_expression {$e = $e1.e;}(POWER e2=sign_expression {$e= new PowerExpression($e,$e2.e); })*;

  
sign_expression returns [AbstractExpression e]
  : 
  (MINUS p1=primary_expression {$e = new UMinusExpression($p1.e); })
  |p2=primary_expression {$e = $p2.e; };
  
  
realnumber returns [Double value]:
  r=REALNUMBER
  {$value = Double.parseDouble($r.text);}
;

integer returns [Integer value]:
  r=INTEGER
  {$value = Integer.parseInt($r.text);}
;

odeTest returns [List<CombinedPopulationProduct> moments, Map<CombinedPopulationProduct, AbstractExpression> odes]
@init{
  $moments = new LinkedList<CombinedPopulationProduct>();
  $odes = new HashMap<CombinedPopulationProduct, AbstractExpression>();
}:
^(ODETEST (m=combinedPowerProduct {$moments.add($m.c);})+
          (^(EXPODE lhs=combinedPowerProduct rhs=expression {$odes.put($lhs.c, $rhs.e);}))+
);