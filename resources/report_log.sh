#!/bin/bash           

count=0
while read line
do
  ((count++))
  echo -e "Sending log. $count secs elapsed..."
  #curl -s -w "%{http_code}" -d "log=$line" http://10.1.99.24:9000/logger/sendLog.json
  stat=$(curl -d "log=$line" -s -w "%{http_code}\\n" "http://10.1.99.24:9000/logger/sendLog.json" -o /dev/null)
  
  if [[ "$stat" == 202 ]]
  then
    echo -e "Failure Anticipated.. Healing Action Taking Place..."
  fi

  if [[ "$line" == *Failure* ]]
  then
    halt
  fi
  sleep 1
done < log_file
