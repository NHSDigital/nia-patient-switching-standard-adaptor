## Test Scenario 3

20 batches of 12 patient transfers x 3 threads

### Test details

720 Transfers requested - 600 expected to succeed, 120 expected to fail, each successful transfer contains:
* 1 RCMR_IN030000UK06 message
* 1 COPC_IN000001UK01 index message
* 2 COPC_IN000001UK01 Fragment messages

### Prerequisites 

You will need to provide the certificate and key required to connect to the inbound service.
* Copy the certificate and key files into the /testPlans/certs folder named 'client.crt' and 'client.key' respectively.
* Ensure that variables in the .env file have been set correctly

### Running Tests

This will build a docker container in the 'nia-ps' network and will execute the tests against the docker test-suite.
Once completed it will copy the docker logs to /test-scenario-1/docker.logs and remove the test container.

To start the tests, run the following: 
```
./start-docker.test.sh
```

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