package com.educba.onlinebookstore.productservice.repository;

import com.educba.onlinebookstore.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {

    Optional<Product> findByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

}
