# Extracting Java representations of ODEs and simulation from gpanalyser #

gpanalyser can be run in debug mode with the command line argument `-debug debugFolder` where `debugFolder` is the folder where all the debug files are output.

Whenever processing an ODE analysis, gpanalyser dynamically generates the necessary ODEs and compiles a Java representation extending the [SystemOfODEs](http://code.google.com/p/gpanalyser/source/browse/src-pctmc/uk/ac/imperial/doc/pctmc/utils/SystemOfODEs.java?name=gpa-0.9.1) class. When run with the debug option, gpanalyser saves the generated Java code in the `odesCode` file in the given folder. gpanalyser also saves a more readable plain text version in the file `odesFriendly`.

When processing a Simulation, gpanalyser dynamically generates an event generator for the underlying PCTMC, compiling a Java representation extending the class [AggregatedStateNexEventGenerator](http://code.google.com/p/gpanalyser/source/browse/src-pctmc/uk/ac/imperial/doc/pctmc/utils/AggregatedStateNextEventGenerator.java?name=gpa-0.9.1). When run with the debug option, gpanalyser saves the generated Java code in the 'codeSim' file in the debug folder.