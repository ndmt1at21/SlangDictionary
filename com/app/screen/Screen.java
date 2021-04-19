package com.app.screen;

import java.util.Scanner;

import com.dict.DictSlangWord;

public abstract class Screen {
    protected Boolean isFirstRun = true;
    protected Scanner scanner = null;
    protected DictSlangWord dictSlangWord = null;

    public Screen(DictSlangWord dict) {
        this.dictSlangWord = dict;
        this.scanner = new Scanner(System.in);
    }

    public Boolean start() {
        if (isFirstRun) {
            printExitInstruction();
            printNameScreen();
            printInstruction();
            isFirstRun = false;
        }

        while (true) {

            if (!onScreenUpdate())
                break;

            if (scanner.hasNext()) {
                System.out.println("jdfhdjfhdjfhj");
                String input = scanner.nextLine();

                if (input.equals("0"))
                    break;

            }

        }

        return true;
    }

    private void printExitInstruction() {
        System.out.println("Sau khi hoan thanh cau lenh, nhan 0 de tro ve man hinh chinh");
    }

    abstract protected void printNameScreen();

    abstract protected void printInstruction();

    abstract protected Boolean onScreenUpdate();
}
