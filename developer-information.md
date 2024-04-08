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

### Perform a smoke test of the release

Deploy this commit to the AWS Path to Live environment.

1. Clicking through to the successful Jenkins build of your commit
1. Navigate to the "Push Image" section of the pipeline, looking for an entry which looks like
   ```
   docker push ...amazonaws.com/pss_gpc_facade:<TAG_NAME>
   ```
1. Make a note of the <TAG_NAME> so it can be deployed in the step below.
1. Log into the [Jenkins Terraform project][jenkins-terraform] and specify project=`nia`, Environment=`ptl`,
   component=`pss`, action=`apply`, variables=`pss_build_id=<TAG_NAME>` and click the Build button waiting
   for the build to finish successfully

[jenkins-terraform]: http://ec2-35-177-12-25.eu-west-2.compute.amazonaws.com/job/Terraform/build?delay=0sec

Perform an end to end smoke test of the adaptor by transferring the patient 9729962871 from C88046 to P83007 using the
[instructions on Confluence][e2e-ptl-test-instructions].
This patient record has:

1. An allergy to penicillin
1. A picture of the Colosseum as a document

Request the patient using the adaptor and check that the allergy is mapped into the Bundle,
and that the document has been transferred to S3.

Reject the transfer by sending a FAILED_TO_INTEGRATE response, that way we can reuse the same patient.

[e2e-ptl-test-instructions]: https://gpitbjss.atlassian.net/wiki/spaces/NIA/pages/12540018795/Testing+an+NME+winning+scenario+PS+Adaptor

### Performing the release

Make a note of the most recent Release within GitHub, and identify what the next version number to use will be.

Create a new release within GitHub, specifying the tag as the version to use (e.g. 1.2.7), and the target being the commit you identified.
Click on the "Generate release notes" button and this will list all the current changes from the recent commit.
Click "Publish Release" which will trigger a GitHub Actions job called "Push Docker Image", which will build and
push images to DockerHub.

Update the `CHANGELOG.md` file, moving the UNRELEASED entries into a line for the new release.
Raise a PR for your changes.

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

Some test tex.