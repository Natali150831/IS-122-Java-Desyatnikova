package ui;

import interfaces.IBookService;
import model.Book;
import service.BookService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LibraryApp extends JFrame {
    private final IBookService bookService = new BookService();
    private final JTextField searchField; // Поле ввода для поиска
    private final JTextArea textArea; // Область вывода результатов

    public LibraryApp() {
        // Инициализация интерфейса
        setTitle("Библиотека");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //полное завершение работы программы после закрытия окна
        setLocationRelativeTo(null); //окно по центру

        JPanel contentPanel = new JPanel(new BorderLayout()); //главный контейнер для всех элементов интерфейса
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); //выравнение полевому краю
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); //тут по правому

        JLabel searchLabel = new JLabel("Поиск по автору или названию: ");
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Искать");
        topPanel.add(searchLabel);
        topPanel.add(searchField);
        topPanel.add(searchButton);

        JButton addButton = new JButton("Добавить книгу");
        JButton deleteButton = new JButton("Удалить книгу");
        JButton sortByAuthorButton = new JButton("Сортировать по автору");
        JButton sortByYearButton = new JButton("Сортировать по году");
        bottomPanel.add(addButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(sortByAuthorButton);
        bottomPanel.add(sortByYearButton);
        //строковая область бля отображения списка книг
        textArea = new JTextArea();
        textArea.setEditable(false);//запрещает редактирование текста в этой области
        JScrollPane scrollPane = new JScrollPane(textArea); //для прокрутки если не вмещается
        contentPanel.add(topPanel, BorderLayout.NORTH); //панель поиска
        contentPanel.add(bottomPanel, BorderLayout.SOUTH); //кнопки снизу
        contentPanel.add(scrollPane, BorderLayout.CENTER); //центр(область с книгами)

        add(contentPanel);

        // Загрузка книг при запуске
        loadBooks();

        // Обработчики событий
        addButton.addActionListener(e -> addBook()); // регистрация событий для каждой кнопки
        deleteButton.addActionListener(e -> deleteBook());

        sortByAuthorButton.addActionListener(e -> {
            List<Book> books = bookService.getAllBooksOrderedByAuthor();
            displayBooks(books);
        });

        sortByYearButton.addActionListener(e -> {
            List<Book> books = bookService.getAllBooksOrderedByYear();
            displayBooks(books);
        });

        searchButton.addActionListener(e -> {
            String query = searchField.getText();
            if (!query.isBlank()) {
                List<Book> filteredBooks = bookService.findBooks(query);
                displayBooks(filteredBooks);
            } else {
                loadBooks(); // Показать все книги, если строка поиска пустая
            }
        });
    }

    private void addBook() {
        String author = JOptionPane.showInputDialog("Введите автора:");
        if (author == null || author.isEmpty()) {
            showErrorMessage("Автор не может быть пустым.");
            return;
        }

        String title = JOptionPane.showInputDialog("Введите название:");
        if (title == null || title.isEmpty()) {
            showErrorMessage("Название не может быть пустым.");
            return;
        }

        // Проверяем, существует ли книга
        Book existingBook = bookService.findExactBook(author, title);

        if (existingBook != null) {
            // Если книга существует, запрашиваем только количество для добавления
            String copiesInput = JOptionPane.showInputDialog( "Книга уже существует. Текущее количество: " + existingBook.getCopies() + "\nВведите количество добавляемых экземпляров:");

            if (copiesInput == null || copiesInput.isEmpty()) {
                showErrorMessage("Количество экземпляров не может быть пустым.");
                return;
            }

            try {
                int copiesToAdd = Integer.parseInt(copiesInput);
                if (bookService.updateCopies(author, title, copiesToAdd)) {
                    showSuccessMessage("Добавлено " + copiesToAdd + " экземпляров книги!");
                    loadBooks();
                } else {
                    showErrorMessage("Ошибка при добавлении экземпляров!");
                }
            } catch (NumberFormatException ex) {
                showErrorMessage("Некорректный ввод! Введите число для количества экземпляров.");
            }
        } else {
            // Если книги нет, запрашиваем полные данные
            String yearInput = JOptionPane.showInputDialog("Введите год издания:");
            if (yearInput == null || yearInput.isEmpty()) {
                showErrorMessage("Год издания не может быть пустым.");
                return;
            }

            String copiesInput = JOptionPane.showInputDialog("Введите начальное количество экземпляров:");
            if (copiesInput == null || copiesInput.isEmpty()) {
                showErrorMessage("Количество экземпляров не может быть пустым.");
                return;
            }

            try {
                int year = Integer.parseInt(yearInput);
                int copies = Integer.parseInt(copiesInput);

                Book book = new Book( author, title, year, copies);
                if (bookService.addBook(book)) {
                    showSuccessMessage("Новая книга добавлена!");
                    loadBooks();
                } else {
                    showErrorMessage("Ошибка при добавлении книги!");
                }
            } catch (NumberFormatException ex) {
                showErrorMessage("Некорректный ввод! Введите числа для года и количества экземпляров.");
            }
        }
    }

    private void deleteBook() {
        // поиск книги
        String searchQuery = JOptionPane.showInputDialog("Введите автора или название книги для поиска:");
        if (searchQuery == null || searchQuery.isEmpty()) {
            showErrorMessage("Поисковый запрос не может быть пустым.");
            return;
        }

        List<Book> foundBooks = bookService.findBooks(searchQuery);
        if (foundBooks.isEmpty()) {
            showErrorMessage("Книги не найдены.");
            return;
        }

        // Показываем список найденных книг для выбора
        String[] bookOptions = new String[foundBooks.size()];
        for (int i = 0; i < foundBooks.size(); i++) {
            Book b = foundBooks.get(i);
            bookOptions[i] = b.getAuthor() + " - " + b.getTitle() + " (" + b.getCopies() + " экз.)";
        }

        int selectedIndex = JOptionPane.showOptionDialog(
                this,
                "Выберите книгу для удаления:",
                "Выбор книги",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                bookOptions,
                bookOptions[0]);

        if (selectedIndex < 0) return; // Пользователь отменил выбор

        Book selectedBook = foundBooks.get(selectedIndex);

        // Выбор действия: полное удаление или удаление экземпляров
        String[] options = {"Удалить все экземпляры", "Удалить часть экземпляров", "Отмена"};
        int action = JOptionPane.showOptionDialog(
                this,
                "Выберите действие для книги:\n" + selectedBook.getAuthor() + " - " + selectedBook.getTitle(),
                "Выбор действия",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (action == 0) { // Полное удаление
            if (bookService.deleteBook(selectedBook.getAuthor(), selectedBook.getTitle())) {
                showSuccessMessage("Книга полностью удалена из библиотеки!");
                loadBooks();
            } else {
                showErrorMessage("Ошибка при удалении книги!");
            }
        } else if (action == 1) { // Удаление части экземпляров
            String countInput = JOptionPane.showInputDialog( "Текущее количество: " + selectedBook.getCopies() + "\nВведите количество экземпляров для удаления:");

            if (countInput == null || countInput.isEmpty()) {
                showErrorMessage("Количество не может быть пустым.");
                return;
            }

            try {
                int count = Integer.parseInt(countInput);
                if (count <= 0) {
                    showErrorMessage("Количество должно быть положительным числом.");
                    return;
                }

                if (bookService.removeCopies(selectedBook.getAuthor(), selectedBook.getTitle(), count)) {
                    showSuccessMessage("Удалено " + count + " экземпляров книги!");
                    loadBooks();
                } else {
                    showErrorMessage("Ошибка при удалении экземпляров!");
                }
            } catch (NumberFormatException ex) {
                showErrorMessage("Некорректный ввод! Введите число.");
            }
        }
    }
    //загрузка списка книг
    private void loadBooks() {
        //поток загрузки книг
        Thread thread = new Thread(() -> {
            try {
                List<Book> books = bookService.getAllBooksOrderedByAuthor();
                if (books != null && !books.isEmpty()) {
                    SwingUtilities.invokeLater(() -> displayBooks(books));
                } else {
                    SwingUtilities.invokeLater(() -> textArea.setText("Список книг пуст."));
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> showErrorMessage("Ошибка при загрузке книг: " + e.getMessage()));
            }
        });thread.start();
    }

    private void displayBooks(List<Book> books) {
        if (books == null || textArea == null) {
            System.err.println("Ошибка: books или textArea равен null.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Book book : books) {
            if (book != null) { // Проверка на null для каждой книги
                sb.append(book).append("\n");
            }
        }

        if (sb.length() > 0) {
            textArea.setText(sb.toString());
        } else {
            textArea.setText("Список книг пуст.");
        }
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Успех", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }
}