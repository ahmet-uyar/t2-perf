#!/bin/bash

EXTRA=5
LOGS_DIR="logs"

if [ $# -ne "4" ]; then
  echo "Please provide following parameters: jobName numberOfWorkers numberOfJobs delaysDir"
  exit 1
fi

jobName=$1
workers=$2
jobs=$3
delaysDir=$4

echo "jobName: $jobName, workers: $workers, jobs: $jobs, delaysDir: $delaysDir"

# generate jobID
jobID=$(java -cp target/t2-perf-1.0.jar:twister2-0.5.0-SNAPSHOT/lib/libapi-utils-java.jar t2.GenJobID $jobName)

echo "jobID: $jobID"
echo $jobID >> jobids.txt

# submit the job
twister2-0.5.0-SNAPSHOT/bin/twister2 submit kubernetes jar target/t2-perf-1.0.jar t2.T2JobForMulti $jobID $workers

########################################
# watch pods until all running
jobPods=$((workers + 1))

label="twister2-job-pods=t2pod-lb-${jobID}"
runningPods=$(kubectl get pods -l $label | grep Running | wc -l)

while [ $runningPods -ne $jobPods ]; do

  # sleep
  sleep 10

  # get number of Running pods
  runningPods=$(kubectl get pods -l $label | grep Running | wc -l)
  echo "Running Pods: $runningPods"
done

echo "All $runningPods pods are running."

# sleep some time for all worker logs to be ready
sleepTime=30
echo "Sleeping $sleepTime ............."
sleep $sleepTime

echo "Sleep finished. Getting logs....."

##############################################
# save log files
jobLogDir=$LOGS_DIR/${jobID}
mkdir ${jobLogDir} 2>/dev/null

# copy jobSubmitTime
mv $HOME/.twister2/${jobID}-time-stamp.txt ${jobLogDir}/jobSubmitTime.txt

# copy launch-delay.txt file
mv $HOME/.twister2/${jobID}-launch-delay.txt ${jobLogDir}/launch-delay.txt

for ((i=0; i<workers ;i++)); do
  podName=${jobID}-0-${i}
  logFile=${jobLogDir}/worker-${i}.log
  kubectl logs $podName > ${logFile}
  echo written logFile: ${logFile}
done

##############################################
# calculate delays and write to file

delayFile=${delaysDir}/${jobID}.txt
java -cp target/t2-perf-1.0.jar t2.Delays $jobLogDir > $delayFile

########################################
# watch pods until all pods running
allPods=$((jobPods * jobs + EXTRA))

runningPods=$(kubectl get pods | grep Running | wc -l)

while [ $runningPods -ne $allPods ]; do

  # sleep
  sleep 10

  # get number of Running pods
  runningPods=$(kubectl get pods | grep Running | wc -l)
  echo "Running Pods: $runningPods"
done

echo "All $runningPods pods are running."

########################################
# sleep some time and kill the job

sleepTime=50
echo "Sleeping $sleepTime ............."
sleep $sleepTime

echo "Sleep finished. killing the job ......"

twister2-0.5.0-SNAPSHOT/bin/twister2 kill kubernetes $jobID

