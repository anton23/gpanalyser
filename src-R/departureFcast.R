#------------------------------------------------------------------------------
# Routines for fitting time series models to forecast future departures for
# time points of a replicated bike journey time series.
#------------------------------------------------------------------------------
library("assertthat")
source("fileUtil.R")
source("fitRepARIMA.R")

histPeriod <- function(ts, stPt, wUp) {head(ts, stPt + wUp - 1)}
warmupPeriod <- function(ts, stPt, wUp) {ts[stPt : (stPt + wUp - 1)]}
fcastPeriod <- function(ts, stPt, wUp, len) {ts[(stPt + wUp) + (0 : len - 1)]}

# Fit RepARIMA models to departure time series reps
#
# p integer vector
# d integer vector
# q integer vector
# normTrainCls list of departure observations and its normalisation constants
#   for each cluster
# w number of warmup intervals
# h number of forecast intervals
#
# Return best departure model for each cluster trying all
#   possible RepARIMA on p x d x q for each cluster in
#   RepARIMATrainDepTs observations
fitRepARIMADepartures <- function(p, d, q, normTrainCls, w, h) {
  # Configurations that we will check
  configs = as.matrix(expand.grid(p = p, d = d, q = q))
  lapply(normTrainCls, function(normTrainCl) {
    # Try different ARIMA departure models
    res <- apply(configs, 1, function(c) {
      tryCatch(
        c(fitRepARIMA(normTrainCl$repTSNorm, c["p"], c["d"], c["q"], w), 
          normTrainCl$repTSMoments),
        error = function(e) {list(loglik = -1000000000, aic = 1000000000)}
      )
    })
    getBestModel(res)
  })
}

genDepFcastModel <- function (
  depFcastMode,
  fcastFreq,
  fcastWarmup,
  fcastLen,
  trainClDepRepTSFiles = NULL,
  trainClDepToDestRepTSFiles = NULL
) {
  # Make sure warmup and len are multiples of frequency
  assert_that(fcastWarmup %% fcastFreq == 0)
  assert_that(fcastLen %% fcastFreq == 0)
  
  # Class like generation of models
  switch(depFcastMode,
    naive = genNaiveDepFcastModel(
      fcastFreq, fcastWarmup, fcastLen,
      trainClDepToDestRepTSFiles
    ),
    arima = genARIMADepFcastModel(
      fcastFreq, fcastWarmup, fcastLen,
      trainClDepToDestRepTSFiles
    ),
    oracle = genOracleDepFcastModel(
      fcastFreq, fcastWarmup, fcastLen
    )
  )
}

genNaiveDepFcastModel <- function(
  fcastFreq,
  fcastWarmup,
  fcastLen,
  trainClDepToDestRepTSFiles
) {
  trainDepToDestRepTS <- loadRepTS(trainClDepToDestRepTSFiles);
  avgDepToDest <- list()
  for (i in 1 : dim(trainDepToDestRepTS)[2]) {
    avgDepToDest[[i]] <- avgAndSDRepTS(trainDepToDestRepTS[,i,])$avgTS
  }
  list(name = "NaiveDepartureForecast",
    genTS = function(cId, startTPt, depTS, depToDestTS) {
      if (startTPt + fcastWarmup + fcastLen > length(depTS)) { return(NULL) }
      # Time series assumed to be known until end of warmup period.
      # After warmup we repeat the departures observed in the last
      # fcastFreq minutes of the time series repeatedly over the
      # fcastLen interval.
      c(
        warmupPeriod(depToDestTS, startTPt, fcastWarmup), 
        round(fcastPeriod(avgDepToDest[[cId]], startTPt, fcastWarmup, fcastLen))
      )
    }
  )
}

genARIMADepFcastModel <- function(
  fcastFreq,
  fcastWarmup,
  fcastLen,
  trainClDepToDestRepTSFiles
) {  
  # Change frequency of observations and normalise to make
  # time homogeneous
  trainDepRepTS <-
    lowerClRepTSFreq(loadRepTS(trainClDepToDestRepTSFiles), fcastFreq)
  w <- fcastWarmup / fcastFreq
  h <- fcastLen / fcastFreq
  normTrainCl <- normClRepTS(trainDepRepTS)
  # Fit RepARIMA models for each RepARIMA series
  models <- fitRepARIMADepartures(0:1, 0, 0:1, normTrainCl, w, h)

  list(name = "ARIMADepartureForecast",
    genTS = function(cId, startTPt, depTS, depToDestTS) {
      if (startTPt + fcastWarmup + fcastLen > length(depTS)) { return(NULL) }
      m <- models[[cId]]
      # Time series assumed to be known up to the end of warmup period
      depToDestTS <- histPeriod(depToDestTS, startTPt, fcastWarmup)
      # Aggregate in order to obtain the right frequency
      depToDestTSAggr <- lowerTSFreq(depToDestTS, fcastFreq)
      depToDestTSAggrNorm <- normTS(depToDestTSAggr, m$avgTS, m$sdTS)

      # Forecast
      clDepRepARIMA <- Arima(
        c(rep(0, 10), depToDestTSAggrNorm), # Pad series a little
        order = m$order, fixed = m$coef,
        transform.pars = FALSE
      )
      depToDestTSAggrNorm <- c(depToDestTSAggrNorm,
        forecast(clDepRepARIMA, h = h)$mean
      )
      # Denormalise
      depToDestTSAggr <- denormTS(
        depToDestTSAggrNorm, m$avgTS, m$sdTS
      )
      # Warmup period departures + departure forecast results in 1 minute
      # sampling frequency
      c(
        warmupPeriod(depToDestTS, startTPt, fcastWarmup),
        as.vector(sapply(round(tail(depToDestTSAggr, h)), function(x) {
          disAggr <- rep(floor(x / fcastFreq), fcastFreq)
          d <- round(x) - sum(disAggr)
          disAggr + c(rep(1, d), rep(0, fcastFreq - d))
        }))
      )
    }
  )
}

genOracleDepFcastModel <- function(
  fcastFreq,
  fcastWarmup,
  fcastLen
) {
  list(name = "OracleDepartureForecast",
    genTS = function(cId, startTPt, depTS, depToDestTS) {
      if (startTPt + fcastWarmup + fcastLen > length(depTS)) { return(NULL) }
      # Pick real departure observations for warmup period and fcast horizon
      c(
        warmupPeriod(depToDestTS, startTPt, fcastWarmup),
        fcastPeriod(depToDestTS, startTPt, fcastWarmup, fcastLen)
      )
    }
  )
}

fcastDepartureTS <- function (
  depModel,
  depTSFile,
  depToDestTSFile,
  startTPt
) {
  depTS <- loadTS(depTSFile)
  depToDestTS <- loadTS(depToDestTSFile)
  assert_that(all(dim(depTS) == dim(depToDestTS)))
  
  # Use forecast model to create departure time series
  fcastDepTS <- c()
  for (i in 1 : dim(depTS)[1]) {
    # Each observation in depToDestTS must be smaller than in depTS
    assert_that(length(which((depTS[i,] - depToDestTS[i,]) < 0)) == 0)
    fcastDepTS <- rbind(
      fcastDepTS, depModel$genTS(i, startTPt, depTS[i,], depToDestTS[i,])
    )
  }
  fcastDepTS
}