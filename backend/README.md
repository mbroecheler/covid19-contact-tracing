# contacttracer

How to start the contacttracer application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/contacttracer-0.1-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8080`

Health Check
---

To see your applications health enter url `http://localhost:8081/healthcheck`

You know have the backend up and running. It exposes the following endpoints:

```
    POST    /device/{device_id}/claim                       Claim a device for a person identified by UUID
    POST    /device/{device_id}/contacts                    Upload contact records from a device
    GET     /tracer/{person_id}/infected?from={timestamp}   Trace potential infection paths for the given individual
```
where `{device_id}` is a string identifier for a tracked device and `{person_id}` is the UUID of an individual. `from` is an optional timestamp parameter 
