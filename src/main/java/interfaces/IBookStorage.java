package interfaces;

import model.Book;
import java.util.List;

public interface IBookStorage {
    boolean addBook(Book book);
    boolean deleteBook(String author, String title); // Полное удаление книги
    boolean removeCopies(String author, String title, int count); // Удаление определенного количества экземпляров
    boolean updateCopies(String author, String title, int count); // Обновление количества (может быть и положительным и отрицательным)
    List<Book> findBooksByAuthorAndTitle(String query);
    List<Book> getAllBooksOrderedByAuthor();
    List<Book> getAllBooksOrderedByYear();
    Book findExactBook(String author, String title);
}