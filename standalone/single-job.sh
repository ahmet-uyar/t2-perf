#! /bin/bash

T2_DIR=/home/ubuntu/twister2/twister2-0.6.0-SNAPSHOT

if [ $# -ne "2" ]; then
  echo "Please provide following parameters: jobName numberOfWorkers"
  exit 1
fi

jobName=$1
workers=$2

logsDir=logs
# create directory if not exist
mkdir $logsDir 2>/dev/null

logFile=${logsDir}/${jobName}.log

# submit the job
# print logs to both console and the logFile
$T2_DIR/bin/twister2 submit standalone jar ../target/t2-perf-1.0.jar standalone.SingleJobWorker $jobName $workers 2>&1 | tee ${logFile}

if [ $? -ne 0 ]; then
  echo "Job did not complete successfully. Exiting..."
  exit 1
fi

# calculate delays
java -cp ../target/t2-perf-1.0.jar standalone.MPIDelays $logFile
