#Export table to csv file
select rec_id, ex_tag, ex_utime, ex_date, ex_source, unix_timestamp(event_time), location, event_type, facility, severity, entry_data into outfile '/tmp/blue_gene_scrubbed.csv' fields terminated by '|' escaped by '\\' lines terminated by '\n' from blue_gene_ras;

