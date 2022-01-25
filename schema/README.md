# Schema module

This module holds xsd schemas and java classes generated based on those schemas.
Schema files can be found inside src/resources directory.

## Generating java classes

#### NOTE: Please use the newest version of the plugin: [gradle-xjc-plugin](https://github.com/unbroken-dome/gradle-xjc-plugin)


1. Add `org.unbroken-dome.xjc` plugin in build.gradle:
    ```groovy
    plugins {
        id 'org.unbroken-dome.xjc' version '2.0.0'
    }
    ```
2. At the bottom of the build.gradle file, add xjc plugin configuration,
   to specify schemas location:
   ```groovy
   xjc {
        srcDirName = 'resources/schema'
   }
   ```
3. After reloading gradle, `xjcGenerate` gradle task should be present under
   'code generation' category. Execute this task, it will generate java classes
   based on schemas pointed in srcDirName property in build.gradle.
   
4. Generated classes should be under `build/generated/sourced/xjc/java/main` path.
   Copy them to `src/main/java` directory. Modify generated classes as needed.
   Execute gradle `clean` task and remove changes added to build.gradle file.
   
## Manually modified classes

Please make sure you don't override the following classes with the generated ones. They had to be manually changed to allow correct unmarshalling xml files to java objects:
- EN.java 
- RCMRMT030101UK04Place.java
- IVLTS.java
- CD.java
- ObjectFactory.java
- package-info.java
- ANY.java
- IVLPQ.java
- RCMRMT030101UK04ObservationStatement.java
- PQInc.java

New file:
- Value.java (joined class containing fields from IVLPQ and PQ classes)
