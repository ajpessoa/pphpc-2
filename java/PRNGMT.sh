#!/bin/bash

configurations=("config25v1" "config25v2" "config50v1" "config50v2" "config200v1" "config200v2"  "config400v1" "config400v2" "config800v1" "config800v2" "config1600v1" "config1600v2" "config3200v1" "config3200v2" "config6400v1" "config6400v2" "config12800v1" "config12800v2")
thread_counts=(8 12 16 24 32 64)
runs=30

mkdir AES-Results

for configuration in "${configurations[@]}"
  do
    for threads in "${thread_counts[@]}"
    do
      echo "Running AES with configuration $configuration and $threads threads"
      for (( i=1; i<=$runs; i++ ))
      do
        name="AES_$configuration-$threads-$i.txt"
        sh ./pp.sh -p ../configs/$configuration.txt -ps EX -n $threads -g AES -s AES-Results/$name
      done
    done
  done