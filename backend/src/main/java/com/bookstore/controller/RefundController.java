package com.bookstore.controller;

import com.bookstore.dto.RefundDto;
import com.bookstore.dto.RefundRequestCreate;
import com.bookstore.model.RefundStatus;
import com.bookstore.model.User;
import com.bookstore.service.RefundService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/refunds")
@CrossOrigin(origins = "http://localhost:3000")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping
    public ResponseEntity<?> requestRefund(@AuthenticationPrincipal User user,
                                           @RequestBody RefundRequestCreate req) {
        if (user == null) return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(refundService.requestRefund(user, req));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getReason()));
        }
    }

    @GetMapping("/mine")
    public ResponseEntity<?> myRefunds(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        List<RefundDto> list = refundService.listForUser(user);
        return ResponseEntity.ok(list);
    }

    @GetMapping
    public ResponseEntity<?> listForManager(@AuthenticationPrincipal User user,
                                            @RequestParam(required = false) String status) {
        if (user == null) return ResponseEntity.status(401).build();
        if (status != null && status.equalsIgnoreCase("ALL")) {
            return ResponseEntity.ok(refundService.listAllForManager());
        }
        RefundStatus filter = null;
        if (status != null && !status.isBlank()) {
            try {
                filter = RefundStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + status));
            }
        }
        return ResponseEntity.ok(refundService.listForManager(filter));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@AuthenticationPrincipal User user, @PathVariable Long id) {
        if (user == null) return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(refundService.approve(user, id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getReason()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@AuthenticationPrincipal User user, @PathVariable Long id) {
        if (user == null) return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(refundService.reject(user, id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getReason()));
        }
    }
}
