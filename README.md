# nia-patient-switching-standard-adaptor
National Integration Adaptor - Patient Switching Standard Adaptor

Incumbent providers (e.g. TPP, EMIS, SystemOne) in order to deploy GP2GP Adaptor in their infrastructure
to support losing practice scenario - i.e. whereby a different practice transfers patient data from the incumbent
would have to make changes to their GP Connect interface implementations.
In particular, they would need to implement 1.6.0 version that is required by the GPC Consumer and GP2GP adaptors. 
This business case is not always easy to be accepted by the incumbent providers, as they would have to invest time to make those changes.

The motivation for the Switching Standard Adaptor is to remove the dependency from incumbent providers to do that work.
The idea is to build an adaptor that could be installed and configured in a New Market Entrant (NME) infrastructure,
and could work with the incumbent’s GPC < 1.6.0.

Adaptor consists of two main components:
- GPC API Facade
- GP2GP Translator
They are Java Spring Boot applications, released as separate docker images.

## Requirements:

* JDK 17 - We develop the adaptor in Java with Spring Boot
* Docker

## Project structure

    .
    ├── db                          # Dockerfile and scripts for local database setup
    ├── snomed-database-loader      # Scripts loading Snomed CT codes into database
    ├── common                      # Common module used by gp2gp-translator, gpc-api-facade and db-connector
    ├── db-connector                # Common module used by gp2gp-translator and gpc-api-facade, used for db-related classes
    ├── gp2gp-translator            # GP2GP Translator
    ├── gpc-api-facade              # GPC API Facade
    └── mhs-adaptor-mock            # Dockerfile and required files for mock of MHS Adaptor

## Snomed CT Database
Please make sure to load the latest release of Snomed CT UK Edition. See [snomed-database-loader](https://github.com/NHSDigital/nia-patient-switching-standard-adaptor/tree/main/snomed-database-loader) for more information.

## Local development
### How to start local environment
1. Go to `docker` directory
2. Create a copy of `example.vars.sh`, name it `vars.sh`
3. Fill in the passwords inside `vars.sh` file:
   - POSTGRES_PASSWORD: Password to be set for the user used to run migrations. It will also be the password for the default postgres user.
   - GPC_FACADE_USER_DB_PASSWORD: Password for the user connecting to the database in the GPC API Facade module.
   - GP2GP_TRANSLATOR_USER_DB_PASSWORD: Password for the user connecting to the database in the GP2GP Translator module.
   
   There is an option to set following env variables when needed:
   - PS_DB_URL: Database URL required to run migrations (for local environment set 'jdbc:postgresql://localhost:5436/patient_switching')
   - PS_DB_OWNER_NAME: Username of user used to run migrations.
   - POSTGRES_PASSWORD: Password to be set for the user used to run migrations. Also set for the default postgres user.
     This user is used to run the init script on the database. It is required during database creation.
   - GPC_FACADE_USER_DB_PASSWORD: Password for the user connecting to the database in the GPC API Facade module.
   - GP2GP_TRANSLATOR_USER_DB_PASSWORD: Password for the user connecting to the database in the GP2GP Translator module.
   - PS_AMQP_BROKER: Address of the broker with the pss queue
   - MHS_AMQP_BROKER: Address of the broker with the mhs queue
   - PS_QUEUE_NAME: Name of the pss queue
   - MHS_QUEUE_NAME: Name of the mhs queue
   - PS_AMQP_MAX_REDELIVERIES: How many times message should be retried in case of fail on pss queue
   - MHS_AMQP_MAX_REDELIVERIES: How many times message should be retried in case of fail on mhs queue
   - GPC_FACADE_SERVER_PORT: port of the GPC API Facade application
   - GP2GP_TRANSLATOR_SERVER_PORT: port of the GP2GP Translator application
   - MHS_BASE_URL: base URL of the MHS Adapter
   - SSL_ENABLED: is SS: enabled (default is false)
   - KEY_STORE: path to the keystore
   - KEY_STORE_PASSWORD: keystore password
   - KEY_PASSWORD: server private key password
   - TRUST_STORE: path to the truststore
   - TRUST_STORE_PASSWORD: truststore password
   
   The following variables are used determine if a migration has timed out:
   - SDS_BASE_URL: url of the SDS FHIR API (default is the Production environment)
   - SDS_API_KEY: authentication for the SDS FHIR API
   - TIMEOUT_EHR_EXTRACT_WEIGHTING: weighting factor to account transmission delays and volume throughput times of the RCMR_IN030000UK06 message (default is 1).  
   - TIMEOUT_COPC_WEIGHTING: weighting factor to account transmission delays and volume throughput times of the COPC_IN000001UK01 message (default is 1).
   - TIMEOUT_CRON_TIME: cron schedule for the timeout check (default is every six hours)
   - TIMEOUT_SDS_POLL_FREQUENCY: The frequency SDS is polled for message persist durations (default is every 3 cron jobs).

   If you plan to use external queues (like ActiveMQ on AWS), you also need to set credentials for those queues:
   - PS_AMQP_USERNAME
   - PS_AMQP_PASSWORD
   - MHS_AMQP_USERNAME
   - MHS_AMQP_PASSWORD
   

3. Run `start-local-environment.sh` script:
   ```shell script
    ./start-local-environment.sh
   ```
   It will execute following steps:
   - create a default postgres database and patient_switching database,
   - start MHS Adaptor mock,
   - start ActiveMQ,
   - run migrations,
   - build and start GPC Api Facade service,
   - build and start GP2GP Translator application.
   All components will run in Docker.

4. Follow the `README` in `snomed-database-loader` directory to load Snomed CT into database 
### Rebuilding services
To rebuild the GPC Api Facade run
```shell script
 ./rebuild-and-restart-gpc-facade.sh
```

To rebuild the GP2GP Translator run
```shell script
 ./rebuild-and-restart-gp2gp-translator.sh
```

To clean all containers run
```shell script
 ./clear-docker.sh
```
### When Using Windows Machine
Before cloning the project, navigate to:
1. Go to `user` directory (e.g: C:\Users\UserName)
2. Open the file `.gitconfig` in a text editor
3. add the following line to the end of the script:
   ```
       [core]
               autocrlf = input
   ```
   the file should look like the following:

   ```
       [user]
               name = user name
               email = useremail@gmail.com
       [core]
               autocrlf = input
   ```
   NOTE: These steps should be done before cloning the project.
   If the project has already been clone, it should be deleted and cloned again


4. install WSL2 and Ubuntu Terminal (You can follow a tutorial or follow these steps)
   1. open powershell as a root user
   2. run to install wls for the first time
      ```
      wsl --install
      ```
   3. run to check the version
      ```
      wsl -l -v
      ```
   4. then Run (depending on versions. need to check which versions are in version 1 and se to version 2)
      ```
      wsl --set-default-version 2
      ```
   5. then Run (depending on versions. need to check which versions are in version 1 and se to version 2)
       ```
         wsl -set-version ubuntu 2
       ```
   6. then Run
         ```
         wsl --instal -d ubuntu
         ```
   7. Download Ubuntu Terminal from microsoft store


5. once installed WSL2 and Ubuntu Terminal are installed,
   open Ubuntu Terminal and Run the following commands:
      ```
      wsl --instal -d ubuntu
      sudo apt update
      sudo apt upgrade
      sudo apt install bpython
      bpython
      ```

6. once installed WSL2 and Ubuntu Terminal need to configure JAVA_HOME:
   run in order (in the ubuntu terminal)

   ```
   sudo apt install default-jdk
   sudo apt update
   ```
   ```
   6. nano ~/.bashrc
   ```
   The command above will open a file. Add the following at the end of the script

      ```
      JAVA_HOME=$(dirname $( readlink -f $(which java) ))
      JAVA_HOME=$(realpath "$JAVA_HOME"/../)
      export JAVA_HOME
      ```
      ```
   7. sudo update-alternatives --config java
   8. sudo apt update
   ```
7. to install unzip run in the Ubuntu Terminal
   ```
   sudo apt-get install unzip
   ```
8. to install postgresql in the Ubuntu Terminal:
   ```
   sudo apt install postgresql postgresql-contrib
   ```

9. WSL needs to be enabled in docker

   1. open docker desktop/settings/resources/WSL INTEGRATION
   2. tick the box where it says "Ubuntu" or the name of your ubuntu terminal
   
## Daisy-Chaining

It is possible to run the PS Adaptor and the [GP2GP Adaptor](https://github.com/nhsconnect/integration-adaptor-gp2gp) side by side using a single instance of the MHS Adaptor.
When using this configuration, messages received by the PS Adaptor with an unrecognised conversation ID are forwarded 
to the GP2GP Adaptor via it's inbound queue. Conversely, the default behaviour without daisy-chain enabled is to put 
messages with an unrecognised conversation ID on a dead letter queue. 

**To enable daisy-chaining the following environment variables need to be set:**

- `PS_DAISY_CHAINING_ACTIVE`: set to `true` to enable daisy-chaining - default = `false`
- `GP2GP_AMQP_BROKERS`: the location of the GP2GP Adaptors inbound queue. This should be set to the url of a single JMS broker 
(the PS Adaptor does not support concurrent GP2GP Adaptor brokers) - default = `amqp://localhost:5672`

**Optional environment variables:**

- `GP2GP_MHS_INBOUND_QUEUE`: The name of the GP2GP Adaptors inbound queue
- `GP2GP_AMQP_USERNAME`: The username for accessing the broker
- `GP2GP_AMQP_PASSWORD`: The password for accessing the broker

An example daisy chaining environment is provided in test-suite/daisy-chaining 

## Endpoints

The Patient Switching adaptors facade exposes two endpoints.

- POST /Patient/$gpc.migratestructuredrecord
- POST /$gpc.ack

### /Patient/$gpc.migratestructuredrecord

The migratestructuredrecord endpoint is the primary endpoint for the adaptor and is used to start an electronic health record transfer.
The following is required to call this endpoint...

- TO_ASID : The ASID identifier of the losing incumbent
- FROM_ASID : The ASID identifier of the winning New Market Entrant (NME)
- TO_ODS : The ODS identifier of the losing incumbent
- FROM_ODS : the ODS identifier of the winning New Market Entrant (NME)
- ConversationId : A unique GUID for each request; if you do not provide one, the adaptor will create one and return it in the response headers. It must be used for all further calls for the patient's NHS number.

The endpoint also requires a JSON body that includes the needed patient NHS number. The format of the body should look like the following...

   ```
   {
      "resourceType": "Parameters",
      "parameter": [
         {
            "name": "patientNHSNumber",
            "valueIdentifier": {
               "system": "https://fhir.nhs.uk/Id/nhs-number",
               "value": "{{nhs-number-test-group-8}}"
            }
         },
         {
            "name": "includeFullRecord",
            "part": [
               {
                  "name": "includeSensitiveInfomation",
                  "valueBoolean": true
               }
            ]
         }
      ]
   }
   ```

Endpoint calling: 

1. Initial request: If you successfully configure the endpoint described above and call it, you should receive a 202-accepted response. This means the adaptor has received the request and is making the relevant requests.
2. Polling the request: after receiving a 202 response, we recommend polling the endpoint at regular intervals using an increasing call gap strategy. Each poll can return the following responses:-
   1. 204 No content: this response indicates that we are still processing the requests / waiting for the EHR message response.
   2. 200 Success: this response indicates we have successfully received and converted the EHR to JSON; you will also receive the FHIR bundle in the response's body.
   3. 400,404,500,501: The endpoint can return all these possible error codes. These will all provide a detailed error with an operationOutcome JSON model response in the body. This looks like...

   ``` 
   {
      "resourceType": "OperationOutcome",
      "meta": {
         "profile": [
            "https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-OperationOutcome-1"
         ]
      },
      "issue": [
         {
            "severity": "error",
            "code": "exception",
            "details": {
               "coding": [
                  {
                     "system": "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1",
                     "code": "PATIENT_NOT_FOUND",
                     "display": "GP2GP - Patient is not registered at the practice"
                  }
               ]
            }
         }
      ]
   }
   ```

### /$gpc.ack

The ack endpoint is the final endpoint to call once you are happy that the EHR record you have received is acceptable.
If you do not call this endpoint after receiving an EHR from the migratestructuredrecord enpoint, then you risk the losing practise triggering off the manual postal transfer.

The following is required to call this endpoint:-

- CONVERSATION_ID: The id associated with the patient transfer request.
- CONFIRMATION_RESPONSE: you can provide the following status:-
   - ACCEPTED: This will tell the sending incumbent that you are happy with the received EHR.
   - FAILED_TO_INTEGRATE: You have encountered a problem integrating the record into your system. This will alert the sender to an error and trigger off the postal process.

Endpoint calling:

This endpoint is a fire-and-forget endpoint.
If your request is successful, you will get a 200: Success response.
If your request is unsuccessful, you will get a 500: Server error response.
If you receive a 500 response, you can retry again at any point, however, it should be noted that you must receive a 200: Success response from the migratestructuredrecord for the given conversation ID to receive a 200: Success from this endpoint.

Note: To improve reliability on this endpoint we are currently looking at a polling change, the documentation will be updated once this has been updated.

## Configuring the SNOMED Database 

### First  installation

As part of the installation of the adaptor, we do not provide the SNOMED database files as they are updated regularly under TRUD (Technology Reference Update Distribution).
To acquire the most recent SNOMED database:-

   1. Head to https://isd.digital.nhs.uk/ and create a new account.
   2. Log in
   3. Search for the following: SNOMED CT UK Monolith Edition, RF2: Snapshot (https://isd.digital.nhs.uk/trud/users/authenticated/filters/0/categories/26/items/1799/releases). We recommend the full Monolith edition, not the delta version.
   4. Subscribe to the data store.
   5. Once subscribed you will be able to download the most recent version of the SNOMED DB, at the time of writing this is release 36.0.0. (uk_sct2mo_36.0.0_20230412000001Z.zip)
   6. During the setup of the adaptor you will now need to set the environment variable “SNOMED_FILE_LOCATION” to the root location of the zip file that you’ve downloaded e.g. export SNOMED_FILE_LOCATION=“/root/uk_sct2mo_36.0.0_20230412000001Z.zip
   7. Now when you run our startup scripts for the first time, the SNOMED database will be installed for you.

### Updating the SNOMED Database

You will now receive email notifications from TRUD once the subscribed data source is updated. We recommend updating your SNOMED version as soon as you receive the notification. To do this:-

   1. Log in to https://isd.digital.nhs.uk/
   2. Download the newest version of the SNOMED Monolith edition
   3….. Update here once update path is completed.


## Licensing
This code is dual licensed under the MIT license and the OGL (Open Government License).
Any new work added to this repository must conform to the conditions of these licenses.
In particular this means that this project may not depend on GPL-licensed or AGPL-licensed libraries,
as these would violate the terms of those libraries' licenses.

The contents of this repository are protected by Crown Copyright (C).
