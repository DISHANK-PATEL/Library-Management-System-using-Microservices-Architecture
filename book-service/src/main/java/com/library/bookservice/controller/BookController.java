package com.library.bookservice.controller;

import com.library.bookservice.dto.BookDTO;
import com.library.bookservice.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<Void> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {
        bookService.updateAvailability(id, available);
        return ResponseEntity.ok().build();
    }
}
