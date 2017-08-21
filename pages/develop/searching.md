---
title: Searching for Dispensers
keywords: develop
tags: [develop]
sidebar: overview_sidebar
permalink: searching.html
summary: How to use the search resources and how the system finds matching dispensers
---


The API provides two search resources:

  * `/dispensers/byLocationDisposition` - used when matching a resource to a patient based on the patient's need and location
  * `/dispensers/byName` - used to locate a specific dispenser which the patient may have named
  
The `/dispenser` resource also retrieves dispensers by ODS code, so may be used where the application keeps a list of commonly used dispensers for example.

## Searching by Location & Disposition ##

The most common operation will be to find a dispenser which can dispense a prescription to the patient within the timeframe dictated by the patient disposition. Patients disposition is allocated by NHS Pathways and include values like `Dx85	- Repeat prescription required within 2 hours`. Passing this disposition to the API will ensure that all dispensers returned in results are open within the next two hours. Once open dispensers are identified the system orders by distance and returs the five nearest. If a maximum distance filter is included this is applied, otherwise the system will default to a maximum distance of 38km. 


### Examples ###

```
TODO
````

## Searching by Name ##


### Examples ###

```
TODO
````

