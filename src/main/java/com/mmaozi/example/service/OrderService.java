package com.mmaozi.example.service;

import com.mmaozi.example.entity.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService {

    private final Map<Integer, List<Order>> orders = new HashMap<>();

    public List<Order> getOrders(Integer customerId) {
        return orders.get(customerId);

    }

    public Order getOrder(Integer customerId, Integer orderId) {
        return orders.get(customerId)
            .stream()
            .filter(order -> order.getOrderId() == orderId)
            .findAny()
            .orElse(null);
    }

    public void addOrder(Integer customerId, Order order) {
        orders.putIfAbsent(customerId, new ArrayList<>());
        orders.get(customerId).add(order);
    }
}
