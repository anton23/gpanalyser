#------------------------------------------------------------------------------
# Routines for fitting time series models to forecast future departures for
# time points of a replicated time series.
#------------------------------------------------------------------------------
library("assertthat")
source("fileUtil.R")
source("fitRepARIMA.R")

# Fit RepARIMA models to departure time series reps
#
# p integer vector
# d integer vector
# q integer vector
# trainDepRepTS departure observations over several days
# w number of warmup intervals for each day in ARIMATrainDepTs
# Return best departure model for each cluster trying all
#         possible RepARIMA on p x d x q for each cluster in
#         RepARIMATrainDepTs observations
fitRepARIMADepartures <- function(p, d, q, trainDepRepTS, w) {
  # Configurations that we will check
  configs = as.matrix(expand.grid(p = p, d = d, q = q))
  mapply(c, apply(trainDepRepTS, 2, function(clDepRepTS) {
    # Normalise
    clDepRepTSMoments <- avgAndSDRepTS(clDepRepTS)
    clDepRepTSNorm <- normRepTS(
      clDepRepTS, clDepRepTSMoments$avgTS, clDepRepTSMoments$sdTS
    )
    
    # Try different ARIMA departure models
    res <- apply(configs, 1, function(c) {
      tryCatch(
        c(fitRepARIMA(clDepRepTSNorm, c["p"], c["d"], c["q"], w), 
          clDepRepTSMoments),
        error = function(e) {list(loglik = -1000000000, aic = 1000000000)}
      )
    })
    getBestModel(res)
  }), SIMPLIFY = FALSE)
}

genDepFcastModel <- function (
  depFcastMode,
  fcastFreq,
  fcastWarmup,
  fcastLen,
  trainDepRepTSFiles = NULL,
  trainDepToDestRepTSFiles = NULL
) {
  # Make sure warmup and len are multiples of frequency
  assert_that(fcastWarmup %% fcastFreq == 0)
  assert_that(fcastLen %% fcastFreq == 0)
  
  # Class like generation of models
  switch(depFcastMode,
    naive = genNaiveDepFcastModel(
      fcastFreq, fcastWarmup, fcastLen
    ),
    arima = genARIMADepFcastModel(
      fcastFreq, fcastWarmup, fcastLen,
      trainDepRepTSFiles, trainDepToDestRepTSFiles
    ),
    oracle = genOracleDepFcastModel(
      fcastWarmup, fcastLen
    )
  )
}

genNaiveDepFcastModel <- function(fcastFreq, fcastWarmup, fcastLen) {
  list(name = "NaiveDepartureForecast",
    genTS = function(cId, depTS, depToDestTS, startTPt) {
      if (startTPt + fcastWarmup + fcastLen > length(depTS)) { return(NULL) }
      # Time series assumed to be known until end of warmup period
      deps <- depToDestTS[startTPt : (startTPt + fcastWarmup - 1)]
      # After warmup we repeat the departures observed in the last
      # fcastFreq minutes of the time series repeatedly over the
      # fcastLen interval
      c(deps, rep(tail(deps, fcastFreq), fcastLen / fcastFreq))
    }
  )
}

genARIMADepFcastModel <- function (
  fcastFreq,
  fcastWarmup,
  fcastLen,
  trainDepRepTSFiles,
  trainDepToDestRepTSFiles
) {  
  # Aggregate departures to fcastFreq
  trainRepTS <- loadRepTS(trainDepToDestRepTSFiles);
  trainRepTSAtFreq <- apply(trainRepTS, c(1,2), function(x) {
    aggregate(x, fcastFreq, fcastFreq, sum) 
  })
  trainRepTSAtFreq <- aperm(trainRepTSAtFreq, c(2,3,1))
  warmupFreq <- fcastWarmup / fcastFreq
  
  # Fit RepARIMA models for each RepARIMA series
  models <- fitRepARIMADepartures(0:1, 0:1, 0:1, trainRepTSAtFreq, warmupFreq)
  
  list(name = "ARIMADepartureForecast",
    genTS = function(cId, depTS, depToDestTS, startTPt) {
      if (startTPt + fcastWarmup + fcastLen > length(depTS)) { return(NULL) }
      m <- models[[cId]]
      # Time series assumed to be known up to the end of warmup period
      depToDestTS <- head(depToDestTS, startTPt + fcastWarmup - 1)
      # Aggregate in order to obtain the right frequency
      depToDestTSAggr <- aggregate(depToDestTS, fcastFreq, fcastFreq, sum)
      depToDestTSAggrNorm <- normTS(depToDestTSAggr, m$avgTS, m$sdTS)

       # Forecast
       h = fcastLen / fcastFreq
       clDepRepARIMA<- Arima(
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
       # Disaggregate split fcastFreq min aggregate departures
       # into fcastFreq 1-minute departures
       depToDestTSDisAggr <- 
         as.vector(sapply(round(tail(depToDestTSAggr, h)), function(x) {
           disAggr <- rep(floor(x / fcastFreq), fcastFreq)
           d <- round(x) - sum(disAggr)
           disAggr + c(rep(1, d), rep(0, fcastFreq - d))
         }))
       c(tail(depToDestTS, fcastWarmup), depToDestTSDisAggr)
     }
  )
}

genOracleDepFcastModel <- function (fcastWarmup, fcastLen) {
  list(name = "OracleDepartureForecast",
    genTS = function(cId, depTS, depToDestTS, startTPt) {
      if (startTPt + fcastWarmup + fcastLen > length(depTS)) { return(NULL) }
      # Pick real departure observations
      depToDestTS[startTPt : (startTPt + fcastWarmup + fcastLen - 1)]
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
      fcastDepTS, depModel$genTS(i, depTS[i,], depToDestTS[i,], startTPt)
    )
  }
  fcastDepTS
}