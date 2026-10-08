package com.maovares.ms_products.product.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.maovares.ms_products.product.application.port.in.CreateProductCommand;
import com.maovares.ms_products.product.application.port.out.ProductEventPublisher;
import com.maovares.ms_products.product.application.port.out.ProductRepository;
import com.maovares.ms_products.product.domain.model.Product;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreateProductService implements CreateProductCommand {

    private final ProductRepository productRepository;
    private final ProductEventPublisher productEventPublisher;

    public CreateProductService(ProductRepository productRepository,
            ProductEventPublisher productEventPublisher) {
        this.productRepository = productRepository;
        this.productEventPublisher = productEventPublisher;
    }

    @Override
    public Product execute(String description, double price, String image, String title) {
        log.info("Creating product - Title: {}, Price: {}, Description: {}", title, price, description);
        
        try {
            String id = UUID.randomUUID().toString();
            log.debug("Generated product ID: {}", id);

            Product product = new Product(id, price, description, image, title);
            
            log.info("Saving product to repository - ID: {}, Title: {}", id, title);
            Product savedProduct = productRepository.save(product);
            
            log.info("Product successfully created and saved - ID: {}, Title: {}", 
                    savedProduct.getId(), savedProduct.getTitle());

            // Evento: el microservicio avisa por su cuenta que se creó un producto
            productEventPublisher.publishProductCreated(savedProduct);
            
            return savedProduct;
        } catch (Exception e) {
            log.error("Error creating product with title '{}': {}", title, e.getMessage(), e);
            throw e;
        }
    }

}