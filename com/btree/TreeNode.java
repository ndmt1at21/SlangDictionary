package com.btree;

import java.io.File;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;

import com.btree.TreeNodeType;
import com.fileManager.FileManager;
import com.utils.TreeNodeException;

@SuppressWarnings("unused")
abstract public class TreeNode {

    private TreeNodeType nodeType; // node of type
    private LinkedList<String> keyArray; // array of keys
    private int currentCapacity; // current capacity of node
    private boolean isDeleted; // flag check node is deleted
    private long position; // Position node in file

    /**
     * 
     * Constructor
     * 
     * @param nodeType node type
     * @param position position node in file
     */
    public TreeNode(TreeNodeType nodeType, long position) throws TreeNodeException {
        this.setNodeType(nodeType);
        this.setPosition(position);
        this.currentCapacity = 0;
        this.keyArray = new LinkedList<>();
        this.isDeleted = false;
    }

    /**
     * 
     * Constructor
     * 
     * @param nodeType node type
     * @param position position node in file
     */
    public TreeNode(TreeNodeType nodeType, int currCapacity, LinkedList<String> keyArray, long position,
            boolean isDeleted) throws TreeNodeException {
        this.setNodeType(nodeType);
        this.setCurrentCapacity(currCapacity);
        this.keyArray = keyArray;
        this.setPosition(position);
        this.setStateDeleted(isDeleted);
    }

    public void setNodeType(TreeNodeType nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * 
     * @param rawVal raw value of node type (0 - 2)
     * @throws InvalidPropertiesFormatException is thrown when raw value out of
     *                                          range (0-2)
     */
    public void setNodeType(short rawVal) throws InvalidPropertiesFormatException {
        this.nodeType = TreeNodeType.setValue(rawVal);
    }

    //////////////////////////////////////////////////////////
    ///////////////// Block Set Function /////////////////////

    /**
     * 
     * Set key in key array at specific position
     * 
     * @param index position in array
     * @param key   key to set at position
     * @throws IndexOutOfBoundsException is thrown when index < 0 || index > size()
     */
    public void setKeyAt(int index, String key) throws IndexOutOfBoundsException {
        keyArray.set(index, key);
    }

    /**
     * 
     * Find last position where new key is larger than key in keyArray of node
     * 
     * @param newKey Key need find
     * @return ith key in node
     */
    public int findLastSmallerThan(String newKey) {
        int i;
        for (i = this.keyArray.size() - 1; i >= 0; i++) {
            if (newKey.compareTo(this.getKeyAt(i)) > 0)
                return i;
        }

        return 0;
    }

    /**
     * 
     * Add key in key array at specific position
     * 
     * @param index Position in array
     * @param key   Key to add
     * @throws IndexOutOfBoundsException is thrown when index < 0 || index > size()
     */
    public void addToKeyArrayAt(int index, String key) throws IndexOutOfBoundsException {
        keyArray.add(index, key);
    }

    /**
     * 
     * Pop and remove key in key array at specific position
     * 
     * @return element in head of linkedlist keys
     * @throws IndexOutOfBoundsException is thrown when index < 0 || index > size()
     */
    public String popKey() {
        return this.keyArray.pop();
    }

    /**
     * 
     * Inserts key at the specified position in key array, shift all elements after
     * current position to the right.
     * 
     * @param index position in array
     * @param key   key to add at position
     * @throws IndexOutOfBoundsException is thrown when index < 0 || index > size()
     */
    public void addKeyAt(int index, String key) throws IndexOutOfBoundsException {
        this.keyArray.add(index, key);
    }

    public String removeKeyAt(int index) {
        return this.keyArray.remove(index);
    }

    /**
     * 
     * @param config BTree configuration for validating current capacity
     * @throws TreeNodeException is thrown when capacity is violted after increasing
     */
    public void setCurrentCapacity(int cap) throws TreeNodeException {
        if (cap < 0)
            throw new TreeNodeException("Capacity cannot less than 0");
        this.currentCapacity = cap;
    }

    /**
     * 
     * Increase current capacity of node
     * 
     * @param config BTree configuration for validating current capacity
     * @throws TreeNodeException is thrown when capacity is violted after increasing
     */
    public void increaseCurrentCapacity(BTreeConfiguration config) throws TreeNodeException {
        this.currentCapacity++;
        this.validateData(config);
    }

    /**
     * 
     * Decrease current capacity of node
     * 
     * @param config BTree configuration for validating current capacity
     * @throws TreeNodeException is thrown when capacity is violted after decreasing
     */
    public void decreaseCurrentCapacity(BTreeConfiguration config) throws TreeNodeException {
        this.currentCapacity--;
        this.validateData(config);
    }

    public void setPosition(long pos) throws TreeNodeException {
        if (pos < 0)
            throw new TreeNodeException("Node's position in file cannot less than zero");
        this.position = pos;
    }

    public void setStateDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    //////////////////////////////////////////////////////////
    ///////////////// Block Get Function /////////////////////

    public String getKeyAt(int index) {
        return this.keyArray.get(index);
    }

    /**
     * 
     * @return True if node is deleted, otherwise false
     */
    public boolean getStateDeleted() {
        return this.isDeleted;
    }

    public TreeNodeType getNodeType() {
        return this.nodeType;
    }

    public int getCurrentCapacity() {
        return this.currentCapacity;
    }

    public long getPosition() {
        return this.position;
    }

    public boolean isInternalNode() {
        return this.nodeType == TreeNodeType.INTERNAL_NODE || this.nodeType == TreeNodeType.ROOT_INTERNAL_NODE;
    }

    public boolean isLeafNode() {
        return this.nodeType == TreeNodeType.LEAF_NODE || this.nodeType == TreeNodeType.ROOT_LEAF_NODE
                || this.nodeType == TreeNodeType.LEAF_OVERFLOW_NODE;
    }

    public boolean isOverflowNode() {
        return this.nodeType == TreeNodeType.LEAF_OVERFLOW_NODE;
    }

    public boolean isRootNode() {
        return this.nodeType == TreeNodeType.ROOT_INTERNAL_NODE || this.nodeType == TreeNodeType.ROOT_LEAF_NODE;
    }

    public boolean isFull(BTreeConfiguration config) {
        if (isLeafNode()) {
            if (isOverflowNode())
                return this.currentCapacity == config.getMaxOverflowNodeCapacity();
            return this.currentCapacity == config.getMaxLeafNodeCapacity();
        }

        return this.currentCapacity == config.getMaxInternalNodeCapacity();
    }

    public boolean isEmpty() {
        return this.currentCapacity == 0;
    }

    private void validateData(BTreeConfiguration config) throws TreeNodeException {
        if (this.currentCapacity < 0)
            throw new TreeNodeException("Capacity cannot less than 0");

        if (this.isLeafNode() && this.currentCapacity > config.getMaxLeafNodeCapacity())
            throw new TreeNodeException("Current capacity exceed allowed capacity");

        if (this.isInternalNode() && this.currentCapacity > config.getMaxInternalNodeCapacity())
            throw new TreeNodeException("Current capacity exceed allowed capacity");
    }

    /**
     * 
     * Traverse from current node
     * 
     * @param fileManager file save node data
     * @param config
     * @throws IOException
     * @throws TreeNodeException
     */
    public void traverse(FileManager fileManager, BTreeConfiguration config) throws IOException, TreeNodeException {
        int currCap = this.getCurrentCapacity();

        this.printNode();

        if (this.isLeafNode()) {
            return;
        }

        TreeInternalNode internalNode = (TreeInternalNode) this;

        // Read pointer
        for (int i = 0; i < currCap; i++) {
            TreeNode childNode = TreeNode.readNode(fileManager, config, internalNode.getPointerAt(i));
            childNode.traverse(fileManager, config);
        }

        // Last ptr
        TreeNode childNode = TreeNode.readNode(fileManager, config, internalNode.getPointerAt(currCap));
        childNode.traverse(fileManager, config);
    }

    /**
     * 
     * Write general header of node, include node type, current capacity and node's
     * state. It starts writting from current file pointer.
     * 
     * @param fileManager target file
     * @param pos         start position for reading
     * @throws IOException is thrown when file cannot be written
     */
    protected void writeHeader(FileManager fileManager) throws IOException {
        // Write node type
        fileManager.writeShort(this.nodeType.getRawValue());

        // Write current capacity
        fileManager.writeInt(this.currentCapacity);

        // Write deleted state
        fileManager.writeBoolean(this.isDeleted);
    }

    /**
     * 
     * Read header of node, include node type, current capacity and node's state. It
     * starts reading from current file pointer.
     * 
     * @param fileManager target file
     * @throws IOException       is thrown when file cannot be read
     * @throws TreeNodeException
     */
    protected void readHeader(FileManager fileManager) throws IOException, TreeNodeException {
        // Read node type
        this.setNodeType(fileManager.readShort());

        // Read current capacity
        this.setCurrentCapacity(fileManager.readInt());

        // Read deleted state
        this.setStateDeleted(fileManager.readBoolean());
    }

    /**
     * 
     * Write node content to file
     * 
     * @param fileManager target file
     * @param config      follow fomat in config to write node
     * @param pos         write to position in file
     * @throws IOException is thrown when file could not be written
     */
    abstract public void writeNode(FileManager fileManager, BTreeConfiguration config) throws IOException;

    /**
     * 
     * Read node from file
     * 
     * @param fileManager target file
     * @param config      follow fomat in config to read node
     * @param pos         start position for reading
     * @return Tree node read from file in pos with config
     * @throws IOException       is thrown when file could not be read
     * @throws TreeNodeException
     * 
     */
    static public TreeNode readNode(FileManager fileManager, BTreeConfiguration config, long pos)
            throws IOException, TreeNodeException {
        // Seek
        fileManager.seek(pos);

        // Read node type
        TreeNodeType nodeType = TreeNodeType.setValue(fileManager.readShort());

        // Create node's instance depend on type of node
        TreeNode node = null;

        if (nodeType == TreeNodeType.INTERNAL_NODE || nodeType == TreeNodeType.ROOT_INTERNAL_NODE) {
            node = new TreeInternalNode(pos);
        } else if (nodeType == TreeNodeType.LEAF_OVERFLOW_NODE) {
            node = new TreeOverflowNode(pos);
        } else if (nodeType == TreeNodeType.LEAF_NODE || nodeType == TreeNodeType.ROOT_LEAF_NODE) {
            node = new TreeLeafNode(pos);
        }

        // Read node
        // Seek (duplicate with first line, node type will be read again but it can be
        // ignored)
        fileManager.seek(pos);

        // Read node header
        node.readHeader(fileManager);

        // Read content internal node
        node.readNodeDependType(fileManager, config);

        return node;
    }

    /**
     * 
     * Read node (depend on node type, just read content and not need reading
     * header) from file. It starts reading from current file pointer.
     * 
     * @param fileManager target file
     * @param config      follow fomat in config to read node
     * @param pos         start position for reading
     * @throws IOException               is thrown when file could not be read
     * @throws IndexOutOfBoundsException is thrown when
     */
    abstract protected void readNodeDependType(FileManager fileManager, BTreeConfiguration config)
            throws IndexOutOfBoundsException, IOException;

    /**
     * Just for testing
     */
    abstract public void printNode();
}
