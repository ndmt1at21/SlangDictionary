package com.btree;

import java.io.IOException;
import java.util.LinkedList;

import com.fileManager.FileManager;
import com.utils.TreeNodeException;

@SuppressWarnings("unused")
public class TreeLeafNode extends TreeNode {

    private LinkedList<String> valueArray;

    private LinkedList<Long> pointerOverflowArray;

    public TreeLeafNode(int currCapacity, LinkedList<String> keyArray, long position, boolean isDeleted)
            throws TreeNodeException {
        super(TreeNodeType.LEAF_NODE, currCapacity, keyArray, position, isDeleted);
        this.valueArray = new LinkedList<>();
        this.pointerOverflowArray = new LinkedList<>();
    }

    public TreeLeafNode(long position) throws TreeNodeException {
        super(TreeNodeType.LEAF_NODE, position);
        this.valueArray = new LinkedList<>();
        this.pointerOverflowArray = new LinkedList<>();
    }

    public String getValueAt(int index) {
        return this.valueArray.get(index);
    }

    public long getOverflowPointerAt(int index) {
        return this.pointerOverflowArray.get(index);
    }

    public void addToValueArrayAt(int index, String val) {
        this.valueArray.add(index, val);
    }

    public String removeValueAt(int index) {
        return this.valueArray.remove(index);
    }

    public long removeOverflowPointerAt(int index) {
        return this.pointerOverflowArray.remove(index);
    }

    public void addToOverflowPointerArrayAt(int index, long pointer) throws IndexOutOfBoundsException {
        this.pointerOverflowArray.add(index, pointer);
    }

    public void setToValueArrayAt(int index, String val) {
        this.valueArray.set(index, val);
    }

    public void setOverflowPointerArrayAt(int index, long pointer) {
        this.pointerOverflowArray.set(index, pointer);
    }

    public String popValue() {
        return this.valueArray.pop();
    }

    public long popOverflowPointer() {
        return this.pointerOverflowArray.pop();
    }

    @Override
    public void writeNode(FileManager fileManager, BTreeConfiguration config) throws IOException {
        // Seek
        fileManager.seek(this.getPosition());

        // Write header
        this.writeHeader(fileManager);

        // Write leaf node
        for (int i = 0; i < this.getCurrentCapacity(); i++) {
            // Write key
            fileManager.writeString(this.getKeyAt(i), config.getKeySize());

            // Write value
            fileManager.writeString(this.getValueAt(i), config.getEntrySize());

            // Write overflow pointer
            fileManager.writeLong(this.getOverflowPointerAt(i));
        }
    }

    @Override
    protected void readNodeDependType(FileManager fileManager, BTreeConfiguration config)
            throws IndexOutOfBoundsException, IOException {

        // Read content leaf node (key, value)
        for (int i = 0; i < this.getCurrentCapacity(); i++) {
            // Read key
            String key = fileManager.readString(config.getKeySize());

            // Read value
            String value = fileManager.readString(config.getEntrySize());

            // Read overflow pointer
            long ovfPointer = fileManager.readLong();

            // Add to key array
            this.addToKeyArrayAt(i, key);

            // Add to value array
            this.addToValueArrayAt(i, value);

            // Add to overflow pointer
            this.addToOverflowPointerArrayAt(i, ovfPointer);
        }
    }

    @Override
    public void printNode() {
        System.out.println("Node type: " + this.getNodeType().toString());
        System.out.println("Current capacity: " + this.getCurrentCapacity());
        System.out.println("State delete: " + this.getStateDeleted());
        System.out.println("Position in file: " + this.getPosition());

        System.out.print("Key/Value array: ");
        for (int i = 0; i < this.getCurrentCapacity(); i++) {
            System.out.print("<");
            System.out.print(this.getKeyAt(i) + ", ");
            System.out.print(this.getValueAt(i));
            System.out.print("> ");
        }
        System.out.println();

        System.out.print("Overflow pointer: ");
        for (int i = 0; i < this.getCurrentCapacity(); i++) {
            System.out.print(this.getOverflowPointerAt(i) + " ");
        }
        System.out.println();
    }
}
