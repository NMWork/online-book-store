package com.educba.onlinebookstore.productservice.exception;

public class DuplicateIsbnException extends RuntimeException{
    public DuplicateIsbnException(String message) {
        super("Product with ISBN " + message + " already exists");
    }
}
