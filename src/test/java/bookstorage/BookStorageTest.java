package bookstorage;

import model.Book;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BookStorageTest {
    private BookStorage bookStorage;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(
                DatabaseConfig.getFullDatabaseUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword()
        );
        bookStorage = new BookStorage();

        // Удаляет
        cleanTestData();
    }

    private void cleanTestData() throws SQLException {
        try (var stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM BOOKS WHERE ID BETWEEN 1 AND 10");
        }
    }

    @AfterEach
    void tearDown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testAddBook() {
        Book book = new Book( "Лев Толстой", "Война и мир", 1865, 5);
        assertTrue(bookStorage.addBook(book));

        Book foundBook = bookStorage.findExactBook("Лев Толстой", "Война и мир");
        assertNotNull(foundBook);
        assertEquals("Лев Толстой", foundBook.getAuthor());
        assertEquals("Война и мир", foundBook.getTitle());
    }

    @Test
    void testDeleteBook() {
        // Подготовка тестовых данных
        Book book = new Book("Федор Достоевский", "Преступление и наказание", 1866, 3);
        bookStorage.addBook(book);

        // Действие + проверка
        assertTrue(bookStorage.deleteBook("Федор Достоевский", "Преступление и наказание"));
        Book deletedBook = bookStorage.findExactBook("Федор Достоевский", "Преступление и наказание");
        assertEquals(0, deletedBook.getCopies());
    }

    @Test
    void testRemoveCopies() {
        // Подготовка
        Book book = new Book( "Антон Чехов", "Вишневый сад", 1904, 5);
        bookStorage.addBook(book);

        // Действие + проверка
        assertTrue(bookStorage.removeCopies("Антон Чехов", "Вишневый сад", 2));
        Book updatedBook = bookStorage.findExactBook("Антон Чехов", "Вишневый сад");
    }

    @Test
    void testFindBooksByAuthorAndTitle() {
        // Подготовка
        bookStorage.addBook(new Book( "Александр Пушкин", "Евгений Онегин", 1833, 4));
        bookStorage.addBook(new Book( "Александр Пушкин", "Капитанская дочка", 1836, 3));

        // Проверки
        List<Book> books = new ArrayList<>(bookStorage.findBooksByAuthorAndTitle("Пушкин"));



        books = bookStorage.findBooksByAuthorAndTitle("Онегин");
        assertEquals("Евгений Онегин", books.get(0).getTitle());
    }



}