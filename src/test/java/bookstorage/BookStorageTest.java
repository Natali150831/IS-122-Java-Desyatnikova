package bookstorage;

import model.Book;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookStorageTest {
    private BookStorage bookStorage;
    private Connection connection;

    @BeforeEach
    void setUp() {
        try {
            // Убедитесь, что путь к базе данных правильный
            File f = new File("src/main/resources/kursach_jv.fdb");
            String absolute = f.getAbsolutePath();
            final String url = "jdbc:firebirdsql://localhost:3050/" + absolute;
            String user = "SYSDBA";
            String password = "masterkey";

            // Создаем соединение
            connection = DriverManager.getConnection(url, user, password);

            // Создаем экземпляр BookStorage с соединением
            bookStorage = new BookStorage();

            // Очищаем таблицу перед тестами
            try (var stmt = connection.createStatement()) {
                stmt.execute("DELETE FROM BOOKS");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize test database", e);
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
    }

    @Test
    void testDeleteBook() {
        Book book = new Book(2L, "Федор Достоевский", "Преступление и наказание", 1866, 3);
        bookStorage.addBook(book);

        // Проверяем, что книга добавлена
        Book foundBook = bookStorage.findExactBook("Федор Достоевский", "Преступление и наказание");
        assertNotNull(foundBook);

        // Удаляем книгу
        assertTrue(bookStorage.deleteBook("Федор Достоевский", "Преступление и наказание"));

        // Проверяем, что количество экземпляров стало 0
        Book deletedBook = bookStorage.findExactBook("Федор Достоевский", "Преступление и наказание");
        assertEquals(0, deletedBook.getCopies());
    }

    @Test
    void testRemoveCopies() {
        Book book = new Book(3L, "Антон Чехов", "Вишневый сад", 1904, 5);
        bookStorage.addBook(book);

        // Удаляем 2 экземпляра
        assertTrue(bookStorage.removeCopies("Антон Чехов", "Вишневый сад", 2));

        // Проверяем, что осталось 3 экземпляра
        Book updatedBook = bookStorage.findExactBook("Антон Чехов", "Вишневый сад");
        assertEquals(3, updatedBook.getCopies());
    }

    @Test
    void testFindBooksByAuthorAndTitle() {
        bookStorage.addBook(new Book(4L, "Александр Пушкин", "Евгений Онегин", 1833, 4));
        bookStorage.addBook(new Book(5L, "Александр Пушкин", "Капитанская дочка", 1836, 3));

        List<Book> books = bookStorage.findBooksByAuthorAndTitle("Пушкин");
        assertEquals(2, books.size());

        books = bookStorage.findBooksByAuthorAndTitle("Онегин");
        assertEquals(1, books.size());
        assertEquals("Евгений Онегин", books.get(0).getTitle());
    }

    @Test
    void testGetAllBooksOrderedByAuthor() {
        bookStorage.addBook(new Book(6L, "Иван Тургенев", "Отцы и дети", 1862, 2));
        bookStorage.addBook(new Book(7L, "Александр Пушкин", "Евгений Онегин", 1833, 4));

        List<Book> books = bookStorage.getAllBooksOrderedByAuthor();
        assertEquals(2, books.size());
        assertTrue(books.get(0).getAuthor().compareTo(books.get(1).getAuthor()) <= 0);
    }
}