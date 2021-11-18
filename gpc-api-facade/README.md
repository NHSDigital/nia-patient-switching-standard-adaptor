# GPC API Facade

GPC API Facade - HTTP FHIR API that is similar to GPC 1.6.0 standard.
It enables NME to request and fetch patient data in the FHIR format.
This facade only serves light tasks, delegating all the work to the async GP2GP Translator component.

## Migrations
Migrations are implemented using Liquibase and will run automatically when `GpcFacadeApplication` starts.
Changelog and migrations can be found under gpc-api-facade/src/main/resources/db/changelog path.
To add a new migration, add the sql script to /migration directory and include it in the `db.changelog-master.yaml` file,
like shown in the following example:

```yaml
- include:
  relativeToChangelogFile: true
  file: migration/001-example.sql
```

Liquibase and datasource properties are set in `gpc-api-facade/src/main/resources/application.yml` file.

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
