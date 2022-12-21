echo "Current working directory: "
pwd
echo
echo "Downloading JMeter..."
curl -O https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.5.tgz
echo "JMeter Downloaded."
echo
echo "Downloading Qpid Jms Client..."
curl -O https://archive.apache.org/dist/qpid/jms/1.3.0/apache-qpid-jms-1.3.0-bin.tar.gz
echo "Downloaded Qpid Jms Client."
echo
echo "Extracting JMeter..."
tar -xvf apache-jmeter-5.5.tgz
echo "Extracted JMeter."
echo
echo "Extracting required .jar files from Qpid Jms Client..."
tar -xvzf apache-qpid-jms-1.3.0-bin.tar.gz --strip-components 2 -C ./apache-jmeter-5.5/lib  --exclude='lib/optional*' '*.jar'
echo "Extracted required .jar files from Qpid Jms Client."
echo
echo "Cleaning up..."
rm apache-jmeter-5.5.tgz
rm apache-qpid-jms-1.3.0-bin.tar.gz
echo "Setup completed."