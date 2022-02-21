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
*Integration tests require running database and queue.*
Running tests form the terminal:
```shell script
./gradlew integrationTest
```
You can also run tests from IntelliJ, just remember to set the database password
inside the `gpc-api-facade/src/integrationTest/resources/application.yml` file (or set GPC_FACADE_USER_DB_PASSWORD variable).

## Troubleshooting

### gradle-wrapper.jar doesn't exist

If gradle-wrapper.jar doesn't exist run in terminal:
* Install Gradle (MacOS) `brew install gradle`
* Update gradle `gradle wrapper`
