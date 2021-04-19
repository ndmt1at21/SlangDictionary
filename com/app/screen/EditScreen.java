package com.app.screen;

import com.dict.DictSlangWord;

public class EditScreen extends Screen {
    public EditScreen(DictSlangWord db) {
        super(db);
    }

    @Override
    protected void printNameScreen() {
        System.out.println("Chuc nang cap nhat sinh vien");
    }

    @Override
    protected void printInstruction() {
        System.out.println("Nhap truy van voi ten cac cot: maHs tenHs diem img diaChi notes");
        System.out.println("Vi du: maHs == 1812 && diem > 9 || diem < 5");
    }

    @Override
    protected Boolean onScreenUpdate() {
        String query = scanner.nextLine();

        return false;

    }
}
