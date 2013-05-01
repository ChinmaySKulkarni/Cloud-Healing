import java.io.*;
import java.util.*;
import weka.core.*;

import weka.associations.AssociationRules;
import weka.associations.Apriori;
import weka.core.converters.ConverterUtils;

public class AssociationRulesGenerator {

    private final String filename;
    private Apriori     apriori              = null;
    // apriori specific parameters
    private double deltaValue                = 0.05;
    private double lowerBoundMinSupportValue =  0.01;
    private int tagId                        =  0;  //Confidence
    // String  metricType: Confidence, Lift, Leverage, Conviction
    private double minMetricValue; //Confidence - Chinmay
    private int numRulesValue                = 9999999;
    private double significanceLevelValue    = -1.0; // (??)
    private double upperBoundMinSupportValue = 1.0;


    public static void main(String args[]) {
        System.out.println("Generating rules...");
        String filename  =  args[0];
        Double confidence = Double.parseDouble(args[1]);
        AssociationRulesGenerator associationRulesGenerator = new AssociationRulesGenerator(filename, confidence);
        associationRulesGenerator.run();


    } // end main


    //
    // constructor
    //

    AssociationRulesGenerator(String filename, double confidence) {
        this.filename = filename;
        this.minMetricValue = confidence;
    }

    public AssociationRules associate(Instances instances) {
        return(associate(instances,deltaValue, lowerBoundMinSupportValue, minMetricValue,
                numRulesValue, significanceLevelValue, upperBoundMinSupportValue,tagId));
    }

    //
    // the main association method
    //
    public AssociationRules associate(Instances instances, double deltaValue, double lowerBoundMinSupportValue, double minMetricValue, int numRulesValue, double significanceLevelValue, double upperBoundMinSupportValue, int tagId) {

        StringBuffer result = new StringBuffer();

        this.deltaValue = deltaValue;
        this.lowerBoundMinSupportValue = lowerBoundMinSupportValue;
        this.numRulesValue             = numRulesValue;
        this.minMetricValue            = minMetricValue;
        this.significanceLevelValue    = significanceLevelValue;

        this.upperBoundMinSupportValue = upperBoundMinSupportValue;

        try {

            //result.append("DATA SET:\n" + instances + "\n");

            // The Apriori Algorithm
            //
            //Tag[] tags = null;
            //SelectedTag d = new SelectedTag(tagId, tags);
            apriori = new Apriori();
            apriori.setDelta(deltaValue);
            //apriori.setMetricType(d);
            apriori.setSignificanceLevel(significanceLevelValue);
            apriori.setLowerBoundMinSupport(lowerBoundMinSupportValue);
            apriori.setMinMetric(minMetricValue);
            apriori.setNumRules(numRulesValue);
            apriori.setUpperBoundMinSupport(upperBoundMinSupportValue);
            apriori.setRemoveAllMissingCols(true);
            apriori.buildAssociations(instances);
            result.append(apriori.toString() + "\n");
            return apriori.getAssociationRules();

        } catch (Exception e) {
            e.printStackTrace();
            result.append("\nException (sorry!):\n" + e.toString());
        } finally {
            System.out.println(result);
        }

        //return result;

        return null;
    } // end associate

    public void setNumRules(int numRulesValue) {
        this.numRulesValue = numRulesValue;
    }

    public void run() {
        Instances   instances       = null;

        String[] name = filename.split("\\.");
        String resultFile;
        ConverterUtils.DataSource source = null;
        try {
            source = new ConverterUtils.DataSource(filename);
            instances = source.getDataSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Make the last attribute be the class
        instances.setClassIndex(instances.numAttributes() - 1);


        AssociationRules rules = associate(instances);

        //System.out.println(associationRules.associate(instances));
        FileOutputStream fout = null;
        try {
            resultFile = name[0] + "_" + minMetricValue + "-rules_apriori.ser";
            fout = new FileOutputStream(resultFile);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(rules);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
