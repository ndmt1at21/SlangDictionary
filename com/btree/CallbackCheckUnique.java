package com.btree;

@FunctionalInterface
public interface CallbackCheckUnique<T> {
    /**
     * 
     * @param keyDup
     * @return true if key is unique, otherwise false
     */
    public boolean isUnique(T keyDup);
}