package bookstorage;

import interfaces.IBookStorage;
import model.Book;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookStorage implements IBookStorage {
    static File f = new File("src/main/resources/kursach_jv.fdb");
    static String absolute = f.getAbsolutePath();
    private static final String URL = "jdbc:firebirdsql://localhost:3050/" + absolute;
    private static final String USER = "sysdba";
    private static final String PASSWORD = "masterkey";

    @Override
    public boolean addBook(Book book) {
        String checkSql = "SELECT ID, COPIES FROM BOOKS WHERE AUTHOR = ? AND TITLE = ?";
        String updateSql = "UPDATE BOOKS SET COPIES = COPIES + ? WHERE ID = ?";
        String insertSql = "INSERT INTO BOOKS (AUTHOR, TITLE, PUBLICATION_YEAR, COPIES) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {

            checkStmt.setString(1, book.getAuthor());
            checkStmt.setString(2, book.getTitle());

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    long existingId = rs.getLong("ID");
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, book.getCopies());
                        updateStmt.setLong(2, existingId);
                        updateStmt.executeUpdate();
                    }
                } else {
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteBook(String author, String title) {
        // Вместо удаления устанавливаем количество экземпляров в 0
        String sql = "UPDATE BOOKS SET COPIES = 0 WHERE AUTHOR = ? AND TITLE = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, author);
            pstmt.setString(2, title);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeCopies(String author, String title, int count) {
        String sql = "UPDATE BOOKS SET COPIES = " +
                "CASE WHEN (COPIES - ?) < 0 THEN 0 ELSE (COPIES - ?) END " +
                "WHERE AUTHOR = ? AND TITLE = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, count);
            pstmt.setInt(2, count);
            pstmt.setString(3, author);
            pstmt.setString(4, title);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateCopies(String author, String title, int count) {
        String sql = "UPDATE BOOKS SET COPIES = " +
                "CASE WHEN (COPIES + ?) < 0 THEN 0 ELSE (COPIES + ?) END " +
                "WHERE AUTHOR = ? AND TITLE = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, count);
            pstmt.setInt(2, count);
            pstmt.setString(3, author);
            pstmt.setString(4, title);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Book findExactBook(String author, String title) {
        String sql = "SELECT * FROM BOOKS WHERE UPPER(AUTHOR) = UPPER(?) AND UPPER(TITLE) = UPPER(?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, author);
            pstmt.setString(2, title);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Book book = new Book(
                            rs.getString("AUTHOR"),
                            rs.getString("TITLE"),
                            rs.getInt("PUBLICATION_YEAR"),
                            rs.getInt("COPIES"));
                    book.setId(rs.getLong("ID"));
                    return book;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Book> findBooksByAuthorAndTitle(String query) {
        String sql = "SELECT * FROM BOOKS WHERE UPPER(AUTHOR) LIKE UPPER(?) OR UPPER(TITLE) LIKE UPPER(?)";
        List<String> params = new ArrayList<>();
        params.add("%" + query + "%");
        params.add("%" + query + "%");
        return getBooks(sql, params);
    }

    @Override
    public List<Book> getAllBooksOrderedByAuthor() {
        String sql = "SELECT * FROM BOOKS ORDER BY UPPER(AUTHOR)";
        return getBooks(sql, new ArrayList<>());
    }

    @Override
    public List<Book> getAllBooksOrderedByYear() {
        String sql = "SELECT * FROM BOOKS ORDER BY PUBLICATION_YEAR";
        return getBooks(sql, new ArrayList<>());
    }

    private List<Book> getBooks(String sql, List<String> params) {
        List<Book> books = new ArrayList<>();

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }
}