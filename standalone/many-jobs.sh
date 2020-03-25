#! /bin/bash

####################################################
# Run many jobs
####################################################

if [ $# -ne "1" ]; then
  echo "Please provide following parameters: delaysDir"
  exit 1
fi

delaysDir=$1
summaryFile=${delaysDir}/summary.txt

# create directory if not exist
mkdir $delaysDir 2>/dev/null

jobs=6
workersPerJob=2

echo jobs: $jobs	workersPerJob: $workersPerJob  >> $summaryFile

rm pids.txt 2>/dev/null
rm jobids.txt 2>/dev/null

for ((i=0; i<jobs ;i++)); do
  jobName="j${i}"

  # start job in background
  ./single-job-for-many.sh $jobName $workersPerJob $delaysDir $summaryFile &
  echo "$!" >> pids.txt
done

# wait all sub processes to finish
wait
