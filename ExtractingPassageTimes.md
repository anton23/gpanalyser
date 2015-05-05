# Introduction #

This manual explains how to use Unified Stochastic Probes (from now on Probes) with gpanalyser to obtain passage time densities. For details about the Unified Stochastic Probes concepts, please read [[1](ExtractingPassageTimes#References.md)]. You can find more details about the implementation in [[2](ExtractingPassageTimes#References.md)]. A short summary of the underlying extension of the tool can be found in [[3](ExtractingPassageTimes#References.md)].

# Overview #

Unified Stochastic Probes provide a convenient and compositional way of expressing behaviour and state based passage times in large scale systems. Each probe is defined using a regular-expression-based syntax. This is automatically translated into a component attached to a portion of the system that triggers signals based on actions of the system. These start and later stop a clock that measures the specified passage time. In many cases, the cumulative distribution function of this time can be approximated by efficient fluid approximation, automatically generated from the probe description.

## Example model ##

This model represents a system of many cooperating Consumer and Producer components, where each Producer has its own Terminal and only when the Terminal is prepared, the Producer can give out the produced information. It has only one-item buffer, and once in a while, if no Consumer is interested, clears it. The overall system is then defined as cooperation between N\_c Consumer and N\_p Producer components.

```

Consumer          = (think, rt).Consumer_decided;
Consumer_decided  = (get_product, rg).Consumer_prep;
Consumer_prep     = (use, ru).Consumer;

Terminal          = (setup, rs).Terminal_get;
Terminal_get      = (get_product, T).Terminal
+ (timeout, rti).Terminal;

Producer          = (init, ri).Producer_inited;
Producer_inited   = (produce, rp).Producer_full;
Producer_full     = (get_product, rgp).Producer
+ (clear, rcl).Producer;
```

## Probes grammar ##

The grammar of both local and global probes is reminiscent of that in [[1](ExtractingPassageTimes#References.md)]. Concretely:

```

Rl ::= {pred}R      \\ state guarded probe, global probes only
R , R        \\ sequence
R | R        \\ choice
R ; R        \\ both
R : signal   \\ probe followed by signal
R [n]        \\ expect R n times
R [n, m]     \\ expect R n to m times
R?           \\ expect zero or one R
R+           \\ expect one or more repetitions of R
R*           \\ expect R arbitrary number of times
R / R        \\ reset
R @ R        \\ fail
R!           \\ negation
.            \\ any action or signal
action       \\ eventual specific action or signal
-action      \\ subsequent specific action or signal
eE           \\ empty action or signal sequence
```

To specify a Probe attached to our model, we need to use a special syntax (after our model). The following specifies a Probe for measuring the time, until one individual Consumer component uses two pieces of information. Formally, we request a substitution of our Consumer components for the same number of Consumer components, but one of those will have a local probe "LProbe" attached (does not matter which one as they are all the same) and observe its actions (in this case only "use" action). At the beginning of the execution, “LProbe” immediately sends "begin" signal for "GProbe" to start the measurement, since we specified to send "begin" signal right after an "empty action". Then "LProbe" expects the "use" action twice, after which it sends the "end" signal to "GProbe to stop the measurement.

```

Probe ["output.dat"] (stopTime=250, stepSize=1, density=10) steady 500
{
GProbe = begin : start, end : stop <-
observes
{
LProbe = eE : begin, use[2] : end <-
}
where
{
Consumers{Consumer[N_c]} =>
Consumers{(Consumer <*> LProbe) | Consumer[N_c - 1]}
}
}
```

Informally, the structure is first a Probe definition, its parameters and then global probe specification. Symbols <- specify repetition. Keyword "observes" with the following local probe definition(s) and substitution(s) is optional.

The Probe definition is interesting here. First, we specify Probe for probing in the fluid flow approximation mode or SimProbe for simulation. After that, optional definition of an output file ("output.dat") follows. If this is specified, it will receive the output of the passage time measurement as a simple time-value pairs, one line per one point. If the file already exists, it will be overwritten.

Simulation and fluid flow approximation modes differ in the third parameter provided in the parentheses. The first common parameter, "stopTime", specifies the time until which we will be solving/simulating the probed system. The second common argument, "stepSize", is used for increasing the precision of analysis - smaller steps mean higher precision, but take longer to finish. Here, we specified fluid flow approximation mode and the third parameter in such case, "density", also helps with precision. For SimProbe, the third parameter is "replications", which specifies number of times the simulation will be repeated. For simulation probe, we would therefore start with this definition instead:

```

SimProbe ["output.dat"] (stopTime=250, stepSize=1, replications=5000)
```

Next, the type of probe is chosen. This can be "steady" for steady-state individual passage time, "transient" for transient individual passage time or, the default (no type), global passage time. Both "steady" and "transient" take one numeric argument, which determines the expected steady-state time for the model. In the case of steady-state individual analysis, we will start passage time analysis at this time of model execution. For transient individual passage time, this determines the time for truncation of the unconditional CDF computation.

The grammar of specifications closely follows the suggested grammar from [[1](ExtractingPassageTimes#References.md)]. That means that for steady-state individual passage time, the following form is used. Please note, that both the local and the global probe is repeating and that local probe waits for "actions1 sequence" to send the "begin" signal and "actions2 sequence" to send the "end" signal. Substitution symbolises, that we attach our local probe to exactly one component amongst many in the same component group.

```

Probe (...) steady X
{
GProbe = begin : start, end : stop <-
observes
{
LProbe = actions1 : begin, actions2 : end <-
}
where
{
ComponentGroup{Component[N_c]} =>
ComponentGroup{(Component <*> LProbe) | Component[N_c - 1]}
}
}
```

For transient individual passage time probes, the form is very similar. However, we have to specify "transient" rather than "steady" and both local and global probes are non-repeating.

```

Probe (...) transient X
{
GProbe = begin : start, end : stop
observes
{
LProbe = actions1 : begin, actions2 : end
}
where
{
ComponentGroup{Component[N_c]} =>
ComponentGroup{(Component <*> LProbe) | Component[N_c - 1]}
}
}
```

Lastly, the grammar for global passage time probes is not limited to such a form. One can utilise the full grammar of probes. The basic template is as follows.

```

Probe (...)
{
GProbe = actions1 : start, actions2 : stop
}
```

This is a probe which is not location-aware - no local probe is attached to any component. To utilise the full power of probes, unlimited number of free-form local probes can be attached. Since we are interested in the whole population, usually we attach one type of probe to all the components in one component group.

```

Probe (...)
{
GProbe = actions1 : start, actions2 : stop
observes
{
Component1Probe = ...
Component2Probe = ...
.
.
.
}
where
{
ComponentGroup{Component[N_c]} =>
ComponentGroup{(Component <*> LProbe)[N_c]},
.
.
.
}
}
```

Please note, that, as argued in [[2](ExtractingPassageTimes#References.md)], global passage time probes in simulation mode do not support predicates. Similarly, the global probe in fluid-flow approximation only supports limited set of operations [[1](ExtractingPassageTimes#References.md)].

## References ##

  * [[1](ExtractingPassageTimes#References.md)] Richard Hayden, Jeremy T. Bradley, Allan Clark: Performance specification and evaluation with Unified Stochastic Probes and fluid analysis. IEEE Transactions on Software Engineering, December, 2011 ( http://dx.doi.org/10.1109/TSE.2012.1 )
  * [[2](ExtractingPassageTimes#References.md)] Matej Kohut: A Unified Performance Query Formalism. Master's thesis (Final project), Imperial College London, Department of computing, 2012, (http://www.doc.ic.ac.uk/teaching/distinguished-projects/2012/m.kohut.pdf)
  * [[3](ExtractingPassageTimes#References.md)] Matej Kohut, Anton Stefanek, Richard A. Hayden, Jeremy T. Bradley: Specification and efficient computation of passage-time distributions in GPA, QEST 2012 (http://www.doc.ic.ac.uk/teaching/distinguished-projects/2012/m.kohut.pdf)