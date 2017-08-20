---
title: API Deployment Overview
keywords: deployment
tags: [testing,integration,deployment]
sidebar: overview_sidebar
permalink: deploy_overview.html
summary: An overview of the API deployment, including how to host API instances in your own environment.
---

## Deploying the API

Details of the solution assurance process that ensures the correct level of clinical safety, governance, and security has been accepted, understood and signed off by the organisation using the APIs and data controllers of the records.

### Lambda ###
The functions below are implemented as java classes which are run as AWS lambda functions. They will also run locally and could be configured to run by another orchestraction mechanism.

#### Download and Unzip ####
##### Handler #####
`uk.nhs.dmd.lambda::lambdaHandler`
##### Config items #####
Environment variables or args:

- _bucket_ the S3 bucket to store releases in
- _trud user_  the trud ftp user
- _trud user_  the trud ftp password
- _trud address_ the  
- _release folder_ the path from point release to release zip

##### Trigger #####
Scheduled daily

##### Failure Behaviour #####
Two retries. Any releases failing to download will be picked up in next run

#### Transform and push to ElasticSearch  ####
##### Handler #####
`uk.nhs.dmd.lambda::transform`
###### Config items #####
Environment variables or args:

- _es-index_ elasticsearch index to store 

##### Trigger #####
Key ending with '/' put into release bucket, signifying download from TRUD complete

##### Failure Behaviour #####
Two retries. 

#### Transform to JSON  ####
##### Handler #####
`uk.nhs.dmd.lambda::transformToJson`
##### Config items #####
Environment variables or args:

- _bucket_ the S3 bucket to store trasformed data in

##### Trigger #####
Key ending with '/' put into release bucket, signifying download from TRUD complete

##### Failure Behaviour #####
Two retries. 


