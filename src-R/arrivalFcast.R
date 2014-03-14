#------------------------------------------------------------------------------
# Routines for fitting time series models to forecast future arrivals for
# time points of a replicated bike journey time series.
#------------------------------------------------------------------------------
library("assertthat")
source("departureFcast.R")

# Fit LinReg-RepARIMA models to arrival time series reps
#
# p integer vector
# d integer vector
# q integer vector
# normTrainClDep list of departure observations and its normalisation constants
#   for each cluster
# normTrainClArr list of arrival observations and its normalisation constants
#   for each cluster
# w number of warmup intervals (this determine the forecast start warmup)
# numXreg number of xreg observations used
# h number of forecast intervals
#
# Return best departure -> arrival regression model with ARIMA error
#   for each cluster trying all possible RepARIMA on p x d x q for
#   each cluster in RepARIMATrainDepTs observations
fitRepARIMAArrivals <- function(
  p, d, q, normTrainClDep, normTrainClArr, w, numXreg, h
) {
  # Configurations that we will check
  configs = as.matrix(expand.grid(p = p, d = d, q = q))
  models <- list()
  for (clId in 1 : length(normTrainClDep)) {
    clDepRepTS = normTrainClDep[[clId]]$repTSNorm
    clArrRepTS = normTrainClArr[[clId]]$repTSNorm

    # Generate xreg from departures
    depXreg = array(dim = c(dim(clDepRepTS), numXreg))
    for (rep in 1 : dim(clDepRepTS)[1]) {
      depXreg[rep,,] <- rollapply(
        c(rep(0, numXreg), head(clDepRepTS[rep,], -1)), numXreg, identity
      )
    }
    
    # Try different ARIMA departure models
    res <- apply(configs, 1, function(c) {
      tryCatch(
        c(fitRepARIMA(clArrRepTS, c["p"], c["d"], c["q"], w, depXreg), 
          depMoments = normTrainClDep[[clId]]$repTSMoments,
          arrMoments = normTrainClArr[[clId]]$repTSMoments),
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
  trainClArrRepTSFiles = NULL,
  numXreg
) {
  assert_that(fcastWarmup > numXreg)
  assert_that(numXreg %% fcastFreq == 0)
  
  # Generate departures models - we simply call the
  # model building function using this function's parameters
  funCall <- match.call(expand = TRUE)
  funCall[[1]] <- genDepFcastModel
  funCall$numXreg <- NULL
  depModels <- eval(funCall, parent.frame())

  # Load repTS and change time series sample frequency to fcastFreq
  clDepRepTS <-
    lowerClRepTSFreq(loadRepTS(trainClDepToDestRepTSFiles), fcastFreq)
  clArrRepTS <-
    lowerClRepTSFreq(loadRepTS(trainClArrRepTSFiles), fcastFreq)
  w <- fcastWarmup / fcastFreq
  nx <- numXreg / fcastFreq
  h <- fcastLen / fcastFreq
  
  # Normalise samples
  normTrainClDep <- normClRepTS(clDepRepTS)
  normTrainClArr <- normClRepTS(clArrRepTS)
  
  # Fit arrival model for all clusters
  models <- fitRepARIMAArrivals(
    0:2, 0, 0:2, normTrainClDep, normTrainClArr, w, nx, h
  )
}