#------------------------------------------------------------------------------
# Routines for loading time series and replicated time series
#------------------------------------------------------------------------------
library("assertthat")

# filename - filename of time series file as string
#
# Return matrix (rows, cols) of elements in file
#
loadTS <- function(filename) {
  l <- read.delim(file = filename, header = F, sep = " ", dec = ".")
  matrix(unlist(l), ncol = dim(l)[2], byrow = F)
}

# files - list of files to be loaded as RepTS. Each file should represent
#         a single time interval. If files have multiple rows each row will
#         be treated as a separate repTS. Thus all files must have the same
#         rows and columns.
#
# Return array (rep, rows, cols). Each row is a unique repTS.
loadRepTS <- function(files) {
  aperm(sapply(files, simplify = "array", loadTS), c(3,1,2))
}