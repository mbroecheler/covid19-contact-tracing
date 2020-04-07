# Contact Tracing Backend Application

## How to run this application

Please follow the quick-start instructions on the homepage of this repository. It is important to have the database running before starting the application.

To see your applications health enter url `http://localhost:8081/healthcheck`

Once you have the application up and running, it exposes the following endpoints:

- `/device/{device_id}/claim` (POST): Claim a device for a person. {device_id} is the string identifier of the device. Expects a JSON payload described by the DeviceClaim class in the api package.
- `/device/{device_id}/contacts` (POST): Upload contact records from a device. Expects a JSON payload described by the DeviceContact class in the api package.
- `/tracer/{person_id}/infected?from={timestamp}` (GET): Trace potential infection paths for the given person identified by the {person_id} UUID. Returns a list of JSON objects described by the Contact class.

The current endpoints are mostly meant to demonstrate how the backend would work. The exact APIs will need to be adjusted to meet the interface of the mobile tracking application (for the `/device` resource)
and the needs of officials and health care workers using the tracing functionality (for the `/tracer' resource).

## Build instructions

Run `mvn package` to build the application

