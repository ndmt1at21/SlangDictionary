package com.btree;

import java.util.InvalidPropertiesFormatException;

public enum TreeNodeType {
    INTERNAL_NODE((short) 0), LEAF_NODE((short) 1), ROOT_INTERNAL_NODE((short) 2), ROOT_LEAF_NODE((short) 3),
    LEAF_OVERFLOW_NODE((short) 4);

    private short rawVal = 0;

    private TreeNodeType(short val) {
        this.rawVal = val;
    }

    public short getRawValue() {
        return this.rawVal;
    }

    static public TreeNodeType setValue(short rawVal) throws InvalidPropertiesFormatException {
        switch (rawVal) {
        case 0:
            return TreeNodeType.INTERNAL_NODE;
        case 1:
            return TreeNodeType.LEAF_NODE;
        case 2:
            return TreeNodeType.ROOT_INTERNAL_NODE;
        case 3:
            return TreeNodeType.ROOT_LEAF_NODE;
        case 4:
            return TreeNodeType.LEAF_OVERFLOW_NODE;
        default:
            throw new InvalidPropertiesFormatException("Unknown type node has code " + rawVal);
        }
    }
}
