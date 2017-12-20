#!/bin/bash
perl /usr/local/bin/melt.pl $1 | tee $2
perl /usr/local/bin/UNAFold.pl $1
perl /usr/local/bin/Ct2B.pl $1.ct | tee -a $2
