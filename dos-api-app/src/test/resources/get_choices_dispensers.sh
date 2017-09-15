#!/bin/bash
while read d;
do
  
  curl "http://nww.etpwebservices.cfh.nhs.uk/ETPWebservices/service.asmx/GetDispenserByNacsCode?strnacscode=$d" > choices_dispenser_$d.xml

done < $1