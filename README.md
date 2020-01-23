# t2-perf
Twister2 Performance Tests in Kubernetes Clusters

Get Twister2 package twister2-0.5.0-SNAPSHOT.tar.gz and unpack it
```bash
tar xf twister2-0.5.0-SNAPSHOT.tar.gz
```

Compile the project 
```bash
mvn package
```
Or you can execute ./compile.sh

## Startup Delay Tests fo Multiple Concurrent Twister2 Jobs
We measure three types of delays: 
* Pod Start Delay: The amount of time it takes from job submission to pod start.
* Twister2 Worker Delay: The amount of time it takes from job submission to worker start
* All Workers Join Delay: The amount of time it takes from job submission to all workers join the job.

## Classes
* t2.SimpleWorker: A simple worker that logs time stamps only. 
* t2.T2JobForMulti: A Job class that submits Twister2 job. 

## Prerequisites
We assume that Kubernetes cluster is installed and kubectl is configured to access the cluster. We also modified Twister2 source code to log pod start times in a branch. 

## Submitting Jobs
You can submit many concurrent jobs by using: 
```text
many-jobs.sh
```
You need to update this file and update **jobs** and **workersPerJob** variables. 

When you run this script, 
* It submits that many jobs in separate scripts using run-job.sh script. 
* It gets the logs of all workers when they started. 
* It calculates delays for each worker and also prepares summaries. 
* It kills all started jobs. 
