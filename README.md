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
    ├── db-connector                # Common module used by gp2gp-translator and gpc-api-facade
    ├── gp2gp-translator            # GP2GP Translator
    ├── gpc-api-facade              # GPC API Facade
    └── mhs-adaptor-mock            # Dockerfile and required files for mock of MHS Adaptor

## Local development
### How to start local environment
1. Go to `docker` directory
2. Fill in the passwords inside `vars.sh` file:
   - POSTGRES_PASSWORD: Password to be set for default postgres user.
     - This user is used to run the init script on the database. It is required during database creation.
   - PS_DB_OWNER_PASSWORD: Password to be set for the user used to run migrations.
   - GPC_FACADE_USER_DB_PASSWORD: Password for the user connecting to the database in the GPC API Facade module.
   - GP2GP_TRANSLATOR_USER_DB_PASSWORD: Password for the user connecting to the database in the GP2GP Translator module.
   
   There is an option to set following env variables when needed:
   - PS_DB_URL: Database URL required to run migrations (for local environment set 'jdbc:postgresql://localhost:5436/patient_switching')
   - POSTGRES_PASSWORD: Password to be set for default postgres user.
     This user is used to run the init script on the database. It is required during database creation.
   - PS_DB_OWNER_NAME: Username of user used to run migrations.
   - PS_DB_OWNER_PASSWORD: Password to be set for the user used to run migrations.
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
 ./clean-docker.sh
```

### Licensing
This code is dual licensed under the MIT license and the OGL (Open Government License).
Any new work added to this repository must conform to the conditions of these licenses.
In particular this means that this project may not depend on GPL-licensed or AGPL-licensed libraries,
as these would violate the terms of those libraries' licenses.

The contents of this repository are protected by Crown Copyright (C).
