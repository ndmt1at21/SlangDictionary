package com.btree;

import java.util.LinkedList;

public class DeleteResult {
    private String key;
    private LinkedList<String> values;

    public DeleteResult(String key, String value) {
        this.key = key;
        this.values = new LinkedList<>();

        if (value != null)
            this.values.push(value);
    }

    public DeleteResult(String key, LinkedList<String> values) {
        this.key = key;

        if (values.size() > 0)
            this.values = values;
    }

    public String getKey() {
        return this.key;
    }

    public LinkedList<String> getValues() {
        return values;
    }

    public boolean isFound() {
        return this.values.size() > 0;
    }
}
