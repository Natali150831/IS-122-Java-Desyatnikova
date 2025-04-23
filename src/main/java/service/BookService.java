package service;

import interfaces.IBookService;
import model.Book;
import bookstorage.BookStorage;

import java.util.List;

public class BookService implements IBookService {
    private BookStorage bookStorage = new BookStorage();

    @Override
    public boolean addBook(Book book) {
        Book existingBook = bookStorage.findExactBook(book.getAuthor(), book.getTitle());
        if (existingBook != null) {
            return bookStorage.updateCopies(book.getAuthor(), book.getTitle(), book.getCopies());
        } else {
            return bookStorage.addBook(book);
        }
    }

    @Override
    public boolean deleteBook(String author, String title) {
        // Теперь это просто установка количества в 0
        return bookStorage.updateCopies(author, title, -Integer.MAX_VALUE);
    }

    @Override
    public boolean removeCopies(String author, String title, int count) {
        if (count <= 0) {
            return false;
        }
        return bookStorage.removeCopies(author, title, count);
    }

    @Override
    public boolean updateCopies(String author, String title, int count) {
        return bookStorage.updateCopies(author, title, count);
    }

    @Override
    public Book findExactBook(String author, String title) {
        return bookStorage.findExactBook(author, title);
    }

    @Override
    public List<Book> findBooks(String query) {
        return bookStorage.findBooksByAuthorAndTitle(query);
    }

    @Override
    public List<Book> getAllBooksOrderedByAuthor() {
        return bookStorage.getAllBooksOrderedByAuthor();
    }

    @Override
    public List<Book> getAllBooksOrderedByYear() {
        return bookStorage.getAllBooksOrderedByYear();
    }
}