package com.btree;

public class SlangWord {
    private final String definition;
    private final String mean;

    public SlangWord(String definition, String mean) {
        this.definition = definition;
        this.mean = mean;
    }

    public String getDefinition() {
        return this.definition;
    }

    public String getMean() {
        return this.mean;
    }
}