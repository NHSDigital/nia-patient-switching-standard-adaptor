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

```shell script
./gradlew integrationTest
```

## Troubleshooting

### gradle-wrapper.jar doesn't exist

If gradle-wrapper.jar doesn't exist run in terminal:
* Install Gradle (MacOS) `brew install gradle`
* Update gradle `gradle wrapper`
