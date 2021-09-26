package com.mmaozi.resti.example.resource;

import com.mmaozi.resti.example.entity.OrderItem;
import com.mmaozi.resti.example.service.ItemService;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

public class OrderItemsResource {

    private final Integer orderId;
    private ItemService itemService;

    public OrderItemsResource(int orderId) {
        this.orderId = orderId;
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
