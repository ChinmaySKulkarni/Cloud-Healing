export MAVEN_OPTS="-Xmx3000m -Xms2048m -XX:MaxPermSize=512"
mvn exec:java -Dexec.mainClass="ARFFGenerator" -Dexec.args="data_sets/BGL/error_codes_list data_sets/BGL/blue_gene_filtered.csv data_sets/BGL/blue_gene.arff" 1> data_sets/BGL/blue_gene.arff
