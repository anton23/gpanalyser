#!/usr/bin/python
import sys
import csv
import re
import scipy as sp
import numpy as np
import scipy.stats
from pprint import pprint
import math as math

reAnalysis    = re.compile(".*Running analysis.*")
reInterval    = re.compile(".*Interval: (.*)")
rePrediction  = re.compile(".*Prediction #(\d+)")
reResStr      = "Mean:(\d+\.\d+) SD:(\d+\.\d+|NaN) Actual:(\d+)"
reResCl       = re.compile(".*Cl:(\d+) " + reResStr)
reResTtl      = re.compile(".*Ttl: " + reResStr + " All:(\d+)")

filename = sys.argv[1]
fcasts = {
  'IPCTMC' : [],
  'Naive' : [],
  'ARIMA' : [],
  'LinRegARIMA' : []
}
with open(filename, "r") as f:
  analysis = 'dummy'

  # Parse results
  for line in f:
    # Which results are we analysing
    if (reAnalysis.match(line) is not None):
      if 'ODE' in line: analysis = 'IPCTMC'
      if 'naive' in line: analysis = 'Naive'
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
