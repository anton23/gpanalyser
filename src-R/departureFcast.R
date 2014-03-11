library("assertthat")
source("fileUtil.R")

trainDepartureFcastModels <- function (
  depFcastMode,
  fcastFreq = 5,
  trainDepTSFiles = NULL,
  trainMuDepTSFiles = NULL
) {
  switch(depFcastMode,
    naive = trainNaiveDepFcast(fcastFreq),
    arima = trainArimaDepFcast(fcastFreq, trainDepTSFiles, trainMuDepTSFiles),
    oracle = trainOracleDepFcast(fcastFreq, trainDepTSFiles, trainMuDepTSFiles)
  )
}

trainNaiveDepFcast <- function(fcastFreq) {
  list(name = "NaiveDepartureForecast",
    genTS = function(depTS, muTS, start, warmup, fcastLen) {
      deps <- muTS[start : (start + warmup - 1)] * 
        depTS[start : (start + warmup - 1)]
      futureDepPerFreq <- mean(tail(deps, fcastFreq))
      deps <- c(deps, rep(futureDepPerFreq, fcastLen))
    }
  )
}

trainARIMADepFcast <- function (
  fcastFreq,
  trainDepTSFiles,
  trainMuDepTSFiles
) {
  
}

trainORACLEDepFcast <- function (
  fcastFreq,
  trainDepTSFiles,
  trainMuDepTSFiles
) {

}

genDepartureTS <- function (
  depModel,
  depTSFile,
  muTSFile,
  start,
  warmup,
  fcastLen
) {
  depTS <- loadFile(depTSFile)
  muTS <- loadFile(muTSFile)
  assert_that(all(dim(depTS) == dim(muTS)))

  # Return departure time series (known + forecast) when possible
  if (start + warmup + fcastLen >= dim(depTS)[2]) {
    return(NULL)
  }
  deps = matrix(nrow = dim(depTS)[1], ncol = warmup + fcastLen)
  for (i in 1 : dim(depTS)[1]) {
    cat(depTS[i, which(muTS[i,] != 0)], '\n')
    # Each mu must have a matching departure
    assert_that(length(which(depTS[i, which(muTS[i,] != 0)] == 0)) == 0)
    deps[i,] <- depModel$genTS(depTS[i,], muTS[i,], start, warmup, fcastLen)
  }
  deps
}