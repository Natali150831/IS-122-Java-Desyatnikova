package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BookTest {

    @Test
    void testBookCreation() {
        Book book = new Book( "Дж. К. Роулинг", "Гарри Поттер", 1997, 5);


        assertEquals("Дж. К. Роулинг", book.getAuthor());
        assertEquals("Гарри Поттер", book.getTitle());
        assertEquals(1997, book.getPublicationYear());
        assertEquals(5, book.getCopies());
    }
}