package com.maovares.ms_products.product.application.port.out;

import com.maovares.ms_products.product.domain.model.Product;

public interface ProductEventPublisher {
    void publishProductCreated(Product product);
}