package com.mmaozi.intg.example.resource;

import com.mmaozi.intg.example.entity.OrderItem;
import com.mmaozi.intg.example.service.ItemService;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import java.util.List;

public class OrderItemsResource {

    private final Integer orderId;
    private ItemService itemService;

    public OrderItemsResource(Integer orderId, ItemService itemService) {
        this.orderId = orderId;
        this.itemService = itemService;
    }

    @GET
    public List<OrderItem> getItems() {
        return itemService.getOrders(orderId);
    }

    @POST
    public void addItem(@BeanParam OrderItem item) {
        itemService.addItem(orderId, item);
    }
}
