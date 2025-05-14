package model;

public class Book {
    private String author;
    private String title;
    private int publicationYear;
    private int copies;
    // Конструктор для создания объекта книги с указанными параметрами
    public Book(String author, String title, int publicationYear, int copies) {
        this.author = author;
        this.title = title;
        this.publicationYear = publicationYear;
        this.copies = copies;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public int getCopies() {
        return copies;
    }

    @Override
    public String toString() {
        return "Автор: " + author + ", Название: " + title + ", Год издания: " + publicationYear + ", Копии: " + copies;
    }


    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }

    public Book() {}
}