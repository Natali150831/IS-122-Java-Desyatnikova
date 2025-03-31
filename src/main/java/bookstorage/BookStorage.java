package bookstorage;

import interfaces.IBookStorage;
import model.Book;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookStorage implements IBookStorage {
    static File f =new File("src/main/resources/kursach_jv.fdb");
    static String absolute = f.getAbsolutePath();
    private static final String URL = "jdbc:firebirdsql://localhost:3050/" + absolute;
    private static final String USER = "sysdba";
    private static final String PASSWORD = "masterkey";

    @Override
    public boolean addBook(Book book) {
        // существует ли книга с таким же автором и названием
        String checkSql = "SELECT ID, COPIES FROM BOOKS WHERE AUTHOR = ? AND TITLE = ?"; //проверка
        String updateSql = "UPDATE BOOKS SET COPIES = COPIES + ? WHERE ID = ?"; //обновление
        String insertSql = "INSERT INTO BOOKS (AUTHOR, TITLE, PUBLICATION_YEAR, COPIES) VALUES (?, ?, ?, ?)"; //вставка

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {

            checkStmt.setString(1, book.getAuthor());
            checkStmt.setString(2, book.getTitle());

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    // Книга уже существует, обновляем количество экземпляров
                    long existingId = rs.getLong("ID");
                    int existingCopies = rs.getInt("COPIES");

                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, book.getCopies()); // Увеличиваем количество экземпляров
                        updateStmt.setLong(2, existingId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Книга не существует, добавляем новую запись
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setString(1, book.getAuthor());
                        insertStmt.setString(2, book.getTitle());
                        insertStmt.setInt(3, book.getPublicationYear());
                        insertStmt.setInt(4, book.getCopies());
                        insertStmt.executeUpdate();
                    }
                }
            }

            return true;
            //обработка ошибок
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteBook(String author, String title) {
        String sql = "DELETE FROM BOOKS WHERE AUTHOR = ? AND TITLE = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, author);
            pstmt.setString(2, title);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateCopies(String author, String title, int count) {
        String sql = "UPDATE BOOKS SET COPIES = COPIES + ? WHERE AUTHOR = ? AND TITLE = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, count);  // Увеличиваем или уменьшаем количество экземпляров
            pstmt.setString(2, author);
            pstmt.setString(3, title);
            pstmt.executeUpdate();

            // Проверяем, чтобы количество экземпляров не стало отрицательным
            String checkSql = "SELECT COPIES FROM BOOKS WHERE AUTHOR = ? AND TITLE = ?";
            try (PreparedStatement checkPstmt = connection.prepareStatement(checkSql)) {
                checkPstmt.setString(1, author);
                checkPstmt.setString(2, title);
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next()) {
                        int copies = rs.getInt("COPIES");
                        if (copies < 0) {
                            // Если количество экземпляров стало отрицательным, устанавливаем его в 0
                            String fixSql = "UPDATE BOOKS SET COPIES = 0 WHERE AUTHOR = ? AND TITLE = ?";
                            try (PreparedStatement fixPstmt = connection.prepareStatement(fixSql)) {
                                fixPstmt.setString(1, author);
                                fixPstmt.setString(2, title);
                                fixPstmt.executeUpdate();
                            }
                        }
                    }
                }
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    //поиск книг по совпадению с query в названии или авторе
    @Override
    public List<Book> findBooksByAuthorAndTitle(String query) {
        String sql = "SELECT * FROM BOOKS WHERE UPPER(AUTHOR) LIKE UPPER(?) OR UPPER(TITLE) LIKE UPPER(?)";
        List<String> params = new ArrayList<>();
        params.add("%" + query + "%");
        params.add("%" + query + "%");
        return getBooks(sql, params);
    }

    //возвращает все книги, сортирует по автору (от а до я)
    @Override
    public List<Book> getAllBooksOrderedByAuthor() {
        String sql = "SELECT * FROM BOOKS ORDER BY UPPER(AUTHOR)";
        return getBooks(sql, new ArrayList<>());
    }

    //возвращает все книги , отсортированныйх по году издания( по возрастанию)
    @Override
    public List<Book> getAllBooksOrderedByYear() {
        String sql = "SELECT * FROM BOOKS ORDER BY PUBLICATION_YEAR";
        return getBooks(sql, new ArrayList<>());
    }

    private List<Book> getBooks(String sql, List<String> params) {
        List<Book> books = new ArrayList<>(); //создаем пустой список для хранения результатов

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book(
                            rs.getString("AUTHOR"),
                            rs.getString("TITLE"),
                            rs.getInt("PUBLICATION_YEAR"),
                            rs.getInt("COPIES"));
                    book.setId(rs.getLong("ID"));
                    books.add(book);

                    if (book.getCopies() == 0) {
                        book.setTitle(book.getTitle() + " (Нет в наличии)");
                    }
                }
            }

            return books;
        } catch (SQLException e) {
            e.printStackTrace();
            return books;
        }
    }
}