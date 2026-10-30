
package com.ecommerce.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/microservices/inventory")
public class InventoryController {

    private static final Map<String, Inventory> INVENTORY = Map.of(
        "P-101", new Inventory("P-101", 15, "IN_STOCK",
            "Almacén Chihuahua", "2 a 4 días hábiles"),
        "P-102", new Inventory("P-102", 0, "OUT_OF_STOCK",
            "Almacén Chihuahua", "Por confirmar"),
        "P-103", new Inventory("P-103", 7, "IN_STOCK",
            "Almacén Ciudad Juárez", "1 a 3 días hábiles")
    );

    @GetMapping("/{productId}")
    public Inventory getInventory(@PathVariable("productId") String productId) {
        Inventory inventory = INVENTORY.get(productId);

        if (inventory == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Inventario no encontrado: " + productId
            );
        }

        return inventory;
    }

    public record Inventory(
        String productId,
        int availableUnits,
        String stockStatus,
        String warehouseLocation,
        String estimatedDelivery
    ) {}
}
