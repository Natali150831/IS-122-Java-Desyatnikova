package bookstorage;

import interfaces.IBookStorage;
import model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookStorage implements IBookStorage {
    private static final String URL = "jdbc:firebirdsql://localhost:3050//home/natali/IdeaProjects/java_kurssach/src/main/java/kursach_jv.fdb";
    private static final String USER = "sysdba";
    private static final String PASSWORD = "masterkey";

    @Override
    public boolean addBook(Book book) {
        String sql = "INSERT INTO BOOKS (AUTHOR, TITLE, PUBLICATION_YEAR, COPIES) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, book.getAuthor());
            pstmt.setString(2, book.getTitle());
            pstmt.setInt(3, book.getPublicationYear());
            pstmt.setInt(4, book.getCopies());
            pstmt.executeUpdate();
            return true;
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
        String sql = "UPDATE BOOKS SET COPIES = COPIES - ? WHERE AUTHOR = ? AND TITLE = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, count);
            pstmt.setString(2, author);
            pstmt.setString(3, title);
            pstmt.executeUpdate();

            // Проверяем, осталось ли экземпляров
            String checkSql = "SELECT COUNT(*) FROM BOOKS WHERE AUTHOR = ? AND TITLE = ? AND COPIES > 0";
            try (PreparedStatement checkPstmt = connection.prepareStatement(checkSql)) {
                checkPstmt.setString(1, author);
                checkPstmt.setString(2, title);
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // Если ни одного экземпляра не осталось, удаляем запись
                        deleteBook(author, title);
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
    public List<Book> findBooksByAuthorAndTitle(String query) {
        String sql = "SELECT * FROM BOOKS WHERE AUTHOR LIKE ? OR TITLE LIKE ?";
        return getBooks(sql, "%" + query + "%", "%" + query + "%");
    }

    @Override
    public List<Book> getAllBooksOrderedByAuthor() {
        String sql = "SELECT * FROM BOOKS ORDER BY AUTHOR";
        return getBooks(sql);
    }

    @Override
    public List<Book> getAllBooksOrderedByYear() {
        String sql = "SELECT * FROM BOOKS ORDER BY PUBLICATION_YEAR";
        return getBooks(sql);
    }

    private List<Book> getBooks(String sql, Object... params) {
        List<Book> books = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
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
                }
            }

            return books;
        } catch (SQLException e) {
            e.printStackTrace();
            return books;
        }
    }
}