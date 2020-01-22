#! /bin/bash

####################################################
# Kill all processes started by many-jobs.sh
# get job ids from pids.txt file
####################################################

kill $(cat pids.txt)
