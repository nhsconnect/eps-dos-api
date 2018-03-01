---
title: Development Overview
keywords: develop
tags: [develop]
sidebar: overview_sidebar
permalink: develop_overview.html
summary: An overview of the information available in the EPS DoS API
---

The EPS DoS API provides a search interface for retrieving information on EPS dispensers.
The information is drawn from NHS Pathways and NHS Choices web services. The web service is
a simple RESTful style service returning JSON dispenser resources. 

## Dispenser Information##

The dispenser object includes the following information:

* ODS Code
* Name
* Service type _currently only EPS R2 enabled pharmacies_
* Address
  - Address line _x 4_
  - Postcode
* Patient contact details, which should not be publicly available
  - Phone
  - Web
* Contact details for prescriber use, which should not be publicly available
  - Phone
  - Fax
  - Email
* Location
  - Easting
  - Northing
* Opening Hours
  - 24/7 opening
  - Monday
    + Open _HH:MM format_
    + Close _HH:MM format_
  - ...
  - Sunday
    + Open
    + Close
  - Bank holiday
    + Open
    + Close
  - Specified dates
    + Date _YYYY-MM-DD format_
      - Open
      - Close
