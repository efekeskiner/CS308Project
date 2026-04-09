package com.bookstore.model;

import javax.persistence.*;

@Entity
  @Table(name = "products")
  public class Product {

    @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

    @Column(nullable = false)
        private String name;

    @Column(columnDefinition = "TEXT")
        private String description;

    @Column(nullable = false)
        private Double price;

    @Column
        private Integer stock;

    @Column
        private String category;

    @Column(name = "image_url")
        private String imageUrl;

    public Long    getId()          { return id; }
        public String  getName()        { return name; }
        public String  getDescription() { return description; }
        public Double  getPrice()       { return price; }
        public Integer getStock()       { return stock; }
        public String  getCategory()    { return category; }
        public String  getImageUrl()    { return imageUrl; }

    public void setId(Long id)               { this.id = id; }
        public void setName(String name)         { this.name = name; }
        public void setDescription(String desc)  { this.description = desc; }
        public void setPrice(Double price)       { this.price = price; }
        public void setStock(Integer stock)      { this.stock = stock; }
        public void setCategory(String category) { this.category = category; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
  }
