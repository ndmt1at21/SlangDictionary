package com.fileManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class SrcSlangParser {
    static public HashMap<String, String[]> parse(String urlFile) {
        HashMap<String, String[]> slangValues = new HashMap<>();
        BufferedReader buffReader = null;

        try {
            buffReader = new BufferedReader(new FileReader(urlFile));

            // Read header
            buffReader.readLine();

            // Read content
            while (true) {
                String line = buffReader.readLine();
                if (line == null)
                    break;

                String[] arrStr = line.split("`");

                if (arrStr.length != 2) {
                    buffReader.close();
                    throw new Exception("File not follow format where key = " + arrStr[0]);
                }

                String key = arrStr[0].trim();
                String[] vals = arrStr[1].split("\\|");

                for (int i = 0; i < vals.length; i++)
                    vals[i].trim();

                slangValues.put(key, vals);
            }

            buffReader.close();

            return slangValues;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return slangValues;

        }

    }

    static public void print(HashMap<String, String[]> hashMap) {
        for (String i : hashMap.keySet()) {
            String key = i;
            String[] arrVals = hashMap.get(key);

            System.out.print("Key: " + key + "; ");
            System.out.print("Value: ");

            for (int j = 0; j < arrVals.length; j++) {
                System.out.print(arrVals[j] + ", ");
            }

            System.out.println();
        }
    }
}
