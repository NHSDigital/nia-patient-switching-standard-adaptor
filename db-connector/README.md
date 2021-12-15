# DB connector

This is a common module, used by GP2GP Translator and GPC Api Facade.
It holds services needed for communication with the database.

## Migrations
Migrations are implemented using Liquibase and need to be run manually before starting applications.
To run migrations, you need to specify the database URL and credentials of the database user
that will be used to run the migrations. Set the following env variables:
- PSS_DB_OWNER_NAME
- PSS_DB_OWNER_PASSWORD
- PSS_DB_URL

To run the migrations use following command:
```shell script
./gradlew update
```
Changelog can be found under /changelog path.

### How to add migrations
To add a new migration, create a new xml file inside `/changelog/migration` directory.
All files from this directory are included in the `db.changelog-master.xml` file.

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
