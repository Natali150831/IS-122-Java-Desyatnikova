package service;

import interfaces.IBookService;
import model.Book;
import bookstorage.BookStorage;

import java.util.List;

public class BookService implements IBookService {
    private BookStorage bookStorage = new BookStorage();

    @Override
    public boolean addBook(Book book) {
        return bookStorage.addBook(book);
    }

    @Override
    public boolean deleteBook(String author, String title) {
        return bookStorage.deleteBook(author, title);
    }

    @Override
    public boolean updateCopies(String author, String title, int count) {
        return bookStorage.updateCopies(author, title, count);
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