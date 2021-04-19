package com.app.screen;

import com.dict.DictSlangWord;

public class InsertScreen extends Screen {
    public InsertScreen(DictSlangWord dict) {
        super(dict);
    }

    @Override
    protected void printNameScreen() {
        System.out.println("Tao sinh vien moi");
    }

    @Override
    protected void printInstruction() {
        System.out.println("Nhap thong tinh sinh vien");
    }

    @Override
    protected Boolean onScreenUpdate() {

    }
}
