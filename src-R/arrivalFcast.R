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
  for (clId in 1 : length(normTrainClArr)) {
    clArrRepTS = normTrainClArr[[clId]]$repTSNorm

    # Generate xreg from departures
    depXreg = NULL
    if (!is.null(normTrainClDep)) {
      clDepRepTS = normTrainClDep[[clId]]$repTSNorm
      depXreg = array(dim = c(dim(clDepRepTS), nx))
      for (rep in 1 : dim(clDepRepTS)[1]) {
        depXreg[rep,,] <- rollapply(
          c(rep(0, nx), head(clDepRepTS[rep,], -1)), nx, identity
        )
      }
    }
    
    # Try different ARIMA departure models
    res <- apply(configs, 1, function(c) {
      tryCatch(c(fitRepARIMA(clArrRepTS, c["p"], c["d"], c["q"], w, depXreg)),
               error = function(e) {list(loglik = -1000000000, aic = 1000000000)}
      )
    })
    models[[clId]] <- getBestModel(res)
    if (!is.null(normTrainClDep)) {
      models[[clId]]$depAvgTS <- normTrainClDep[[clId]]$repTSMoments$avgTS
      models[[clId]]$depSDTS <- normTrainClDep[[clId]]$repTSMoments$sdTS
    }
    models[[clId]]$arrAvgTS <- normTrainClArr[[clId]]$repTSMoments$avgTS
    models[[clId]]$arrSDTS <- normTrainClArr[[clId]]$repTSMoments$sdTS
  }
  models
}

genArrFcastModel <- function(
  arrFcastMode,
  fcastFreq,
  fcastWarmup,
  fcastLen,
  minXreg,
  trainClDepRepTSFiles = NULL,
  trainClDepToDestRepTSFiles = NULL,
  trainClArrRepTSFiles = NULL
) {
  # Class like generation of models
  arrModel <- switch(arrFcastMode,
    naive = genNaiveArrFcastModel(
      fcastFreq, fcastWarmup, fcastLen
    ),
    avg = genARIMAArrFcastModel(
      fcastFreq, fcastWarmup, fcastLen,
      trainClArrRepTSFiles,
      TRUE
    ),
    arima = genARIMAArrFcastModel(
      fcastFreq, fcastWarmup, fcastLen,
      trainClArrRepTSFiles
    ),
    linreg = genLinRegARIMAArrFcastModel(
      fcastFreq, fcastWarmup, fcastLen, minXreg,
      trainClDepRepTSFiles,
      trainClDepToDestRepTSFiles,
      trainClArrRepTSFiles,
      TRUE
    ),
    linregarima = genLinRegARIMAArrFcastModel(
      fcastFreq, fcastWarmup, fcastLen, minXreg,
      trainClDepRepTSFiles,
      trainClDepToDestRepTSFiles,
      trainClArrRepTSFiles
    ),
    oracle = genOracleArrFcastModel(
      fcastFreq, fcastWarmup, fcastLen
    )
  )
  arrModel$fcastFreq = fcastFreq
  arrModel$fcastWarmup = fcastWarmup
  arrModel$fcastLen = fcastLen
  arrModel
}

genNaiveArrFcastModel <- function(
  fcastFreq,
  fcastWarmup,
  fcastLen
) {
  list(name = "NaiveArrForecast",
    fcastTPt = function(cId, startTPt, depModel, depTS, depToDestTS, arrTS) {
      sum(tail(warmupPeriod(arrTS, startTPt, fcastWarmup), fcastLen))
    }
  )
}

genARIMAArrFcastModel <- function(
  fcastFreq,
  fcastWarmup,
  fcastLen,
  trainClArrRepTSFiles,
  zerofcast = FALSE
) {
  # Load repTS and change time series sample frequency to fcastFreq
  clArrRepTS <- lowerClRepTSFreq(loadRepTS(trainClArrRepTSFiles), fcastFreq)
  w <- fcastWarmup / fcastFreq
  h <- fcastLen / fcastFreq

  # Normalise samples
  normTrainClArr <- normClRepTS(clArrRepTS)
  
  # Fit arrival model for all clusters
  arrModels <- fitRepARIMAArrivals(0, 0, 0, NULL, normTrainClArr, w, 0, h)
  if (!zerofcast) {
    arrModels <- fitRepARIMAArrivals(1, 0, 0:1, NULL, normTrainClArr, w, 0, h)
  }
  
  list(name = "ARIMAArrForecast",
    fcastTPt = function(cId, startTPt, depModel, depTS, depToDestTS, arrTS) {
      arrMod <- arrModels[[cId]]
      # Normalise
      arrTSNorm <- normTS(
        lowerTSFreq(histPeriod(arrTS, startTPt, fcastWarmup), fcastFreq),
        arrMod$arrAvgTS, arrMod$arrSDTS
      )
      # Forecast
      clArrRepARIMA <- Arima(
        arrTSNorm, order = arrMod$order, fixed = arrMod$coef,
        transform.pars = FALSE
      )
      arrTSNorm <- c(arrTSNorm, forecast(clArrRepARIMA, h = h)$mean)
      # Denormalise
      arrTSFcast <- denormTS(arrTSNorm, arrMod$arrAvgTS, arrMod$arrSDTS)
      # We are interested in sum of all arrivals within fcast horizon
      max(sum(tail(arrTSFcast, h)), 0)
    }
  )
}

genLinRegARIMAArrFcastModel <- function (
  fcastFreq,
  fcastWarmup,
  fcastLen,
  minXreg,
  trainClDepRepTSFiles = NULL,
  trainClDepToDestRepTSFiles = NULL,
  trainClArrRepTSFiles = NULL,
  zerofcast = FALSE
) {
  assert_that(fcastWarmup > minXreg)
  assert_that(minXreg %% fcastFreq == 0)

  # Load repTS and change time series sample frequency to fcastFreq
  clDepRepTS <-
    lowerClRepTSFreq(loadRepTS(trainClDepToDestRepTSFiles), fcastFreq)
  clArrRepTS <- lowerClRepTSFreq(loadRepTS(trainClArrRepTSFiles), fcastFreq)
  w <- fcastWarmup / fcastFreq
  nx <- minXreg / fcastFreq
  h <- fcastLen / fcastFreq
  
  # Normalise samples
  normTrainClDep <- normClRepTS(clDepRepTS)
  normTrainClArr <- normClRepTS(clArrRepTS)
  
  # Fit arrival model for all clusters
  arrModels <-
    fitRepARIMAArrivals(0, 0, 0, normTrainClDep, normTrainClArr, w, nx, h)
  if (!zerofcast) {
    arrModels <-
      fitRepARIMAArrivals(1, 0, 0:1, normTrainClDep, normTrainClArr, w, nx, h)
  }
  
  list(name = "LinRegARIMAArrForecast",
    fcastTPt = function(cId, startTPt, depModel, depTS, depToDestTS, arrTS) {
      arrMod <- arrModels[[cId]]
      # Calculate the xreg using known and forecasted departures
      depTSFcast <- depModel$genTS(cId, startTPt, depTS, depToDestTS)
      depTSFcastNorm <- normTS(
        lowerTSFreq(
          # Append history and forecast
          c(histPeriod(depToDestTS, startTPt, 0), depTSFcast), fcastFreq
        ),
        arrMod$depAvgTS, arrMod$depSDTS
      )
      xreg <-
        rollapply(c(rep(0, nx), head(depTSFcastNorm, -1)), nx, identity)

      # Normalise known arrivals
      arrTSNorm <- normTS(
        lowerTSFreq(histPeriod(arrTS, startTPt, fcastWarmup), fcastFreq),
        arrMod$arrAvgTS, arrMod$arrSDTS
      )

      # Forecast
      clArrRepARIMA <- Arima(
        arrTSNorm, order = arrMod$order, fixed = arrMod$coef,
        xreg = head(xreg, -h), transform.pars = FALSE
      )
      arrTSNorm <- c(
        arrTSNorm, forecast(clArrRepARIMA, xreg = tail(xreg, h), h = h)$mean
      )
      # Denormalise
      arrTSFcast <- denormTS(arrTSNorm, arrMod$arrAvgTS, arrMod$arrSDTS)
      # We are interested in sum of all arrivals within fcast horizon
      max(sum(tail(arrTSFcast, h)), 0)
    }
  )
}

genOracleArrFcastModel <- function(
  fcastFreq,
  fcastWarmup,
  fcastLen
) {
  list(name = "OracleArrForecast",
    fcastTPt = function(cId, startTPt, depModel, depTS, depToDestTS, arrTS) {
      sum(fcastPeriod(arrTS, startTPt, fcastWarmup, fcastLen))
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
  startTPts <- seq(
    1,
    length(depTS[1, ]) - arrModel$fcastWarmup - arrModel$fcastLen,
    arrModel$fcastFreq
  )
  fcastArrTS <- c()
  for (cId in 1 : dim(depTS)[1]) {
    # Each observation in depToDestTS must be smaller than in depTS
    assert_that(length(which((depTS[cId,] - depToDestTS[cId,]) < 0)) == 0)
    assert_that(length(which(arrTS[cId,] < 0)) == 0)
    fcastArr <- c()
    for (startTPt in startTPts) {
      fcastArr <- c(fcastArr, arrModel$fcastTPt(
        cId, startTPt, depModel, depTS[cId,], depToDestTS[cId,], arrTS[cId,]
      ))
    }
    fcastArrTS <- rbind(fcastArrTS, fcastArr)
  }
  fcastArrTS
}

fcastOracleArrivalTS <- function (
  fcastFreq,
  fcastWarmup,
  fcastLen,
  arrTSFile
) {
  arrModel = genOracleArrFcastModel(fcastFreq, fcastWarmup, fcastLen)
  arrTS <- loadTS(arrTSFile)

  startTPts <- seq(
    1, length(arrTS[1, ]) - fcastWarmup - fcastLen, fcastFreq
  )
  fcastArrTS <- c()
  for (cId in 1 : dim(arrTS)[1]) {
    fcastArr <- c()
    for (startTPt in startTPts) {
      fcastArr <- c(fcastArr, arrModel$fcastTPt(
        cId, startTPt, NULL, NULL, NULL, arrTS[cId,]
      ))
    }
    fcastArrTS <- rbind(fcastArrTS, fcastArr)
  }
  fcastArrTS
}