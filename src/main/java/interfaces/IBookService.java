package interfaces;

import model.Book;
import java.util.List;

public interface IBookService {
    boolean addBook(Book book);
    boolean deleteBook(String author, String title); // Полное удаление
    boolean removeCopies(String author, String title, int count); // Удаление N экземпляров
    boolean updateCopies(String author, String title, int count); // Изменение количества
    List<Book> findBooks(String query);
    List<Book> getAllBooksOrderedByAuthor();
    List<Book> getAllBooksOrderedByYear();
    Book findExactBook(String author, String title);
}