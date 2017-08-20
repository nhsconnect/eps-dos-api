---
title: Developing with DM+D resources
keywords: develop
tags: [develop]
sidebar: overview_sidebar
permalink: kop.html
summary: REST endpoints
---
# Extracting EPS Message Data for a Clinical Safety Pack
## Introduction
When changes are introduced in dispensing and prescribing systems a clinical safety pack containing the contents of EPS messages relating to a given number of prescribed items is produced for review by an EPS clinician. The purpose of this is for the structure of the messages to be checked by Solutions Assurance tooling (currently TKW) and for the messages' clinical content to be reviewed.
Historically the extract and input process has been complicated as it required suppliers to extract from their systems, anonymise and send. This often resulted in wrongly encoded or extracted messages and difficulty delivering due to size. 

## Prerequisites

- Access to Spine audit logs
- Access to Cherwell or ability to raise service requests
- Prescriptions issued within the last 56 days


## Process
The process should be similar for both dispensing and prescribing systems:

1. Identify the test systems
2. Get the internalIDs of prescription upload or download messages for prescribing and dispensing systems respectively
3. Get the prescription IDs for created/downloaded prescriptions
4. Get the internalIDs for EPS messages relating to the identified prescription IDs
5. Raise a service request for Spine operations to extract the messages with those internalIDs
6. Spine ops team curl the identified messages from the event source (message store) and run an XSLT which removes patient details

### Pharmacy Messaging
Identify the ODS code and time period for message capture
Get the ASID for the ODS codes by searching SDS for:
```
(&(nhsidcode={ODS code}})(objectclass=nhsas)) 
```
and noting the *dn* attribute for the appropriate system - this is the ASID.

List the nominated prescription download messages by running:
```
index=spine* outboundInteractionID=PORX_IN070101UK31 messageSender={asid} | table internalID
```
This should be a small time period as is likely to be a large number. Messages are archived after 56 days so prescriptions should be more recent thatn this. Once the search is finished extract and save the data.

Get the prescription IDs in nominated DLs by searching over the same time period:
```
index=spinevfm* logReference=EPS0271 prescriptionID [search index=spinevfm* outboundInteractionID=PORX_IN070101UK31 messageSender={asid} | table internalID]| stats count by prescriptionID | fields + prescriptionID
```
Once the search is finished extract the data.

From the saved list of prescriptionIDs generate prescription ID query strings (e.g. in Excel) of the form *8FA0EC-B84005-BE7FD OR 934DD7-B85020-D46B5 OR 935B48-C86038-082ED... OR {prescription n}*

List the messages relating to these prescriptions by searching for:
```
index=spine* (Process=porx_in090101uk31_w* OR  Process=porx_in080101uk31_w* {...OR any other interaction IDs here}) logReference=EPS0139 0203FE-E87738-36530 OR ... FB5F63-E87013-3D4D3 OR FF7D72-E87738-36BC5 | table internalID
```
There is a limit to the size of the query, so it may be necessary to query in batches of say 400 prescription IDs.
The query time period should be any time after the prescription download. 

Cat all of the output together te
Raise a service request with Spine core requesting 
