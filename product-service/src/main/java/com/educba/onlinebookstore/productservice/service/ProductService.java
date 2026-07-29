package com.educba.onlinebookstore.productservice.service;

import com.educba.onlinebookstore.productservice.dto.ProductRequest;
import com.educba.onlinebookstore.productservice.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getAllProducts();

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);


}
