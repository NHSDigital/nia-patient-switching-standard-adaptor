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

## Generating individual JAXB elements for testing

When writing unit tests, you may be required to unmarshall code elements that haven't been pre-generated. The error you will see if this is the case will follow the general pattern of: 

```
unexpected element (uri:"", local:"YOUR_ELEMENT"). Expected elements are <{urn:hl7-org:v3}PARENT_ELEMENT>
```

This will require you add these elements directly to the `ObjectFactory.java` class independently.

1. In `ObjectFactory.java`, register your element's QNAME in the custom made QNAME area.
```
   private final static QName _EHR_COMPOSITION_QNAME = new QName("urn:hl7-org:v3", "ehrComposition");
```

2. Then at the bottom of the same file, create the required JAXB element, using the already registered QNAME.
```
@XmlElementDecl(namespace = "", name = "ehrComposition")
    public JAXBElement<RCMRMT030101UK04EhrComposition> createLocation(RCMRMT030101UK04EhrComposition value) {
        return new JAXBElement<RCMRMT030101UK04EhrComposition>(_EHR_COMPOSITION_QNAME, RCMRMT030101UK04EhrComposition.class, null, value);
    }
```

   
## Manually modified classes

Please make sure you don't override the following classes with the generated ones. They had to be manually changed to allow correct unmarshalling xml files to java objects:
- EN.java 
- RCMRMT030101UK04Place.java
- IVLTS.java
- TS.java
- CD.java
- ObjectFactory.java
- package-info.java
- AD.java
- ANY.java
- IVLPQ.java
- RCMRMT030101UK04ObservationStatement.java
- RCMRMT030101UK04CompoundStatement.java
- PQInc.java
- RCMRMT030101UK04Annotation.java
- RCMRMT030101UK04EhrComposition.java
- RCMRMT030101UK04PlanStatement.java
- RCCTMT120101UK01Organization.java
- RCMRMT030101UK04RequestStatement.java
- II.java
- PQ.java
- RCMRMT030101UK04Author.java
- RCMRMT030101UK04Author3.java
- RCMRMT030101UK04Authorise.java
- RCMRMT030101UK04Component2.java
- RCMRMT030101UK04Component02.java
- RCMRMT030101UK04Component4.java
- RCMRMT030101UK04Consumable.java
- RCMRMT030101UK04Discontinue.java
- RCMRMT030101UK04EhrExtract.java
- RCMRMT030101UK04ExternalDocument.java
- RCMRMT030101UK04ManufacturedProduct.java
- RCMRMT030101UK04Material.java
- RCMRMT030101UK04MedicationDosage.java
- RCMRMT030101UK04MedicationRef.java
- RCMRMT030101UK04MedicationStatement.java
- RCMRMT030101UK04NarrativeStatement.java
- RCMRMT030101UK04Participant.java
- RCMRMT030101UK04Participant2.java
- RCMRMT030101UK04PertinentInformation.java
- RCMRMT030101UK04PertinentInformation2.java
- RCMRMT030101UK04Prescribe.java
- RCMRMT030101UK04SupplyAnnotation.java
- TS.java
- RCMRMT030101UK04NarrativeStatement.java
- RCMRMT030101UK04InterpretationRange.java
- RCCTMT120101UK01Device.java
- RCMRMT030101UK04InterpretationRange.java
- RCMRMT030101UK04SpecimenMaterial.java

Removed files:
- ST.java

New file:
- ValueAdapter.java - this is custom adapter helping with unmarshalling value element,
  which can be of few different types
