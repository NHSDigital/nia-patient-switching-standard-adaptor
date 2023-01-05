## PS_NF_STR_2

#### Can the system process 30 batches of 12 messages submitted in parallel?

### Running Tests
* Ensure the jdni.properties file in the ConfigurationFolder is set up correctly
* Update the config.properties file with the details for the environment to execute against
* Execute the script 'run-tests.sh' and wait to complete.

By default, this test is configured to use 12 threads which will send:
* 10 Messages expected to process and completed successfully.
* 2 Messages expected to fail when processing. * 

Once all 12 threads have completed the next batch will start.  By default, this is set to run 30 times.

You will not see any update in the command line window as the messages are being sent, however the log files will be
updated as each test stage runs.

For this test plan you will the duration of each thread, and also the total run time for the thread group in the log 
file and the summary report file. You should appear in the format below under the column header 'label':

```
[1] Expected Passed Message Flow - 1997 ms
[Thread Group]: 8125 ms
```
