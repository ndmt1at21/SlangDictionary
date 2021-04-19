package com.btree;

import java.util.LinkedList;

public class SearchResult {
    private String key;
    private LinkedList<String> result;

    public SearchResult(String key, String result) {
        this.result = new LinkedList<>();

        if (result != null)
            this.result.add(result);
    }

    public SearchResult(String key, LinkedList<String> result) {
        this.result = result;
    }

    public LinkedList<String> getSearchResult() {
        return this.result;
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public String toString() {
        super.toString();
        String result = "";

        for (int i = 0; i < this.result.size(); i++) {
            result += this.result.get(i) + "; ";
        }

        return result;
    }
}
