# Syntax of input files #

We illustrate the syntax of gpanalyser on a simple GPEPA client/server model. In this model, we will have a number of client and server components. Each client sends a request to the servers, waits for a reply and then waits for the data (possibly sent by a different server from the one that responded). The client then performs some independent 'thinking' and repeats the whole procedure. In addition, the servers can break, in which case they can't serve any requests and have to be reset. The simple diagram below could depict such a system:

![http://models.gpanalyser.googlecode.com/hg/images/clientServer.png](http://models.gpanalyser.googlecode.com/hg/images/clientServer.png)

Each gpanalyser file consists of four sections:

  * _Definition of constants_ - this is where various numerical constants, such as rages or initial component populations are defined.
  * _Model definition_ - the model is defined here. In case of GPEPA, it consists of the definitions of sequential components and a system equation.
  * _Analyses_ - this is where the computation takes place. Different analyses can be described (such as ODE analysis and simulation), together with the expressions of interest (such as different moments and rewards based on the model populations).
  * _Optimisation experiments_ - in addition to single runs of the above analyses, gpanalyser can use repeat the computation for different parameter values and use an analysis as a basis for the objective and constraint function of global optimisation problems.

## GPEPA model definition ##

Leaving out the definition of constants for now, we could write the client server model in GPEPA as
```
Client = (request,rr).Client_waiting; 
Client_waiting = (data,rd).Client_think; 
Client_think = (think,rt).Client; 

Server = (request,rr).Server_get + (break,rb).Server_broken; 
Server_get = (data,rd).Server; 
Server_broken = (reset,rrst).Server; 


Clients{Client[n]}<request,data>Servers{Server[m]}
```

In general, gpanalyser supports full GPEPA syntax and also a simple language for directly defining a population CTMC with stoichiometric equations. See [model definition details](ModelDetails.md).

## Definition of constants ##
For this to work, the preceding constants definition has to define each used constant, so we could for example set
```
rr = 2.0; 
rt = 0.27;
rb = 0.1; 
rd = 1.0; 
rrst = 1.0; 

n = 100.0; 
m = 60.0; 
```

## Definition of analyses ##

In order for any computation to take place, the input file has to describe one or more analyses that will be performed on the model. For example, to run an ODE analysis on the client/server model, we can write
```
ODEs(stopTime = 40.0, stepSize = 0.1, density = 10){
  E[Clients:Client],E[Clients:Client_waiting],E[Clients:Client_think];
  E[Servers:Server],E[Servers:Server_get],E[Servers:Server_broken]; 
}
```

This results in gpanalyser generating a system of ODEs for the first order moments of population counts in the above model and then running a built-in Runge-Kutta solver up to time 40.0, saving a data point at each step of 0.1 and implicitly using 10 subintervals in each such step. After obtaining the numerical results, gpanalyser plots the given moment-based expressions - in this case, a window with 2 tabs appears, one for the first list of expressions (the means of client populations) and one for the second (the means of server populations):
![http://models.gpanalyser.googlecode.com/hg/images/clientsSmall.png](http://models.gpanalyser.googlecode.com/hg/images/clientsSmall.png) ![http://models.gpanalyser.googlecode.com/hg/images/serversSmall.png](http://models.gpanalyser.googlecode.com/hg/images/serversSmall.png)

The ODE generator automatically determines which moment ODEs to include in the resulting system so that all the described plots can be produced. For example, if we add the following lines for plotting the variance of client and server components
```
  Var[Clients:Client],Var[Clients:Client_waiting],Var[Clients:Client_think];
  Var[Servers:Server],Var[Servers:Server_get],Var[Servers:Server_broken];
```
to the above list of plot expressions, gpanalyser generates a system of second order moment ODEs and displays the following plots:
![http://models.gpanalyser.googlecode.com/hg/images/clientsVarSmall.png](http://models.gpanalyser.googlecode.com/hg/images/clientsVarSmall.png) ![http://models.gpanalyser.googlecode.com/hg/images/serversVarSmall.png](http://models.gpanalyser.googlecode.com/hg/images/serversVarSmall.png)

The plot expressions can be any arithmetic expressions involving moments, for example
```
  E[Servers:Server],E[Servers:Server]-1.95*Var[Servers:Server]^0.5,E[Servers:Server]+1.95*Var[Servers:Server]^0.5;
```
See [plot expressions details](PlotExpressions.md).

gpanalyser also supports a standard stochastic simulation analysis that can be used to evaluate accuracy of the ODE analysis. The following analysis
```
Simulation(stopTime = 40.0, stepSize = 0.1, replications = 10000){
  ...
}
```
runs a generated simulation of the underlying CTMC and plots the given expressions using sample moments.

## Iteration and optimisation experiments ##

![http://models.gpanalyser.googlecode.com/hg/images/iterateRb.png](http://models.gpanalyser.googlecode.com/hg/images/iterateRb.png)![http://models.gpanalyser.googlecode.com/hg/images/iterateRbRrst.png](http://models.gpanalyser.googlecode.com/hg/images/iterateRbRrst.png)