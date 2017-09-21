# EPS DoS API Stub Architecture

## Components

The EPS DoS API Stub is composed of the following elements:

### Ansible Playbooks
Responsible for:

* Network (VPC) creation
* Host creation and bootstrapping
* Deployment - pulling & starting docker images on host(s)
* Sorry-server careation and switching (TODO)

### Nginx Proxy
Responsible for:

* Authentication (TODO)
* Rate-limiting
* Logging requests in & out
* Assigning request ID for logging
* Failure handling
* SSL termination (TODO)
* Mapping API version number to container name and forwarding requests
  - a request for `http://dos.eps.digital.nhs.uk/v0.0.2/dispenser/FA123` is proxied to `http://dos-api-app_v0-0-2:8086/dispenser/FA123`

### dos-api-app
 
A vertx instance running in a container listening on 8086. One instance per container and one container per API version.

The instance has the following services:

#### DispenserInformationServiceVerticle
This is the main entry point. It is responsible for:

* Logging the request
* Mapping requests to the appropriate handler
* Validating requests
* Orchestrating service calls 
* Matching results from different services and removing orphans
* merging results

#### DispenserDetailService

Responsible for:

* Retrieving name & address information from Choices
* Parsing response XML into Dispenser objects

#### DispenserAccessInformationService

Responsible for:

* Retrieving location, phone/fax/web/email, and opening hours information from Pathways
* Parsing response Json into Dispenser objects

#### DispenserAvailableService

Responsible for:

* Deciding which of a list of Dispensers are available within a given period