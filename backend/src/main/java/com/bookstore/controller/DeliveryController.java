package com.bookstore.controller;

import com.bookstore.dto.DeliveryDto;
import com.bookstore.dto.DeliveryStatusRequest;
import com.bookstore.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "http://localhost:3000")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    public ResponseEntity<List<DeliveryDto>> list() {
        return ResponseEntity.ok(deliveryService.list());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody DeliveryStatusRequest req) {
        try {
            return ResponseEntity.ok(deliveryService.updateStatus(id, req.getStatus()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
