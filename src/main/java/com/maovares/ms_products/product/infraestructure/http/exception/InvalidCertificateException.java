package com.maovares.ms_products.product.infraestructure.http.exception;

public class InvalidCertificateException extends RuntimeException {
    public InvalidCertificateException(String message) {
        super(message);
    }

    public InvalidCertificateException(String message, Throwable cause) {
        super(message, cause);
    }

}
