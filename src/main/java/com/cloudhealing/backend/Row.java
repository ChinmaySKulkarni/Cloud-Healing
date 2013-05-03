package com.cloudhealing.backend;

public class Row {

    String node;
    String entry_data;
    long time;
    private static int timeIndex = 5;
    private static int nodeIndex = 6;
    private static int entry_dataIndex = 10;

    public static Row generate(String row) {
        String rowItem[] = row.split(",");
        //System.out.println(StringUtils.join(rowItem, "|"));
        return new Row(rowItem[nodeIndex], rowItem[entry_dataIndex], Long.parseLong(rowItem[timeIndex]));
    }

    Row(String node, String entry_data, long time) {
        this.node = node;
        this.entry_data = entry_data;
        this.time = time;
    }

    String getNode() {
        return node;
    }

    void setNode(String node) {
        this.node = node;
    }

    String getEntry_data() {
        return entry_data;
    }

    void setEntry_data(String entry_data) {
        this.entry_data = entry_data;
    }

    long getTime() {
        return time;
    }

    void setTime(long time) {
        this.time = time;
    }
}
