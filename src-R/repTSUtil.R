#------------------------------------------------------------------------------
# We are dealing with replicated time series. Technically these are
# normal seasonal time series which gaps. A good example for a replicated
# time series observations are time arrivals at a tube station between
# 9 - 11am for dates 11/05/2013 - 21/05/2013. We refer each consecutive time
# interval in the replicated series as a repl(ication). A time point refers to a
# specific time such that for a replicated time series with n replications we
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
  avgTs <- apply(repTS, 2, mean)
  avgTs2 <- rollapply(avgTs, 3, function(x) c(1, 1, 1) %*% x / 3)
  avgTs <- c(avgTs[1], avgTs2, avgTs[length(avgTs)])
  sdTs <- apply(repTS, 2, sd)
  sdTs2 <- rollapply(sdTs, 3, function(x) c(1, 1, 1) %*% x / 3)
  sdTs <- c(sdTs[1], sdTs2, sdTs[length(sdTs)])
  list(avgTs = avgTs, sdTs = sdTs)
}

# Normalise an observation using its respective time point avg and std
#
# idxRep - index of replication in time series
# idxTp  - index of time point in time series
# M      - matrix storing replicated time series
# avgTs  - average observation by time point
# sdTs   - standard deviation by time point
#
# Return normalised M[idxRep, idxTp]
normObs <- function(idxRep, idxTp, M, avgTs, sdTs) {
  if (sdTs[idxTp] == 0) { return(M[idxRep, idxTp]) }
  (M[x, y] - avgTs[y]) / sdTs[y]
}
normRep <- Vectorize(normObs, vectorize.args = c('x', 'y'))

# Normalise replicated time series
# 
# repTS   - has n time series with m obs each
# avgTs   - average of time points 
# sdTs    - standard deviation of time points
#
# Return normalised replicated time series observations
normRepTS <- function(repTS, avgTS, sdTS) {
  outer(1:nrow(repTS), 1:ncol(repTS), FUN = normRep, repTS, avgTs, sdTs)
}

# Denormalise an observation using its respective time point avg and std
#
# idxRep - index of replication in time series
# idxTp  - index of time point in time series
# M      - matrix storing replicated time series
# avgTs  - average observation by time point
# sdTs   - standard deviation by time point
#
# Return denormalised M[idxRep, idxTp]
denormObs <- function(idxRep, idxTp, M, avgTs, sdTs) {
  if (sdTs[idxTp] == 0) {return(M[idxRep, idxTp])}
  (M[idxRep, idxTp] * sdTs[idxTp) + avgTs[idxTp]
}
denormRep <- Vectorize(denormObs, vectorize.args = c('x', 'y'))

# Denormalise replicated time series
# 
# repTS   - has n time series with m obs each
# avgTs   - average of time points 
# sdTs    - standard deviation of time points
#
# Return normalised replicated time series observations
denormRepTS <- function(repTS, avgTS, sdTS) {
  outer(1:nrow(repTS), 1:ncol(repTS), FUN = denormRep, repTS, avgTs, sdTs)
}