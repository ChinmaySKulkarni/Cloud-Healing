#!/bin/bash
while read line
do
  first_part=`echo $line | cut -f 1-2 -d\ `
  second_part=`echo $line | cut -f 3 -d\ `
  let day=second_part-1128396134
  let day=day/86400
  echo "$first_part $day"
done < $1
