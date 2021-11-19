# DB connector

This is a common module, used by GP2GP Translator and GPC Api Facade.
It holds services needed for communication with the database.

## Migrations
Migrations are implemented using Liquibase and need to be run manually before starting applications.
To run the migrations use following command:
```shell script
./gradlew update
```
Changelog can be found under /changelog path.
To add a new migration, add a new `<changeSet>` tag to the `db.changelog-master.xml` file.

## How to run tests

### How to run unit tests:

```shell script
./gradlew test
```

### How to run all checks:

```shell script
./gradlew check
```

## Troubleshooting

### gradle-wrapper.jar doesn't exist

If gradle-wrapper.jar doesn't exist run in terminal:
* Install Gradle (MacOS) `brew install gradle`
* Update gradle `gradle wrapper`
