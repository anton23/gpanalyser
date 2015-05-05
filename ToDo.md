# TO DO #

## Internals ##

  * **Variables in generated code** it should be possible to generate code that uses variables as defined in the GPEPA code. For example, if there is a variable `$temp=...` then the code should also have a corresponding `double temp = `
  * **Use existing generated (Java) ODE/Simulation code** instead of generating one. The generated code can be optimised by hand.
  * Experiment with **arithmetic simplification** of the generated ODEs.