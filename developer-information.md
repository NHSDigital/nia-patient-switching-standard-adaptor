# Developer Information

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
Please make sure to load the latest release of Snomed CT UK Edition. See [Configuring the SNOMED Database](./OPERATING.md#populating-the-snomed-database) and [snomed-database-loader](/snomed-database-loader/README.md) for more information.

## Local development
### How to start local environment
1. Go to `docker` directory
2. Create a copy of `example.vars.sh`, name it `vars.sh`
3. Fill in the passwords inside `vars.sh` file:
    - POSTGRES_PASSWORD: Password to be set for the user used to run migrations. It will also be the password for the default postgres user.
    - GPC_FACADE_USER_DB_PASSWORD: Password for the user connecting to the database in the GPC API Facade module.
    - GP2GP_TRANSLATOR_USER_DB_PASSWORD: Password for the user connecting to the database in the GP2GP Translator module.

    For the description and purpose of other environment variables, refer to the [end user OPERATING guidance](OPERATING.md#environment-variables).


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

## Releasing a new version to Docker Hub

First identify which is the most recent commit within GitHub which contains only changes which are marked as Done within Jira.
You can also review what commits have gone in by using the git log command or IDE.

Make a note of the most recent Release within GitHub, and identify what the next version number to use will be.

Create a new release within GitHub, specifying the tag as the version to use (e.g. 1.2.7), and the target being the commit you identified.
Click on the "Generate release notes" button and this will list all the current changes from the recent commit.

From the root of this repository, update the `/release-scripts/release.sh`, changing the `BUILD_TAG` value to match the release created above.
Update the `CHANGELOG.md` file, moving the UNRELEASED entries into a line for the new release.
Raise a PR for your changes.

Once your changes have been merged, log into DockerHub using the credentials stored within our AWS accounts Secrets Manager, secret name `nhsdev-dockerhub-credentials` in London region.
Go to AWS Management Console > Secrets Manager then find the option 'retrieve keys'.

If you have not created a release before then you will first need to create a new docker builder instance using `docker buildx create --use`.

Execute `./release.sh`.

Log out of DockerHub.

## Rebuilding services
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
## Getting started for Windows Users
A setup guide is provided for Windows users [here](./getting-started-with-windows.md)