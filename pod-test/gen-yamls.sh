#!/bin/bash

# create target directory if not exist
mkdir $4 2>/dev/null

java -cp ../target/t2-perf-1.0.jar plainpods.GeneratePodYamls $1 $2 $3 $4
