package com.btree;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.plaf.metal.MetalIconFactory.TreeLeafIcon;

import com.fileManager.FileManager;
import com.utils.BTreeConfigurationException;
import com.utils.BTreeException;
import com.utils.TreeNodeException;

@SuppressWarnings("unused")
public class BTreeStore {
    private BTreeConfiguration config;
    private FileManager fileManager;
    private TreeNode root;
    private long totalTreePages;

    public BTreeStore() throws IOException, BTreeConfigurationException, TreeNodeException, BTreeException {
        BTreeConfiguration config = new BTreeConfiguration();
        initCommon();
        openFile("btree.dat", config);
    }

    public BTreeStore(String urlFile, BTreeConfiguration config)
            throws IOException, BTreeConfigurationException, TreeNodeException, BTreeException {
        initCommon();
        openFile(urlFile, config);
    }

    public int getPositionRootPointerStart() {
        return (Integer.SIZE * 4 + Long.SIZE) / 8; // in bytes
    }

    public int getBTreeHeaderSize() {
        return (Integer.SIZE * 4 + Long.SIZE * 2) / 8;
    }

    public void readBTreeHeader() throws IOException, BTreeConfigurationException, TreeNodeException, BTreeException {
        // Seek
        fileManager.seek(0);

        // Read page size
        int pageSize = this.fileManager.readInt();

        // Read key size
        int keySize = this.fileManager.readInt();

        // Read entry size
        int entrySize = this.fileManager.readInt();

        // Read tree degree
        int treeDegree = this.fileManager.readInt();

        // Read total tree pages
        this.totalTreePages = this.fileManager.readLong();

        // Read position of root
        System.out.println(this.fileManager.getFilePointer());
        long rootPos = this.fileManager.readLong();
        if (rootPos == -1L)
            return;

        if (rootPos < -1L)
            throw new BTreeException("Root index cannot negative");

        // Setup config
        this.config = new BTreeConfiguration(pageSize, keySize, entrySize, treeDegree);

        // Read root
        this.root = TreeNode.readNode(this.fileManager, this.config, rootPos);
    }

    public void writePositionRootPointer(long pos) throws IOException {
        System.out.println(pos);
        this.fileManager.seek(this.getPositionRootPointerStart());
        this.fileManager.writeLong(pos);
    }

    public void writeBTreeHeader() throws IOException, BTreeConfigurationException {
        // Seek
        fileManager.seek(0);

        // Write page size
        fileManager.writeInt(this.config.getPageSize());

        // Write key size
        fileManager.writeInt(this.config.getKeySize());

        // Write entry size
        fileManager.writeInt(this.config.getEntrySize());

        // Write tree degree
        fileManager.writeInt(this.config.getTreeDegree());

        // Write totalTreePages
        fileManager.writeLong(this.totalTreePages);

        // Write position of root node
        if (this.root == null) {
            this.writePositionRootPointer(-1L);
            return;
        }

        this.writePositionRootPointer(this.root.getPosition());
        this.root.writeNode(this.fileManager, this.config);
    }

    public long generateAvailablePageIndex() {
        long index = this.config.getPageSize() * this.totalTreePages + getBTreeHeaderSize();
        this.totalTreePages++;

        return index;
    }

    public void deletePage() {
        this.totalTreePages--;
    }

    public void close() throws IOException, BTreeConfigurationException {
        System.out.println("Closing...");
        this.fileManager.close();
    }

    /**
     * 
     * Find position where key in node
     * 
     * @param node node to search
     * @param key  Key need find
     * @param rank rank of the search (lower/upper)
     * @return index of the bound or found key
     */
    public int findKeyInNode(TreeNode node, String key, Rank rank) {
        return this.binaryNodeSearch(node, key, rank, 0, node.getCurrentCapacity() - 1);
    }

    /**
     * 
     * @param node node to serach
     * @param key  key to search
     * @param rank rank of the search (lower/upper)
     * @param l    left part of array
     * @param r    right part of array
     * @return index of the bound or found key
     */
    private int binaryNodeSearch(TreeNode node, String key, Rank rank, int l, int r) {
        int m;
        String mkey;

        if (l > r) {
            switch (rank) {
            case Pred:
                return l == 0 ? l : l - 1;
            case Succ:
                return l > 0 && l == node.getCurrentCapacity() ? l - 1 : l;
            default:
                return l;
            }
        } else {
            m = (l + r) / 2;
            mkey = node.getKeyAt(m);
        }

        if (mkey.compareTo(key) < 0) {
            return binaryNodeSearch(node, key, rank, m + 1, r);
        } else if (mkey.compareTo(key) > 0) {
            return binaryNodeSearch(node, key, rank, l, m - 1);
        } else { // this is equal
            return rank == Rank.PlusOne ? m + 1 : m;
        }
    }

    public SearchResult search(String key) throws IOException, TreeNodeException {
        return search(this.root, key);
    }

    private SearchResult search(TreeNode node, String key) throws IOException, TreeNodeException {
        // Search key
        int idx = binaryNodeSearch(node, key, Rank.Exact, 0, node.getCurrentCapacity() - 1);

        // Leaf node
        if (node.isLeafNode()) {
            TreeLeafNode leafNode = (TreeLeafNode) node;

            // Found key
            if (idx < node.getCurrentCapacity() && key.compareTo(node.getKeyAt(idx)) == 0) {
                LinkedList<String> sResult = new LinkedList<>();

                // Value in leaf node
                sResult.push(leafNode.getValueAt(idx));

                // Read value in overflow node
                if (leafNode.getOverflowPointerAt(idx) == -1L)
                    return new SearchResult(key, (String) null);

                TreeOverflowNode ovfNode = (TreeOverflowNode) TreeNode.readNode(this.fileManager, this.config,
                        leafNode.getOverflowPointerAt(idx));

                while (true) {

                    for (int i = 0; i < ovfNode.getCurrentCapacity(); i++)
                        sResult.push(ovfNode.getValueOverflowAt(i));

                    if (ovfNode.getNextPointer() == -1l)
                        break;

                    ovfNode = (TreeOverflowNode) TreeNode.readNode(this.fileManager, this.config,
                            ovfNode.getNextPointer());
                }

                return new SearchResult(key, sResult);
            }

            // Nothing can find
            return new SearchResult(key, (String) null);

        }
        // Internal node
        else {
            // If key = key in array (range [0, cap - 1] -> increase idx
            // Because key search = key in internal node -> right side
            if (idx <= node.getCurrentCapacity() && key.compareTo(node.getKeyAt(idx)) >= 0)
                idx++;

            // Recusive seach
            TreeInternalNode internalNode = (TreeInternalNode) node;
            TreeNode childNode = TreeNode.readNode(this.fileManager, this.config, internalNode.getPointerAt(idx));
            return search(childNode, key);
        }
    }

    public void insert(String key, String value, CallbackCheckUnique<String> callbackDup)
            throws IOException, TreeNodeException, BTreeConfigurationException {

        if (this.root == null) {
            TreeLeafNode newRoot = new TreeLeafNode(this.generateAvailablePageIndex());

            newRoot.setNodeType(TreeNodeType.ROOT_LEAF_NODE);
            newRoot.addKeyAt(0, key);
            newRoot.addToValueArrayAt(0, value);
            newRoot.addToOverflowPointerArrayAt(0, -1L);
            newRoot.increaseCurrentCapacity(this.config);

            // Update current root
            this.root = newRoot;

            this.root.printNode();
            // Commit node to file
            newRoot.writeNode(this.fileManager, this.config);
            this.writeBTreeHeader();

        } else if (this.root.isFull(this.config)) {
            // Init new root
            TreeInternalNode newRootNode = new TreeInternalNode(this.generateAvailablePageIndex());
            newRootNode.setNodeType(TreeNodeType.ROOT_INTERNAL_NODE);

            // Set old root type
            if (this.root.getNodeType() == TreeNodeType.ROOT_LEAF_NODE) {
                this.root.setNodeType(TreeNodeType.LEAF_NODE);
            } else if (this.root.getNodeType() == TreeNodeType.ROOT_INTERNAL_NODE) {
                this.root.setNodeType(TreeNodeType.INTERNAL_NODE);
            }

            // Make old root as child of newRoot
            newRootNode.addToPointerArrayAt(0, this.root.getPosition());

            // Split old root and move 1 child to new root
            // index = 0? newRootNode after split have only one key
            splitTreeNode(newRootNode, this.root, 0);

            // Change current root
            this.root = newRootNode;

            // Commit root change
            this.writeBTreeHeader();

            insertNonFull(newRootNode, key, value, callbackDup);

        } else {
            insertNonFull(this.root, key, value, callbackDup);
        }
    }

    /**
     * 
     * Split child that parent node at index in keyArray point to
     * 
     * @param parentNode parent the split
     * @param childNode  child of parent need splitting
     * @param index      ith child of its parent node
     * @throws TreeNodeException
     * @throws IOException
     */
    protected void splitTreeNode(TreeInternalNode parentNode, TreeNode childNode, int index)
            throws TreeNodeException, IOException {
        // Create new node store (t - 1) keys (half capacity of node)
        TreeNode yNode = childNode;
        TreeNode zNode = null;
        int t = this.config.getTreeDegree();

        // If yNode is internal, copy the first (t - 1 + 1) pointer
        if (yNode.isInternalNode()) {

            TreeInternalNode yInternalNode = (TreeInternalNode) yNode;
            TreeInternalNode zInternalNode = new TreeInternalNode(this.generateAvailablePageIndex());

            // Copy the first (t - 1) keys, pointer
            for (int i = 0; i < t - 1; i++) {
                zInternalNode.addKeyAt(i, yInternalNode.popKey());
                zInternalNode.addToPointerArrayAt(i, yInternalNode.popPointer());
            }
            zInternalNode.addToPointerArrayAt(t - 1, yInternalNode.popPointer());

            // Change current capacity
            yInternalNode.setCurrentCapacity(t - 1);
            zInternalNode.setCurrentCapacity(t - 1);

            // Update refenrece
            zNode = zInternalNode;
        }
        // Leaf
        else {

            TreeLeafNode yLeafNode = (TreeLeafNode) yNode;
            TreeLeafNode zLeafNode = new TreeLeafNode(this.generateAvailablePageIndex());

            // Copy the first (t - 1) key, value, overflow pointer
            for (int i = 0; i < t - 1; i++) {
                zLeafNode.addKeyAt(i, yLeafNode.popKey());
                zLeafNode.addToValueArrayAt(i, yLeafNode.popValue());
                zLeafNode.addToOverflowPointerArrayAt(i, yLeafNode.popOverflowPointer());

                yLeafNode.decreaseCurrentCapacity(this.config);
                zLeafNode.increaseCurrentCapacity(this.config);
            }

            // Update reference
            zNode = zLeafNode;
        }

        // Get and move middle key of childNode to parentNode
        parentNode.addKeyAt(index, yNode.getKeyAt(0));
        parentNode.increaseCurrentCapacity(this.config);

        // Update pointer
        parentNode.addToPointerArrayAt(index, zNode.getPosition());

        // Commit change
        yNode.writeNode(this.fileManager, this.config);
        zNode.writeNode(this.fileManager, this.config);
        parentNode.writeNode(this.fileManager, this.config);
    }

    /**
     * 
     * A utility function to insert a new key in node. The assumption is, the node
     * must be non-full when this function is called
     * 
     * @param node       target node
     * @param primaryKey key for inserting
     * @throws TreeNodeException
     * @throws IOException
     */
    protected void insertNonFull(TreeNode node, String key, String value, CallbackCheckUnique<String> callbackDup)
            throws IOException, TreeNodeException {
        // Find the location of new key to be inserted
        int idx = findKeyInNode(node, key, Rank.PlusOne);

        // Duplicate key
        // Leaf node
        if (node.isLeafNode()) {
            TreeLeafNode leafNode = (TreeLeafNode) node;

            // Adjust idx for checking duplicate key
            int adjIndex = (node.getCurrentCapacity() > 0 && idx == 0) ? idx : idx - 1;

            if (node.getCurrentCapacity() > 0 && node.getKeyAt(adjIndex).compareTo(key) == 0) {
                // If user said key is unique
                if (callbackDup.isUnique(key)) {
                    return;
                }

                // User accept duplicate
                // Overflow page doesn't exist (first overflow)
                long srcOverflowPointer = leafNode.getOverflowPointerAt(adjIndex);
                if (srcOverflowPointer < 0) {
                    TreeOverflowNode overflowNode = new TreeOverflowNode(generateAvailablePageIndex());

                    // Add value to value list overflow page
                    overflowNode.pushOverflowValue(value);

                    // Update current capacity
                    overflowNode.increaseCurrentCapacity(this.config);

                    // Update pointer of parent node
                    leafNode.addToOverflowPointerArrayAt(adjIndex, overflowNode.getPosition());

                    // Commit change
                    leafNode.writeNode(this.fileManager, this.config);
                    overflowNode.writeNode(this.fileManager, this.config);
                }
                // Overflow page already exist
                else {
                    // Case 1. Ovf page have enough space for push new value
                    // Case 2. Ovf page is full -> check next overflow page
                    TreeOverflowNode overflowNode = (TreeOverflowNode) TreeNode.readNode(this.fileManager, this.config,
                            srcOverflowPointer);

                    // Case 2
                    // Check full until last ovf page
                    while (overflowNode.isFull(this.config)) {
                        // Check if have more? if not create
                        if (overflowNode.getNextPointer() < 0) {
                            // Create new overflow page
                            TreeOverflowNode newOvfNode = new TreeOverflowNode(generateAvailablePageIndex());
                            newOvfNode.pushOverflowValue(value);
                            newOvfNode.increaseCurrentCapacity(this.config);

                            // Set next pointer point to newOvfNode
                            overflowNode.setNextPointer(newOvfNode.getPosition());

                            // Commit change
                            newOvfNode.writeNode(this.fileManager, this.config);
                            overflowNode.writeNode(this.fileManager, this.config);
                            return;
                        }

                        // Have next ovf pointer
                        overflowNode = (TreeOverflowNode) TreeNode.readNode(this.fileManager, this.config,
                                overflowNode.getNextPointer());
                    }

                    // Case 1
                    overflowNode.pushOverflowValue(value);
                    overflowNode.increaseCurrentCapacity(this.config);
                    overflowNode.writeNode(this.fileManager, this.config);
                }
            }
            // Normal key
            else {
                // Add key to index and shift all element after add position to right
                leafNode.addKeyAt(idx, key);
                leafNode.addToValueArrayAt(idx, value);
                leafNode.addToOverflowPointerArrayAt(idx, -1L);
                leafNode.increaseCurrentCapacity(this.config);

                // Write leaf node to file
                leafNode.writeNode(this.fileManager, this.config);
            }
        }
        // Internal node
        else {
            TreeInternalNode internalNode = (TreeInternalNode) node;

            // Get child at position
            long posChildInFile = internalNode.getPointerAt(idx);
            TreeNode childNode = TreeNode.readNode(this.fileManager, this.config, posChildInFile);

            // Child node is full
            if (childNode.isFull(this.config)) {
                splitTreeNode(internalNode, childNode, idx);
            }

            insertNonFull(childNode, key, value, callbackDup);
        }
    }

    /**
     * 
     * @param key      key to delete
     * @param value    value to delete
     * @param isUnique delete all values with key or one value
     * @return object present number of deleted values
     * @throws TreeNodeException
     * @throws IOException
     */
    public DeleteResult delete(String key, String value, boolean isUnique) throws IOException, TreeNodeException {
        if (this.root == null)
            return new DeleteResult(key, (String) null);

        return delete(this.root, key, value, isUnique);
    }

    /**
     * 
     * @param node     current node that we need to recur
     * @param key      key to delete
     * @param value    value to delete
     * @param isUnique delete all values with key or one value
     * @return object present number of deleted values
     * @throws TreeNodeException
     * @throws IOException
     */
    public DeleteResult delete(TreeNode node, String key, String value, boolean isUnique)
            throws IOException, TreeNodeException {

        // Find key to be removed present in
        int idx = findKeyInNode(node, key, Rank.Exact);

        // If leaf node, return result
        if (node.isLeafNode())
            return removeFromLeaf(node, value, idx, isUnique);

        // Internal node, read child
        if (idx < node.getCurrentCapacity() && key.compareTo(node.getKeyAt(idx)) == 0)
            idx++;
        TreeInternalNode internalNode = (TreeInternalNode) node;
        TreeNode child = TreeNode.readNode(this.fileManager, this.config, internalNode.getPointerAt(idx));

        // Recursive delete child
        return delete(child, key, value, isUnique);
    }

    /**
     * 
     * @param node  target node
     * @param index index of key will be removed
     * @return object present number of deleted values
     * @throws TreeNodeException
     * @throws IOException
     */
    private DeleteResult removeFromLeaf(TreeNode node, String value, int index, boolean isUnique)
            throws TreeNodeException, IOException {
        if (isUnique)
            return this.removeFromLeafUnique(node, value, index);

        return this.removeFromLeafNonUnique(node, index);
    }

    /**
     * 
     * Delete value and all overflow pages with key equal key in node
     * 
     * @param node
     * @param index
     * @return
     * @throws IOException
     * @throws TreeNodeException
     */
    private DeleteResult removeFromLeafNonUnique(TreeNode node, int index) throws IOException, TreeNodeException {
        TreeLeafNode leafNode = (TreeLeafNode) node;

        // Create variable contain all remove values
        LinkedList<String> removeValues = new LinkedList<>();

        if (leafNode.getOverflowPointerAt(index) != -1L) {

            // Read first overflow page
            TreeOverflowNode ovfNode = (TreeOverflowNode) TreeNode.readNode(this.fileManager, this.config,
                    leafNode.getOverflowPointerAt(index));

            // Interate overflow pages
            while (true) {

                // Add all values to remove list
                for (int i = 0; i < ovfNode.getCurrentCapacity(); i++) {
                    removeValues.push(ovfNode.removeValueOverflowAt(i));
                    ovfNode.decreaseCurrentCapacity(this.config);
                }

                // Delete page
                this.deletePage();

                // Commit
                ovfNode.writeNode(this.fileManager, this.config);

                // Check condition
                if (ovfNode.getNextPointer() < 0)
                    break;

                // Read next overflow page
                ovfNode = (TreeOverflowNode) TreeNode.readNode(this.fileManager, this.config,
                        leafNode.getOverflowPointerAt(index));
            }
        }

        // Remove entry
        // (just tricky, program should implement btree
        // deletion :)))
        String key = leafNode.removeKeyAt(index);
        String value = leafNode.getValueAt(index);
        leafNode.setToValueArrayAt(index, null);
        leafNode.removeOverflowPointerAt(index);
        leafNode.decreaseCurrentCapacity(config);

        // Add value in leaf node to remove values
        removeValues.push(value);

        // Commit change
        leafNode.writeNode(this.fileManager, this.config);

        return new DeleteResult(key, removeValues);
    }

    /**
     * 
     * Delete all value with key
     * 
     * @param node
     * @param value
     * @param index
     * @return
     * @throws IOException
     * @throws TreeNodeException
     */
    private DeleteResult removeFromLeafUnique(TreeNode node, String value, int index)
            throws IOException, TreeNodeException {
        TreeLeafNode leafNode = (TreeLeafNode) node;

        // Remove value that it equals source value in overflow page of leaf node
        TreeOverflowNode lastOvfNonEmpty = null;
        if (leafNode.getOverflowPointerAt(index) > 0) {
            lastOvfNonEmpty = removeAllValueOverflowPages(leafNode, value, index);
        }

        // After delete, check overflow node in leaf is exist
        if (leafNode.getOverflowPointerAt(index) > 0) {
            // Bring last value in last overflow node to leaf node
            // !! (Bug) overflow node have one value, it will be not deleted
            leafNode.setToValueArrayAt(index, lastOvfNonEmpty.popOverflowValue());
        } else {
            leafNode.removeKeyAt(index);
            leafNode.removeValueAt(index);
            leafNode.removeOverflowPointerAt(index);
            leafNode.decreaseCurrentCapacity(config);

            // Check validate current capacity (just tricky, program should implement btree
            // deletion :)))
            if (leafNode.getCurrentCapacity() < this.config.getMinLeafNodeCapacity())
                leafNode.setStateDeleted(true);
        }

        // Commit change
        leafNode.writeNode(this.fileManager, this.config);
        return new DeleteResult(node.getKeyAt(index), value);
    }

    /**
     * 
     * @param leafNode
     * @param value
     * @param index
     * @return
     * @throws IOException
     * @throws TreeNodeException
     */
    private TreeOverflowNode removeAllValueOverflowPages(TreeLeafNode leafNode, String value, int index)
            throws IOException, TreeNodeException {

        // Read first overflow page
        TreeOverflowNode ovfNode = (TreeOverflowNode) TreeNode.readNode(this.fileManager, this.config,
                leafNode.getOverflowPointerAt(index));
        TreeNode prevNode = leafNode;
        TreeOverflowNode nextNode = null;

        while (true) {
            // Find all index of value in array
            LinkedList<Integer> indexList = ovfNode.findIndexOverflowValue(value);

            // Read next overflow page
            if (ovfNode.getNextPointer() > 0) {
                nextNode = (TreeOverflowNode) TreeNode.readNode(this.fileManager, this.config,
                        ovfNode.getNextPointer());
            }

            // Remove value in overflow node
            for (int i = 0; i < indexList.size(); i++) {
                ovfNode.removeValueOverflowAt(i);
                ovfNode.decreaseCurrentCapacity(this.config);
            }

            // Check node empty
            if (ovfNode.isEmpty()) {
                long indexNextNode = nextNode == null ? -1L : nextNode.getPosition();

                // Prev node is overflow node, update next pointer in overflow node
                if (prevNode.isOverflowNode()) {
                    ((TreeOverflowNode) prevNode).setNextPointer(indexNextNode);
                }
                // Prev node is leaf node, update overflow pointer in leaf node
                else {
                    ((TreeLeafNode) prevNode).setOverflowPointerArrayAt(index, indexNextNode);
                }

                // Delete overflow page
                deletePage();
            }

            // Commit change prevNode
            prevNode.writeNode(this.fileManager, this.config);

            // Condition break
            if (nextNode == null)
                break;

            // Update prev, ovf, next
            if (!ovfNode.isEmpty())
                prevNode = ovfNode;
            ovfNode = nextNode;
            nextNode = null;
        }

        if (!prevNode.isOverflowNode())
            return null;

        TreeOverflowNode lastNodeNonEmpty = ovfNode.isEmpty() ? (TreeOverflowNode) prevNode : ovfNode;
        return lastNodeNonEmpty;
    }

    private void initCommon() {
        this.totalTreePages = 0L;
    }

    private void openFile(String urlFile, BTreeConfiguration config)
            throws IOException, BTreeConfigurationException, TreeNodeException, BTreeException {
        this.fileManager = new FileManager(urlFile, "rw");

        // Start with nothing
        if (this.fileManager.getLength() == 0) {
            System.out.println("Initializing the file...");
            this.config = config;
            this.writeBTreeHeader();
        }
        // File exists
        else {
            System.out.println("78454uycheck");
            System.out.println("File already exists, trying to read it...");
            this.readBTreeHeader();
            System.out.println("File valid, loaded OK");
        }
    }

    // Just for testing
    public void traverse() throws IOException, TreeNodeException {
        this.root.traverse(this.fileManager, this.config);
    }

    public TreeNode getRoot() {
        return this.root;
    }

    public void printBTreeInfor() {
        System.out.println("- BTree -");
        this.config.print();
        System.out.println("Total tree pages: " + this.totalTreePages);

        if (this.root != null)
            this.root.printNode();
    }

    public void printPrettyBTree() throws IOException, TreeNodeException {
        BTreePrinter btreePrinter = new BTreePrinter(
                (int) (Math.log(this.totalTreePages) / Math.log(this.config.getTreeDegree())));
        printByLevel(this.root, 0, btreePrinter);
        btreePrinter.print();
    }

    private void printByLevel(TreeNode node, int level, BTreePrinter printer) throws IOException, TreeNodeException {
        int currCap = node.getCurrentCapacity();

        for (int i = 0; i < node.getCurrentCapacity(); i++)
            printer.add(level, "<" + node.getKeyAt(i) + ">");

        if (node.isLeafNode()) {
            return;
        }

        // Next level
        TreeInternalNode internalNode = (TreeInternalNode) node;
        level++;

        // Read pointer
        for (int i = 0; i < currCap; i++) {
            TreeNode childNode = TreeNode.readNode(fileManager, config, internalNode.getPointerAt(i));
            printByLevel(childNode, level, printer);
        }

        // Last ptr
        TreeNode childNode = TreeNode.readNode(fileManager, config, internalNode.getPointerAt(currCap));
        printByLevel(childNode, level, printer);
    }

    public void printNodeAtIndex(long index) throws IOException, TreeNodeException {
        TreeNode node = TreeNode.readNode(this.fileManager, this.config, index);
        node.printNode();
    }

    public void printFullOverflowNode(long index) throws IOException, TreeNodeException {
        TreeOverflowNode node = (TreeOverflowNode) TreeNode.readNode(fileManager, config, index);

        int j = 0;
        while (true) {
            j++;
            System.out.println("Overflow node " + j);
            for (int i = 0; i < node.getCurrentCapacity(); i++) {
                System.out.print(node.getOverflowValueAt(i) + " ");
            }
            System.out.println();

            if (node.getNextPointer() < 0)
                break;

            node = (TreeOverflowNode) TreeNode.readNode(fileManager, config, node.getNextPointer());
        }
    }

    public void printDetailNode(String key) throws IOException, TreeNodeException {
        printDetailNodeRecur(this.root, key);
    }

    private void printDetailNodeRecur(TreeNode node, String key) throws IOException, TreeNodeException {
        // Search key
        int idx = binaryNodeSearch(node, key, Rank.Exact, 0, node.getCurrentCapacity() - 1);

        // Leaf node
        if (node.isLeafNode()) {
            TreeLeafNode leafNode = (TreeLeafNode) node;

            // Found key
            if (idx < node.getCurrentCapacity() && key.compareTo(node.getKeyAt(idx)) == 0) {
                leafNode.printNode();

                // Value in leaf node
                System.out.println("Value: " + leafNode.getValueAt(idx));

                // Read value in overflow node
                if (leafNode.getOverflowPointerAt(idx) == -1L)
                    return;

                TreeOverflowNode ovfNode = (TreeOverflowNode) TreeNode.readNode(this.fileManager, this.config,
                        leafNode.getOverflowPointerAt(idx));

                System.out.print("Overflow values:  ");
                while (true) {

                    ovfNode.printNode();

                    if (ovfNode.getNextPointer() == -1l)
                        break;

                    ovfNode = (TreeOverflowNode) TreeNode.readNode(this.fileManager, this.config,
                            ovfNode.getNextPointer());
                }
                System.out.println();
            }

            // Nothing can find
            System.out.println("Empty");

        }
        // Internal node
        else {
            // If key = key in array (range [0, cap - 1] -> increase idx
            // Because key search = key in internal node -> right side
            if (idx < node.getCurrentCapacity() && key.compareTo(node.getKeyAt(idx)) >= 0)
                idx++;

            // Recusive seach
            TreeInternalNode internalNode = (TreeInternalNode) node;
            TreeNode childNode = TreeNode.readNode(this.fileManager, this.config, internalNode.getPointerAt(idx));
            printDetailNodeRecur(childNode, key);
        }
    }
}

class BTreePrinter {
    private int height = 0;
    private ArrayList<ArrayList<String>> textEachLevel = new ArrayList<>();

    BTreePrinter(int height) {
        this.height = height;
        textEachLevel = new ArrayList<ArrayList<String>>();

        for (int i = 0; i <= height; i++) {
            ArrayList<String> arr = new ArrayList<>();
            textEachLevel.add(arr);
        }
    }

    public void add(int level, String data) {
        textEachLevel.get(level).add(data);
    }

    public void print() {
        for (int i = 0; i < this.textEachLevel.size(); i++) {
            indent(getLenAtLevel(this.height), getLenAtLevel(i));
            System.out.println(levelToString(i));
            System.out.println();
        }
    }

    private void indent(int lenLevelBelow, int lenLevelAbove) {
        for (int i = 0; i < (lenLevelBelow - lenLevelAbove) / 15; i++)
            System.out.print(" ");
    }

    private int getLenAtLevel(int level) {
        return levelToString(level).length();
    }

    private String levelToString(int level) {
        String line = "[";
        int size = this.textEachLevel.get(level).size();

        for (int i = 0; i < size; i++) {
            String delim = i == size - 1 ? "" : " ";
            line += this.textEachLevel.get(level).get(i) + delim;
        }
        line += "]";

        return line;
    }

}

/**
 * 
 * Rank will be used when use binary search
 * 
 */
enum Rank {
    /**
     * Predecessor
     * 
     * <p>
     * If key to search not found in array, search function will return index of
     * nearest smaller key in array of key to search (index in range [0, size - 1])
     * <p>
     * If key found in array, return exact index of key in array
     */
    Pred,
    /**
     * Successor
     * 
     * <p>
     * If key to search not found in array, search function will return index of
     * nearest larger key in array of key to search (index in range [0, size - 1])
     * <p>
     * If key found in array, return exact index of key in array
     */
    Succ,
    /**
     * Exact will return exactly result.
     * <p>
     * If key is not found in array, search function will return index of nearest
     * larger key in array of key to search (if key is larger than all element in
     * array, return size of array) (index in range [0, size])
     * <p>
     * If key to search found in array, return exact index of key in array
     */
    Exact,
    /**
     * PlusOne will return index of key plus one
     * <p>
     * It is similar to Exact option, but if key to search found in array, return
     * index of key plus one
     */
    PlusOne
}