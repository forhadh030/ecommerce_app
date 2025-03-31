package com.project.ecommerce.service;

import com.project.ecommerce.dto.CheckoutRequest;
import com.project.ecommerce.dto.OrderDto;
import com.project.ecommerce.dto.OrderItemDto;
import com.project.ecommerce.model.*;
import com.project.ecommerce.repository.CartItemRepository;
import com.project.ecommerce.repository.OrderItemRepository;
import com.project.ecommerce.repository.OrderRepository;
import com.project.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    public List<OrderDto> getUserOrders() {
        User currentUser = userService.getCurrentUser();
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(currentUser);

        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public OrderDto getOrderById(Long id) {
        User currentUser = userService.getCurrentUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to view this order");
        }

        return convertToDto(order);
    }

    @Transactional
    public OrderDto checkout(CheckoutRequest checkoutRequest) {
        User currentUser = userService.getCurrentUser();
        List<CartItem> cartItems = cartItemRepository.findByUser(currentUser);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Your cart is empty");
        }

        // Create new order
        Order order = new Order();
        order.setUser(currentUser);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setShippingAddress(checkoutRequest.getShippingAddress());
        order.setPaymentMethod(checkoutRequest.getPaymentMethod());

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Create order items and update product stock
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Check if product is in stock
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Product " + product.getName() + " is out of stock");
            }

            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());

            order.getItems().add(orderItem);

            // Calculate total amount
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cartService.clearCart();

        return convertToDto(savedOrder);
    }

    public OrderDto updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        return convertToDto(updatedOrder);
    }

    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setPaymentMethod(order.getPaymentMethod());

        List<OrderItemDto> orderItemDtos = order.getItems().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        dto.setItems(orderItemDtos);

        return dto;
    }

    private OrderItemDto convertToDto(OrderItem orderItem) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setProductImageUrl(orderItem.getProduct().getImageUrl());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setSubtotal(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        return dto;
    }
}