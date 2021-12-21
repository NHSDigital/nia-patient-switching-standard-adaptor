# GPC API Facade

GPC API Facade - HTTP FHIR API that is similar to GPC 1.6.0 standard.
It enables NME to request and fetch patient data in the FHIR format.
This facade only serves light tasks, delegating all the work to the async GP2GP Translator component.

## How to run tests

### How to run unit tests:

```shell script
./gradlew test
```

### How to run all checks:

```shell script
./gradlew check
```

### How to run integration tests:

Set credentials for a database that tests can connect to, by setting env variables:
- PS_DB_URL
- GPC_FACADE_USER_NAME
- GPC_FACADE_USER_DB_PASSWORD,

or values in integrationTest/resources/application.yml file:
- spring.datasource.url
- spring.datasource.username
- spring.datasource.password

Then execute:

```shell script
./gradlew integrationTest
```

## Troubleshooting

### gradle-wrapper.jar doesn't exist

If gradle-wrapper.jar doesn't exist run in terminal:
* Install Gradle (MacOS) `brew install gradle`
* Update gradle `gradle wrapper`
