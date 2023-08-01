# nia-patient-switching-standard-adaptor
National Integration Adaptor - Patient Switching Standard Adaptor

Incumbent providers (e.g. TPP, EMIS, SystemOne) in order to deploy GP2GP Adaptor in their infrastructure
to support losing practice scenario - i.e. whereby a different practice transfers patient data from the incumbent
would have to make changes to their GP Connect interface implementations.
In particular, they would need to implement 1.6.0 version that is required by the GPC Consumer and GP2GP adaptors.
This business case is not always easy to be accepted by the incumbent providers, as they would have to invest time to make those changes.

The motivation for the Switching Standard Adaptor is to remove the dependency from incumbent providers to do that work.
The idea is to build an adaptor that could be installed and configured in a New Market Entrant (NME) infrastructure,
and could work with the incumbent’s GPC < 1.6.0.

Adaptor consists of two main components:
- GPC API Facade
- GP2GP Translator

Both are Java Spring Boot applications, released as separate docker images.

## Developer Information
 See [Operating The Patient Switching Adaptor](./OPERATING.md) for Guidance on operating the Patient Switching Adaptor in production.

Information for contributors and running the adaptor locally is available in [Developer Information](./developer-information.md).   

## Daisy-Chaining

It is possible to run the PS Adaptor and the [GP2GP Adaptor](https://github.com/nhsconnect/integration-adaptor-gp2gp) side by side using a single instance of the MHS Adaptor.
When using this configuration, messages received by the PS Adaptor with an unrecognised conversation ID are forwarded
to the GP2GP Adaptor via it's inbound queue. Conversely, the default behaviour without daisy-chain enabled is to put
messages with an unrecognised conversation ID on a dead letter queue.

**To enable daisy-chaining the following environment variables need to be set:**

- `PS_DAISY_CHAINING_ACTIVE`: set to `true` to enable daisy-chaining - default = `false`
- `GP2GP_AMQP_BROKERS`: the location of the GP2GP Adaptors inbound queue. This should be set to the url of a single JMS broker
  (the PS Adaptor does not support concurrent GP2GP Adaptor brokers) - default = `amqp://localhost:5672`

**Optional environment variables:**

- `GP2GP_MHS_INBOUND_QUEUE`: The name of the GP2GP Adaptors inbound queue
- `GP2GP_AMQP_USERNAME`: The username for accessing the broker
- `GP2GP_AMQP_PASSWORD`: The password for accessing the broker

An example daisy chaining environment is provided in test-suite/daisy-chaining

## Endpoints

The Patient Switching adaptors facade exposes two endpoints.

- POST /Patient/$gpc.migratestructuredrecord
- POST /$gpc.ack

### /Patient/$gpc.migratestructuredrecord

The migratestructuredrecord endpoint is the primary endpoint for the adaptor and is used to start an electronic health record transfer.
The following is required to call this endpoint...

- TO_ASID : The ASID identifier of the losing incumbent
- FROM_ASID : The ASID identifier of the winning New Market Entrant (NME)
- TO_ODS : The ODS identifier of the losing incumbent
- FROM_ODS : the ODS identifier of the winning New Market Entrant (NME)
- ConversationId : A unique GUID for each request; if you do not provide one, the adaptor will create one and return it in the response headers. It must be used for all further calls for the patient's NHS number.

The endpoint also requires a JSON body that includes the needed patient NHS number. The format of the body should look like the following...

   ```
   {
      "resourceType": "Parameters",
      "parameter": [
         {
            "name": "patientNHSNumber",
            "valueIdentifier": {
               "system": "https://fhir.nhs.uk/Id/nhs-number",
               "value": "{{nhs-number-test-group-8}}"
            }
         },
         {
            "name": "includeFullRecord",
            "part": [
               {
                  "name": "includeSensitiveInfomation",
                  "valueBoolean": true
               }
            ]
         }
      ]
   }
   ```

Endpoint calling:

1. Initial request: If you successfully configure the endpoint described above and call it, you should receive a 202-accepted response. This means the adaptor has received the request and is making the relevant requests.
2. Polling the request: after receiving a 202 response, we recommend polling the endpoint at regular intervals using an increasing call gap strategy. Each poll can return the following responses:-
    1. 204 No content: this response indicates that we are still processing the requests / waiting for the EHR message response.
    2. 200 Success: this response indicates we have successfully received and converted the EHR to JSON; you will also receive the FHIR bundle in the response's body.
    3. 400,404,500,501: The endpoint can return all these possible error codes. These will all provide a detailed error with an operationOutcome JSON model response in the body. This looks like...

   ``` 
   {
      "resourceType": "OperationOutcome",
      "meta": {
         "profile": [
            "https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-OperationOutcome-1"
         ]
      },
      "issue": [
         {
            "severity": "error",
            "code": "exception",
            "details": {
               "coding": [
                  {
                     "system": "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1",
                     "code": "PATIENT_NOT_FOUND",
                     "display": "GP2GP - Patient is not registered at the practice"
                  }
               ]
            }
         }
      ]
   }
   ```

### /$gpc.ack

The ack endpoint is the final endpoint to call once you are happy that the EHR record you have received is acceptable.
If you do not call this endpoint after receiving an EHR from the migratestructuredrecord enpoint, then you risk the losing practise triggering off the manual postal transfer.

The following is required to call this endpoint:-

- CONVERSATION_ID: The id associated with the patient transfer request.
- CONFIRMATION_RESPONSE: you can provide the following status:-
    - ACCEPTED: This will tell the sending incumbent that you are happy with the received EHR.
    - FAILED_TO_INTEGRATE: You have encountered a problem integrating the record into your system. This will alert the sender to an error and trigger off the postal process.

Endpoint calling:

This endpoint is a fire-and-forget endpoint.
If your request is successful, you will get a 200: Success response.
If your request is unsuccessful, you will get a 500: Server error response.
If you receive a 500 response, you can retry again at any point, however, it should be noted that you must receive a 200: Success response from the migratestructuredrecord for the given conversation ID to receive a 200: Success from this endpoint.

Note: To improve reliability on this endpoint we are currently looking at a polling change, the documentation will be updated once this has been updated.

## Configuring the SNOMED Database

### First  installation

As part of the installation of the adaptor, we do not provide the SNOMED database files as they are updated regularly under TRUD (Technology Reference Update Distribution).
To acquire the most recent SNOMED database:-

1. Head to https://isd.digital.nhs.uk/ and create a new account.
2. Log in
3. Search for the following: SNOMED CT UK Monolith Edition, RF2: Snapshot (https://isd.digital.nhs.uk/trud/users/authenticated/filters/0/categories/26/items/1799/releases). We recommend the full Monolith edition, not the delta version.
4. Subscribe to the data store.
5. Once subscribed you will be able to download the most recent version of the SNOMED DB, at the time of writing this is release 36.0.0. (uk_sct2mo_36.0.0_20230412000001Z.zip)
6. During the setup of the adaptor you will now need to set the environment variable “SNOMED_FILE_LOCATION” to the root location of the zip file that you’ve downloaded e.g. export SNOMED_FILE_LOCATION=“/root/uk_sct2mo_36.0.0_20230412000001Z.zip
7. Now when you run our startup scripts for the first time, the SNOMED database will be installed for you.

### Updating the SNOMED Database

You will now receive email notifications from TRUD once the subscribed data source is updated. We recommend updating your SNOMED version as soon as you receive the notification. To do this:-

1. Log in to https://isd.digital.nhs.uk/
2. Download the newest version of the SNOMED Monolith edition.
3. Navigate to directory 'snomed-database-loader'.
4. Before continuing, please be aware that the database will be unavailable whilst being rebuilt, so this should be completed during a maintenance window.
5. Execute the script 'load_release-postgresql.sh' followed by the path to the root location of the zip file that you have downloaded. For example:

   ``` 
   ./load_release_postgresql.sh /root/uk_sct2mo_36.0.0_20230412000001Z.zip
   ```

## Licensing
This code is dual licensed under the MIT license and the OGL (Open Government License).
Any new work added to this repository must conform to the conditions of these licenses.
In particular this means that this project may not depend on GPL-licensed or AGPL-licensed libraries,
as these would violate the terms of those libraries' licenses.

The contents of this repository are protected by Crown Copyright (C).
