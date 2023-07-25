#!/bin/bash

now=$(date "+%y%m%d-%H%M%S")
echo "Executing tests ${now}..."
jmeter.sh -p  ../../jmeter-config/config.properties -n  \
  -D javax.net.ssl.keyStore=../../jmeter-config/certs/jmeterkeystore.p12 -D javax.net.ssl.keyStorePassword=password  \
  -t ../../jmeter-config/testplan.jmx -l ../../jmeter-results/jmeter.log
echo "Tests completed"
echo "Log File  /results/jmeter-log"