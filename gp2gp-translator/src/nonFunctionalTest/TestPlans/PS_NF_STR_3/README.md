## PS_NF_STR_2

#### Can the system process 20 batches of 12 messages submitted in parallel using 3 threads/instances?

Please note that at present this test will only run on a local test environment due to the requirement
to spin up extra instances of the GP2GP adaptor.

### Preparing Tests

* Ensure that the docker (in the root of the project) folder contains a valid vars.sh.
* These settings will be used to create the extra instances of the GP2GP Adaptor.
* Execute the script: setup-test-environment.sh.
* 

### Running Tests
* Ensure the jdni.properties file in the ConfigurationFolder is set up correctly.
* Update the config.properties file with the details for the environment to execute against.
* Execute the script 'run-tests.sh' and wait to complete.

By default, this test is configured to use 12 threads which will send:
* 10 Messages expected to process and completed successfully.
* 2 Messages expected to fail when processing.

Once all 12 threads have completed the next batch will start.  By default, this is set to run 30 times.

You will not see any update in the command line window as the messages are being sent, however the log files will be
updated as each test stage runs.

For this test plan you will see the duration of each thread, and also the total run time for the thread group in the log 
file and the summary report file. You should appear in the format below under the column header 'label':

```
[1] Expected Passed Message Flow - 1997 ms
[Thread Group]: 8125 ms
```

Full report details will also be generated in the ../Results/PS_NF_STR_3/report-<timestamp> folder.
