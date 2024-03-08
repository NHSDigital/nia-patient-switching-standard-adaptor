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

Use this command from a terminal to run load tests:
jmeter -n -t  ./niad-3029/nia-patient-switching-standard-adaptor/test-suite/non-functional-tests/ \
test-scenario-6/testplan.jmx -q ./niad-3029/nia-patient-switching-standard-adaptor/ \
test-suite/non-functional-tests/test-scenario-6/config-template.properties -Lorg.apache.jmeter.JMeter=OFF \
-l ~/results11.jtl -e -o ./niad-3029/nia-patient-switching-standard-adaptor/test-suite/non-functional-tests/test-scenario-6/jmeter_report

To run for 2000 transfers the properties file is configured to run 400 transfers per loop for 5 loops with a ramp up time of 30 seconds.
The environment variables need to be populated with the appropriate endpoints e.g. load balancer endpoints as below:

facadePort = 8081
facadeUrl = nia-gp2gp-elb-ebd5089784c3d1ec.elb.eu-west-2.amazonaws.com
inboundUrl = nia-gp2gp-elb-ebd5089784c3d1ec.elb.eu-west-2.amazonaws.com
inboundPort = 443
protocol = http

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

