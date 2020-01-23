#!/bin/bash

if [ $# -ne "2" ]; then
  echo "Please provide following parameters: yamlDirecory logsDir"
  exit 1
fi

dir="./${1}"
logsDir=$2

# create logs directory if not exist
mkdir $logsDir 2>/dev/null

pods=$(ls -1q $dir | wc -l)
echo "Number of pods to be created: $pods"

# delete last char if it is slash
if [[ "$dir" == */ ]]; then
  dir=${dir::-1}
fi

# save job submit time
echo $(date +%s%3N) > ${logsDir}/job-submit-time.txt

kubectl apply -f $dir

########################################
# wait until all Running
runningPods=$(kubectl get pods -l app=t2-pod | grep Running | wc -l)

while [ $runningPods -ne $pods ]; do

  # sleep
  sleep 3

  # get number of Running pods
  runningPods=$(kubectl get pods -l app=t2-pod | grep Running | wc -l)
  echo "Running t2-pods: $runningPods"
done

echo "All $runningPods t2-pods are running."

########################################
# wait and get log files

echo Sleeping 30 seconds
sleep 30

podNames=$(kubectl get pods -l app=t2-pod --output=jsonpath={.items..metadata.name})
for pn in $podNames; do
  kubectl logs $pn > ${logsDir}/${pn}.log
  echo written log file: ${logsDir}/${pn}.log
done

########################################
# calculate delays and write to file
java -cp ../target/t2-perf-1.0.jar plainpods.Delays $logsDir $logsDir

########################################
# delete all pods

echo deleting all pods ..............
kubectl get pods -l app=t2-pod --output=jsonpath={.items..metadata.name} | xargs kubectl delete pod
