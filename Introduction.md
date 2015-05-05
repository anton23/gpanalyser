# Introduction #

gpanalyser (GPA) is a tool for analysing stochastic models described in a stochastic process algebra.
GPA implements the fluid analysis techniques [[1](Introduction#References.md)] that derive systems of ordinary differential equations (ODE) from models defined in the Grouped PEPA (GPEPA) variant of the PEPA stochastic process algebra.

Based on an earlier version [[4](Introduction#References.md)], GPA can additionally compute further derived measures, such as various passage times [[2](Introduction#References.md)] and moments of accumulated rewards [[3](Introduction#References.md)].

## How to run gpanalyser ##

GPA is a command line tool, but also provides a fast graphical output of the calculated data.
GPA requires Java JDK 6 (can be obtained [here](http://www.oracle.com/technetwork/java/javase/downloads/index.html))
GPA can run on any platform with Java JDK. However, in case of 3D graphical output GPA is using OpenGL libraries requiring native support. The GPA archive contains scripts to run GPA on a variety of platforms (Linux and Windows 32/64 bit and Mac OS X). On other platforms, GPA can be run without the 3D capabilities with the command
```
	java -jar build/dist/gpa-0.9.1.jar
```

GPA requires an input model file as an argument on the command line. Example models can be found in
the [models repository](http://code.google.com/p/gpanalyser/source/browse/?repo=models#hg%2Fexamples)

## Sample model files ##

[Client/server model](https://code.google.com/p/gpanalyser/source/browse/examples/clientServer.gpepa?repo=models)

[Pattern expressions](https://code.google.com/p/gpanalyser/source/browse/examples/clientServerProbed.gpepa?repo=models)

[Accumulated rate rewards](https://code.google.com/p/gpanalyser/source/browse/examples/clientServerAccRewards.gpepa?repo=models)

[Impulse rewards](https://code.google.com/p/gpanalyser/source/browse/examples/clientServerImpulse.gpepa?repo=models)

[Scalability experiments](https://code.google.com/p/gpanalyser/source/browse/examples/clientServerScalability.gpepa?repo=models)

## References ##

  * [[1](Introduction#References.md)] Richard Hayden, Jeremy T. Bradley: A fluid analysis framework for a Markovian process algebra. Theoretical Computer Science, Volume 411, May 2010 ( http://dx.doi.org/10.1016/j.tcs.2010.02.001 )
  * [[2](Introduction#References.md)] Richard Hayden, Anton Stefanek, Jeremy T. Bradley: Fluid computation of passage time distributions in large Markov models. Technical report, November 2010 ( http://aesop.doc.ic.ac.uk/pubs/fluid-passage-time/ ).
  * [[3](Introduction#References.md)] Anton Stefanek, Richard Hayden, Jeremy T. Bradley:  Fluid analysis of energy consumption using rewards in massively parallel Markov models. 2nd ACM/SPEC International Conference on Performance Engineering, March 14-16, 2011, Karlsruhe, Germany ( http://aesop.doc.ic.ac.uk/pubs/fluid-energy-rewards/ )
  * [[4](Introduction#References.md)] Anton Stefanek, Richard Hayden, Jeremy T. Bradley:  A new tool for the performance analysis of massively parallel computer systems. Eighth Workshop on Quantitative Aspects of Programming Languages, March 27-28, 2010, Paphos, Cyprus ( http://dx.doi.org/10.4204/EPTCS.28.11 )