package com.cloudhealing.backend;

import java.io.*;
import java.util.*;

public class Grapher {

    private HashMap<String, Integer> failureIndices;
    private HashMap<String, Integer> nodeIndices;
    private String actualFailuresFile;
    private String predictedFailuresFile;
    private HashMap<String, Map<String, List<Long>>> actualFailures;
    private HashMap<String, Map<String, List<Long>>> predictedFailures;
    private HashMap<Long, AccuracyDataPoint> accuracyDistribution;
    private static final int ACCURACY_INTERVAL = 7;
    private static final int TIME_DIFFERENCE = 300;

    public Grapher(String actualFailuresFile, String predictedFailuresFile) {
        this.failureIndices = new HashMap<String, Integer>();
        this.nodeIndices = new HashMap<String, Integer>();
        this.actualFailuresFile = actualFailuresFile;
        this.predictedFailuresFile = predictedFailuresFile;
        this.accuracyDistribution = new HashMap<Long, AccuracyDataPoint>();
        this.actualFailures = readObject(this.actualFailuresFile);
        this.predictedFailures = readObject(this.predictedFailuresFile);
    }

    public void calculatePrecisionRecall()
    {
        for (String node : predictedFailures.keySet()) {
            if (actualFailures.get(node) == null) {
                System.out.println("here");
                continue;
            }
            Map<String, List<Long>> failureMap = predictedFailures.get(node);
            for(String failure : failureMap.keySet()) {
                List<Long> failureTimes = failureMap.get(failure);
                for (Long time : failureTimes) {
                    Long failureTime = time + TIME_DIFFERENCE;
                    List<Long> actualFailureTimes = actualFailures.get(node).get(failure);
                    if (actualFailureTimes == null) {
                        registerFalsePositive(time);
                    } else {
                        boolean alreadyRegistered = false;
                        for (Long actualTime : actualFailureTimes) {
                            if (actualTime >= time && actualTime <= failureTime) {
                                registerTruePositive(time);
                                alreadyRegistered = true;
                                break;
                            } else if (actualTime > failureTime) {
                                registerFalsePositive(time);
                                alreadyRegistered = true;
                                break;
                            }
                        }
                        if (!alreadyRegistered)
                            registerFalsePositive(time);
                    }
                }
            }
        }

        //code for False Negatives
        for (String node : actualFailures.keySet()) {
            Map<String, List<Long>> failureMap = actualFailures.get(node);
            for(String failure : failureMap.keySet()) {
                List<Long> failureTimes = failureMap.get(failure);
                for (Long time : failureTimes) {
                    try {
                        List<Long> predictedFailureTimes = predictedFailures.get(node).get(failure);
                        if (predictedFailureTimes == null) {
                            registerFalseNegative(time);
                        } else {
                            boolean foundOne = false;
                            for (Long predictedTime : predictedFailureTimes) {
                                if (time >= predictedTime && time <= predictedTime + TIME_DIFFERENCE) {
                                    foundOne = true;
                                    break;
                                }
                            }
                            if (!foundOne)
                                registerFalseNegative(time);
                        }
                    } catch (NullPointerException e) {
                        //e.printStackTrace();
                    }
                }
            }
        }

        //Writing to file
        writeAccuracyDataPointsToFile();
    }

    public void writeAccuracyDataPointsToFile() {
        String accuracyFile = "data_sets/BGL/plot/" + actualFailuresFile.split("/")[2].replace(".ser", ".plot").replace("actualFailures", "accuracy");
        writeAccuracyDataPointsToFile(accuracyFile);
    }
    public void writeAccuracyDataPointsToFile(String fileName) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            //System.out.println(accuracyDistribution.toString());
            ArrayList<Long> sortedList = new ArrayList<Long>(accuracyDistribution.keySet());
            //System.out.println("List" + sortedList);
            Collections.sort(sortedList);
            //System.out.println("Sorted List" + sortedList);
            for (Long date : sortedList) {
                AccuracyDataPoint dataPoint = accuracyDistribution.get(date);
                Double precision = dataPoint.getPrecision();
                Double recall = dataPoint.getRecall();
                if ((precision != null && recall != null) && (precision != 0 && recall != 0))
                    writer.write(date + "\t" + dataPoint.getPrecision() + "\t" + dataPoint.getRecall() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void registerTruePositive(Long time) {
        AccuracyDataPoint dataPoint = getAccuracyDataPoint(time);
        dataPoint.setTruePositive(dataPoint.getTruePositive() + 1);
    }

    private AccuracyDataPoint getAccuracyDataPoint(Long time) {
        //Calendar cal = Calendar.getInstance();
        //cal.setTimeInMillis(time*1000);
        //String date = Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + Integer.toString(cal.get(Calendar.MONTH)) + Integer.toString(cal.get(Calendar.YEAR));
        time = time - (time % (ACCURACY_INTERVAL * 86400));
        AccuracyDataPoint dataPoint = accuracyDistribution.get(time);
        if(dataPoint == null) {
            dataPoint = new AccuracyDataPoint();
            accuracyDistribution.put(time, dataPoint);
        }
        return dataPoint;
    }

    private void registerFalsePositive(Long time) {
        AccuracyDataPoint dataPoint = getAccuracyDataPoint(time);
        dataPoint.setFalsePositive(dataPoint.getFalsePositive() + 1);
    }

    private void registerFalseNegative(Long time) {
        AccuracyDataPoint dataPoint = getAccuracyDataPoint(time);
        dataPoint.setFalseNegative(dataPoint.getFalseNegative() + 1);
    }

    public HashMap<String, Map<String, List<Long>>> readObject(String fileName) {
        FileInputStream fout = null;
        HashMap<String, Map<String, List<Long>>> object = null;
        try {
            fout = new FileInputStream(fileName);
            ObjectInputStream oos = new ObjectInputStream(fout);
            object = (HashMap<String, Map<String, List<Long>>>) oos.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fout != null) try {
                fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return object;
        }
    }

    public static void main(String... args){

        String actualFailuresFile = args[0];
        String predictedFailuresFile = args[1];

        Grapher grapher = new Grapher(actualFailuresFile, predictedFailuresFile);
        //grapher.writeDataPointsToFile();
        grapher.calculatePrecisionRecall();
        /*
        HashMap<String, AccuracyDataPoint> test = new HashMap<String, AccuracyDataPoint>();
        AccuracyDataPoint a = new AccuracyDataPoint(1, 1, 1);
        test.put("A", a);
        a.setTruePositive(10);
        a = test.get("A");
        //a.setTruePositive(20);
        System.out.println(test.get("A").getTruePositive());
        */
    }

    public void writeDataPointsToFile() {
        String actualFailuresPlotFile = "data_sets/BGL/plot/" + actualFailuresFile.split("/")[2].replace(".ser", ".plot");
        String predictedFailuresPlotFile = "data_sets/BGL/plot/" + predictedFailuresFile.split("/")[2].replace(".ser", ".plot");
        writeDataPointsToFile(actualFailures, actualFailuresPlotFile);
        writeDataPointsToFile(predictedFailures, predictedFailuresPlotFile);
        writeDataPointsToFile();
    }
    public void writeDataPointsToFile(HashMap<String, Map<String, List<Long>>> dataPoints, String outputFile) {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(outputFile));

            for(String node : dataPoints.keySet()) {
                Integer nodeIndex = nodeIndices.get(node);
                if (nodeIndex == null) {
                    if (nodeIndices.values().size() != 0) {
                        nodeIndices.put(node, Collections.max(nodeIndices.values()) + 1);
                    } else {
                        nodeIndices.put(node, 0);
                    }
                }
                nodeIndex = nodeIndices.get(node);
                if (nodeIndex > 7346)
                    continue;
                Map<String, List<Long>> failureTimes = dataPoints.get(node);
                for(String failure : failureTimes.keySet()) {
                    List<Long> times = failureTimes.get(failure);
                    Integer failureIndex = failureIndices.get(failure);
                    if (failureIndex == null) {
                        if (failureIndices.values().size() != 0) {
                            failureIndices.put(failure, Collections.max(failureIndices.values()) + 1);
                        } else {
                            failureIndices.put(failure, 0);
                        }
                    }
                    failureIndex = failureIndices.get(failure);
                    //if (failureIndex > 35)
                        //continue;
                    for (long time : times)
                        writer.write(nodeIndex + "\t" + failureIndex + "\t" + time + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
class AccuracyDataPoint {
    private int truePositive;
    private int falsePositive;
    private int falseNegative;

    int getTruePositive() {
        return truePositive;
    }

    void setTruePositive(int truePositive) {
        this.truePositive = truePositive;
    }

    int getFalsePositive() {
        return falsePositive;
    }

    void setFalsePositive(int falsePositive) {
        this.falsePositive = falsePositive;
    }

    int getFalseNegative() {
        return falseNegative;
    }

    void setFalseNegative(int falseNegative) {
        this.falseNegative = falseNegative;
    }

    public AccuracyDataPoint() {
        this.truePositive = 0;
        this.falseNegative = 0;
        this.falsePositive = 0;
    }

    public AccuracyDataPoint(int truePositive, int falsePositive, int falseNegative) {
        this.truePositive = truePositive;
        this.falsePositive = falsePositive;
        this.falseNegative = falseNegative;
    }


    public Double getPrecision() {
        if ((truePositive + falsePositive) != 0 )
            return new Double((double)truePositive/(truePositive + falsePositive));
        return null;
    }

    public Double getRecall() {
        if ((truePositive + falseNegative) != 0)
            return new Double((double)truePositive/(truePositive + falseNegative));
        return null;
    }

    public String toString() {
        return (" TP:" + truePositive + " FN:" + falseNegative + " FP:" + falsePositive);
    }
}