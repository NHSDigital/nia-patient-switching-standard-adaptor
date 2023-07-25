## Test Scenario 1

12 patient transfers x 1 thread

### Prerequisites 

You will need to provide the certificate and key required to connect to the inbound service.
 * Copy the certificate and key files into the /testPlans/certs folder named 'client.crt' and 'client.key' respectively.
 * Ensure that varaibles in the .env file have been set correctly

### Running Tests

This will build a docker container in the 'nia-ps' network and will execute the tests against the docker test-suite.
Once completed it will copy the docker logs to /test-scenario-1/docker.logs and remove the test container.

To start the tests, run the following: 
```
./start-docker.test.sh
```
