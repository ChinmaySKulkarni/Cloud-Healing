#!/bin/bash

select_data(){
		  #Input variables
		  file_name=${1}
		  echo "where file name is $file_name"
		
		  #Update the file

		  mysql -u cloud --password=Shaatir1\! data_set < $file_name
}
select_data $1

