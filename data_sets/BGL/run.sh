#!/bin/bash

create_table() {
mysql -u cloud --password=Shaatir1\! -e '
	CREATE  TABLE `data_set`.`blue_gene_ras_bak` (
	  `rec_id` INT NOT NULL ,
	  `ex_tag` VARCHAR(45) NULL ,
	  `ex_utime` INT(11) NULL ,
	  `ex_date` DATE NULL ,
	  `ex_source` VARCHAR(45) NULL ,
	  `event_time` DATETIME NULL ,
	  `location` VARCHAR(45) NULL ,
	  `event_type` VARCHAR(45) NULL ,
	  `facility` VARCHAR(45) NULL ,
	  `severity` VARCHAR(45) NULL ,
	  `entry_data` TEXT NULL ,
	  PRIMARY KEY (`rec_id`) );' data_set

}
load_data(){
		  #Input variables
		  input_file=${1}
		  echo "input file is $input_file"
		
		  #Update the file

		  mysql  --local-infile -u cloud --password=Shaatir1\! <<!
		  load data local infile '$input_file' 
		  into table data_set.blue_gene_ras_bak
		  fields terminated by ','
		  lines terminated by '\n'
		  (rec_id, ex_tag, ex_utime, @ex_date, ex_source, @event_time, location, event_type, facility, severity, entry_data)
                  set ex_date = str_to_date(@ex_date, '%Y.%m.%e'),
                      event_time = str_to_date(@event_time, '%Y-%m-%e-%H.%i.%S.%f');
!
}
create_table
load_data $1
