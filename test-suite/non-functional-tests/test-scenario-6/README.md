## Test Scenario 6

120 simultaneous transfers

### Test details

120 Transfers requested - 100 expected to succeed, 20 expected to fail, each successful transfer contains:
* 1 RCMR_IN030000UK06 message
* 1 COPC_IN000001UK01 index message
* 2 COPC_IN000001UK01 Fragment messages

### Prerequisites 

You will need to provide the certificate and key required to connect to the inbound service.
* Copy the certificate and key files into the /certs folder named 'client.crt' and 'client.key' respectively.
* Ensure that variables in the .env file have been set correctly

### Running Tests

This will build a docker container in the 'nia-ps' network and will execute the tests against the docker test-suite.
Once completed it will copy the docker logs to /test-scenario-1/docker.logs and remove the test container.

To start the tests, run the following: 
```
./start-docker.test.sh
```


### Load Testing

Used this command from a terminal to run load tests:

jmeter -n -t  ../nia-patient-switching-standard-adaptor/test-suite/non-functional-tests/ \
test-scenario-6/testplan.jmx -q ../nia-patient-switching-standard-adaptor/ \
test-suite/non-functional-tests/test-scenario-6/config-template.properties -Lorg.apache.jmeter.JMeter=OFF \
-l ~/results.jtl -e -o ../nia-patient-switching-standard-adaptor/test-suite/non-functional-tests/test-scenario-6/jmeter_report

To run for 2000 transfers the properties file is configured to run 400 transfers per loop for 5 loops with a ramp up time of 30 seconds.
The environment variables need to be populated with the appropriate endpoints e.g. load balancer endpoints as below:

facadePort = 8081
<facadeUrl = <url-of-gp2gp-translator-facade-instance>
inboundUrl = <gp2gp-inbound-instance-url>
inboundPort = 443
protocol = http

By varying the thread count and keeping the ramp up time close to zero the instantaneous load can be varied. It was found that adding 
2000 threads instantly would not reflect real life conditions as the requests would come from distributed sources not one machine. It was 
found that by spreading the load over four bursts allowed the test machine to cope with the load more easily. Hence, the threads are split into 
5 batches of 400 threads each batch. The ramp up time also helped the test machine to cope with load change more easily, it was found that 
to allow at least 30 seconds for ramp up value for the test to work with the test machine and AWS infrastructure setup.

threadCount = 400

# the amount of time between starting threads
rampUp = 30

# the number of times each thread will run
loopCount = 5

# the number of times the test will run per loop
batchCount = 1

### More Information

Each test in the non-functional tests are pre-configured via properties set within the config-template.properties.
These properties are detailed below:
* facadePort: Populated when the docker container is built by the values set in .env file
* facadeUrl: Populated when the docker container is built by the values set in .env file
* inboundUrl: Populated when the docker container is built by the values set in .env file
* inboundPort: Populated when the docker container is built by the values set in .env file
* threadCount: The number of simultaneous threads running the tests
* rampUp: The amount of time between starting threads
* loopCount: The number of times each thread will run
* batchCount: The number of times the test will run per loop

