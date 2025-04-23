import ui.LibraryApp;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LibraryApp app = new LibraryApp();// Создаём экземпляр главного окна
                app.setVisible(true); // Делаем окно видимым
            }
        });
    }
}