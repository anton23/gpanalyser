# GPAnalyser #

GPAnalyser is a tool for fluid analysis of massively parallel systems. The core of GPA is based on the [paper](http://dx.doi.org/10.1016/j.tcs.2010.02.001) by Hayden et. al. showing how to derive systems of ordinary differential equations approximating moments of populations in models described in the Markovian process algebra PEPA.

GPA efficiently implements these techniques and extensions allowing calculation of accumulated rewards and computation of passage time probabilities. The techniques are slightly generalised and GPA supports a larger class of models. In addition to GPEPA, the input files can use a simple chemical-equations-like language. GPA also provides mechanisms to explore large parameter spaces, either by naive parameter sweeping or via calls to MATLAB's global optimisation toolbox.

## Getting started ##
  * Download the newest version of GPA [here](http://gpanalyser.googlecode.com/files/gpa-0.9.2.zip)
  * [Introduction](Introduction.md) gives an overview of GPA and provides references to related literature.
  * Basic syntax of GPEPA input files is described [here](Syntax.md).
  * See the website of Richard Hayden's [thesis](http://www.doc.ic.ac.uk/~rh/thesis) for some additional information and sample models

## News ##
  * A beta version of an extension implementing the Unified Stochastic Probes language with efficient fluid approximations of passage times can be downloaded [here](http://gpanalyser.googlecode.com/files/gpa-0.9.3b-probes.zip)
  * See [Wiki](ExtractingPassageTimes.md) for more details

![http://models.gpanalyser.googlecode.com/hg/images/example.png](http://models.gpanalyser.googlecode.com/hg/images/example.png)![http://models.gpanalyser.googlecode.com/hg/images/passage.png](http://models.gpanalyser.googlecode.com/hg/images/passage.png)![http://models.gpanalyser.googlecode.com/hg/images/sweeping.png](http://models.gpanalyser.googlecode.com/hg/images/sweeping.png)![http://models.gpanalyser.googlecode.com/hg/images/clock-sim.png](http://models.gpanalyser.googlecode.com/hg/images/clock-sim.png)