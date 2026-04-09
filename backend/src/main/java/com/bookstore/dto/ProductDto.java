package com.bookstore.dto;

import com.bookstore.model.Product;

public class ProductDto {
      private final Long    id;
      private final String  name;
      private final String  description;
      private final Double  price;
      private final Integer stock;
      private final String  category;
      private final String  imageUrl;

    public ProductDto(Product p) {
              this.id          = p.getId();
              this.name        = p.getName();
              this.description = p.getDescription();
              this.price       = p.getPrice();
              this.stock       = p.getStock();
              this.category    = p.getCategory();
              this.imageUrl    = p.getImageUrl();
    }

    public Long    getId()          { return id; }
      public String  getName()        { return name; }
      public String  getDescription() { return description; }
      public Double  getPrice()       { return price; }
      public Integer getStock()       { return stock; }
      public String  getCategory()    { return category; }
      public String  getImageUrl()    { return imageUrl; }
}
