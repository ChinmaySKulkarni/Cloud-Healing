#!/bin/bash


load_data(){
		  #Input variables
		  input_file=${1}
		  echo "input file is $input_file"
		
		  #Update the file

		  mysql  --local-infile -u cloud --password=Shaatir1\! <<!
		  load data local infile '$input_file' 
		  into table data_set.blue_gene_ras
		  fields terminated by ','
		  lines terminated by '\n'
		  (rec_id, ex_tag, ex_utime, @ex_date, ex_source, @event_time, location, event_type, facility, severity, entry_data)
                  set ex_date = str_to_date(@ex_date, '%Y.%m.%e'),
                      event_time = str_to_date(@event_time, '%Y-%m-%e-%H.%i.%S.%f');
!
}

load_data $1
