#------------------------------------------------------------------------------
# Routines for fitting RepARIMA and linear regression + RepARIMA models
#
# For more information see
# "A single series representation of multiple independent ARMA processes",
# Bowden et al.
#------------------------------------------------------------------------------
source("repTSUtil.R")
library("forecast")

printModel <- function(msg, model) {
  # Print model fit statistics
  cat(msg,
    " - Order:", model$order,
    "#Xreg:", model$numXreg,
    "AIC:", model$aic,
    "Log-Likelihood:", model$loglik,
    "Coeff:", model$coef,
  "\n")
} 

# Choose the best among all ARIMA models
#
# models - list of ARIMA models
#
# Return best model
getBestModel <- function(models) {
  bestIndex <- which.max(lapply(models, function(t){t$loglik}))
  printModel("Best model", models[[bestIndex]])
  models[[bestIndex]]
}

# To fit a RepARIMA(p, d, q) to a repTS with m repetitions we need
# to use the interleaving technique described by Bowden. This function
# computes the SARIMA parameters required to do that
#
# Returns a vector with values NA for coefficients that need to be
# fitted to a SARIMA (p * m, 0, q * m), (0, d, 0) or a linear regression
# model with SARIMA error. All other coefficients are 0
interleavedSARIMACoeffs <- function(p, d, q, m, xreg) {
  # ARMA coeffs
  fixedCoeffs <- c()
  if (p + q > 0) {
    fixedCoeffs <- c(rep(0, times = p * m), rep(0, times = q * m))
    fixedCoeffs[c(rep(FALSE, times = m - 1), TRUE)] <- NA
  }
  # Add intercept + xreg coefficients if needed
  if (!is.null(xreg)) {
    c(fixedCoeffs, rep(NA, dim(xreg)[3] + (d == 0) * 1))    
  } else {
    c(fixedCoeffs, rep(NA, (d == 0) * 1))
  }
}

# Returns the ARIMA(p, d, q) coefficients for the coeffsInterleave of
# an interleaved  SARIMA (p * m, 0, q * m), (0, d, 0) model
interleavedSARIMAToARIMACoeffs <- function(coeffsInterleave, p, q, m) {
  # interleaved ARIMA(p*m,0,q*m) coeffs, (intercept + xregs) coeffs
  coeffs <- coeffsInterleave
  if (p + q > 0) {
    coeffs <- c(coeffsInterleave[seq(m, p * m + q * m, by = m)],
                tail(coeffsInterleave, -(p + q) * m))
  }
  # Remove names (since the offsets have changed)
  names(coeffs) <- NULL
  coeffs
}

# For repTS[reps][obs] fit an ARIMA(p, d, q) using the
# observations repTS[reps, (w - s):] where s = max(p, q) + d.
# The warmup _w_ allows us to ignore some initial observations
#
# Returns list(coef, order, loglik) _coef_ficients for ARIMA of _order_
# with _loglik_lyhood
fitRepARIMA <- function (repTS, p, d, q, w, xreg = NULL) {
  s = max(p, q) + d  # Need the samples from t - s for forecast at t
  m = dim(repTS)[1] # number of replications
  
  # We only fit for observations that lie in our forecast period
  repTSInterleaved <- as.vector(repTS[, (w - s) : dim(repTS)[2]])
  xregInterleaved <- NULL
  if (!is.null(xreg)) {
    xregInterleaved <- 
      matrix(as.vector(xreg[, (w - s) : (dim(xreg)[2]),]), ncol = dim(xreg)[3])
  }

  # Build the model using the interleaved time series
  # The seasonal difference D=d is like the ARIMA difference d in a non-interleaved model
  model <- Arima(
    repTSInterleaved,
    order = c(p * m, 0, q * m),
    seasonal = list(order = c(0, d, 0), period = m),
    fixed = interleavedSARIMACoeffs(p, d, q, m, xreg),
    xreg = xregInterleaved,
    transform.pars = FALSE
  )
  # Our return structure for the RepARIMA
  model <- list(
    coef = interleavedSARIMAToARIMACoeffs(model$coef, p, q, m),
    order = c(p, d, q),
    numXreg = length(xreg),
    loglik = model$loglik,
    aic = model$aic
  )
  printModel("Candidate model", model)
  model
}