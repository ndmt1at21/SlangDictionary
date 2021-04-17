package com.btree;

import java.io.IOException;
import java.util.LinkedList;

import com.fileManager.FileManager;
import com.utils.TreeNodeException;

public class TreeOverflowNode extends TreeNode {
    private LinkedList<String> valueArray; // overflow value array
    private long nextPointer; // next overflow page pointer

    public TreeOverflowNode(int currCapacity, LinkedList<String> keyArray, long position, boolean isDeleted)
            throws TreeNodeException {
        super(TreeNodeType.LEAF_OVERFLOW_NODE, currCapacity, keyArray, position, isDeleted);
        this.valueArray = new LinkedList<>();
        this.nextPointer = -1L;
    }

    public TreeOverflowNode(long position) throws TreeNodeException {
        super(TreeNodeType.LEAF_OVERFLOW_NODE, position);
        this.valueArray = new LinkedList<>();
        this.nextPointer = -1L;
    }

    public LinkedList<Integer> findIndexOverflowValue(String val) {
        LinkedList<Integer> indexList = new LinkedList<>();

        for (int i = 0; i < this.valueArray.size(); i++) {
            if (getValueOverflowAt(i).compareTo(val) == 0)
                indexList.push(i);
        }

        return indexList;
    }

    public String removeValueOverflowAt(int index) {
        return this.valueArray.remove(index);
    }

    public long getNextPointer() {
        return this.nextPointer;
    }

    public String getValueOverflowAt(int index) {
        return this.valueArray.get(index);
    }

    public void addToOverflowValueArrayAt(int index, String val) {
        this.valueArray.add(index, val);
    }

    // Should use if you can because it fast
    public void pushOverflowValue(String val) {
        this.valueArray.push(val);
    }

    // Should use if you can because it fast
    public String popOverflowValue() {
        return this.valueArray.pop();
    }

    public String getOverflowValueAt(int index) {
        return this.valueArray.get(index);
    }

    public void setNextPointer(long nextPointer) {
        this.nextPointer = nextPointer;
    }

    @Override
    public void writeNode(FileManager fileManager, BTreeConfiguration config) throws IOException {
        // Seek
        fileManager.seek(this.getPosition());

        // Write header
        this.writeHeader(fileManager);

        // Write overflow node
        for (int i = 0; i < this.getCurrentCapacity(); i++) {
            // Write value
            fileManager.writeString(this.getOverflowValueAt(i), config.getEntrySize());
        }

        // Write next pointer
        fileManager.writeLong(this.nextPointer);
    }

    @Override
    protected void readNodeDependType(FileManager fileManager, BTreeConfiguration config)
            throws IndexOutOfBoundsException, IOException {

        // Read content overflow node
        for (int i = 0; i < this.getCurrentCapacity(); i++) {
            // Read value
            String value = fileManager.readString(config.getKeySize());

            // Add to value array
            this.addToOverflowValueArrayAt(i, value);
        }

        // Read next pointer
        this.nextPointer = fileManager.readLong();
    }

    @Override
    public void printNode() {
        System.out.println();
        System.out.print("Value array: ");
        for (int i = 0; i < this.valueArray.size(); i++) {
            String delim = i == this.valueArray.size() - 1 ? "" : ", ";
            System.out.print(this.valueArray.get(i) + delim);
        }
        System.out.println();
        System.out.println("Next overflow pointer: " + nextPointer);
        System.out.println();
    }
}
