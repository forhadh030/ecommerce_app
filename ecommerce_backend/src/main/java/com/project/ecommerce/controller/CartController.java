package com.project.ecommerce.controller;

import com.project.ecommerce.dto.CartDto;
import com.project.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("hasRole('USER')")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart() {
        CartDto cart = cartService.getCart();
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<CartDto> addToCart(@RequestParam Long productId, @RequestParam Integer quantity) {
        CartDto cart = cartService.addToCart(productId, quantity);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<CartDto> updateCartItem(@PathVariable Long cartItemId, @RequestParam Integer quantity) {
        CartDto cart = cartService.updateCartItem(cartItemId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<CartDto> removeFromCart(@PathVariable Long cartItemId) {
        CartDto cart = cartService.removeFromCart(cartItemId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}