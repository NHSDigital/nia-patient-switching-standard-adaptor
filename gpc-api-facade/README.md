# GPC API Facade

GPC API Facade - HTTP FHIR API that is similar to GPC 1.6.0 standard.
It will enable NME to request and fetch patient data in the FHIR format.
This facade will only serve light tasks, delegating all the work to the async GP2GP Translator component.

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

```shell script
./gradlew integrationTest
```

## Troubleshooting

### gradle-wrapper.jar doesn't exist

If gradle-wrapper.jar doesn't exist run in terminal:
* Install Gradle (MacOS) `brew install gradle`
* Update gradle `gradle wrapper`
