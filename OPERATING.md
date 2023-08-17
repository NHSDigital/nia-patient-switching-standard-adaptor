# Operating The Patient Switching Adaptor

## Logging and tracing

The Patient Switching Adaptors services emit logs which are captured by the docker containers they are hosted within. Whichever Docker container orchestration technology is used, the log streams can be captured and forwarded to an appropriate log indexing service for consumption, storage and subsequent queries. 

The consumption of these logs forms an essential part of issue investigation and resolution. 

The log messages relating to a specific transfer can be identified by the Conversation ID. Which is a correlating ID present throughout the patient record migration and carried in the GP2GP messages themselves.

### Log message format

```text
yyyy-mm-dd HH:mm:ss.SSS Level=DEBUG Logger=u.n.a.p.t.s.BundleMapperService ConversationId=6836FD37-B856-4167-A087-7E3989020FA3 Thread="org.springframework.jms.JmsListenerEndpointContainer#0-1" Message="Mapped Bundle with [261] entries"
```
- Level: The logging level of the message (INFO/DEBUG/WARN/ERROR) 
- Logger: The name of the Java class that emitted the message
- ConversationId: The ID correlating all messages for a patient transfer
- Message: The log message 

## Timeout functionality

## Database requirements

## Message broker requirements

## Object storage

## AWS daisy chaining example

## Environment variables


