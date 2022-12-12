## PS_NF_REL1

Details of the specification for this test are below:

* Can the system run for more than 24 hours under a consistent load?
* Configure a local environment
* Develop a script to randomly generate messages and place them in the activeMQ queue
(A range of data would be ideal including COPC messages).
* Run for 24 hours monitoring for any fail-overs.
* Repeat in a cloud infrastructure monitoring for any fail overs.

### Running Tests
* Ensure the jdni.properties file in the ConfigurationFolder is set up correctly
* Update the config.properties file with the details for the environment to execute against
* Execute the script 'run-tests.sh' and wait to complete.

By default, this test is configured to use 5 threads, each running for 24 hours.
You will not see any command line output at this time, however the log files will be updated as each test stage runs.
