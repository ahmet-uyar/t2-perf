#! /bin/bash

for i in {1..1023}; do
  result=$(grep  "joined the job" logs/au-kk-hhogqx6.log | grep $i)
  if [ -z "$result" ]; then echo $i did not join; fi
done
