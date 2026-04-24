package com.bookstore.dto;

import com.bookstore.model.Category;

public class CategoryDto {
    private final Long id;
    private final String name;

    public CategoryDto(Category c) {
        this.id = c.getId();
        this.name = c.getName();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}
