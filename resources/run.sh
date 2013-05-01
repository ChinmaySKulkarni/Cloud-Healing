export MAVEN_OPTS="-Xmx3000m -Xms2048m -XX:MaxPermSize=512"
for confidence in 0.4 0.6
do
	mvn exec:java -Dexec.mainClass=AssociationRulesGenerator -Dexec.args="data_sets/BGL/blue_gene.arff $confidence"
	mvn exec:java -Dexec.mainClass=Predictor -Dexec.args="data_sets/BGL/blue_gene_$confidence-rules_apriori.ser data_sets/BGL/actualFailures_$confidence.ser data_sets/BGL/predictedFailures_$confidence.ser"
	mvn exec:java -Dexec.mainClass=Grapher -Dexec.args="data_sets/BGL/actualFailures_$confidence.ser data_sets/BGL/predictedFailures_$confidence.ser"
done
