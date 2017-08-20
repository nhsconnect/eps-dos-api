---
title: Download DM+D data
keywords: develop
tags: [develop]
sidebar: overview_sidebar
permalink: data_download.html
summary: Download complete DM+D releases in an immediately useful format via the API
---

# Data Download #

The API transforms each DM+D release into a number of formats which are suitable for immediate use. This page provides inforamtion on these formats, and ways to download them.

Other formats can be produced: where users do further transforms or processing on releases they are encouraged to share their transforms which can be incorporated into the API and used to produce further formats.

## API Key Registration ##

Applications are required to register for and use an API key for all DM+D API access, this includes data downloads. 

## Data Formats ##
Data is transformed into and available in the following formats. Files are bundled and compressed in the zip format. 

### CSV ###
Comma separated value

#### Schema ####
ISO SQL DDL, with one table per CSV file. 

### XML ###
The original XML files as held on TRUD. Schemas are included in the zip archive.

### JSON ###
JSON files following the same schema as documented in the REST resources reference. There is one file per resource type.

### RDF ###
TODO
#### Schema ####
TODO

## Accessing the Data ##
Data can currently be accessed via:

- [x] HTTP 
- [x] AWS S3 sync 
- [ ] Torrent 
- [ ] sftp 
- [ ] ssh 
- [ ] rsync 

### HTTP ###
The most recent release of the data should always be available at: [http://dmd.ebor.tech/nhsdmdrelease/v0.0.1/data/latest/{format}], where format is one of:

```
csv
xml
json
rdf
```

Individual relases are available at http://dmd.ebor.tech/nhsdmdrelease/v0.0.1/data/{releasenumber}/{format}, for example [http://dmd.ebor.tech/nhsdmdrelease/v0.0.1/data/2.2.0/rdf] contains the rdf format data for release 2.2.0.

All requests must include an API key in the Authorization header. Response to this request will be a 302 Redirect to the data. 

### Example ###
The example below 

```
curl -H "Authorization: MYAPIKEY123" http://dmd.ebor.tech/nhsdmdrelease/v0.0.1/data/2.2.0/rdf
```

## AWS S3 Sync ##
The releases are held in the *nhsdmdrelase* AWS S3 bucket. This means that they can easily (and cheaply) be mirrored to another S3 bucket by using the aws SDK or cli tools. Before accessing the data you will need to request a temporary access token from the _/data_auth_ resource, passing it your API key and AWS principal. This will allow read access to the S3 bucket for an hour.

The layout of the bucket is: 


| Key                                                       | Content                                             |
|-----------------------------------------------------------|-----------------------------------------------------|
| nhsdmdrelase/{release}/*                                  | unzipped raw files from TRUD                        |
| nhsdmdrelease/{release}/.work/*                           | intermediate files used in transforming the data and can be ignored |
| nhsdmdrelease/v0.0.1/schema/{format}/*                    | schema files for the respective format, which are included in compressed bundles|
| nhsdmdrelease/v0.0.1/{release&#124;latest}/{format}       | bundled compressed content in the respective format |
| nhsdmdrelease/v0.0.1/{release&#124;latest}/{format}/*     | uncompressed individual files in the respective format |

### Examples ###
Once you have a temporary access token the _nhsdmdrelease_ bucket can be browsed from the AWS console, and synched or pulled using AWS cli or SDK as shown below.

```bash
aws s3 sync $(curl -H "Authorization: MYAPIKEY123" https://dmd.ebor.tech/nhsdmdrelease/v0.0.1/data_auth/raw) s3://nhsdmdrelease/v0.0.1/latest/csv s3://mypharmacy/dmd
```

or 

```python
import boto

AWS_ACCESS_KEY = 'Your access key'
AWS_SECRET_KEY = 'Your secret key'
DEST_BUCKET_NAME = 'mypharmacy'

conn = boto.s3.connection.S3Connection(AWS_ACCESS_KEY, AWS_SECRET_KEY)
bucket = boto.s3.bucket.Bucket(conn, 'nhsdmdrelease')

for item in bucket:
    bucket.copy(DEST_BUCKET_NAME, item.key)
```

