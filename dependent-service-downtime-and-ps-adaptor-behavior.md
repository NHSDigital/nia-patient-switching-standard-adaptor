## Service Failures and Expected Behavior

This guide outlines the Behavior Of The Requesting Adaptor when various dependent services are down. 
The Requesting Adaptor works in conjunction with several services including the GP2GP Translator, MHS Adaptor, and others. 
The guide explains the effects of outages and how the system behaves in each case.

### System Overview
The Requesting Adaptor initiates a conversation for transferring patient records, relying on several interconnected services:
- GP2GP Translator: Sends patient records to the Requesting Adaptor via a queue.
- Facade Application: Works with the Requesting Adaptor to manage transfer requests.
- MHS Adaptor: Manages inbound and outbound queues, which are responsible for transferring patient records to the incumbent system.
- Other Dependencies: Include Requesting Adaptor database, MHS inbound and outbound services and File storage.

### Service Outage Scenarios And Behavior Of The Adaptor In Certain Scenarios
1. GP2GP Translator is Down
    - Initial Request:
      - Scenario: The GP2GP Translator is not operational and cannot send patient records.
      - Expected Behavior: No messages are sent to the incumbent system while the translator is down. Once the GP2GP Translator recovers, 
           the transfer process resumes, and messages are processed as normal.
      - Recovery: Automatic upon Translator recovery; transfer resumes without manual intervention.
   
    - Transfer in Progress:
      - Scenario: The GP2GP Translator is not operational and cannot send patient records.
      - Expected Behavior: Facade responds as it would if the GP2GP Translator was up and running.
      - Recovery: When the GP2GP Translator recovers the transfer is processed as normal.
      
2. Message Broker is Down
   - Initial Request:
      - Scenario: The message broker responsible for transferring data between the GP2GP Translator and Requesting Adaptor is down.
      - Expected Behavior: The initial request is never sent to the incumbent system because the queue is unavailable.
                           The facade responds with 500 "Internal Server Error". The transfer wasn't sent to the sending adapter.
                           The transfer can be requested again once the message broker is up and running.
      - Recovery: After the queue is restored, the request can be processed normally, but any messages queued during downtime are not sent.
   
   - Transfer in Progress:
      - Scenario: The message broker responsible for transferring data between the GP2GP Translator and Requesting Adaptor is down.
      - Expected Behavior: Facade responds with 204, indicating the migration is still in progress.
      - Recovery: This is non-recoverable as retries with the same message ID will produce the same error, even after the queue comes back up.
     
4. Requesting Adaptor Database (DB) is Down
   - Initial Request:
      - Scenario: The Requesting Adaptor's database is not operational.
      - Expected Behavior: The initial transfer request is not sent because the adaptor cannot access the necessary data.
      - Recovery: After the database is restored the transfer is processed as normal.
     
   - Transfer in Progress:
      - Scenario: The Requesting Adaptor's database is not operational.
      - Expected Behavior: Facade responds with 500 "Internal Server Error"
      - Recovery: After the database is restored the transfer is processed as normal.
     
4. Facade Application is Down
   - Initial Request:
      - Scenario: The facade application that works with the Requesting Adaptor is down.
      - Expected Behavior: The initial transfer request is not sent. The communication between the Requesting Adaptor and the incumbent system is blocked.
      - Recovery: Once the facade application is operational, the system can process requests as normal.
     
   - Transfer in Progress:
      - Scenario: The facade application that works with the Requesting Adaptor is down.
      - Expected Behavior: No negative effects on the ongoing transfer and it proceeds as normal however the final ACK won't be sent until
                            the facade is operational again.
      - Recovery: Once the facade application is operational, the system can process requests and finish workflows as normal.

5. MHS Outbound Adaptor is down
   - Initial Request:
      - Scenario: The MHS Outbound adaptor is not operational.
      - Expected Behavior: The EHR request is not sent to the incumbent system while the MHS Outbound Adaptor is down. 
        Facade responds with 204 indicating the migration is still in progress. Once the MHS Outbound adaptor recovers, the request is sent as normal.
      - Recovery: Once the MHS Outbound Adaptor is operational, the system can process incoming requests as normal.
   
   - Transfer in Progress:
      - Scenario: The MHS Outbound adaptor is not operational.
      - Expected Behavior: Facade responds with 204, indicating the migration is still in progress.
      - Recovery: When the MHS Outbound service recovers, the migration is processed as normal.
     
6. MHS Inbound Adaptor is Down
   - Initial Request:
      - Scenario: The MHS Inboound adaptor is not operational.
      - Expected Behavior: As the EHR extract never reaches the adaptor, the facade receives 204 "No Content" response.
      - Recovery: No action is required as the request is still processed.
     
   - Transfer in Progress:
      - Scenario: The MHS Inboound adaptor is not operational.
      - Expected Behavior: As the EHR extract never reaches the adaptor, the facade receives 204 "No Content" response.
      - Recovery: After the service is restored, the transfer request can be requested manually once again.

7. File Storage is Down
   - Initial Request:
      - Scenario: The file storage system is not operational.
      - Expected Behavior: 
                       - If the EHR Extract doesn't contain inline attachments, the Facade responds with 204, to indicate the migration is in progress.
                       - If the EHR Extract does contain inline attachments, the Facacde responds with 500 "Internal Server Error" 
                         and the body doesn't have any error details.
      - Recovery: After the file storage is restored, the transfer request should be repeated.

   - Transfer in Progress:
      - Scenario: The file storage system is not operational.
      - Expected Behavior: The Facade responds with 204, indicating the migration is in progress.
                           The migration will eventually be failed by the scheulded timeout check. 
                           After which, the Facade will return 500 "Internal Server Error".
      - Recovery: When the file storage service is up and running, the transfer request should be repeated.

### Conclusion
In the event of a service outage, the Requesting Adaptor system behaves differently based on the specific service affected. 
In some cases, the system will automatically recover and resume processing once the affected service is restored. 
However, some outages, particularly those involving the queue or MHS outbound, 
may require manual intervention to resend requests that were missed during downtime.