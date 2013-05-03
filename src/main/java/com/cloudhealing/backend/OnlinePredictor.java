package com.cloudhealing.backend;

import weka.associations.AssociationRule;
import weka.associations.AssociationRules;
import weka.associations.Item;

import java.io.*;
import java.util.*;

public class OnlinePredictor {
    //prepare F list:records list of triggering events for each failure event
    private Map<String, List<Set<String>>> fList;
    private Map<String, Set<String>> eList;
    private Map<String, Map<String, List<Long>>> wLists;
    private Map<String, Map<String, List<Long>>> predictedFailures;
    private Map<String, Map<String, List<Long>>> actualFailures;

    private static final String testingFile = "data_sets/BGL/testing_data.csv";
    private static final long timeDifference = 300;

    private String rulesFile;
    private String actualFailuresFile;
    private String predictedFailuresFile;

    public OnlinePredictor(String rulesFile, String actualFailuresFile, String predictedFailuresFile) {
        fList = new HashMap<String, List<Set<String>>>();
        eList = new HashMap<String, Set<String>>();
        wLists = new HashMap<String, Map<String, List<Long>>>();
        predictedFailures = new HashMap<String, Map<String, List<Long>>>();
        actualFailures = new HashMap<String, Map<String, List<Long>>>();
        this.rulesFile = rulesFile;
        this.actualFailuresFile = actualFailuresFile;
        this.predictedFailuresFile = predictedFailuresFile;
        generateLists();

    }

    public void generateLists() {
        FileInputStream fout = null;
        try {
            fout = new FileInputStream(rulesFile);
            ObjectInputStream oos = new ObjectInputStream(fout);
            AssociationRules rules = (AssociationRules) oos.readObject();

            /*
            System.out.println(rules.getRules().toString());
            System.out.println("Count : " + rules.getNumRules());
            */

            for(AssociationRule rule : rules.getRules() ) {
                Collection<Item> rhsList = rule.getConsequence();
                Collection<Item> lhsList = rule.getPremise();
                for(Item rhs : rhsList) {
                    if(rhs.getAttribute().name().contains("Failure")) {
                        //FList
                        List<Set<String>> triggers = fList.get(rhs.getAttribute().name());
                        if (triggers == null)
                            triggers = new ArrayList<Set<String>>();

                        HashSet<String> set = new HashSet<String>();

                        for(Item lhs : lhsList) {
                            set.add(lhs.getAttribute().name());
                            //System.out.println(lhs.getAttribute().name());

                            //EList
                            Set<String> failuresTriggered = eList.get(lhs.getAttribute().name());
                            if (failuresTriggered == null)
                                failuresTriggered = new HashSet<String>();

                            failuresTriggered.add(rhs.getAttribute().name());
                            eList.put(lhs.getAttribute().name(), failuresTriggered);
                            //EList ends
                        }

                        triggers.add(set);
                        fList.put(rhs.getAttribute().name(), triggers);

                    }

                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
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

    private void predict() {
        BufferedReader reader = null;
        String rowString;
        try {
            reader = new BufferedReader(new FileReader(testingFile));
            int counter  = 0;
            while ((rowString = reader.readLine()) != null) {
                Row row = Row.generate(rowString);
                updateWList(row.node, row.time);
                if((counter % 66631) == 0)
                    System.out.println("Rows Completed : " + counter);
                predict(row.node, row.time, row.entry_data);
                counter++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    writeResultsToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void writeToFile(String filename, Object object) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(object);
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
    private void writeResultsToFile() {
        writeToFile(actualFailuresFile, actualFailures);
        writeToFile(predictedFailuresFile, predictedFailures);
    }

    private void updateWList(String node, long time) {
        Map<String, List<Long>> currentList = wLists.get(node);
        List<String> keysToRemove = new ArrayList<String>();
        if (currentList != null) {
            for (String key : currentList.keySet()) {
                List<Long> eventTimes = currentList.get(key);
                List<Long> newEventTimes = new ArrayList<Long>();
                for(long eventTime : eventTimes) {
                    if((time - eventTime) <= timeDifference)
                        newEventTimes.add(eventTime);
                }
                if (newEventTimes.size() != 0) {
                    currentList.put(key, newEventTimes);
                } else {
                    keysToRemove.add(key);
                }
            }
            for (String key : keysToRemove)
                currentList.remove(key);
        }
    }

    public void predict(String node, long time, String entry_data) {
        addToWLists(node, time, entry_data);
        runPredictor(node, time, entry_data);
        if (entry_data.contains("Failure")) {
            //System.out.println("+");
            recordFailure(node, time, entry_data);
        }
    }

    private boolean runPredictor(String node, long time, String entry_data) {
        Set<String> failuresTriggered = eList.get(entry_data);
        if (failuresTriggered != null) {
            Set<String> currentWList = wLists.get(node).keySet();
            for (String failure : failuresTriggered) {
                List<Set<String>> triggeringEvents = fList.get(failure);
                for (Set eventSet : triggeringEvents) {
                    if (currentWList.containsAll(eventSet)) {
                        raiseWarning(node, time, failure);
                        //System.out.print(".");
                        //return true;
                        continue;
                    }
                }

            }
        }
        return false;
    }

    private void addToFailureLists(Map<String, Map<String, List<Long>>> failureList, String node, long time, String failure) {
        Map<String, List<Long>> nodeFailures = failureList.get(node);
        if (nodeFailures == null)
            nodeFailures = new HashMap<String, List<Long>>();
        List<Long> failureTimes = nodeFailures.get(failure);
        if (failureTimes == null)
            failureTimes = new ArrayList<Long>();
        failureTimes.add(time);
        nodeFailures.put(failure, failureTimes);
        failureList.put(node, nodeFailures);
    }


    private void recordFailure(String node, long time, String failure) {
        addToFailureLists(actualFailures, node, time, failure);
    }

    private void raiseWarning(String node, long time, String failure) {
        addToFailureLists(predictedFailures, node, time, failure);
    }


    private void addToWLists(String node, long time, String entry_data) {
        Map<String, List<Long>> wList = wLists.get(node);
        if (wList == null)
            wList = new HashMap<String, List<Long>>();
        List<Long> eventTimes = wList.get(entry_data);
        if (eventTimes == null)
            eventTimes = new ArrayList<Long>();
        eventTimes.add(time);
        wList.put(entry_data, eventTimes);
        wLists.put(node, wList);
    }

    public void verify() {

    }


    public static void main(String... args) {
        String rulesFile = args[0];
        String actualFailuresFile = args[1];
        String predictedFailuresFile = args[2];
        OnlinePredictor predictor = new OnlinePredictor(rulesFile, actualFailuresFile, predictedFailuresFile);
        predictor.predict();
        /*
        HashSet<String> superSet = new HashSet<String>();
        superSet.add("A");
        superSet.add("B");
        superSet.add("C");
        HashSet<String> subSet = new HashSet<String>();
        subSet.add("A");
        subSet.add("B");
        System.out.println("Result: " + superSet.containsAll(subSet));
        */
    }
}
