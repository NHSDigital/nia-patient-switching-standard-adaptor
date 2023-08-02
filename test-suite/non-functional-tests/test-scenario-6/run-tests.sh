#!/bin/bash
sed -i "s|{{facade-port}}|$FACADE_PORT|g" ../../jmeter-config/config.properties  \
    && sed -i "s|{{facade-url}}|$FACADE_URL|g" ../../jmeter-config/config.properties \
    && sed -i "s|{{inbound-port}}|$INBOUND_PORT|g" ../../jmeter-config/config.properties \
    && sed -i "s|{{inbound-url}}|$INBOUND_URL|g" ../../jmeter-config/config.properties

start=$(date "+%y%m%d-%H%M%S")
echo "Executing tests ${start}..."

JVM_ARGS="-Xms1024m -Xmx2048m" jmeter -p  ../../jmeter-config/config.properties -n  \
  -D javax.net.ssl.keyStore=../../jmeter-config/certs/jmeterkeystore.p12 -D javax.net.ssl.keyStorePassword=password  \
  -t ../../jmeter-config/testplan.jmx -l ../../jmeter-results/jmeter.log

finish=$(date "+%y%m%d-%H%M%S")
echo "Tests completed ${finish}"

echo "Log File  /results/jmeter.log"