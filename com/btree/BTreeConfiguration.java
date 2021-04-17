package com.btree;

import com.utils.BTreeConfigurationException;

@SuppressWarnings("unused")
public class BTreeConfiguration {
    private int keySize; // max key size (bytes)
    private int entrySize; // entry size (bytes)
    private int treeDegree; // tree degree
    private int maxCapacity; // maximum capacity of node
    private int pageSize; // page size (node size) in file

    /**
     * 
     * Default constructor
     * 
     */
    public BTreeConfiguration() {
        this.pageSize = 1024;
        this.keySize = 20;
        this.entrySize = 20;
        this.treeDegree = 3;
        this.maxCapacity = calcCapacity();
    }

    /**
     * 
     * Constructor
     * 
     * @param pageSize   key size (default is Long/8 bytes)
     * @param keySize    key size (default is Long/8 bytes)
     * @param entrySize  entry/valye size (default is 20 bytes)
     * @param treeDegree tree degree size (default is 3)
     * 
     */
    public BTreeConfiguration(int pageSize, int keySize, int entrySize, int treeDegree)
            throws BTreeConfigurationException {
        this.pageSize = pageSize;
        this.keySize = keySize;
        this.entrySize = entrySize;
        this.treeDegree = treeDegree;
        this.maxCapacity = calcCapacity();

        validateData();
    }

    /**
     * 
     * Check all value of property in class is valid
     * 
     */
    private void validateData() throws BTreeConfigurationException {
        if (this.pageSize < 0)
            throw new BTreeConfigurationException("Page size cannot negative");

        if (this.keySize < 0)
            throw new BTreeConfigurationException("Size of key cannot negative");

        if (this.entrySize < 0)
            throw new BTreeConfigurationException("Size of entry cannot negative");

        if (this.treeDegree <= 0)
            throw new BTreeConfigurationException("Tree degree cannot negative");
    }

    ////////////// Get ////////////////
    public int getPageSize() {
        return this.pageSize;
    }

    public int getKeySize() {
        return this.keySize;
    }

    public int getEntrySize() {
        return this.entrySize;
    }

    public int getTreeDegree() {
        return this.treeDegree;
    }

    public int getMaxInternalNodeCapacity() {
        return 2 * this.treeDegree - 1;
    }

    public int getMaxLeafNodeCapacity() {
        return 2 * this.treeDegree - 1;
    }

    public int getMaxOverflowNodeCapacity() {
        return 2 * this.treeDegree - 1;
    }

    public int getMinInternalNodeCapacity() {
        return this.treeDegree - 1;
    }

    public int getMinLeafNodeCapacity() {
        return this.treeDegree - 1;
    }

    // Set
    // public void setPageSize(int pageSize) throws BTreeConfigurationException {
    // this.pageSize = pageSize;
    // this.validateData();
    // }

    // public void setKeySize(int keySize) throws BTreeConfigurationException {
    // this.key1Size = keySize;
    // this.validateData();
    // }

    // public void setEntrySize(int entrySize) throws BTreeConfigurationException {
    // this.entrySize = entrySize;
    // this.validateData();
    // }

    // public void setTreeDegree(int treeDegree) throws BTreeConfigurationException
    // {
    // this.treeDegree = treeDegree;
    // this.validateData();
    // }

    // public void setMaxCapacity(int maxCapacity) throws
    // BTreeConfigurationException {
    // this.maxCapacity = maxCapacity;
    // }

    private int calcCapacity() {
        return 2 * this.treeDegree - 1;
    }

    public void print() {
        System.out.println("Page size: " + pageSize);
        System.out.println("Key size: " + keySize);
        System.out.println("Entry size: " + entrySize);
        System.out.println("Tree degree: " + treeDegree);
        System.out.println("Capacity: " + maxCapacity);
    }
}
