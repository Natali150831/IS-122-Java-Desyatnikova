package service;

import bookstorage.BookStorage;
import model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookStorage bookStorage;

    @InjectMocks
    private BookService bookService;

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book(1L, "Дж. К. Роулинг", "Гарри Поттер", 1997, 5);
    }

    @Test
    void testAddBook_NewBook() {
        when(bookStorage.findExactBook(book.getAuthor(), book.getTitle())).thenReturn(null);
        when(bookStorage.addBook(book)).thenReturn(true);

        boolean result = bookService.addBook(book);

        assertTrue(result);
        verify(bookStorage).findExactBook(book.getAuthor(), book.getTitle());
        verify(bookStorage).addBook(book);
    }
}