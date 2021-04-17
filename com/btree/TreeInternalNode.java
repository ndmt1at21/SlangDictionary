package com.btree;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import com.fileManager.FileManager;
import com.utils.TreeNodeException;

@SuppressWarnings("unused")
public class TreeInternalNode extends TreeNode {

    private LinkedList<Long> pointerArray; // array of pointers (position in file) that internal node point to

    public TreeInternalNode(long position) throws TreeNodeException {
        super(TreeNodeType.INTERNAL_NODE, position);
        this.pointerArray = new LinkedList<>();
    }

    public TreeInternalNode(int currCapacity, LinkedList<String> keyArray, LinkedList<Long> pointerArray, long position,
            boolean isDeleted) throws TreeNodeException {
        super(TreeNodeType.INTERNAL_NODE, currCapacity, keyArray, position, isDeleted);
        this.pointerArray = pointerArray;
    }

    public long getPointerAt(int index) {
        return this.pointerArray.get(index);
    }

    public void setPointerAt(int index, long ptr) throws IndexOutOfBoundsException {
        this.pointerArray.set(index, ptr);
    }

    public void addToPointerArrayAt(int index, long ptrValue) throws IndexOutOfBoundsException {
        this.pointerArray.add(index, ptrValue);
    }

    public long popPointer() {
        return this.pointerArray.pop();
    }

    public long removePointerAt(int index) {
        return this.pointerArray.remove(index);
    }

    @Override
    public void writeNode(FileManager fileManager, BTreeConfiguration config) throws IOException {
        // Seek
        fileManager.seek(this.getPosition());

        // Write general header of node
        this.writeHeader(fileManager);

        // Write keys and pointers
        for (int i = 0; i < this.getCurrentCapacity(); i++) {
            // Write key
            fileManager.writeString(this.getKeyAt(i), config.getKeySize());

            // Write position/pointer that node point to another node in file
            fileManager.writeLong(this.getPointerAt(i));
        }

        // Write final position/pointer
        fileManager.writeLong(this.getPointerAt(this.getCurrentCapacity()));

        // Resize file
        if (fileManager.getLength() < this.getPosition() + config.getPageSize())
            fileManager.setLength(this.getPosition() + config.getPageSize());
    }

    @Override
    protected void readNodeDependType(FileManager fileManager, BTreeConfiguration config)
            throws IndexOutOfBoundsException, IOException {
        // Read keys and pointers
        for (int i = 0; i < this.getCurrentCapacity(); i++) {
            // Read key
            String key = fileManager.readString(config.getKeySize());

            // Add to node
            this.addToKeyArrayAt(i, key);

            // Read position/pointer that node point to another node in file
            this.addToPointerArrayAt(i, fileManager.readLong());
        }

        // Read final position/pointer
        this.addToPointerArrayAt(this.getCurrentCapacity(), fileManager.readLong());
    }

    @Override
    public void printNode() {
        System.out.println();
        System.out.println("Node type: " + this.getNodeType().toString());
        System.out.println("Current capacity: " + this.getCurrentCapacity());
        System.out.println("State delete: " + this.getStateDeleted());
        System.out.println("Position in file: " + this.getPosition());

        System.out.print("Key array: ");
        for (int i = 0; i < this.getCurrentCapacity(); i++) {
            System.out.print(this.getKeyAt(i) + " ");
        }
        System.out.println();

        System.out.print("Pointer array: ");
        for (int i = 0; i <= this.getCurrentCapacity(); i++) {
            System.out.print(this.getPointerAt(i) + ", ");
        }
        System.out.println();
    }
}
