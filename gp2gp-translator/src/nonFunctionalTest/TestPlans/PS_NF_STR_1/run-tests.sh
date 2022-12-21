#!/bin/bash

now=$(date "+%y%m%d-%H%M%S")
echo "Executing tests ${now}..."
../../apache-jmeter-5.5/bin/jmeter.sh -p  ../../TestPlans/PS_NF_STR_1/config.properties -n -t ../../TestPlans/PS_NF_STR_1/PS_NF_STR_1.jmx -l ../../Results/PS_NF_STR_1/jmeter-log-"$now".log
echo "Tests completed"