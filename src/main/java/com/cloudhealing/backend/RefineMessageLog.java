package com.cloudhealing.backend;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: chinmay
 * Date: 17/4/13
 * Time: 3:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class RefineMessageLog {
    public static final String TABLE_NAME = "blue_gene_ras";

    public void refine(String inputFileName, String outputFileName){
        try {
            File output = new File(outputFileName);
            output.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        BufferedReader messageLog = null;
        BufferedWriter refinedMessageLog = null;
        String dataRow;
        try {
            messageLog = new BufferedReader(new FileReader(inputFileName));
            refinedMessageLog = new BufferedWriter(new FileWriter(outputFileName));
            dataRow = messageLog.readLine();
            while(dataRow != null){
                String[] recordRow = dataRow.split("\t");
                String query = "UPDATE " + TABLE_NAME +" SET entry_data = '" + recordRow[1] + "' WHERE entry_data like '" + recordRow[0] + "';";
                refinedMessageLog.write(query);
                refinedMessageLog.newLine();
                dataRow = messageLog.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }finally {
            try {
                messageLog.close();
                refinedMessageLog.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        String inputFile = args[0];
        String[] fileName = inputFile.split("\\.");
        String outputFile = fileName[0] + "_query.sql";
        RefineMessageLog data = new RefineMessageLog();
        data.refine(inputFile,outputFile);
    }
}