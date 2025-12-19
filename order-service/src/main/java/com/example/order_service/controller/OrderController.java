package com.example.order_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.order_service.client.ProductClient;
import com.example.order_service.dto.ProductDTO;
import com.example.order_service.dto.OrderRequest;
import com.example.order_service.entity.Order;
import com.example.order_service.repository.OrderRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderController(OrderRepository orderRepository, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    @PostMapping
    @CircuitBreaker(name = "productService", fallbackMethod = "placeOrderFallback")
    public Order placeOrder(@RequestBody OrderRequest request) {
        log.info("Placing order for product: {}", request.getProductId());
        ProductDTO product = productClient.getProductById(request.getProductId());

        Order order = new Order();
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(product.getPrice() * request.getQuantity());

        log.info("Order placed successfully with ID: {}", order.getId());
        return orderRepository.save(order);
    }

    // --- FALLBACK METHOD ---
    // Rule 1: Must have the exact same signature as the original method.
    // Rule 2: Must accept a Throwable as the last argument.
    public Order placeOrderFallback(OrderRequest request, Throwable t) {
        log.info("Fallback executed! Reason: " + t.getMessage());

        // Return a safe "Failure" response instead of crashing
        Order order = new Order();
        order.setProductId(request.getProductId());
        order.setProductName("Unavailable - Please Retry Later");
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(0.0);

        log.info("Fallback executed! Reason: " + t.getMessage());
        return order;
    }

}
