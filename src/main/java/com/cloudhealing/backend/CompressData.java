package com.cloudhealing.backend;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CompressData {

    private static final int recIdIndex = 0;
    private static final int exTagIndex = 1;
    private static final int exUtimeIndex = 2;
    private static final int exDateIndex = 3;
    private static final int exSourceIndex = 4;
    private static final int eventTimeIndex = 5;
    private static final int locationIndex = 6;
    private static final int eventTypeIndex = 7;
    private static final int facilityIndex = 8;
    private static final int severityIndex = 9;
    private static final int entryDataIndex = 10;
    private static final String DELIMITER = "\\|";

    private static final int timeDifference = 300;

    public void temporalCompress(String inFileName, String outFileName) {
        ArrayList<Integer> fieldIndexList = new ArrayList<Integer>();
        fieldIndexList.add(locationIndex);
        fieldIndexList.add(entryDataIndex);

        compress(inFileName, outFileName, fieldIndexList, eventTimeIndex, timeDifference);
    }

    public void spatialCompress(String inFileName, String outFileName) {
        ArrayList<Integer> fieldIndexList = new ArrayList<Integer>();
        fieldIndexList.add(entryDataIndex);
        fieldIndexList.add(4);
        fieldIndexList.add(8);

        compress(inFileName, outFileName, fieldIndexList, eventTimeIndex, timeDifference);
    }

    public void removeDuplicates(String inFileName, String outFileName) {
        ArrayList<Integer> fieldIndexList = new ArrayList<Integer>();
        fieldIndexList.add(2);
        fieldIndexList.add(4);
        fieldIndexList.add(8);
    }

    private void compress(String inFileName, String outFileName, List<Integer> fieldIndexList, int timeIndex, long timeDifference) {
        BufferedReader BGLFile = null;
        int rowCount = 0;
        //BufferedWriter OutFile = null;
        String dataRow;
        HashMap<List<String>, String[]> RecordLookup = new HashMap<List<String>, String[]>();
        try {
            BGLFile = new BufferedReader(new FileReader(inFileName));
            //OutFile = new BufferedWriter(new FileWriter(outFileName));
            rowCount++;
            dataRow = BGLFile.readLine();
            long currentTime = 0L;
            while (dataRow != null) {
                String[] recordRow = dataRow.split(DELIMITER);

                ArrayList<String> recordRowList = new ArrayList<String>();
                for(int fieldIndex : fieldIndexList)
                    recordRowList.add(recordRow[fieldIndex]);

                String[] temp = RecordLookup.get(recordRowList);
                if (temp == null) {
                    RecordLookup.put(recordRowList, recordRow);
                    //OutFile.write(StringUtils.join(recordRow, ","));
                    //OutFile.flush();
                    //System.out.println(StringUtils.join(recordRow, ","));
                }

                else if((Long.parseLong(recordRow[timeIndex])-Long.parseLong(temp[timeIndex])) > timeDifference) {
                    // Write new data and push this new record in.
                    RecordLookup.put(recordRowList, recordRow);
                    //OutFile.write(StringUtils.join(recordRow, ","));
                    //OutFile.flush();
                    //System.out.println(StringUtils.join(recordRow, ","));
                    System.out.println(StringUtils.join(temp, ","));
                }

                if((Long.parseLong(recordRow[timeIndex])-currentTime) > 10*timeDifference) {
                    currentTime = Long.parseLong(recordRow[timeIndex]);
                    ArrayList<List<String>> keysToRemove = new ArrayList<List<String>>();
                    for (List<String> key : RecordLookup.keySet()) {
                        String[] row = RecordLookup.get(key);
                        if (currentTime-Long.parseLong(row[timeIndex]) > timeDifference) {
                            System.out.println(StringUtils.join(row, ","));
                            keysToRemove.add(key);
                        }
                    }
                    for (List<String> key : keysToRemove) {
                        RecordLookup.remove(key);
                    }
                }
                rowCount++;
                dataRow = BGLFile.readLine();
            }
            System.err.println("########-Writing extra values-########");
            for (String[] remainingRow : RecordLookup.values()) {
                System.out.println(StringUtils.join(remainingRow, ","));
            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Row:" + rowCount);
        } finally {
            try {
                BGLFile.close();
                //OutFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        String inputFile = args[1];
        String temporalFile = inputFile.substring(0, inputFile.length()-4) + "_temporal.csv";
        String spatialFile = temporalFile.substring(0, temporalFile.length()-4) + "_spatial.csv";

        CompressData data = new CompressData();
        if(args[0].equals("temporal")) {
            System.err.println("########-Starting Temporal Compression-########");
            data.temporalCompress(inputFile, temporalFile);
            System.err.println("########-Temporal Compression Over-########");
        }
        else if (args[0].equals("spatial")) {
            System.err.println("########-Starting Spatial Compression-########");
            data.spatialCompress(temporalFile, spatialFile);
            System.err.println("########-Spatial Compression Over-########");
        }
    }
}