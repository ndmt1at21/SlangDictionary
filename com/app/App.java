import java.util.Scanner;

import com.dict.DictSlangWord;

public class App {
    private Boolean isRunning = false;
    private Scanner scanner = null;
    private DictSlangWord dictSlangWord;

    public App() {
        dictSlangWord = new DictSlangWord();
        scanner = new Scanner(System.in);
    }

    public void start() {
        isRunning = true;

        while (isRunning) {
            clearScreen();
            printMenu();

            String option = scanner.nextLine();

            runMenu(option);
        }
    }

    private void printMenu() {
        System.out.println("Chuong trinh quan ly hoc sinh");
        System.out.println("1. Tao sinh vien moi");
        System.out.println("2. Truy xuat sinh vien");
        System.out.println("3. Cap nhat sinh vien");
        System.out.println("4. Xoa sinh vien");
        System.out.println("5. Xuat danh sach ra csv");
        System.out.println("6. Nhap danh sach tu csv");
        System.out.println("7. Thoat");
    }

    private void runMenu(String option) {
        switch (option) {
        case "1":
            runInsertStatement();
            break;
        case "2":
            runSelectStatement();
            break;
        case "3":
            runUpdateStatement();
            break;
        case "4":
            runDeleteStatement();
            break;
        case "5":
            runExportStatement();
            break;
        case "6":
            runImportStatement();
            break;
        case "7":
            this.isRunning = false;
            break;
        default:
        }
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void runInsertOption() {
        clearScreen();
        InsertScreen insertScreen = new InsertScreen(db);
        insertScreen.start();
    }

    private void runEditOption() {
        clearScreen();
        EditScreen screen = new EditScreen(this.db);
        screen.start();
    }

    private void runRandomOption() {
        clearScreen();
        SelectScreen screen = new SelectScreen(this.db);
        screen.start();
    }

    private void runDeleteStatement() {
        clearScreen();
        DeleteScreen screen = new DeleteScreen(this.db);
        screen.start();
    }

    private void runResetOption() {
        clearScreen();
        DeleteScreen screen = new DeleteScreen(this.db);
        screen.start();
    }
}
