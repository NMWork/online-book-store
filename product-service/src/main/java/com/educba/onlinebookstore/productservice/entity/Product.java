package com.educba.onlinebookstore.productservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name="author", nullable = false)
    private String author;

    @Column(columnDefinition = "TEXT", name = "description")
    private String description;

    @Column(nullable = false, unique = true, name = "isbn")
    private String isbn;

    @Column(name="category")
    private String category;

    @Column(name="price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name="stock", nullable = false)
    private int stock;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime  createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime  updatedAt;
}
