package com.maovares.ms_products.product.infraestructure.messaging;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.maovares.ms_products.product.application.port.out.ProductEventPublisher;
import com.maovares.ms_products.product.domain.model.Product;

import lombok.extern.slf4j.Slf4j;

/**
 * Publica el evento ProductCreated en la cola productsqueue de Azure Storage
 * usando la API REST de Queue Storage con un token SAS.
 * El microservicio no sabe quién consume el evento; solo lo deja en la cola.
 */
@Component
@Slf4j
public class AzureQueueProductEventPublisher implements ProductEventPublisher {

    // URL de la cola con el token SAS, se configura en el App Service
    private static final String QUEUE_URL = System.getenv("PRODUCTS_QUEUE_URL");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public void publishProductCreated(Product product) {
        if (QUEUE_URL == null || QUEUE_URL.isBlank()) {
            log.warn("PRODUCTS_QUEUE_URL no está configurada, no se publica el evento");
            return;
        }

        try {
            String json = "{"
                    + "\"event\":\"ProductCreated\","
                    + "\"createdAt\":\"" + Instant.now() + "\","
                    + "\"product\":{"
                    + "\"id\":\"" + escape(product.getId()) + "\","
                    + "\"title\":\"" + escape(product.getTitle()) + "\","
                    + "\"description\":\"" + escape(product.getDescription()) + "\","
                    + "\"price\":" + product.getPrice() + ","
                    + "\"image\":\"" + escape(product.getImage()) + "\""
                    + "}}";

            // La Function espera el mensaje codificado en base64
            String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            String body = "<QueueMessage><MessageText>" + base64 + "</MessageText></QueueMessage>";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(QUEUE_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/xml")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                log.info("Evento ProductCreated publicado en la cola - ID: {}", product.getId());
            } else {
                log.error("La cola respondió {} al publicar el evento: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            // Si falla la cola, el producto ya quedó guardado; solo se registra el error
            log.error("Error publicando el evento ProductCreated: {}", e.getMessage(), e);
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}