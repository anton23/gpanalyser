lexer grammar PlainLexer;

import PCTMCLexerPrototype;

@header{
  package uk.ac.imperial.doc.gpa.plain.syntax;
   
}
 
 
COUNT: '#';
ODE_BIKE_FCAST: 'OdeBikeFcast';
SIM_BIKE_FCAST: 'SimBikeFcast';
TS_R_BIKE_FCAST: 'TSRBikeFcast';
ODES_TI: 'ODEsTI';
SIM_TI: 'SimTI';

ARIMA_ERROR: 'arimaError';
ARR_FCAST_MODE: 'arrFcastMode';
FCAST_WARMUP: 'fcastWarmup';
FCAST_LEN: 'fcastLen';
FCAST_FREQ: 'fcastFreq';
CL_DEP_STATES: 'clDepStates';
CL_ARR_STATES: 'clArrStates';
DEP_FCAST_MODE: 'depFcastMode';
TRAIN_CL_DEP_TS: 'trainClDepTS';
TRAIN_CL_DEP_TO_DEST_TS: 'trainClDepToDestTS';
TRAIN_CL_ARR_TS: 'trainClArrTS';
CL_DEP_TS: 'clDepTS';
CL_DEP_TO_DEST_TS: 'clDepToDestTS';
CL_ARR_TS: 'clArrTS';
MIN_XREG: 'minXreg';