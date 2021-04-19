import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import com.btree.BTreeConfiguration;
import com.btree.BTreeStore;
import com.btree.SlangWord;
import com.btree.TreeNode;
import com.fileManager.SrcSlangParser;

public class Main {
    public static void delete() {
        try {
            File myObj = new File("./test.dat");
            if (myObj.delete()) {
                System.out.println("File deleted");
            } else {
                System.out.println("File caanot delete");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            Main.delete();
            BTreeConfiguration config = new BTreeConfiguration(1024, 20, 20, 2);
            BTreeStore btree = new BTreeStore("./test.dat", config);

            ArrayList<SlangWord> arrSlangWord = TestData.readTest();

            // Insert
            arrSlangWord.forEach(slangWord -> {
                try {
                    btree.insert(slangWord.getDefinition(), slangWord.getMean(), (a) -> {
                        return false;
                    });
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });

            // btree.printBTreeInfor();
            btree.printPrettyBTree();

            // btree.printFullOverflowNode(1056L);
            // btree.delete("6", "1", true);
            // btree.printFullOverflowNode(1056L);

            btree.delete("5", null, false);
            btree.printDetailNode("5");
            btree.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

class TestData {
    static public ArrayList<SlangWord> readTest() {
        ArrayList<SlangWord> priKeys = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader("./testData.txt"));

            while (true) {
                String line = reader.readLine();
                if (line == null)
                    break;

                String[] keys = line.split(" ");

                priKeys.add(new SlangWord(keys[0], keys[1]));
            }

            reader.close();
            return priKeys;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return priKeys;
        }

    }
}
