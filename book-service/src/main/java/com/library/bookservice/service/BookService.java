package com.library.bookservice.service;

import com.library.bookservice.dto.BookDTO;
import com.library.bookservice.entity.Book;
import com.library.bookservice.mapper.BookMapper;
import com.library.bookservice.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String port;

    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        String instanceId = applicationName + "-" + port;
        return BookMapper.toDTO(book, instanceId);
    }

    public void updateAvailability(Long id, boolean available) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        book.setAvailable(available);
        bookRepository.save(book);
    }
}
