package com.project.ecommerce.service;

import com.project.ecommerce.dto.CartDto;
import com.project.ecommerce.dto.CartItemDto;
import com.project.ecommerce.model.CartItem;
import com.project.ecommerce.model.Product;
import com.project.ecommerce.model.User;
import com.project.ecommerce.repository.CartItemRepository;
import com.project.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    public CartDto getCart() {
        User currentUser = userService.getCurrentUser();
        List<CartItem> cartItems = cartItemRepository.findByUser(currentUser);

        List<CartItemDto> cartItemDtos = cartItems.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        CartDto cartDto = new CartDto();
        cartDto.setItems(cartItemDtos);
        cartDto.setTotalItems(cartItemDtos.size());
        cartDto.setTotalPrice(calculateTotalPrice(cartItemDtos));

        return cartDto;
    }

    @Transactional
    public CartDto addToCart(Long productId, Integer quantity) {
        User currentUser = userService.getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough stock available");
        }

        Optional<CartItem> existingCartItem = cartItemRepository.findByUserAndProduct(currentUser, product);

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUser(currentUser);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        return getCart();
    }

    @Transactional
    public CartDto updateCartItem(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + cartItemId));

        User currentUser = userService.getCurrentUser();
        if (!cartItem.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to update this cart item");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            if (cartItem.getProduct().getStockQuantity() < quantity) {
                throw new RuntimeException("Not enough stock available");
            }
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        return getCart();
    }

    @Transactional
    public CartDto removeFromCart(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + cartItemId));

        User currentUser = userService.getCurrentUser();
        if (!cartItem.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to remove this cart item");
        }

        cartItemRepository.delete(cartItem);
        return getCart();
    }

    @Transactional
    public void clearCart() {
        User currentUser = userService.getCurrentUser();
        List<CartItem> cartItems = cartItemRepository.findByUser(currentUser);
        cartItemRepository.deleteAll(cartItems);
    }

    private CartItemDto convertToDto(CartItem cartItem) {
        CartItemDto dto = new CartItemDto();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setProductImageUrl(cartItem.getProduct().getImageUrl());
        dto.setProductPrice(cartItem.getProduct().getPrice());
        dto.setQuantity(cartItem.getQuantity());
        dto.setSubtotal(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        return dto;
    }

    private BigDecimal calculateTotalPrice(List<CartItemDto> cartItems) {
        return cartItems.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}