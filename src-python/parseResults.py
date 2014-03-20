#!/usr/bin/python
import sys
import csv
import re
import scipy as sp
import numpy as np
import scipy.stats
from pprint import pprint
import math as math
import numpy as np

# Mean absolute scaled error
def MASE(actualTS, naiveTS, fcastTS):
  avgNaiveE = np.mean([abs(a - b) for (a, b) in zip(actualTS, naiveTS)])
  fcastE = [abs(a - b) for (a, b) in zip(actualTS, fcastTS)]
  return sum(fcastE) / (len(actualTS) * avgNaiveE)

# Root mean square error
def RMSE(actualTS, fcastTS):
  return \
    math.sqrt(np.mean([(a - b) * (a - b) for (a, b) in zip(actualTS, fcastTS)]))

# Get TS by cluster
def getTSByCluster(clId, fcastResult):
  meanFcast = [float(fcastResult[a][b][clId][0]) for b in range(len(fcastResult[0]))
		  for a in range(len(fcastResult))]
  sdFcast = [float(fcastResult[a][b][clId][1]) for b in range(len(fcastResult[0]))
		  for a in range(len(fcastResult))]
  act = [float(fcastResult[a][b][clId][2]) for b in range(len(fcastResult[0]))
		  for a in range(len(fcastResult))]
  return (meanFcast, sdFcast, act)

# Analysis
def analyse(fcasts, analysis):
  fcastRes = fcasts[analysis]
  naiveRes = fcasts['Naive']
  numClusters = len(fcastRes[0][0]) - 1
  print("%s results:" % analysis)
  for clId in range(numClusters):
    (m, sd, act) = getTSByCluster(clId, fcastRes)
    (mNaive, sdNaive, act2) = getTSByCluster(clId, naiveRes)
    print(
      "Cl#%d: MASE: %.4f RMSE: %.4f" % (clId, MASE(act, mNaive, m), RMSE(act, m))
    ) 
  (m, sd, act) = getTSByCluster(numClusters, fcastRes)
  (mNaive, sdNaive, act2) = getTSByCluster(numClusters, naiveRes)
  print(
    "Ttl:  MASE: %.4f RMSE: %.4f" % (MASE(act, mNaive, m), RMSE(act, m))
  )

reAnalysis    = re.compile(".*Running analysis.*")
reInterval    = re.compile(".*Interval: (.*)")
rePrediction  = re.compile(".*Prediction #(\d+)")
reResStr      = "Mean:(\d+\.\d+) SD:(\d+\.\d+|NaN) Actual:(\d+)"
reResCl       = re.compile(".*Cl:(\d+) " + reResStr)
reResTtl      = re.compile(".*Ttl: " + reResStr + " All:(\d+)")
fcasts = {
  'IPCTMC' : [],
  'Naive' : [],
  'Avg' : [],
  'ARIMA' : [],
  'LinRegARIMA' : []
}
with open(sys.argv[1], "r") as f:
  analysis = 'dummy'

  # Parse results
  for line in f:
    # Which results are we analysing
    if (reAnalysis.match(line) is not None):
      if 'ODE' in line: analysis = 'IPCTMC'
      if 'naive' in line: analysis = 'Naive'
      if 'avg' in line: analysis = 'Avg'
      if 'arima' in line: analysis = 'ARIMA'
      if 'linregarima' in line: analysis = 'LinRegARIMA'
      continue
    
    # A new interval
    if (reInterval.match(line) is not None):
      fcasts[analysis].append([])
    
    # A new cluster prediction
    if (reResCl.match(line) is not None):
      if (int(reResCl.match(line).group(1)) == 0): 
	fcasts[analysis][-1].append([])
      fcasts[analysis][-1][-1].append(reResCl.match(line).groups()[1:4])

    # A new aggregate prediction
    if (reResTtl.match(line) is not None):
      fcasts[analysis][-1][-1].append(reResTtl.match(line).groups()[0:4])

# Analysis
for analysis in fcasts.keys():
  analyse(fcasts, analysis)
