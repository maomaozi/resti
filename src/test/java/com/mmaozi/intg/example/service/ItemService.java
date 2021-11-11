package com.mmaozi.intg.example.service;

import com.mmaozi.intg.example.entity.OrderItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemService {

    private final Map<Integer, List<OrderItem>> items = new HashMap<>();

    public List<OrderItem> getOrders(Integer orderId) {
        return items.get(orderId);
    }

    public void addItem(Integer orderId, OrderItem item) {
        items.putIfAbsent(orderId, new ArrayList<>());
        items.get(orderId).add(item);
    }
}
