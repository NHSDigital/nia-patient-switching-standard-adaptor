## Non-Functional Testing for GP2GP Adaptor

To run the non-functional tests, you will need will first need to install JMeter and the required jdbc client. 

### Setup Script

Please execute the following script to complete the setup:

```
./setup-jmeter.sh
```

### Manual Setup

If you wish to set up manually instead of running the script, please follow the steps below:

* Download Apache JMeter:
```
curl -O https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.5.tgz
```
* Download Qpid client:
```
curl -O https://archive.apache.org/dist/qpid/jms/1.3.0/apache-qpid-jms-1.3.0-bin.tar.gz
```
* Extract the downloaded jmeter archive into the current directory:
```
tar -xvf apache-jmeter-5.5.tgz
```

* Extract the jar files from Qpid Client archive into the jmeter '/lib' directory.  
You do not need to copy the jar files from '/lib/optional' in the source archive:
```
tar -xvzf apache-qpid-jms-1.3.0-bin.tar.gz --strip-components 2 -C ./apache-jmeter-5.5/lib  --exclude='lib/optional*' '*.jar'
```

The downloaded archives can now be deleted.

## JDNI Configuration

Within the 'Configuration' folder you will find a configuration file called 'jndi.properties'.
In that file you will need to set the ConnectionFactory, which is the Url to the message queue.
For example if you are running locally:
```
connectionfactory.qpidConnectionFactory = amqp://localhost:5672
```

The queue names can also be configured by changing 'queue.pssQueueName' and 'queue.mhsQueueName' from the default values.

## Test configuration 
Within the 'Configuration' folder you find a configuration file for each test called '<Test Name>-config.properties'.
The following configuration options can be set in here.

### GPC Facade
* facadeProtocol - the protocol to connect to facade (i.e http, https)
* facadeServerName - the server name or IP address of the facade (i.e. localhost, 127.0.0.1)
* facadePort - the port used by the facade (i.e. 8081)
* jdniPropertiesPath - the path of the jdniProperties file to use. You should not need to change this value

### Thread Group Properties
* threadCount - the number of threads to use in the test (i.e. 5)
* rampUp - the amount of time it will take Jmeter to add all threads to the execution plan
* loopCount - the number of times each thread will run. To run indefinitely set this value to -1.

For example:
* 100 target threads with 100 seconds ramp-up: JMeter will add one user each second
* 100 target threads with 10 seconds ramp-up: JMeter will add 10 users each second
* 100 target threads with 5 seconds ramp-up: JMeter will add 20 users each second

Note that from some tests the script is set to run with a thread lifetime, this is not configurable through properties.
To change these open the test file in JMeter and control the settings in the thread group (marked with ⚙️)

### EHR Message Header Properties
These contain the Asid and Ods values sent in EHR message.  These should not need to be changed.

### Message Type Properties
There are 3 types of message that can be sent:
* 0: RCMR_IN030000UK06 - Valid
* 1: RCMR_IN030000UK06 - Invalid
* 2: COPC_IN000001UK01

To change the test to send just one type of message set the 'fixedMessageType' to the number of that message.
For example to send just 'RCMR_IN030000UK06 - Valid' messages set the following:
```
fixedMessageType = 0
```

## Running the tests
Within the 'TestPlans' folder for each test there will be a folder with the test name.
Contained within this folder will be a set of files:
* <Test Name>.jmx - This is the test plan and can be opened and viewed in JMeter UI.
* config.properties - This is configuration file for this test.
* run-tests.sh - The script to execute the test plan using the provided configuration.

There may also be a README.md file if the test require any special steps such as needing to be run in stages.
There may also be multiple test plans if it is a multipart test.

To run the tests simple navigate to the required test folder and execute the following:
```
./run-tests.sh
```

To stop the test execution part way through you will need to execute the script 'shutdown.sh'.
This is located within the 'apache-jmeter-5.5/bin' folder.

## Result Output
Each time the tests are run they will output to files into a folder named 'results' within the 'nonFunctionalTest'
folder.  Two files are prefixed with either 'AssertionResults' or 'SummaryReport' and are suffixed with the current 
datetime.  the other file is the jmeter log output, again prefixed by the date.

The AssertionResult file will contain the results of the assertions made in the tests.  Any marked as FALSE will have
failed.
The SummaryReport file contains the details breakdown including timings of requests made.

The summary report can be loaded back into Jmeter through an appropriate sampler to summarise the data in various ways.

## JMeter UI
Note the tests can be executed through JMeter UI, but it is not recommend to do so unless debugging.
To see the test running disable the step 'View Results Tree'.
It is essential that you disable this step again before running through the command line or you may experience
performance issues.
