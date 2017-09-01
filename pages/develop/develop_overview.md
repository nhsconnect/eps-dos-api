---
title: Development Overview
keywords: develop
tags: [develop]
sidebar: overview_sidebar
permalink: develop_overview.html
summary: An overview of the information available in the EPS DoS API
---

## Dispenser Information##

The dispenser object includes the following information:

* ODS Code
* Name
* Service type _currently only EPS R2 enabled pharmacies_
* Address
  - Address line x 4
  - Postcode
* Patient Contact Details
  - Phone
  - Web
* Prescriber Contact Details
  - Phone
  - Fax
  - Email
* Location
  - Easting
  - Northing
* Opening Hours
  - 24/7 opening
  - Monday
    + Open
    + Close
  - ...
  - Sunday
    + Open
    + Close
  - Bank holiday
    + Open
    + Close
  - Specified dates

