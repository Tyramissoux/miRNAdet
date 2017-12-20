#!/bin/bash


#script takes input fasta file, redirects it to UNAfold, redirects that output to Ct2b which then ends up in a file defined as second parameter

#inputFile =$1
#outputFile = $2
#temp = $3

perl /usr/local/bin/UNAFold.pl -P100 -t20 $1 
