
package com.ecommerce.composite;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/v1/products")
public class CompositeController {

    private final RestClient client;
    private final String catalogUrl;
    private final String inventoryUrl;
    private final String exchangeUrl;
    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();

    public CompositeController(
            RestClient.Builder builder,
            @Value("${services.catalog.url}") String catalogUrl,
            @Value("${services.inventory.url}") String inventoryUrl,
            @Value("${services.exchange.url}") String exchangeUrl) {

        this.client = builder.build();
        this.catalogUrl = catalogUrl;
        this.inventoryUrl = inventoryUrl;
        this.exchangeUrl = exchangeUrl;
    }

    @GetMapping("/{id}/summary")
    public ProductSummary getSummary(@PathVariable("id") String id) {

        CompletableFuture<Product> productFuture = CompletableFuture.supplyAsync(
            () -> client.get()
                .uri(catalogUrl + "/microservices/products/{id}", id)
                .retrieve()
                .body(Product.class),
            executor
        );

        CompletableFuture<Inventory> inventoryFuture = CompletableFuture.supplyAsync(
            () -> client.get()
                .uri(inventoryUrl + "/microservices/inventory/{id}", id)
                .retrieve()
                .body(Inventory.class),
            executor
        );

        CompletableFuture<ExchangeResponse> exchangeFuture = CompletableFuture.supplyAsync(
            () -> client.get()
                .uri(exchangeUrl)
                .retrieve()
                .body(ExchangeResponse.class),
            executor
        );

        try {
            CompletableFuture.allOf(
                productFuture,
                inventoryFuture,
                exchangeFuture
            ).join();

            Product product = productFuture.join();
            Inventory inventory = inventoryFuture.join();
            ExchangeResponse exchange = exchangeFuture.join();

            if (product == null || inventory == null || exchange == null ||
                    exchange.rates() == null ||
                    exchange.rates().get("MXN") == null) {

                throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Una de las fuentes devolvió una respuesta incompleta"
                );
            }

            BigDecimal rate = exchange.rates().get("MXN");

            BigDecimal priceMxn = product.priceUsd()
                .multiply(rate)
                .setScale(2, RoundingMode.HALF_UP);

            return new ProductSummary(
                product.productId(),
                product.title(),
                product.description(),
                product.category(),
                product.priceUsd(),
                priceMxn,
                rate,
                inventory.stockStatus(),
                inventory.availableUnits(),
                inventory.warehouseLocation(),
                inventory.estimatedDelivery(),
                "ExchangeRate-API (open.er-api.com)",
                "SUCCESS",
                Instant.now().toString()
            );

        } catch (CompletionException | RestClientException ex) {

            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "No fue posible consultar uno o más servicios. Verifica que los servicios estén activos y que la API externa responda.",
                ex
            );
        }
    }

    public record Product(
        String productId,
        String title,
        String description,
        String category,
        BigDecimal priceUsd
    ) {}

    public record Inventory(
        String productId,
        int availableUnits,
        String stockStatus,
        String warehouseLocation,
        String estimatedDelivery
    ) {}

    public record ExchangeResponse(
        String result,
        Map<String, BigDecimal> rates
    ) {}

    public record ProductSummary(
        String product_id,
        String title,
        String description,
        String category,
        BigDecimal price_usd,
        BigDecimal price_mxn,
        BigDecimal exchange_rate_mxn,
        String stock_status,
        int available_units,
        String warehouse_location,
        String estimated_delivery,
        String currency_conversion_source,
        String status,
        String generated_at
    ) {}
}
