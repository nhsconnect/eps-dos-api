#!/bin/bash
for f in `ls dispenser_ods`
do
  echo "processing dispensers in $f"
  echo "[" > dispenser_ods_output/$f
  ./get_pathways_dispensers.sh dispenser_ods/$f dispenser_ods_output/$f
  echo "finished processing dispensers in $f"
  echo "]" >> dispenser_ods_output/$f
done