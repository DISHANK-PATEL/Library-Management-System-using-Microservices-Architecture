package com.library.bookservice.mapper;

import com.library.bookservice.dto.BookDTO;
import com.library.bookservice.entity.Book;

public class BookMapper {

    public static BookDTO toDTO(Book book, String instanceId) {
        return BookDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .available(book.isAvailable())
                .servedByInstance(instanceId)
                .build();
    }
}