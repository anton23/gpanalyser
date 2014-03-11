library("assertthat")
source("fileUtil.R")

trainDepartureFcastModels <- function (
  depFcastMode,
  fcastFreq = 5,
  trainDepTSFiles = NULL,
  trainDepToDestTSFiles = NULL
) {
  switch(depFcastMode,
    naive = trainNaiveDepFcast(fcastFreq),
    arima = trainArimaDepFcast(fcastFreq, trainDepTSFiles, trainDepToDestTSFiles),
    oracle = trainORACLEDepFcast(fcastFreq)
  )
}

trainNaiveDepFcast <- function(fcastFreq) {
  list(name = "NaiveDepartureForecast",
    genTS = function(depTS, depToDestTS, start, warmup, fcastLen) {
      # Check that the forecast length is a multiple of the forecast frequency
      assert_that(fcastLen %% fcastFreq == 0)
      deps <- depToDestTS[start : (start + warmup - 1)]
      futureDepPerFreq <- tail(deps, fcastFreq)
      c(deps, rep(futureDepPerFreq, (fcastLen / fcastFreq)))
    }
  )
}

trainARIMADepFcast <- function (
  fcastFreq,
  trainDepTSFiles,
  trainDepToDestTSFiles
) {
  
}

trainORACLEDepFcast <- function (fcastFreq) {
  list(name = "OracleDepartureForecast",
    genTS = function(depTS, depToDestTS, start, warmup, fcastLen) {
      depToDestTS[start : (start + warmup + fcastLen - 1)]
    }
  )
}

genDepartureTS <- function (
  depModel,
  depTSFile,
  depToDestTSFile,
  start,
  warmup,
  fcastLen
) {
  depTS <- loadFile(depTSFile)
  depToDestTS <- loadFile(depToDestTSFile)
  assert_that(all(dim(depTS) == dim(depToDestTS)))
  
  # Return departure time series (known + forecast) when possible
  if (start + warmup + fcastLen >= dim(depTS)[2]) {
    return(NULL)
  }
  deps = matrix(nrow = dim(depTS)[1], ncol = warmup + fcastLen)
  for (i in 1 : dim(depTS)[1]) {
    # Each observation in depToDestTS must be smaller than matching departure
    assert_that(length(which((depTS[i,] - depToDestTS[i,]) < 0)) == 0)
    deps[i,] <- depModel$genTS(depTS[i,], depToDestTS[i,], start, warmup, fcastLen)
  }
  deps
}