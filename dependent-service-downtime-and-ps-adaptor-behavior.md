## Service Failures and Expected Behavior

This guide outlines the expected behavior of the PS Adaptor when various dependent services are down. 
The PS Adaptor works in conjunction with several services including the GP2GP Translator, MHS Adaptor, Facade, and others. 
The guide explains the effects of outages and how the system behaves in each case.

### System Overview
The PS Adaptor initiates a conversation for transferring patient records, relying on several interconnected services:
- GP2GP Translator: Sends patient records to the PS Adaptor via a queue.
- Facade Application: Works with the PS Adaptor to manage transfer requests.
- MHS Adaptor: Manages inbound and outbound queues, which are responsible for transferring patient records to the incumbent system.
- Other Dependencies: Include PS Adaptor database, MHS inbound and outbound services, file storage, Redis cache, and MHS outbound SDS.

### Service Outage Scenarios and Expected Behavior
1. GP2GP Translator is Down
   - Scenario: The GP2GP Translator is not operational and cannot send patient records.
   - Expected Behavior: No messages are sent to the incumbent system while the translator is down. Once the GP2GP Translator recovers, 
     the transfer process resumes, and messages are processed as normal.
   - Recovery: Automatic upon Translator recovery; transfer resumes without manual intervention.
2. Queue is Down
   - Scenario: The queue responsible for transferring data between the GP2GP Translator and PS Adaptor is down.
   - Expected Behavior: The initial request is never sent to the incumbent system because the queue is unavailable.
   - Recovery: After the queue is restored, the request can be processed normally, but any messages queued during downtime are not sent.
3. PS Adaptor Database (DB) is Down
   - Scenario: The PS Adaptor's database is not operational.
   - Expected Behavior: The initial request to the incumbent system is not sent because the adaptor cannot access the necessary data.
   - Recovery: After the database is restored, the system may resume operations, but requests during the outage are not processed.
4. Facade Application is Down
   - Scenario: The facade application that works with the PS Adaptor is down.
   - Expected Behavior: The initial request to the incumbent system is not sent. The communication between the PS Adaptor and the incumbent system is blocked.
   - Recovery: Once the facade application is operational, the system can process requests as normal.
5. MHS Adaptor is Down
   - Scenario: The MHS Adaptor (including its database) is not operational.
   - Expected Behavior: The EHR request is not sent to the incumbent system while the MHS Adaptor is down. 
     Once the MHS Adaptor's database recovers, the request is sent as normal.
   - Recovery: Automatic upon recovery of the MHS Adaptor’s database.
6. MHS Adaptor Inbound Queue is Down
   - Scenario: The inbound queue managed by the MHS Adaptor is down.
   - Expected Behavior: The EHR request is successfully sent to the incumbent system.
   - Recovery: No action is required as the request is still processed.
7. MHS Adaptor Outbound Queue is Down
   - Scenario: The outbound queue managed by the MHS Adaptor is down.
   - Expected Behavior: The request is not sent to the incumbent system while the outbound queue is down. 
     Once the outbound service recovers, the request is processed and sent.
   - Recovery: The system automatically sends the request when the outbound queue becomes operational again.
8. File Storage is Down
   - Scenario: The file storage system is not operational.
   - Expected Behavior: Despite the outage, the EHR request is still sent to the incumbent system. 
     However, any operations that rely on file storage will fail.
   - Recovery: File storage recovery may be necessary for non-EHR-related operations, but the EHR transfer proceeds.
9. Redis is Down
   - Scenario: Redis, used for caching, is not operational.
   - Expected Behavior: There are no negative effects on the transfer process. The translation of patient records continues as normal.
   - Recovery: No specific recovery action is required for Redis as its absence does not hinder the transfer process.
10. MHS Outbound SDS is Down
    - Scenario: The MHS outbound SDS is down.
    - Expected Behavior: The request is never sent to the incumbent system.
    - Recovery: Once the MHS outbound SDS is operational again, the request will need to be manually retried or reprocessed.

### Conclusion
In the event of a service outage, the PS Adaptor system behaves differently based on the specific service affected. 
In many cases, the system will automatically recover and resume processing once the affected service is restored. 
However, some outages, particularly those involving the queue or MHS outbound, 
may require manual intervention to resend requests that were missed during downtime.