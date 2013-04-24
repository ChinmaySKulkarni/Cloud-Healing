/**
 * Created with IntelliJ IDEA.
 * User: chinmay
 * Date: 21/4/13
 * Time: 7:16 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.*;
import java.util.*;
import weka.core.*;

import weka.associations.*;
import weka.associations.Apriori.*;
import weka.core.converters.ConverterUtils;

public class AssociationRules {

    private Apriori     apriori              = null;
    // apriori specific parameters
    private double deltaValue                = 0.05;
    private double lowerBoundMinSupportValue =  0.01;
    private int tagId                        =  0;  //Confidence
    // String  metricType: Confidence, Lift, Leverage, Conviction
    private double minMetricValue            = 0.1;
    private int numRulesValue                = 9999999;
    private double significanceLevelValue    = -1.0; // (??)
    private double upperBoundMinSupportValue = 1.0;


    public static void main(String args[]) {

        Instances   instances       = null;
        String filename  =  args[0];
        String[] name = filename.split("\\.");
        String resultFile = name[0] + "_rules_apriori.txt";
        ConverterUtils.DataSource source = null;
        try {
            source = new ConverterUtils.DataSource(filename);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            instances = source.getDataSet();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        // Make the last attribute be the class
        instances.setClassIndex(instances.numAttributes() - 1);

        AssociationRules associationRules = new AssociationRules();

        //System.out.println(associationRules.associate(instances));
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(resultFile));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            out.write(associationRules.associate(instances).toString());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    } // end main


    //
    // constructor
    //

    AssociationRules() { }

    public StringBuffer associate(Instances instances ) {
        return(associate(instances,deltaValue, lowerBoundMinSupportValue, minMetricValue,
                numRulesValue, significanceLevelValue, upperBoundMinSupportValue,tagId));
    }

    //
    // the main association method
    //
    public StringBuffer associate(Instances instances, double deltaValue, double lowerBoundMinSupportValue, double minMetricValue, int numRulesValue, double significanceLevelValue, double upperBoundMinSupportValue, int tagId) {

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

            apriori.buildAssociations(instances);
            result.append(apriori.toString() + "\n");


        } catch (Exception e) {
            e.printStackTrace();
            result.append("\nException (sorry!):\n" + e.toString());
        }

        return result;

    } // end associate

    public void setNumRules(int numRulesValue) {
        this.numRulesValue = numRulesValue;
    }
}
