library("assertthat")
library("zoo")

# x - incoming array
# startOffset - initial position in array
# windowSize - time in minutes that we aggregate
# freq - time in minutes that each aggregate we shift startOffset by
# fun - function to be applied to windows of size fcastFreq
#
# Return array of elements resulting from applying fun to x with windowSize
#
aggregate <- function(x, startOffset, windowSize, freq, fun) {
  #Elements in array should be multiple of freq
  assert_that((length(x) - startOffset) %% freq == 0)
  rollapply(
    tail(x, length(x) - startOffset),
    width = windowSize, by = freq, FUN = fun
  )
}

# filename - file name as string
#
# Return matrix (rows, cols) of elements in file
#
loadFile <- function(filename) {
  l <- read.delim(file = filename, header = F, sep = " ", dec = ".")
  matrix(unlist(l), ncol = dim(l)[2], byrow = F)
}

# files - name of files to be loaded as RepTS. Each file should represent
#         a single time interval. If files have multiple rows each row will
#         be treated as a separate repTS. Thus all files must have the same
#         rows and columns.
#
# Return array (rep, rows, cols). Each row is a unique repTS.
loadRepTS <- function(files) {
  data <- sapply(files, simplify="array", function(x) {
    loadFile(x)
  })
  aperm(data,c(3,1,2))
}