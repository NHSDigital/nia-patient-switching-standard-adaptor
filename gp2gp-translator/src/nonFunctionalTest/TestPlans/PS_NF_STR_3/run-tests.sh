#!/bin/bash

now=$(date "+%y%m%d-%H%M%S")
echo "Executing tests ${now}..."
../../apache-jmeter-5.5/bin/jmeter.sh -p  ../../TestPlans/PS_NF_STR_3/config.properties -n \
 -t ../../TestPlans/PS_NF_STR_3/PS_NF_STR_3.jmx -l ../../Results/PS_NF_STR_3/jmeter-log-"$now".log \
 -o ../../Results/PS_NF_STR_3/ResultsDashboard-"$now"
echo "Tests completed"
../../apache-jmeter-5.5/bin/jmeter.sh -g ../../Results/PS_NF_STR_3/jmeter-log-"$now".log -o ../../Results/PS_NF_STR_3/report-"$now"
echo "HTML Report generated"