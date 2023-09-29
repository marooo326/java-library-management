package com.programmers.service;

import com.programmers.common.Messages;
import com.programmers.domain.Book;
import com.programmers.domain.BookState;
import com.programmers.repository.BookRepository;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class BookService {
    private static BookRepository bookRepository;
    private static final int organizingMilliseconds = 1000 * 10; // 10초

    private BookService() {
    }

    private static class Holder {
        private static final BookService INSTANCE = new BookService();
    }

    public static BookService getInstance() {
        Optional.ofNullable(bookRepository).orElseThrow(NoSuchElementException::new);
        return BookService.Holder.INSTANCE;
    }

    public static void setBookRepository(BookRepository _bookRepository) {
        bookRepository = _bookRepository;
    }

    public void registerBook(Book book) {
        bookRepository.addBook(book);
    }

    public void showAllBooks() {
        bookRepository.getAllBooks().forEach(book -> System.out.println(book.toString()));
    }

    public void searchBookByTitle(String title) {
        bookRepository.findBookByTitle(title)
                .forEach(book -> System.out.println(book.toString()));
    }

    public void rentBook(int bookId) {
        Book book = bookRepository.findBookById(bookId)
                .orElseThrow(() -> new NoSuchElementException(Messages.BOOK_NOT_EXIST.toString()));
        if (book.isRentable()) bookRepository.updateBookState(book, BookState.RENTED);
    }

    public void returnBook(int bookId) {
        Book book = bookRepository.findBookById(bookId)
                .orElseThrow(() -> new NoSuchElementException(Messages.BOOK_NOT_EXIST.toString()));
        if (book.isReturnable()) {
            bookRepository.updateBookState(book, BookState.ORGANIZING);
            startOrganizing(book);
        }
    }

    public void lostBook(int bookId) {
        Book book = bookRepository.findBookById(bookId)
                .orElseThrow(() -> new NoSuchElementException(Messages.BOOK_NOT_EXIST.toString()));
        if (book.isReportableAsLost()) bookRepository.updateBookState(book, BookState.LOST);
    }

    public void deleteBook(int bookId) {
        Book book = bookRepository.findBookById(bookId)
                .orElseThrow(() -> new NoSuchElementException(Messages.BOOK_NOT_EXIST.toString()));
        bookRepository.deleteBook(book);
    }

    private void startOrganizing(Book book) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                bookRepository.updateBookState(book, BookState.AVAILABLE);
            }
        }, organizingMilliseconds); // 10초
    }
}