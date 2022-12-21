#!/bin/bash

echo "Current working directory: "
pwd
echo

echo "Downloading Qpid Jms Client..."
curl -O https://archive.apache.org/dist/qpid/jms/1.3.0/apache-qpid-jms-1.3.0-bin.tar.gz
echo "Downloaded Qpid Jms Client."
echo
echo "Downloading Parallel Container Plugin..."
curl -O https://jmeter-plugins.org/files/packages/bzm-parallel-0.11.zip
echo
echo "Extracting JMeter..."
tar -xvf apache-jmeter-5.5.tgz

tar -xvzf apache-qpid-jms-1.3.0-bin.tar.gz --strip-components 2 -C ./apache-jmeter-5.5/lib  --exclude='lib/optional*' '*.jar'
echo "Extracted required .jar files from Qpid Jms Client."
echo
echo "Extracting Parallel Container Plugin..."
unzip bzm-parallel-0.11.zip -d "./apache-jmeter-5.5"
echo "Extracted Parallel Container Plugin."
echo
echo "Cleaning up..."
rm apache-jmeter-5.5.tgz
rm apache-qpid-jms-1.3.0-bin.tar.gz
rm bzm-parallel-0.11.zip
echo "Setup completed."
