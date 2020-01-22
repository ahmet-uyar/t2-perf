#! /bin/bash

####################################################
# Kill all submitted jobs
# get job ids from jobids.txt file
####################################################

cat jobids.txt | while read line
do
   echo Killind the job: $line
   twister2-0.5.0-SNAPSHOT/bin/twister2 kill kubernetes $line
done