# nia-patient-switching-standard-adaptor
National Integration Adaptor - [GP2GP Requesting Adaptor](https://digital.nhs.uk/developer/api-catalogue/gp2gp/patient-switching---integration-adaptor)

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


## Table of contents

1. [Guidance for operating the adaptor as a New Market Entrant](/OPERATING.md)
1. [Guidance on integrating with the adaptors APIs](#endpoints)
1. [Guidance for developing the adaptor](/developer-information.md)
1. [Guidance for developing the adaptor on Windows](/getting-started-with-windows.md)
1. [Documentation on how this adaptor maps GP2GP concepts to GPConnect concepts](https://github.com/NHSDigital/patient-switching-adaptors-mapping-documentation)

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

   ```json
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

Responds with one of:

1. Initial request: If you successfully configure the endpoint described above and call it, you should receive a 202-accepted response. This means the adaptor has received the request and is making the relevant requests.
2. Polling the request: after receiving a 202 response, we recommend polling the endpoint at regular intervals using an increasing call gap strategy. Each poll can return the following responses:-
    - 204 No content: this response indicates that we are still processing the requests / waiting for the EHR message response.
    - 200 Success: this response indicates we have successfully received and converted the EHR to JSON; you will also receive the FHIR bundle in the response's body.
    - 400,404,500,501: The endpoint can return all these possible error codes. These will all provide a detailed error with an operationOutcome JSON model response in the body. This looks like...

      ```json
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
                     },
                     {
                       "system": "2.16.840.1.113883.2.1.3.2.4.17.101"
                       "code": "06"
                       "display": "Patient not at surgery"
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

## Licensing
This code is dual licensed under the MIT license and the OGL (Open Government License).
Any new work added to this repository must conform to the conditions of these licenses.
In particular this means that this project may not depend on GPL-licensed or AGPL-licensed libraries,
as these would violate the terms of those libraries' licenses.

The contents of this repository are protected by Crown Copyright (C).

## Performance
The performance of PS Adaptor was tested with JMeter tool. 
The use case tested was the simulation of the patient transfer request. 
This was tested by sending EHR record requests to the PS Adaptor, and we expected to receive a bundle back. 
The patient transfer used two attachments,both of them are text attachments of size 2.44 MB and 0.7 MB.
We used these tests to observe how the PS Adaptor handles a heavy workload and to profile the CPU and memory usage 
during the testing process.

There was a series of tests run with the following setup and parameters:
- PS Adaptor was run in ECS AWS environment with 4 CPUs and 16 GB memory.
- MHS Adaptor was run in ECS AWS environment with 4 CPUs and 16 GB memory.
- For the message queue mq.m5.xlarge host type was used
- RDS DB host type was set to db.t3.xlarge

The test used 2000 transfers which were split into 5 batches of 400 transfers.
The time for the test was counted from the start of the initial request(s) to the end of the test of all transfers.
Pause time between transfers was set to 1 sec and the socket timeout was to 2 minutes.

The test load with 2000 transactions finished successfully in 14 minutes which gives on average 420-435 ms per transfer.
The observed CPU utilization was around 50-60% and memory usage was around 70% which leaves plenty of headroom for additional load.

![report1.jpg](test-suite%2Fnon-functional-tests%2Ftest-scenario%2Fperf_report%2Freport1.jpg)

Overall performance statistics:
![report2.png](test-suite%2Fnon-functional-tests%2Ftest-scenario%2Fperf_report%2Freport2.png)

Active transfers per one iteration was 400:
![report4.png](test-suite%2Fnon-functional-tests%2Ftest-scenario%2Fperf_report%2Freport4.png)

Response time:
![report5.png](test-suite%2Fnon-functional-tests%2Ftest-scenario%2Fperf_report%2Freport5.png)

Results can be seen in using graphs by importing results8.jtl into Jmeter.
