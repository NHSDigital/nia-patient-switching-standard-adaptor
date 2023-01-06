#!/bin/bash

now=$(date "+%y%m%d-%H%M%S")
echo "-------------------------------------------------------------------------------------"
echo "Before starting this test, please ensure that the translator container is not running"
echo "-------------------------------------------------------------------------------------"
echo
echo "******** Press any key to send messages to the queue to establish a backlog *********"
read -r -n 1
../../apache-jmeter-5.5/bin/jmeter.sh -p  ../../TestPlans/PS_NF_REL_2/config_SendInitialMessages.properties -n \
 -t ../../TestPlans/PS_NF_REL_2/PS_NF_REL_2.jmx -l ../../Results/PS_NF_REL_2/SendInitialMessages_jmeter-log-"$now".log
echo "[INFO]: Initial Messages have been sent to queue."
echo "[INFO]: Sending continuous messages to queue"
../../apache-jmeter-5.5/bin/jmeter.sh -p  ../../TestPlans/PS_NF_REL_2/config_SendContinuousMessages.properties -n \
 -t ../../TestPlans/PS_NF_REL_2/PS_NF_REL_2.jmx \
 -l ../../Results/PS_NF_REL_2/SendContinuousMessages_jmeter-log-"$now".log
echo
echo "-------------------------------------------------------------------------------------"
echo " Restart the translator container and take a note of the time, and messages in queue "
echo "-------------------------------------------------------------------------------------"
echo
echo "******** Press any key to stop continuous messages when backlog has cleared *********"
echo "When the backlog has cleared Press any key to shutdown the running jmeter tests"
read -r -n 1
../../apache-jmeter-5.5/bin/shutdown.sh
