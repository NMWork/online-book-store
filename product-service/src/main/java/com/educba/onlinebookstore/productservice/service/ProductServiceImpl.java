package com.educba.onlinebookstore.productservice.service;

import com.educba.onlinebookstore.productservice.dto.ProductRequest;
import com.educba.onlinebookstore.productservice.dto.ProductResponse;
import com.educba.onlinebookstore.productservice.entity.Product;
import com.educba.onlinebookstore.productservice.exception.DuplicateIsbnException;
import com.educba.onlinebookstore.productservice.exception.ProductNotFoundException;
import com.educba.onlinebookstore.productservice.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    @Override
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.findByIsbn(request.isbn()).isPresent()) {
            throw new DuplicateIsbnException(request.isbn());
        }
        Product savedProduct = productRepository.save(prepareProduct(request));
        return prepareProductResponse(savedProduct);
    }

    @Override
    public ProductResponse getProductById(Long id){
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
            return prepareProductResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::prepareProductResponse)
                .toList();
    }

    @Transactional
    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (productRepository.existsByIsbnAndIdNot(request.isbn(), id)) {
            throw new DuplicateIsbnException(request.isbn());
        }

        updateProductFields(product, request);

        Product updatedProduct = productRepository.saveAndFlush(product);
        return prepareProductResponse(updatedProduct);
    }

    @Transactional
    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    private ProductResponse prepareProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .author(product.getAuthor())
                .description(product.getDescription())
                .category(product.getCategory())
                .isbn(product.getIsbn())
                .price(product.getPrice())
                .stock(product.getStock())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private Product prepareProduct(ProductRequest request) {
        return Product.builder()
                .author(request.author())
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .isbn(request.isbn())
                .price(request.price())
                .stock(request.stock())
                .build();
    }

    private void updateProductFields(Product product, ProductRequest request) {
        product.setTitle(request.title());
        product.setAuthor(request.author());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setIsbn(request.isbn());
        product.setPrice(request.price());
        product.setStock(request.stock());
    }
}
