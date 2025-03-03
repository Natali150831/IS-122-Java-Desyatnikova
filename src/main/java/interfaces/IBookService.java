package interfaces;

import model.Book;
import java.util.List;

public interface IBookService {
    boolean addBook(Book book);
    boolean deleteBook(String author, String title);
    boolean updateCopies(String author, String title, int count);
    List<Book> findBooks(String query);
    List<Book> getAllBooksOrderedByAuthor();
    List<Book> getAllBooksOrderedByYear();
}