#------------------------------------------------------------------------------
# Routines for fitting time series models to forecast future arrivals for
# time points of a replicated bike journey time series.
#------------------------------------------------------------------------------
library("assertthat")
source('departureFcast.R')

# Fit LinReg-RepARIMA models to arrival time series reps
#
# p integer vector
# d integer vector
# q integer vector
# trainDepRepTS departure observations over several days
# trainArrRepTS arrrival observations over several days
# w number of warmup intervals
# h number of forecast intervals
#
# Return best departure -> arrival regression model with ARIMA error
#   for each cluster trying all possible RepARIMA on p x d x q for
#   each cluster in RepARIMATrainDepTs observations
fitRepARIMAArrivals <- function(p, d, q, trainDepRepTS, trainArrRepTS, w, h) {
  # Configurations that we will check
  configs = as.matrix(expand.grid(p = p, d = d, q = q))
  models <- list()
  for (clId in 1 : dim(trainDepRepTS)[2]) {
    clDepRepTS = trainDepRepTS[, clId, ]
    clArrRepTS = trainArrRepTS[, clId, ]
        
    # Normalise
    clDepRepTSMoments <- avgAndSDRepTS(clDepRepTS)
    clDepRepTS <- normRepTS(
      clDepRepTS, clDepRepTSMoments$avgTS, clDepRepTSMoments$sdTS
    )
    clArrRepTSMoments <- avgAndSDRepTS(clArrRepTS)
    clArrRepTS <- normRepTS(
      clArrRepTS, clArrRepTSMoments$avgTS, clArrRepTSMoments$sdTS
    )
    
    # Generate xreg from departures
    depXreg = array(dim = c(dim(clDepRepTS), w))
    for (rep in 1 : dim(clDepRepTS)[1]) {
      depXreg[rep,,] <-
        rollapply(c(rep(0, w), head(clDepRepTS[rep,], -1)), w, identity)
    }
    
    # Try different ARIMA departure models
    res <- apply(configs, 1, function(c) {
      tryCatch(
        c(fitRepARIMA(clArrRepTS, c["p"], c["d"], c["q"], w, depXreg), 
          clDepRepTSMoments, clArrRepTSMoments),
        error = function(e) {list(loglik = -1000000000, aic = 1000000000)}
      )
    })
    models <- list(models, getBestModel(res))
  }
  models
}

genArrFcastModel <- function (
  depFcastMode,
  fcastFreq,
  fcastWarmup,
  fcastLen,
  trainClDepRepTSFiles = NULL,
  trainClDepToDestRepTSFiles = NULL,
  trainClArrRepTSFiles = NULL
) {
  # Generate departures models - we simply call the
  # model building function using this function's parameters
  funCall <- match.call(expand = TRUE)
  funCall[[1]] <- genDepFcastModel
  depModels <- eval(funCall, parent.frame())

  # Aggregate departures to fcastFreq
  depRepTSAtFreq <- aperm(
    apply(loadRepTS(trainClDepToDestRepTSFiles), c(1,2), function(x) {
      aggregate(x, fcastFreq, fcastFreq, sum) 
    }), c(2,3,1)
  )
  # Aggregate arrivals to fcastFreq
  arrRepTSAtFreq <- aperm(
    apply(loadRepTS(trainClArrRepTSFiles), c(1,2), function(x) {
      aggregate(x, fcastFreq, fcastFreq, sum) 
    }), c(2,3,1)
  )
  warmupFreq <- fcastWarmup / fcastFreq
  lenFreq = fcastLen / fcastFreq
  
  # Fit arrival model for all clusters
  models <- fitRepARIMAArrivals(
    0:2, 0, 0:2, depRepTSAtFreq, arrRepTSAtFreq, warmupFreq, lenFreq
  )
}