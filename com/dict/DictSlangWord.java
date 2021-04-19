package com.dict;

import java.io.IOException;

import com.btree.BTreeConfiguration;
import com.btree.BTreeStore;
import com.btree.CallbackCheckUnique;
import com.btree.DeleteResult;
import com.btree.SearchResult;
import com.btree.SlangWord;
import com.utils.BTreeConfigurationException;
import com.utils.BTreeException;
import com.utils.TreeNodeException;

@SuppressWarnings("unused")
public class DictSlangWord {
    private BTreeStore indexSlangWord;
    private BTreeStore indexDefinition;

    public DictSlangWord() throws IOException, BTreeConfigurationException, TreeNodeException, BTreeException {
        BTreeConfiguration config = new BTreeConfiguration(1024, 20, 50, 5);
        this.indexSlangWord = new BTreeStore("./resource/indexSlang.dat", config);
        this.indexDefinition = new BTreeStore("./resource/indexDifinition.dat", config);
    }

    public void insert(SlangWord slangWord, CallbackCheckUnique<String> unique) {

    }

    public DeleteResult delete(SlangWord slangWord, CallbackCheckUnique<String> unique) {

    }

    public SearchResult search(SlangWord slangWord) {

    }

    public RandomResult random() {

    }
}
