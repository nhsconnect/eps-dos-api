---
title: Development Overview
keywords: develop
tags: [develop]
sidebar: overview_sidebar
permalink: develop_overview.html
summary: An overview of the high-level approach for the DM+D API.
---

##Data Download##

The data are available for download in the folloing formats:

- CSV, for bulk copy into a relational database. Standard ISO SQL DDL is provided.
- Raw xml, as available compressed from TRUD
- JSON, in the same format as provided in the REST interface
- RDF

## Security ##

Security of the service will be enforced by checking ...

## REST interface ##

All REST requests are standard HTTP requests using the `application/json` mime-type.

# Response #

The json response will follow a simple structure...

## Error Handling ##

If there is a problem, e.g. an invalid request parameter, concept can't be found...
