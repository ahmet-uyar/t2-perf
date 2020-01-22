#! /bin/bash

####################################################
# Run many jobs
####################################################

if [ $# -ne "2" ]; then
  echo "Please provide following parameters: delaysDir extraPods"
  exit 1
fi

delaysDir=$1
extraPods=$2

# create directory if not exist
mkdir $delaysDir 2>/dev/null

jobs=4
workersPerJob=4

rm pids.txt 2>/dev/null
rm jobids.txt 2>/dev/null

for ((i=0; i<jobs ;i++)); do
  jobName="j${i}"

  # start job in background
  ./run-job.sh $jobName $workersPerJob $jobs $delaysDir $extraPods &
  echo "$!" >> pids.txt
done

# wait all sub processes to finish
wait

########################################
# wait until all killed
runningPods=$(kubectl get pods | grep Running | wc -l)

while [ $runningPods -ne $extraPods ]; do

  # sleep
  sleep 10

  # get number of Running pods
  runningPods=$(kubectl get pods | grep Running | wc -l)
  echo "Running Pods: $runningPods"
done

echo "Only $runningPods pods are running."
