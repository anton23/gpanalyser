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
# nx number of xreg observations used
# h number of forecast intervals
#
# Return best departure -> arrival regression model with ARIMA error
#   for each cluster trying all possible RepARIMA on p x d x q for
#   each cluster in RepARIMATrainDepTs observations
fitRepARIMAArrivals <- function(
  p, d, q, normTrainClDep, normTrainClArr, w, nx, h
) {
  # Configurations that we will check
  configs = as.matrix(expand.grid(p = p, d = d, q = q))
  models <- list()
  for (clId in 1 : length(normTrainClDep)) {
    clDepRepTS = normTrainClDep[[clId]]$repTSNorm
    clArrRepTS = normTrainClArr[[clId]]$repTSNorm

    # Generate xreg from departures
    depXreg = array(dim = c(dim(clDepRepTS), nx))
    for (rep in 1 : dim(clDepRepTS)[1]) {
      depXreg[rep,,] <- rollapply(
        c(rep(0, nx), head(clDepRepTS[rep,], -1)), nx, identity
      )
    }
    
    # Try different ARIMA departure models
    res <- apply(configs, 1, function(c) {
      tryCatch(c(fitRepARIMA(clArrRepTS, c["p"], c["d"], c["q"], w, depXreg)),
               error = function(e) {list(loglik = -1000000000, aic = 1000000000)}
      )
    })
    models[[clId]] <- getBestModel(res)
    models[[clId]]$depAvgTS <- normTrainClDep[[clId]]$repTSMoments$avgTS
    models[[clId]]$depSDTS <- normTrainClDep[[clId]]$repTSMoments$sdTS
    models[[clId]]$arrAvgTS <- normTrainClArr[[clId]]$repTSMoments$avgTS
    models[[clId]]$arrSDTS <- normTrainClArr[[clId]]$repTSMoments$sdTS
  }
  models
}

genArrFcastModel <- function (
  depModel,
  fcastFreq,
  fcastWarmup,
  fcastLen,
  trainClDepRepTSFiles = NULL,
  trainClDepToDestRepTSFiles = NULL,
  trainClArrRepTSFiles = NULL,
  minXreg
) {
  assert_that(fcastWarmup > minXreg)
  assert_that(minXreg %% fcastFreq == 0)

  # Load repTS and change time series sample frequency to fcastFreq
  clDepRepTS <-
    lowerClRepTSFreq(loadRepTS(trainClDepToDestRepTSFiles), fcastFreq)
  clArrRepTS <-
    lowerClRepTSFreq(loadRepTS(trainClArrRepTSFiles), fcastFreq)
  w <- fcastWarmup / fcastFreq
  nx <- minXreg / fcastFreq
  h <- fcastLen / fcastFreq
  
  # Normalise samples
  normTrainClDep <- normClRepTS(clDepRepTS)
  normTrainClArr <- normClRepTS(clArrRepTS)
  
  # Fit arrival model for all clusters
  arrModels <- fitRepARIMAArrivals(
    0:2, 0, 0:2, normTrainClDep, normTrainClArr, w, nx, h
  )
  
  list(name = "LinRegARIMAForecast",
    genTS = function(cId, depModel, depTS, depToDestTS, arrTS) {
      arrMod <- arrModels[[cId]]

      fcastArr <- c()
      for (startTPt in seq(1, length(depTS), fcastFreq)) {
        # Calculate the xreg using known and forecasted departures
        depTSFcast <- depModel$genTS(cId, depTS, depToDestTS, startTPt)
        if (is.null(depTSFcast)) {
          break;
        }
        depTSFcastNorm <- normTS(
          lowerTSFreq(
            c(head(depToDestTS, startTPt - 1), depTSFcast), fcastFreq
          ),
          arrMod$depAvgTS, arrMod$depSDTS
        )

        xreg = rollapply(
          c(rep(0, nx), head(depTSFcastNorm, -1)), nx, identity
        )

        # Get known arrivals
        arrTSNorm <- normTS(
          lowerTSFreq(head(arrTS, startTPt + fcastWarmup - 1), fcastFreq),
          arrMod$arrAvgTS, arrMod$arrSDTS
        )
        
        # Forecast
        clArrRepARIMA <- Arima(
          arrTSNorm,
          order = arrMod$order, fixed = arrMod$coef,
          xreg = head(xreg, -h),
          transform.pars = FALSE
        )
        arrTSNorm <- c(
          arrTSNorm, forecast(clArrRepARIMA, xreg = tail(xreg, h), h = h)$mean
        )
        arrTSFcast <- denormTS(
          arrTSNorm, arrMod$arrAvgTS, arrMod$arrSDTS
        )
        fcastArr <- c(fcastArr, max(sum(tail(arrTSFcast, h)), 0))
      }
      fcastArr
    }
  )
}

fcastArrivalTS <- function (
  depModel,
  arrModel,
  depTSFile,
  depToDestTSFile,
  arrTSFile
) {
  depTS <- loadTS(depTSFile)
  depToDestTS <- loadTS(depToDestTSFile)
  arrTS <- loadTS(arrTSFile)
  assert_that(all(dim(depTS) == dim(depToDestTS)))
  assert_that(all(dim(depTS) == dim(arrTS)))
  
  # Use forecast model to create future arrival forecasts
  fcastArrTS <- c()
  for (cId in 1 : dim(depTS)[1]) {
    # Each observation in depToDestTS must be smaller than in depTS
    assert_that(length(which((depTS[cId,] - depToDestTS[cId,]) < 0)) == 0)
    assert_that(length(which(arrTS[cId,] < 0)) == 0)
    fcastArrTS <- rbind(
      fcastArrTS,
      arrModel$genTS(cId, depModel, depTS[cId,], depToDestTS[cId,], arrTS[cId,])
    )
  }
  fcastArrTS
}