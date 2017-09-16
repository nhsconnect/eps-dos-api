import json, sys

json_object = json.load(sys.stdin)
for dispenser in json_object:
  service = dispenser['success']['services'][0]
  odsCode = service['odsCode']
  for d in service['openingTimes']['days']:
    if len(d['sessions']) > 1:
      print(odsCode)