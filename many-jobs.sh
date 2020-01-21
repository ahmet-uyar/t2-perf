#! /bin/bash

####################################################
# Run many jobs
####################################################

if [ $# -ne "1" ]; then
  echo "Please provide following parameters: delaysDir"
  exit 1
fi

delaysDir=$1
# create directory if not exist
mkdir $delaysDir 2>/dev/null

jobs=4
workersPerJob=4

rm pids.txt 2>/dev/null
rm jobids.txt 2>/dev/null

for ((i=0; i<jobs ;i++)); do
  jobName="j${i}"

  # start job in background
  ./run-job.sh $jobName $workersPerJob $jobs $delaysDir &
  echo "$!" >> pids.txt
done

wait

java -cp target/t2-perf-1.0.jar t2.ManyJobDelays $delaysDir
