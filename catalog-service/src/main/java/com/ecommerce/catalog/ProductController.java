
package com.ecommerce.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@RestController
@RequestMapping("/microservices/products")
public class ProductController {

    private static final Map<String, Product> PRODUCTS = Map.of(
        "P-101", new Product("P-101", "Laptop Gamer XYZ",
            "Laptop para videojuegos y tareas exigentes", "Computadoras",
            new BigDecimal("1200.00")),
        "P-102", new Product("P-102", "Monitor UltraWide",
            "Monitor panorámico de 34 pulgadas", "Accesorios",
            new BigDecimal("450.00")),
        "P-103", new Product("P-103", "Teclado Mecánico",
            "Teclado mecánico retroiluminado", "Accesorios",
            new BigDecimal("85.50"))
    );

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable("id") String id) {
        Product product = PRODUCTS.get(id);

        if (product == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Producto no encontrado: " + id
            );
        }

        return product;
    }

    public record Product(
        String productId,
        String title,
        String description,
        String category,
        BigDecimal priceUsd
    ) {}   
}
