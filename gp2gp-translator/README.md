# GP2GP Translator

GP2GP Translator - new system that is the heart of the PSS Adaptor.
It is responsible for GPC ↔︎ GP2GP translations, assembly of binary document data,
communication with the incumbent system via MHS Adaptor and data cleanup.

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
*Integration tests require running database and queue. Make sure GP2GP Translator application is off,
because if it's not, it will steal the message from the MHS Queue before the application started by tests have a chance to grab it.*

Running tests form the terminal:
```shell script
./gradlew integrationTest
```
You can also run tests from IntelliJ, just remember to set the database password
inside the `gp2gp-translator/src/integrationTest/resources/application.yml` file (or set GP2GP_TRANSLATOR_USER_DB_PASSWORD variable).

## Troubleshooting

### gradle-wrapper.jar doesn't exist

If gradle-wrapper.jar doesn't exist run in terminal:
* Install Gradle (MacOS) `brew install gradle`
* Update gradle `gradle wrapper`
