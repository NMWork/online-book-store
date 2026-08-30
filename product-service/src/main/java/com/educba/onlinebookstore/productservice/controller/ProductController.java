package com.educba.onlinebookstore.productservice.controller;

import com.educba.onlinebookstore.productservice.dto.ProductRequest;
import com.educba.onlinebookstore.productservice.dto.ProductResponse;
import com.educba.onlinebookstore.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Value("${server.port}")
    private String serverPort;

    @PostMapping ("/products")
    public ResponseEntity<ProductResponse>  addProduct(@Valid @RequestBody ProductRequest productRequest) {
        ProductResponse response = productService.createProduct(productRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping ("/products")
    public ResponseEntity<List<ProductResponse>> getProducts() {
        List<ProductResponse> response = productService.getAllProducts();
        return  ResponseEntity.ok(response);
    }

    @GetMapping ("/products/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
       ProductResponse response = productService.getProductById(id);
       return ResponseEntity
               .status(HttpStatus.OK)
               .body(response);
    }

    @PutMapping ("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest) {
        ProductResponse response = productService.updateProduct(id, productRequest);
        return ResponseEntity
               .status(HttpStatus.OK)
               .body(response);
    }

    @DeleteMapping ("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products/instance")
    public String getInstance() {
        return "Request handled by Product Service running on port: " + serverPort;
    }

}
