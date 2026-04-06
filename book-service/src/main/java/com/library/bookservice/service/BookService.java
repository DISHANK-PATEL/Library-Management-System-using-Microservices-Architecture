package com.library.bookservice.service;

import com.library.bookservice.dto.BookDTO;
import com.library.bookservice.entity.Book;
import com.library.bookservice.exception.ResourceNotFoundException;
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

    @Value("${eureka.instance.instance-id:${server.port}}")
    private String instanceId;

    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        String instance = applicationName + "-" + instanceId;
        return BookMapper.toDTO(book, instanceId);
    }

    public void updateAvailability(Long id, boolean available) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        book.setAvailable(available);
        bookRepository.save(book);
    }
}
