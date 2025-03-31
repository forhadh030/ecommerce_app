package com.project.ecommerce.controller;

import com.project.ecommerce.dto.CheckoutRequest;
import com.project.ecommerce.dto.OrderDto;
import com.project.ecommerce.model.Order;
import com.project.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderDto>> getUserOrders() {
        List<OrderDto> orders = orderService.getUserOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> checkout(@RequestBody CheckoutRequest checkoutRequest) {
        OrderDto order = orderService.checkout(checkoutRequest);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable Long id, @RequestParam Order.OrderStatus status) {
        OrderDto order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(order);
    }
}