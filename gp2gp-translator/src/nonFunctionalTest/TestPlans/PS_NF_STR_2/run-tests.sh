#!/bin/bash

now=$(date "+%y%m%d-%H%M%S")
echo "Executing tests ${now}..."
../../apache-jmeter-5.5/bin/jmeter.sh -p  ../../TestPlans/PS_NF_STR_2/config.properties -n \
 -t ../../TestPlans/PS_NF_STR_2/PS_NF_STR_2.jmx -l ../../Results/PS_NF_STR_2/jmeter-log-"$now".log \
 -o ../../Results/PS_NF_STR_2/ResultsDashboard-"$now"
echo "Tests completed"
../../apache-jmeter-5.5/bin/jmeter.sh -g ../../Results/PS_NF_STR_2/jmeter-log-"$now".log -o ../../Results/PS_NF_STR_2/report-"$now"
echo "HTML Report generated"