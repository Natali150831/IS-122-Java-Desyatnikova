package bookstorage;

import model.Book;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

        // Удалtybt ntcnjds[ lfyys[
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
        Book book = new Book(1L, "Лев Толстой", "Война и мир", 1865, 5);
        assertTrue(bookStorage.addBook(book));

        Book foundBook = bookStorage.findExactBook("Лев Толстой", "Война и мир");
        assertNotNull(foundBook);
        assertEquals("Лев Толстой", foundBook.getAuthor());
        assertEquals("Война и мир", foundBook.getTitle());
        assertEquals(1L, foundBook.getId()); // Проверяем что ID сохранился
    }

    @Test
    void testDeleteBook() {
        // Подготовка тестовых данных
        Book book = new Book(2L, "Федор Достоевский", "Преступление и наказание", 1866, 3);
        bookStorage.addBook(book);

        // Действие + проверка
        assertTrue(bookStorage.deleteBook("Федор Достоевский", "Преступление и наказание"));
        Book deletedBook = bookStorage.findExactBook("Федор Достоевский", "Преступление и наказание");
        assertEquals(0, deletedBook.getCopies());
    }

    @Test
    void testRemoveCopies() {
        // Подготовка
        Book book = new Book(3L, "Антон Чехов", "Вишневый сад", 1904, 5);
        bookStorage.addBook(book);

        // Действие + проверка
        assertTrue(bookStorage.removeCopies("Антон Чехов", "Вишневый сад", 2));
        Book updatedBook = bookStorage.findExactBook("Антон Чехов", "Вишневый сад");
        assertEquals(3, updatedBook.getCopies());
    }

    @Test
    void testFindBooksByAuthorAndTitle() {
        // Подготовка
        bookStorage.addBook(new Book(4L, "Александр Пушкин", "Евгений Онегин", 1833, 4));
        bookStorage.addBook(new Book(5L, "Александр Пушкин", "Капитанская дочка", 1836, 3));

        // Проверки
        List<Book> books = bookStorage.findBooksByAuthorAndTitle("Пушкин")
                .stream()
                .filter(b -> b.getId() == 4L || b.getId() == 5L)
                .collect(Collectors.toList());

        assertEquals(2, books.size());

        books = bookStorage.findBooksByAuthorAndTitle("Онегин");
        assertEquals(1, books.size());
        assertEquals("Евгений Онегин", books.get(0).getTitle());
    }

    @Test
    void testGetAllBooksOrderedByAuthor() {
        // Подготовка
        bookStorage.addBook(new Book(6L, "Иван Тургенев", "Отцы и дети", 1862, 2));
        bookStorage.addBook(new Book(7L, "Александр Пушкин", "Евгений Онегин", 1833, 4));

        // Проверка
        List<Book> books = bookStorage.getAllBooksOrderedByAuthor()
                .stream()
                .filter(b -> b.getId() == 6L || b.getId() == 7L)
                .collect(Collectors.toList());

        assertEquals(2, books.size());
        assertEquals("Александр Пушкин", books.get(0).getAuthor());
        assertEquals("Иван Тургенев", books.get(1).getAuthor());
    }
}