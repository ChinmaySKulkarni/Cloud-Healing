import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

public class ARFFGenerator {
    private LinkedHashMap<String, String> defaultLinkedHashMap;
    private static final int NODE_ID_INDEX = 6;
    private static final int DATE_INDEX = 5;
    private static final int ENTRY_DATA_INDEX = 10;
    private static final int TIME_DIFFERENCE = 300;
    private static final String DELIMITER = ",";
    private String inputFile;
    private String outputFile;
    public ARFFGenerator(String errorCodeFile, String inputFile, String outputFile) {
        BufferedWriter outFile = null;
        try {
            BufferedReader codeFile = new BufferedReader(
                    new FileReader(errorCodeFile));
            String errorCode = null;
            defaultLinkedHashMap = new LinkedHashMap<String, String>();
            while((errorCode = codeFile.readLine()) != null)
                defaultLinkedHashMap.put(errorCode, "?");
            this.inputFile = inputFile;
            this.outputFile = outputFile;

            outFile = new BufferedWriter(new FileWriter(outputFile));
            outFile.write("@RELATION blue_gene_ras\n");
            for(String key : defaultLinkedHashMap.keySet()) {
                outFile.write("@ATTRIBUTE " + key + " {true}\n");
            }
            outFile.write("@DATA\n");
        } catch (FileNotFoundException e) {
            System.err.println("Error:" + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error:" + e.getMessage());
        } finally {
            if (outFile != null) {
                try {
                    outFile.close();
                } catch (IOException e) {
                    System.err.println("Error:" + e.getMessage());
                }
            }
        }
    }
    public static void main(String... args) {
        if (args.length != 3)
            throw new IllegalArgumentException("Filenames not passed.");
        ARFFGenerator generator = new ARFFGenerator(args[0], args[1], args[2]);
        generator.generate();
    }
    private LinkedHashMap getDefaultLinkedHashMap() {
        return new LinkedHashMap<String, String>(this.defaultLinkedHashMap);
    }
    private void generate() {
        LinkedHashMap<String, RuleEntry> finalLinkedHashMap = new LinkedHashMap<String, RuleEntry>();
        BufferedReader inFile = null;
        int count = 0;
        try {
            inFile = new BufferedReader(
                    new FileReader(inputFile));
            String row = null;
            while((row = inFile.readLine()) != null) {
                count++;
                if ((count % 100000) == 0) {
                    System.err.println("Rows Processed:" + count);
                }
                String[] rowEntries = row.split(DELIMITER);
                String nodeId = rowEntries[NODE_ID_INDEX];
                long entryDate = Long.parseLong(rowEntries[DATE_INDEX]);
                String entryData = rowEntries[ENTRY_DATA_INDEX];

                RuleEntry existingRuleEntry = finalLinkedHashMap.get(nodeId);
                if (existingRuleEntry == null) {
                    LinkedHashMap newRuleHash = getDefaultLinkedHashMap();
                    newRuleHash.put(entryData, true);
                    RuleEntry newRuleEntry = new RuleEntry(entryDate, newRuleHash, 1);
                    finalLinkedHashMap.put(nodeId, newRuleEntry);
                }
                else if ((entryDate - existingRuleEntry.getTime()) > TIME_DIFFERENCE) {
                    if(existingRuleEntry.getCount() > 1)
                        writeHash(existingRuleEntry.getRuleHash());
                    LinkedHashMap newRuleHash = getDefaultLinkedHashMap();
                    newRuleHash.put(entryData, true);
                    RuleEntry newRuleEntry = new RuleEntry(entryDate, newRuleHash, 1);
                    finalLinkedHashMap.put(nodeId, newRuleEntry);
                } else {
                    existingRuleEntry.setCount(existingRuleEntry.getCount() + 1);
                    existingRuleEntry.getRuleHash().put(entryData, "true");
                }
            }
            for (RuleEntry remainingRuleEntry : finalLinkedHashMap.values()) {
                if(remainingRuleEntry.getCount() > 1)
                    writeHash(remainingRuleEntry.getRuleHash());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error:" + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error:" + e.getMessage());
        } catch (Exception e) {
            System.err.println("Row:" + count);
            System.err.println("Error:" + e.getMessage());
        } finally {
            if (inFile != null) {
                try {
                    inFile.close();
                } catch (IOException e) {
                    System.err.println("Error:" + e.getMessage());
                }
            }
        }
    }

    private void writeHash(LinkedHashMap<String, String> ruleHash) {
        BufferedWriter outFile = null;
        try {
            outFile = new BufferedWriter(
                    new FileWriter(outputFile, true));
            //Collection<String> rule = ruleHash.values();
            //if(Collections.frequency(rule, true) > 1)
                outFile.write(StringUtils.join(ruleHash.values(), ",") + "\n");
        } catch (IOException e) {
            System.err.println("Error:" + e.getMessage());
        } finally {
            if (outFile != null) {
                try {
                    outFile.close();
                } catch (IOException e) {
                    System.err.println("Error:" + e.getMessage());
                }
            } else
                System.err.println("OutFile was null");
        }

        //System.out.println(String.valueOf(ruleHash.values()).toUpperCase());

    }
}
class RuleEntry {
    private long time;
    private LinkedHashMap<String, String> ruleHash;
    private int count;

    RuleEntry(long time, LinkedHashMap<String, String> ruleHash, int count) {
        this.time = time;
        this.ruleHash = ruleHash;
        this.count = count;
    }

    long getTime() {
        return time;
    }

    void setTime(long time) {
        this.time = time;
    }

    LinkedHashMap<String, String> getRuleHash() {
        return ruleHash;
    }

    void setRuleHash(LinkedHashMap<String, String> ruleHash) {
        this.ruleHash = ruleHash;
    }
    int getCount() {
        return count;
    }

    void setCount(int count) {
        this.count = count;
    }

}