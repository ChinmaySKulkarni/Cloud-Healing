export MAVEN_OPTS="-Xmx3000m -Xms2048m -XX:MaxPermSize=512"
mvn exec:java -Dexec.mainClass=AssociationRulesGenerator -Dexec.args=data_sets/BGL/blue_gene.arff
mvn exec:java -Dexec.mainClass=Predictor
mvn exec:java -Dexec.mainClass=Grapher
