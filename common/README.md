# Common

This is a common module used by GP2GP Translator, GPC Api Facade and DB Connector modules.
It holds any common util classes used by more than one module.

## Util classes description
### DateUtils
Class for date-related methods.
- `getCurrentOffsetDateTime()` method returns current UTC OffsetDateTime.

### FhirParser
This class offers methods for parsing and encoding FHIR resources.
- `parseResource(String body, Class<T> fhirClass)` method parses String to given FHIR resource.
- `encodeToJson(IBaseResource resource)` method parses FHIR resource to String.

### ParametersUtils
Here you can find method related to `Parameters` FHIR resource.
- `getNhsNumberFromParameters(Parameters parameters)` method retrieves patient's NHS number value
 from Parameters resource.

### CreateParametersUtil
This class keeps convenient methods for creating `Parameters` FHIR resource objects.
Meant to be used inside test classes.
- `createValidParametersResource(String nhsNumberValue)` method returns a `Parameters` resource
 with given patient NHS number set.
