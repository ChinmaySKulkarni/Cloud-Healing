#!/bin/bash

select_data(){
		  #Input variables
		  where_clause=${@}
		  echo "where clause is $where_clause"
		
		  #Update the file

		  mysql -u cloud --password=Shaatir1\! -e \
                  "select facility, severity, entry_data from data_set.blue_gene_ras where entry_data like '$where_clause' group by entry_data limit 10;"
}

while read line
do
    select_data $line
    echo "Done!"
done < /dev/tty
