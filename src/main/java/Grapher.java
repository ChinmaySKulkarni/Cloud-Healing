import weka.associations.AssociationRules;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grapher {

    HashMap<String, Integer> failureIndices = new HashMap<String, Integer>();
    HashMap<String, Integer> nodeIndices = new HashMap<String, Integer>();


    public Grapher() {
        failureIndices = new HashMap<String, Integer>();
    }

    public static void main(String... args){
        String actualFailuresFile = args[0];
        String predictedFailuresFile = args[1];
        String actualFailuresPlotFile = "data_sets/BGL/plot/" + actualFailuresFile.split("/")[2].replace(".ser", ".plot");
        String predictedFailuresPlotFile = "data_sets/BGL/plot/" + predictedFailuresFile.split("/")[2].replace(".ser", ".plot");
        Grapher grapher = new Grapher();
        grapher.writeDataPointsToFile(actualFailuresFile, actualFailuresPlotFile);
        grapher.writeDataPointsToFile(predictedFailuresFile, predictedFailuresPlotFile);
    }

    public void writeDataPointsToFile(String inputFile, String outputFile) {
        BufferedWriter writer = null;
        FileInputStream fout = null;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            fout = new FileInputStream(inputFile);
            ObjectInputStream oos = new ObjectInputStream(fout);
            HashMap<String, Map<String, List<Long>>> dataPoints = (HashMap<String, Map<String, List<Long>>>) oos.readObject();
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
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        if (fout != null) {
            try {
                fout.close();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    }
}
