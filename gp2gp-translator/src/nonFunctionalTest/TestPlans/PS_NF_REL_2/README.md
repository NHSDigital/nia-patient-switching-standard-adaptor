## PS_NF_REL2

#### Can the system run with a queue backlog of 1000+ messages?

* Run the above until there are 1000+ messages in the ActiveMQ queue and 1000+ uncompleted migration requests in the DB
(Stop the translator container).
* Start the container while still receiving messages.
* Make note of how long the application takes to catch up.

### Running Tests
* Ensure the jdni.properties file in the ConfigurationFolder is set up correctly.
* Update the config.properties file with the details for the environment to execute against.
* Stop the translator container.
* Execute the script 'run-tests.sh' and wait until prompted to restart the container.
* Press any key in the script window and take a note of the time.
* Monitor the queue until it is down to a low number of items. Note that it is unlikely to reach zero items in the queue
as there are continuous messages being sent and take a note of the time.
* Finally, press any key in the script window to stop the continuous messages being sent.

You will not see any update in the command line window as the messages are being sent, however the log files will be 
updated as each test stage runs.
