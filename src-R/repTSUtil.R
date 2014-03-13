#------------------------------------------------------------------------------
# We are dealing with replicated time series. Technically these are
# normal seasonal time series which gaps. A good example for a replicated
# time series observations are time arrivals at a tube station between
# 9 - 11am for dates 11/05/2013 - 21/05/2013. We refer each consecutive time
# interval in the replicated series as a repl(ication). A time point refers to
# a specific time such that for a replicated time series with n replications we
# have n observations for each distinct time point, e.g. for 9.30am. Each
# observation is uniquely identified by its corresponding replication and time
# point.
#
# For more information see
# "A single series representation of multiple independent ARMA processes",
# Bowden et al.
# 
# Routines for making replicated TS time inhomogeneous
#------------------------------------------------------------------------------
library("zoo")

# Compute first and second moments of the replicate time series
avgAndSDRepTS <- function(repTS) {
  # Compute seasonal (i.e. for each obs at the same time)
  # mean and s.d.
  avgTS <- apply(repTS, 2, mean)
  avgTS <- rollapply(
    c(avgTS[1], avgTS, tail(avgTS, 1)), 3, function(x) {c(0.25, 0.5, 0.25) %*% x}
  )
  sdTS <- apply(repTS, 2, sd)
  sdTS <- rollapply(
    c(sdTS[1], sdTS, tail(sdTS, 1)), 3, function(x) {c(0.25, 0.5, 0.25) %*% x}
  )
  list(avgTS = avgTS, sdTS = sdTS)
}

# Normalise an observation using its respective time point avg and std
#
# idxRep - index of replication in time series
# idxTPt  - index of time point in time series
# M      - matrix storing replicated time series
# avgTS  - average observation by time point
# sdTS   - standard deviation by time point
#
# Return normalised M[idxRep, idxTPt]
normObs <- function(idxRep, idxTPt, M, avgTS, sdTS) {
  if (sdTS[idxTPt] == 0) { return(0) }
  (M[idxRep, idxTPt] - avgTS[idxTPt]) / sdTS[idxTPt]
}
norm <- Vectorize(normObs, vectorize.args = c('idxRep', 'idxTPt'))

# Normalise replicated time series
# 
# repTS   - has n time series with m obs each
# avgTS   - average of time points 
# sdTS    - standard deviation of time points
#
# Return normalised replicated time series observations
normRepTS <- function(repTS, avgTS, sdTS) {
  outer(1 : nrow(repTS), 1 : ncol(repTS), FUN = norm, repTS, avgTS, sdTS)
}
normTS <- function(ts, avgTS, sdTS) {
  normRepTS(t(matrix(ts)), avgTS, sdTS)[1,]
}

# Denormalise an observation using its respective time point avg and std
#
# idxRep - index of replication in time series
# idxTPt  - index of time point in time series
# M      - matrix storing replicated time series
# avgTS  - average observation by time point
# sdTS   - standard deviation by time point
#
# Return denormalised M[idxRep, idxTPt]
denormObs <- function(idxRep, idxTPt, M, avgTS, sdTS) {
  if (sdTS[idxTPt] == 0) { return(0) }
  (M[idxRep, idxTPt] * sdTS[idxTPt]) + avgTS[idxTPt]
}
denorm <- Vectorize(denormObs, vectorize.args = c('idxRep', 'idxTPt'))

# Denormalise replicated time series
# 
# repTS   - has n time series with m obs each
# avgTS   - average of time points 
# sdTS    - standard deviation of time points
#
# Return normalised replicated time series observations
denormRepTS <- function(repTS, avgTS, sdTS) {
  outer(1:nrow(repTS), 1:ncol(repTS), FUN = denorm, repTS, avgTS, sdTS)
}
denormTS <- function(ts, avgTS, sdTS) {
  denormRepTS(t(matrix(ts)), avgTS, sdTS)[1,]
}