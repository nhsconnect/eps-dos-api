#!/bin/bash
while read d;
do
  curl --basic -u robgooch:Abigail01 https://uat.pathwaysdos.nhs.uk/app/controllers/api/v1.0/services/byOdsCode/$d >> $2
  echo ","  >> $2
  sleep 0.3
done < $1