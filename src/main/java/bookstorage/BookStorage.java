package bookstorage;

import interfaces.IBookStorage;
import model.Book;
import config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookStorage implements IBookStorage {
    private static final String URL = DatabaseConfig.getFullDatabaseUrl();
    private static final String USER = DatabaseConfig.getUser();
    private static final String PASSWORD = DatabaseConfig.getPassword();

    @Override
    public boolean addBook(Book book) {
        String checkBookSql = "SELECT ID FROM BOOK_INFO WHERE AUTHOR = ? AND TITLE = ?";
        String insertBookSql = "INSERT INTO BOOK_INFO (AUTHOR, TITLE, PUBLICATION_YEAR) VALUES (?, ?, ?)";
        String insertCopiesSql = "INSERT INTO BOOKS (BOOK_ID, COPIES) VALUES (?, ?)"; // Изменено с UPDATE на INSERT
        String updateCopiesSql = "UPDATE BOOKS SET COPIES = COPIES + ? WHERE BOOK_ID = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement checkStmt = connection.prepareStatement(checkBookSql)) {

            checkStmt.setString(1, book.getAuthor());
            checkStmt.setString(2, book.getTitle());

            long bookId;
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    bookId = rs.getLong("ID");
                    // Если книга существует, обновляем количество копий
                    try (PreparedStatement updateCopiesStmt = connection.prepareStatement(updateCopiesSql)) {
                        updateCopiesStmt.setInt(1, book.getCopies());
                        updateCopiesStmt.setLong(2, bookId);
                        updateCopiesStmt.executeUpdate();
                    }
                } else {
                    // Если книги нет, добавляем новую запись
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertBookSql, Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setString(1, book.getAuthor());
                        insertStmt.setString(2, book.getTitle());
                        insertStmt.setInt(3, book.getPublicationYear());
                        insertStmt.executeUpdate();

                        try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                bookId = generatedKeys.getLong(1);
                                // Добавляем запись о количестве копий
                                try (PreparedStatement insertCopiesStmt = connection.prepareStatement(insertCopiesSql)) {
                                    insertCopiesStmt.setLong(1, bookId);
                                    insertCopiesStmt.setInt(2, book.getCopies());
                                    insertCopiesStmt.executeUpdate();
                                }
                            } else {
                                throw new SQLException("Не удалось получить ID книги.");
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

    @Override
    public boolean deleteBook(String author, String title) {
        String sql = "UPDATE BOOKS SET COPIES = 0 WHERE BOOK_ID = " +
                "(SELECT ID FROM BOOK_INFO WHERE AUTHOR = ? AND TITLE = ?)";

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
                "WHERE BOOK_ID = (SELECT ID FROM BOOK_INFO WHERE AUTHOR = ? AND TITLE = ?)";

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
                "WHERE BOOK_ID = (SELECT ID FROM BOOK_INFO WHERE AUTHOR = ? AND TITLE = ?)";

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
        String sql = "SELECT bi.*, b.COPIES FROM BOOK_INFO bi " +
                "JOIN BOOKS b ON bi.ID = b.BOOK_ID " +
                "WHERE UPPER(bi.AUTHOR) = UPPER(?) AND UPPER(bi.TITLE) = UPPER(?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, author);
            pstmt.setString(2, title);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Book(
                            rs.getString("AUTHOR"),
                            rs.getString("TITLE"),
                            rs.getInt("PUBLICATION_YEAR"),
                            rs.getInt("COPIES")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Book> findBooksByAuthorAndTitle(String query) {
        String sql = "SELECT bi.*, b.COPIES FROM BOOK_INFO bi " +
                "JOIN BOOKS b ON bi.ID = b.BOOK_ID " +
                "WHERE UPPER(bi.AUTHOR) LIKE UPPER(?) OR UPPER(bi.TITLE) LIKE UPPER(?)";
        List<String> params = new ArrayList<>();
        params.add("%" + query + "%");
        params.add("%" + query + "%");
        return getBooks(sql, params);
    }

    @Override
    public List<Book> getAllBooksOrderedByAuthor() {
        String sql = "SELECT bi.*, b.COPIES FROM BOOK_INFO bi " +
                "JOIN BOOKS b ON bi.ID = b.BOOK_ID " +
                "ORDER BY UPPER(bi.AUTHOR)";
        return getBooks(sql, new ArrayList<>());
    }

    @Override
    public List<Book> getAllBooksOrderedByYear() {
        String sql = "SELECT bi.*, b.COPIES FROM BOOK_INFO bi " +
                "JOIN BOOKS b ON bi.ID = b.BOOK_ID " +
                "ORDER BY bi.PUBLICATION_YEAR";
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
                            rs.getInt("COPIES")
                    );
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