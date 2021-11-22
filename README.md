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

## Project structure

    .
    ├── db                          # Dockerfile and scripts for local database setup
    ├── db-connector                # Common module used by gp2gp-translator and gpc-api-facade
    ├── gp2gp-translator            # GP2GP Translator
    ├── gpc-api-facade              # GPC API Facade
    └── mhs-adaptor-mock            # Dockerfile and required files for mock of MHS Adaptor

## How to start local environment
1. Export following env variables:
    - PS_DB_URL: Database URL required to run migrations (for local environment set 'jdbc:postgresql://localhost:5436/patient_switching')
    - POSTGRES_PASSWORD: Password to be set for default postgres user. 
      This user is used to run the init script on the database. It is required during database creation.
    - PS_DB_OWNER_NAME: Username of user used to run migrations.
    - PS_DB_OWNER_PASSWORD: Password to be set for the user used to run migrations.
    - GPC_USER_DB_PASSWORD: Password for the user connecting to the database in the GPC API Facade module.
    - GP2GP_USER_DB_PASSWORD: Password for the user connecting to the database in the GP2GP Translator module.
2. Start the database by running 
    ```shell script
    docker-compose up
    ```
   command in the root directory.
3. Run migrations by executing
    ```shell script
    ./gradlew update
    ```
    command in db-connector folder.
4. Start GPC Api Facade application in IntelliJ
5. Start GP2GP Translator application in IntelliJ

### Licensing
This code is dual licensed under the MIT license and the OGL (Open Government License).
Any new work added to this repository must conform to the conditions of these licenses.
In particular this means that this project may not depend on GPL-licensed or AGPL-licensed libraries,
as these would violate the terms of those libraries' licenses.

The contents of this repository are protected by Crown Copyright (C).
